package ferrefix.ordenes.service;

import ferrefix.ordenes.dto.PerfilUsuario;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthClient {
    private final RestClient client;
    public AuthClient(@Value("${app.auth.url}") String url) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    public PerfilUsuario perfil(String token) {
        return client.get().uri("/api/v1/auth/me").header("Authorization", token).retrieve().body(PerfilUsuario.class);
    }
}
