package ferrefix.ordenes.service;

import ferrefix.ordenes.model.*;
import ferrefix.ordenes.repository.CarritoRepository;
import ferrefix.ordenes.repository.PedidoRepository;

import ferrefix.ordenes.dto.ComercioDtos;
import ferrefix.ordenes.dto.PerfilUsuario;

import static ferrefix.ordenes.dto.ComercioDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @Transactional
public class ComercioService {
    // Este flujo confirma un pago simulado al registrar la compra, sin pasarela bancaria.
    private static final String ESTADO_PAGO_SIMULADO = "Pagado";
    private final CarritoRepository carritos;
    private final PedidoRepository pedidos;
    private final CatalogoClient catalogo;

    public ComercioService(CarritoRepository carritos, PedidoRepository pedidos, CatalogoClient catalogo) {
        this.carritos = carritos;
        this.pedidos = pedidos;
        this.catalogo = catalogo;
    }

    public List<ItemCarrito> cargarCarrito(String propietario, String invitado) {
        var claves = new TreeSet<String>(); claves.add(propietario);
        if (invitado != null) claves.add(invitado);
        claves.forEach(carritos::bloquear);
        if (invitado != null && !invitado.equals(propietario)) {
            var pendientes = carritos.cantidades(invitado);
            if (!pendientes.isEmpty()) {
                var actuales = carritos.cantidades(propietario);
                pendientes.forEach((id, cantidad) -> actuales.merge(id, cantidad, (a,b) -> Math.min(999, a+b)));
                if (actuales.size() > 100) throw error(HttpStatus.CONFLICT, "El carrito admite hasta 100 productos distintos.");
                actuales.forEach((id, cantidad) -> carritos.guardarCantidad(propietario, id, cantidad));
                carritos.vaciar(invitado);
            }
        }
        return vistaCarrito(propietario);
    }

    private List<ItemCarrito> vistaCarrito(String propietario) {
        var result = new ArrayList<ItemCarrito>();
        carritos.cantidades(propietario).forEach((id, cantidad) -> {
            var p = catalogo.buscar(id);
            result.add(p == null ? new ItemCarrito(id, "Producto no disponible", "", "", BigDecimal.ZERO, 0,
                    "", "unidad", "", cantidad, false) : new ItemCarrito(id, p.nombre(), p.marca(), p.categoria(),
                    p.precioVenta(), p.stock(), p.imagen(), p.unidadVenta(), p.contenidoPresentacion(), cantidad, true));
        });
        return result;
    }

    public List<ItemCarrito> modificarCarrito(String propietario, String id, int cantidad, boolean sumar) {
        carritos.bloquear(propietario);
        var actuales = carritos.cantidades(propietario);
        if (!actuales.containsKey(id) && actuales.size() >= 100) throw error(HttpStatus.CONFLICT, "El carrito admite hasta 100 productos distintos.");
        int nueva = sumar ? actuales.getOrDefault(id, 0) + cantidad : cantidad;
        var p = catalogo.buscar(id);
        if (p == null || nueva > p.stock() || nueva > 999) throw error(HttpStatus.CONFLICT, "No hay stock suficiente para esa cantidad.");
        carritos.guardarCantidad(propietario, id, nueva);
        return vistaCarrito(propietario);
    }

    public List<ItemCarrito> quitarCarrito(String propietario, String id) {
        carritos.bloquear(propietario);
        if (id == null) carritos.vaciar(propietario);
        else carritos.eliminar(propietario, id);
        return vistaCarrito(propietario);
    }

    public Pedido crearPedido(String propietario, PerfilUsuario perfil, CrearPedido datos) {
        carritos.bloquear(propietario);
        String id = datos.idSolicitud().toString();
        var previo = pedidos.propietario(id);
        if (previo.isPresent()) {
            if (!previo.get().equals(propietario)) throw error(HttpStatus.CONFLICT, "Identificador de solicitud no válido.");
            return pedidos.buscar(id);
        }
        Map<String,Integer> solicitados = new TreeMap<>();
        for (var item : datos.items()) {
            if (solicitados.put(item.idProducto(), item.cantidad()) != null) throw error(HttpStatus.BAD_REQUEST, "No repitas productos en el pedido.");
        }
        if (!carritos.cantidades(propietario).equals(solicitados)) throw error(HttpStatus.CONFLICT, "El carrito cambió. Actualízalo antes de confirmar.");
        validarRut(datos.cliente().rut());
        var items = new ArrayList<ItemPedido>();
        for (var item : datos.items()) {
            var p = catalogo.buscar(item.idProducto());
            if (p == null || item.cantidad() > p.stock()) throw error(HttpStatus.CONFLICT, "Un producto ya no tiene stock suficiente. Revisa tu carrito.");
            items.add(new ItemPedido(p.idProducto(), p.nombre(), item.cantidad(), p.precioVenta()));
        }
        BigDecimal subtotal = items.stream().map(i -> i.precioUnitario().multiply(BigDecimal.valueOf(i.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal despacho = datos.metodoEntrega().equals("retiro") || subtotal.compareTo(BigDecimal.valueOf(50000)) > 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(3990);
        var c = datos.cliente();
        Instant fecha = Instant.now();
        var cliente = new ClientePedido(c.nombre().strip(), c.rut().replace(".", "").toUpperCase(), c.email().strip(),
                c.telefono().strip(), c.direccion().strip(), c.region(), c.comuna());
        var pedido = new Pedido(id, perfil == null ? null : perfil.idUsuario(), fecha, cliente, items,
                subtotal, despacho, subtotal.add(despacho), ESTADO_PAGO_SIMULADO, datos.metodoPago(), datos.metodoEntrega(), datos.indicaciones());
        pedidos.guardar(propietario, pedido);
        carritos.vaciar(propietario);
        return pedidos.buscar(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos(PerfilUsuario perfil) {
        return perfil.administrador() ? pedidos.listarTodos() : pedidos.listarPorUsuario(perfil.idUsuario());
    }

    private void validarRut(String valor) {
        String rut = valor.replace(".", "").toUpperCase();
        if (!rut.matches("[1-9][0-9]{6,7}-[0-9K]")) throw error(HttpStatus.BAD_REQUEST, "El RUT no es válido.");
        String cuerpo = rut.substring(0, rut.length()-2);
        int suma=0, factor=2;
        for (int i=cuerpo.length()-1; i>=0; i--) { suma += (cuerpo.charAt(i)-'0')*factor; factor=factor==7?2:factor+1; }
        int resto=11-suma%11;
        String dv=resto==11?"0":resto==10?"K":String.valueOf(resto);
        if (!rut.endsWith("-"+dv)) throw error(HttpStatus.BAD_REQUEST, "El RUT no es válido.");
    }

    private ResponseStatusException error(HttpStatus status, String mensaje) { return new ResponseStatusException(status, mensaje); }
}
