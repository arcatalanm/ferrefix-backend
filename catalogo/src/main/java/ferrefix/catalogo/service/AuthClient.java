package ferrefix.catalogo.service;
import ferrefix.catalogo.dto.PerfilUsuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name="auth",url="${app.auth.url}")
public interface AuthClient {
    @GetMapping("/api/v1/auth/me")
    PerfilUsuario perfil(@RequestHeader("Authorization") String token);
}
