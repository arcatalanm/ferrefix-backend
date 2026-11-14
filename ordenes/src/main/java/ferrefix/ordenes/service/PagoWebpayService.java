package ferrefix.ordenes.service;


import static ferrefix.ordenes.dto.WebpayDtos.*;
import ferrefix.ordenes.model.PagoWebpay;
import ferrefix.ordenes.repository.CarritoRepository;
import ferrefix.ordenes.repository.PagoWebpayRepository;
import ferrefix.ordenes.repository.PedidoRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service @Transactional
public class PagoWebpayService {
    private static final Logger log = LoggerFactory.getLogger(PagoWebpayService.class);

    // Estados del pedido
    private static final String PEDIDO_PENDIENTE = "Pendiente de pago";
    private static final String PEDIDO_PAGADO = "Pagado";
    private static final String PEDIDO_RECHAZADO = "Pago rechazado";
    private static final String PEDIDO_ANULADO = "Pago anulado";
    private static final String METODO_TRANSFERENCIA = "Transferencia Bancaria";

    // Estados del pago
    private static final String INICIADO = "INICIADO";
    private static final String AUTORIZADO = "AUTORIZADO";
    private static final String RECHAZADO = "RECHAZADO";
    private static final String ABORTADO = "ABORTADO";
    private static final String EXPIRADO = "EXPIRADO";

    private final WebpayClient webpay;
    private final PagoWebpayRepository pagos;
    private final PedidoRepository pedidos;
    private final CarritoRepository carritos;
    private final String returnUrl;

    public PagoWebpayService(WebpayClient webpay, PagoWebpayRepository pagos, PedidoRepository pedidos,
            CarritoRepository carritos, @Value("${app.webpay.return-url}") String returnUrl) {
        this.webpay = webpay;
        this.pagos = pagos;
        this.pedidos = pedidos;
        this.carritos = carritos;
        this.returnUrl = returnUrl;
    }

    // ------------------------------------------------------------------ 1) Iniciar pago

    public IniciarPagoRespuesta iniciar(String idPedido, String propietario) {
        carritos.bloquear(propietario); // mutex por dueño: evita doble clic / doble transacción

        var duenio = pedidos.propietario(idPedido)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Pedido no encontrado."));
        if (!duenio.equals(propietario)) throw error(HttpStatus.NOT_FOUND, "Pedido no encontrado.");

        var pedido = pedidos.buscar(idPedido);
        if (!PEDIDO_PENDIENTE.equals(pedido.estado()) || METODO_TRANSFERENCIA.equals(pedido.metodoPago()))
            throw error(HttpStatus.CONFLICT, "El pedido no admite pago con Webpay.");
        if (pagos.porPedido(idPedido).isPresent())
            throw error(HttpStatus.CONFLICT, "Este pedido ya tiene un intento de pago. Genera un nuevo pedido para reintentar.");

        long monto = pedido.total().longValueExact();          // CLP sin decimales
        String buyOrder = idPedido.replace("-", "").substring(0, 26); // Webpay: máx. 26 caracteres

        TransaccionCreada tx;
        try {
            tx = webpay.crear(buyOrder, idPedido, monto, returnUrl); // session_id = id del pedido (36 ≤ 61)
        } catch (RestClientException ex) {
            log.error("Webpay: no se pudo crear la transacción del pedido {}: {}", idPedido, ex.getMessage());
            throw error(HttpStatus.BAD_GATEWAY, "No se pudo iniciar el pago. Intenta nuevamente.");
        }
        if (tx == null || tx.token() == null || tx.url() == null)
            throw error(HttpStatus.BAD_GATEWAY, "No se pudo iniciar el pago. Intenta nuevamente.");

        Instant ahora = Instant.now();
        pagos.insertar(new PagoWebpay(idPedido, buyOrder, tx.token(), idPedido, monto, INICIADO,
                null, null, null, null, null, null, ahora, ahora));
        return new IniciarPagoRespuesta(tx.url(), tx.token());
    }

    // ------------------------------------------------------------------ 2) Retorno desde Webpay

    /**
     * Flujos de retorno de Webpay Plus:
     *  - Pago normal ........... llega solo token_ws                         → hay que confirmar (commit)
     *  - Pago abortado ......... llega TBK_TOKEN + TBK_ORDEN_COMPRA + TBK_ID_SESION (sin token_ws válido)
     *  - Error en formulario ... llega token_ws + TBK_TOKEN + ...            → NO se confirma
     *  - Timeout en formulario . llega solo TBK_ORDEN_COMPRA + TBK_ID_SESION → no hay token
     * En integración Webpay devuelve por POST; en producción por GET. El controller acepta ambos.
     */
    public ResultadoRetorno procesarRetorno(String tokenWs, String tbkToken, String tbkOrdenCompra) {
        if (!vacio(tbkToken)) { // abortado o error de formulario: nunca confirmar
            return pagos.porTokenBloqueando(recortar(tbkToken))
                    .map(p -> cerrarSinPago(p)).orElse(new ResultadoRetorno(null, "error"));
        }
        if (!vacio(tokenWs)) return confirmar(recortar(tokenWs));
        if (!vacio(tbkOrdenCompra)) { // timeout
            return pagos.porBuyOrderBloqueando(recortar(tbkOrdenCompra))
                    .map(p -> cerrarSinPago(p)).orElse(new ResultadoRetorno(null, "error"));
        }
        throw error(HttpStatus.BAD_REQUEST, "Retorno de Webpay sin datos.");
    }

