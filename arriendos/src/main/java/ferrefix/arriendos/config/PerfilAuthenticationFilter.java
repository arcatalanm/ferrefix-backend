package ferrefix.arriendos.config;

import static ferrefix.arriendos.exception.ApiSecurityError.error;
import ferrefix.arriendos.service.AuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.filter.OncePerRequestFilter;

public class PerfilAuthenticationFilter extends OncePerRequestFilter {
    private final AuthClient authClient;
    public PerfilAuthenticationFilter(AuthClient authClient) { this.authClient = authClient; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getMethod().equals("OPTIONS") || !request.getServletPath().startsWith("/api/v1/arriendos/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String token = req.getHeader("Authorization");
        if (token != null) {
            if (!token.startsWith("Bearer ")) { error(res, 401); return; }
            try {
                var perfil = authClient.perfil(token);
                if (perfil == null || perfil.idUsuario() == null) { error(res, 503); return; }
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken(perfil, null,
                        List.of(new SimpleGrantedAuthority(perfil.administrador() ? "ROLE_ADMIN" : "ROLE_CLIENTE"))));
                SecurityContextHolder.setContext(context);
            } catch (RestClientResponseException ex) {
                error(res, ex.getStatusCode().value() == 401 ? 401 : 503); return;
            } catch (RestClientException ex) {
                error(res, 503); return;
            }
        }
        chain.doFilter(req, res);
    }
}
