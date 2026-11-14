package ferrefix.ordenes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class WebpayDtos {
    private WebpayDtos() {}

    /** Cuerpo del POST /transactions */
    public record CrearTransaccion(@JsonProperty("buy_order") String buyOrder,
            @JsonProperty("session_id") String sessionId, long amount,
            @JsonProperty("return_url") String returnUrl) {}

    /** Respuesta del POST /transactions */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransaccionCreada(String token, String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetalleTarjeta(@JsonProperty("card_number") String cardNumber) {}

    /** Respuesta del PUT (commit) y del GET (status) /transactions/{token} */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultadoTransaccion(String vci, long amount, String status,
            @JsonProperty("buy_order") String buyOrder,
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("card_detail") DetalleTarjeta cardDetail,
            @JsonProperty("accounting_date") String accountingDate,
            @JsonProperty("transaction_date") String transactionDate,
            @JsonProperty("authorization_code") String authorizationCode,
            @JsonProperty("payment_type_code") String paymentTypeCode,
            @JsonProperty("response_code") Integer responseCode,
            @JsonProperty("installments_number") Integer installmentsNumber) {}

    // ---- Contratos propios de tu API ----

    /** Respuesta de POST /pedidos/{id}/pago/webpay: el frontend hace form POST a `url` con `token_ws`. */
    public record IniciarPagoRespuesta(String url, String token) {}

    /** Resultado interno del retorno. estado: pagado | rechazado | anulado | pendiente | error */
    public record ResultadoRetorno(String idPedido, String estado) {}
}