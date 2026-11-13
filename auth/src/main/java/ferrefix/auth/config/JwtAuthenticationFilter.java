package ferrefix.auth.config;

import ferrefix.auth.service.JwtService;
import ferrefix.auth.exception.ApiSecurityError;

import ferrefix.auth.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioRepository usuarios;

    public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equals(request.getMethod())
                || ("POST".equals(request.getMethod())
                && (path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/login")));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
                ApiSecurityError.unauthorized(response);
                return;
            }
            String id;
            try {
                id = jwt.obtenerUsuarioId(header.substring(7));
            } catch (JwtException | IllegalArgumentException exception) {
                ApiSecurityError.unauthorized(response);
                return;
            }
            var usuario = usuarios.findById(id);
            if (usuario.isEmpty()) {
                ApiSecurityError.unauthorized(response);
                return;
            }
            var authentication = new UsernamePasswordAuthenticationToken(id, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.get().getRol().name())));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }
}
