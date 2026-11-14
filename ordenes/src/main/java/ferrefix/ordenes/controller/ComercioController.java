package ferrefix.ordenes.controller;

import ferrefix.ordenes.model.ItemCarrito;
import ferrefix.ordenes.model.Pedido;

import ferrefix.ordenes.service.ComercioService;
import ferrefix.ordenes.dto.ComercioDtos.Cantidad;
import ferrefix.ordenes.dto.ComercioDtos.CrearPedido;
import ferrefix.ordenes.dto.PerfilUsuario;

import ferrefix.ordenes.service.PagoWebpayService;
import ferrefix.ordenes.dto.WebpayDtos.IniciarPagoRespuesta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/ordenes")
public class ComercioController {

    private final ComercioService comercio;
    private final PagoWebpayService pagos;
    public ComercioController(ComercioService comercio, PagoWebpayService pagos) {
        this.comercio = comercio;
        this.pagos = pagos;
    }

    private String invitado(HttpServletRequest req, HttpServletResponse res) {
        String id = req.getCookies() == null ? null : Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals("ferrefix_carrito")).map(c -> c.getValue()).findFirst().orElse(null);
        if (id == null || !id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            id = UUID.randomUUID().toString();
            res.addHeader("Set-Cookie", ResponseCookie.from("ferrefix_carrito", id).httpOnly(true).secure(req.isSecure())
                    .sameSite("Lax").path("/api/v1/ordenes").maxAge(Duration.ofDays(30)).build().toString());
        }
        return "invitado:" + id;
    }
    private String propietario(PerfilUsuario perfil, HttpServletRequest req, HttpServletResponse res) {
        return perfil == null ? invitado(req,res) : "usuario:" + perfil.idUsuario();
    }
    @GetMapping("/carrito")
    public List<ItemCarrito> carrito(@AuthenticationPrincipal PerfilUsuario perfil, HttpServletRequest req, HttpServletResponse res) {
        String invitado = invitado(req,res);
        return comercio.cargarCarrito(perfil == null ? invitado : "usuario:"+perfil.idUsuario(), perfil == null ? null : invitado);
    }
    @PostMapping("/carrito/items/{id}")
    public List<ItemCarrito> agregar(@PathVariable @Size(max=36) String id, @Valid @RequestBody Cantidad cantidad,
            @AuthenticationPrincipal PerfilUsuario perfil, HttpServletRequest req, HttpServletResponse res) {
        return comercio.modificarCarrito(propietario(perfil,req,res), id, cantidad.cantidad(), true);
    }
    @PutMapping("/carrito/items/{id}")
    public List<ItemCarrito> modificar(@PathVariable @Size(max=36) String id, @Valid @RequestBody Cantidad cantidad,
            @AuthenticationPrincipal PerfilUsuario perfil, HttpServletRequest req, HttpServletResponse res) {
        return comercio.modificarCarrito(propietario(perfil,req,res), id, cantidad.cantidad(), false);
    }
    @DeleteMapping("/carrito/items/{id}")
    public List<ItemCarrito> quitar(@PathVariable String id, @AuthenticationPrincipal PerfilUsuario perfil,
            HttpServletRequest req, HttpServletResponse res) {
        return comercio.quitarCarrito(propietario(perfil,req,res), id);
    }
    @DeleteMapping("/carrito")
    public List<ItemCarrito> vaciar(@AuthenticationPrincipal PerfilUsuario perfil, HttpServletRequest req, HttpServletResponse res) {
        return comercio.quitarCarrito(propietario(perfil,req,res), null);
    }
    @GetMapping("/pedidos")
    public List<Pedido> pedidos(@AuthenticationPrincipal PerfilUsuario perfil) { return comercio.listarPedidos(perfil); }
    @PostMapping("/pedidos") @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody CrearPedido datos, @AuthenticationPrincipal PerfilUsuario perfil,
            HttpServletRequest req, HttpServletResponse res) {
        return comercio.crearPedido(propietario(perfil,req,res), perfil, datos);
    }

    @GetMapping("/pedidos/{id}")
    public Pedido pedido(@PathVariable @Size(max=36) String id, @AuthenticationPrincipal PerfilUsuario perfil,
            HttpServletRequest req, HttpServletResponse res) {
        return comercio.obtenerPedido(id, propietario(perfil, req, res), perfil);
    }

    @PostMapping("/pedidos/{id}/pago/webpay")
    public IniciarPagoRespuesta pagarConWebpay(@PathVariable @Size(max=36) String id,
            @AuthenticationPrincipal PerfilUsuario perfil, HttpServletRequest req, HttpServletResponse res) {
        return pagos.iniciar(id, propietario(perfil, req, res));
    }
}