    private ResultadoRetorno confirmar(String token) {
        var existente = pagos.porTokenBloqueando(token);
        if (existente.isEmpty()) return new ResultadoRetorno(null, "error");
        var pago = existente.get();
        if (!INICIADO.equals(pago.estado())) return resultadoActual(pago); // retorno repetido (F5, doble POST)

        ResultadoTransaccion r;
        try {
            r = webpay.confirmar(token);
        } catch (RestClientException ex) {
            // Falla de red/timeout: puede que Transbank sí haya procesado el commit. Consultamos el estado.
            log.warn("Webpay: commit falló para pedido {} ({}). Consultando estado...", pago.idPedido(), ex.getMessage());
            try {
                r = webpay.consultar(token);
            } catch (RestClientException ex2) {
                log.error("Webpay: tampoco se pudo consultar el estado del pedido {}", pago.idPedido());
                return new ResultadoRetorno(pago.idPedido(), "pendiente"); // sigue INICIADO
            }
        }
        if (r == null || r.status() == null || "INITIALIZED".equals(r.status()))
            return new ResultadoRetorno(pago.idPedido(), "pendiente");

        boolean coincide = pago.buyOrder().equals(r.buyOrder()) && pago.monto() == r.amount();
        if (!coincide)
            log.error("Webpay: datos no coinciden en pedido {} (buy_order={}, amount={}). Revisar manualmente.",
                    pago.idPedido(), r.buyOrder(), r.amount());
        boolean aprobado = coincide && "AUTHORIZED".equals(r.status()) && Integer.valueOf(0).equals(r.responseCode());

        String ultimos4 = r.cardDetail() == null ? null : r.cardDetail().cardNumber();
        pagos.actualizarResultado(pago.idPedido(), aprobado ? AUTORIZADO : RECHAZADO, r.responseCode(),
                r.authorizationCode(), r.paymentTypeCode(), r.installmentsNumber(), ultimos4, fecha(r.transactionDate()));

        if (aprobado) {
            pedidos.cambiarEstado(pago.idPedido(), PEDIDO_PENDIENTE, PEDIDO_PAGADO);
            pedidos.propietario(pago.idPedido()).ifPresent(prop -> { carritos.bloquear(prop); carritos.vaciar(prop); });
            return new ResultadoRetorno(pago.idPedido(), "pagado");
        }
        pedidos.cambiarEstado(pago.idPedido(), PEDIDO_PENDIENTE, PEDIDO_RECHAZADO);
        return new ResultadoRetorno(pago.idPedido(), "rechazado");
    }

    /** Pago abortado / timeout / error de formulario. */
    private ResultadoRetorno cerrarSinPago(PagoWebpay pago) {
        if (INICIADO.equals(pago.estado())) {
            pagos.actualizarEstado(pago.idPedido(), ABORTADO);
            pedidos.cambiarEstado(pago.idPedido(), PEDIDO_PENDIENTE, PEDIDO_ANULADO);
            return new ResultadoRetorno(pago.idPedido(), "anulado");
        }
        return resultadoActual(pago);
    }

    private ResultadoRetorno resultadoActual(PagoWebpay pago) {
        String estado = switch (pago.estado()) {
            case AUTORIZADO -> "pagado";
            case RECHAZADO -> "rechazado";
            case ABORTADO, EXPIRADO -> "anulado";
            default -> "pendiente";
        };
        return new ResultadoRetorno(pago.idPedido(), estado);
    }

    // ------------------------------------------------------------------ 3) Expiración (opcional, ver sección 12)

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    public void expirarPendientes() {
        Instant limite = Instant.now().minus(Duration.ofMinutes(30));
        int a = pagos.anularPedidosConPagoVencido(limite); // primero pedidos (mientras el pago sigue INICIADO)
        int b = pagos.expirarPagos(limite);
        int c = pagos.anularPedidosSinPago(limite);
        if (a + b + c > 0) log.info("Webpay: expirados {} pagos y {} pedidos sin pago", b, a + c);
    }

    // ------------------------------------------------------------------ utilidades

    private static boolean vacio(String s) { return s == null || s.isBlank(); }

    private static String recortar(String s) {
        String v = s.strip();
        if (v.length() > 64) throw error(HttpStatus.BAD_REQUEST, "Parámetro inválido.");
        return v;
    }

    private static Instant fecha(String iso) {
        try { return iso == null ? null : Instant.parse(iso); } catch (DateTimeParseException e) { return null; }
    }

    private static ResponseStatusException error(HttpStatus status, String mensaje) {
        return new ResponseStatusException(status, mensaje);
    }
}