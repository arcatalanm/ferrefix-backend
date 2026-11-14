package ferrefix.ordenes.repository;


import ferrefix.ordenes.model.PagoWebpay;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PagoWebpayRepository {
    private static final RowMapper<PagoWebpay> MAPPER = (rs, n) -> new PagoWebpay(
            rs.getString("id_pedido"), rs.getString("buy_order"), rs.getString("token"), rs.getString("session_id"),
            rs.getLong("monto"), rs.getString("estado"), rs.getObject("codigo_respuesta", Integer.class),
            rs.getString("codigo_autorizacion"), rs.getString("tipo_pago"), rs.getObject("cuotas", Integer.class),
            rs.getString("tarjeta_ultimos4"),
            rs.getTimestamp("fecha_transaccion") == null ? null : rs.getTimestamp("fecha_transaccion").toInstant(),
            rs.getTimestamp("fecha_creacion").toInstant(), rs.getTimestamp("fecha_actualizacion").toInstant());

    private final JdbcTemplate jdbc;

    public PagoWebpayRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static Timestamp ts(Instant i) { return i == null ? null : Timestamp.from(i); }

    public void insertar(PagoWebpay p) {
        jdbc.update("INSERT INTO pagos_webpay (id_pedido,buy_order,token,session_id,monto,estado,codigo_respuesta,codigo_autorizacion,tipo_pago,cuotas,tarjeta_ultimos4,fecha_transaccion,fecha_creacion,fecha_actualizacion) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                p.idPedido(), p.buyOrder(), p.token(), p.sessionId(), p.monto(), p.estado(), p.codigoRespuesta(),
                p.codigoAutorizacion(), p.tipoPago(), p.cuotas(), p.tarjetaUltimos4(), ts(p.fechaTransaccion()),
                ts(p.fechaCreacion()), ts(p.fechaActualizacion()));
    }

    public Optional<PagoWebpay> porPedido(String idPedido) {
        return jdbc.query("SELECT * FROM pagos_webpay WHERE id_pedido = ?", MAPPER, idPedido).stream().findFirst();
    }

    public Optional<PagoWebpay> porTokenBloqueando(String token) {
        return jdbc.query("SELECT * FROM pagos_webpay WHERE token = ? FOR UPDATE", MAPPER, token).stream().findFirst();
    }

    public Optional<PagoWebpay> porBuyOrderBloqueando(String buyOrder) {
        return jdbc.query("SELECT * FROM pagos_webpay WHERE buy_order = ? FOR UPDATE", MAPPER, buyOrder).stream().findFirst();
    }

    public void actualizarEstado(String idPedido, String estado) {
        jdbc.update("UPDATE pagos_webpay SET estado = ?, fecha_actualizacion = ? WHERE id_pedido = ?",
                estado, Timestamp.from(Instant.now()), idPedido);
    }

    public void actualizarResultado(String idPedido, String estado, Integer codigoRespuesta, String codigoAutorizacion,
            String tipoPago, Integer cuotas, String ultimos4, Instant fechaTransaccion) {
        jdbc.update("UPDATE pagos_webpay SET estado = ?, codigo_respuesta = ?, codigo_autorizacion = ?, tipo_pago = ?, cuotas = ?, tarjeta_ultimos4 = ?, fecha_transaccion = ?, fecha_actualizacion = ? WHERE id_pedido = ?",
                estado, codigoRespuesta, codigoAutorizacion, tipoPago, cuotas, ultimos4, ts(fechaTransaccion),
                Timestamp.from(Instant.now()), idPedido);
    }

    // ---- Expiración de pagos abandonados (ver sección 12) ----

    /** Pedidos cuyo pago quedó INICIADO y venció. Debe ejecutarse ANTES de expirarPagos. */
    public int anularPedidosConPagoVencido(Instant limite) {
        return jdbc.update("UPDATE pedidos p JOIN pagos_webpay w ON w.id_pedido = p.id SET p.estado = 'Pago anulado' WHERE w.estado = 'INICIADO' AND w.fecha_creacion < ? AND p.estado = 'Pendiente de pago'",
                Timestamp.from(limite));
    }

    public int expirarPagos(Instant limite) {
        return jdbc.update("UPDATE pagos_webpay SET estado = 'EXPIRADO', fecha_actualizacion = ? WHERE estado = 'INICIADO' AND fecha_creacion < ?",
                Timestamp.from(Instant.now()), Timestamp.from(limite));
    }

    /** Pedidos Webpay que nunca llegaron a iniciar el pago. */
    public int anularPedidosSinPago(Instant limite) {
        return jdbc.update("UPDATE pedidos SET estado = 'Pago anulado' WHERE estado = 'Pendiente de pago' AND metodo_pago <> 'Transferencia Bancaria' AND fecha < ? AND id NOT IN (SELECT id_pedido FROM pagos_webpay)",
                Timestamp.from(limite));
    }
}