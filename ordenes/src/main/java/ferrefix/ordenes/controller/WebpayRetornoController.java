package ferrefix.ordenes.controller;

import static ferrefix.ordenes.dto.WebpayDtos.ResultadoRetorno;
import ferrefix.ordenes.service.PagoWebpayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/ordenes/pagos/webpay")
public class WebpayRetornoController {
    private static final Logger log = LoggerFactory.getLogger(WebpayRetornoController.class);
    private final PagoWebpayService pagos;
    private final String frontendUrl;

    public WebpayRetornoController(PagoWebpayService pagos, @Value("${app.webpay.frontend-url}") String frontendUrl) {
        this.pagos = pagos;
        this.frontendUrl = frontendUrl;
    }

    // Integración: Webpay vuelve por POST (form). Producción: por GET. Aceptamos ambos.
    @RequestMapping(value = "/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> retorno(
            @RequestParam(name = "token_ws", required = false) String tokenWs,
            @RequestParam(name = "TBK_TOKEN", required = false) String tbkToken,
            @RequestParam(name = "TBK_ORDEN_COMPRA", required = false) String tbkOrdenCompra,
            @RequestParam(name = "TBK_ID_SESION", required = false) String tbkIdSesion) {
        ResultadoRetorno r;
        try {
            r = pagos.procesarRetorno(tokenWs, tbkToken, tbkOrdenCompra);
        } catch (RuntimeException ex) {
            log.error("Error procesando retorno de Webpay", ex);
            r = new ResultadoRetorno(null, "error");
        }
        var destino = UriComponentsBuilder.fromUriString(frontendUrl).queryParam("estado", r.estado());
        if (r.idPedido() != null) destino.queryParam("pedido", r.idPedido());
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(destino.build().toUri()).build();
    }
}