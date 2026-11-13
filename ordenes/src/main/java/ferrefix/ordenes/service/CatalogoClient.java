package ferrefix.ordenes.service;

import ferrefix.ordenes.dto.ComercioDtos.Producto;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogoClient {
    private final RestClient cliente;

    public CatalogoClient(@Value("${app.catalogo.url}") String url) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        cliente = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    public Producto buscar(String id) {
        try {
            var producto = cliente.get().uri("/api/v1/catalogo/productos/{id}", id).retrieve().body(Producto.class);
            if (producto == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar el catálogo.");
            return producto;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) return null;
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar el catálogo. Intenta nuevamente.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar el catálogo. Intenta nuevamente.");
        }
    }
}
