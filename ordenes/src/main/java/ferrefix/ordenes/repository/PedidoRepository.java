package ferrefix.ordenes.repository;

import ferrefix.ordenes.model.ClientePedido;
import ferrefix.ordenes.model.ItemPedido;
import ferrefix.ordenes.model.Pedido;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRepository {
    private final JdbcTemplate jdbc;

    public PedidoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<String> propietario(String id) {
        return jdbc.queryForList("SELECT propietario FROM pedidos WHERE id = ?", String.class, id).stream().findFirst();
    }

    public void guardar(String propietario, Pedido pedido) {
        var c = pedido.cliente();
        jdbc.update("INSERT INTO pedidos (id,propietario,id_usuario,fecha,nombre,rut,email,telefono,direccion,region,comuna,subtotal,despacho,total,estado,metodo_pago,metodo_entrega,indicaciones) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                pedido.idOrden(), propietario, pedido.idUsuario(), Timestamp.from(pedido.fecha()), c.nombre(), c.rut(),
                c.email(), c.telefono(), c.direccion(), c.region(), c.comuna(), pedido.subtotal(), pedido.despacho(), pedido.total(),
                pedido.estado(), pedido.metodoPago(), pedido.metodoEntrega(), pedido.indicaciones());
        for (var item : pedido.items()) {
            jdbc.update("INSERT INTO pedido_items (id_pedido,id_producto,nombre,cantidad,precio_unitario) VALUES (?,?,?,?,?)",
                    pedido.idOrden(), item.idProducto(), item.nombre(), item.cantidad(), item.precioUnitario());
        }
    }

    public List<Pedido> listarTodos() {
        return jdbc.queryForList("SELECT id FROM pedidos ORDER BY fecha DESC", String.class).stream().map(this::buscar).toList();
    }

    public List<Pedido> listarPorUsuario(String idUsuario) {
        return jdbc.queryForList("SELECT id FROM pedidos WHERE id_usuario = ? ORDER BY fecha DESC", String.class, idUsuario)
                .stream().map(this::buscar).toList();
    }

    public Pedido buscar(String id) {
        var items = jdbc.query("SELECT id_producto,nombre,cantidad,precio_unitario FROM pedido_items WHERE id_pedido = ? ORDER BY id_producto",
                (rs, n) -> new ItemPedido(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getBigDecimal(4)), id);
        return jdbc.queryForObject("SELECT * FROM pedidos WHERE id = ?", (rs, n) -> new Pedido(rs.getString("id"), rs.getString("id_usuario"),
                rs.getTimestamp("fecha").toInstant(), new ClientePedido(rs.getString("nombre"), rs.getString("rut"), rs.getString("email"),
                rs.getString("telefono"), rs.getString("direccion"), rs.getString("region"), rs.getString("comuna")), items,
                rs.getBigDecimal("subtotal"), rs.getBigDecimal("despacho"), rs.getBigDecimal("total"), rs.getString("estado"),
                rs.getString("metodo_pago"), rs.getString("metodo_entrega"), rs.getString("indicaciones")), id);
    }

    /** Cambio de estado condicional: solo si el pedido sigue en `desde` (evita pisar estados finales). */
    public int cambiarEstado(String id, String desde, String hacia) {
        return jdbc.update("UPDATE pedidos SET estado = ? WHERE id = ? AND estado = ?", hacia, id, desde);
    }
}
