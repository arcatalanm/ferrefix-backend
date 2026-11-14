package ferrefix.ordenes.service;


import static ferrefix.ordenes.dto.WebpayDtos.*;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Cliente de la API REST de Webpay Plus. No captura excepciones:
 * RestClientException / RestClientResponseException suben al servicio, que decide qué hacer.
 */
@Service
public class WebpayClient {
    private static final String BASE = "/rswebpaytransaction/api/webpay/v1.2/transactions";
    private final RestClient cliente;

    public WebpayClient(@Value("${app.webpay.url}") String url,
            @Value("${app.webpay.commerce-code}") String codigoComercio,
            @Value("${app.webpay.api-key}") String apiKey) {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.cliente = RestClient.builder().baseUrl(url).requestFactory(factory)
                .defaultHeader("Tbk-Api-Key-Id", codigoComercio)
                .defaultHeader("Tbk-Api-Key-Secret", apiKey)
                .build();
    }

    /** POST /transactions → token + url del formulario de pago. */
    public TransaccionCreada crear(String buyOrder, String sessionId, long monto, String returnUrl) {
        return cliente.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CrearTransaccion(buyOrder, sessionId, monto, returnUrl))
                .retrieve().body(TransaccionCreada.class);
    }

    /** PUT /transactions/{token} → confirma (commit). Solo se puede hacer UNA vez por token. */
    public ResultadoTransaccion confirmar(String token) {
        return cliente.put().uri(BASE + "/{token}", token)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve().body(ResultadoTransaccion.class);
    }

    /** GET /transactions/{token} → estado actual (útil si el commit falló por red). */
    public ResultadoTransaccion consultar(String token) {
        return cliente.get().uri(BASE + "/{token}", token)
                .retrieve().body(ResultadoTransaccion.class);
    }
}