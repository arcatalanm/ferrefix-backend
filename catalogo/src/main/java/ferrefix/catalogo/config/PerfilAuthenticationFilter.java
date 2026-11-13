package ferrefix.catalogo.config;

import static ferrefix.catalogo.exception.ApiSecurityError.error;
import ferrefix.catalogo.service.AuthClient;
import feign.FeignException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class PerfilAuthenticationFilter extends OncePerRequestFilter {
    private final AuthClient auth;
    public PerfilAuthenticationFilter(AuthClient auth) { this.auth = auth; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getMethod().equals("GET") || request.getMethod().equals("HEAD") || request.getMethod().equals("OPTIONS")
            || !request.getServletPath().startsWith("/api/v1/catalogo/");
    }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)
        throws ServletException,IOException {
        String token=req.getHeader("Authorization");
        if(token==null || !token.startsWith("Bearer ")) { error(res,401); return; }
        try {
            var usuario=auth.perfil(token);
            if(usuario == null || usuario.idUsuario() == null) { error(res,503); return; }
            if(!"admin".equals(usuario.rol())) { error(res,403); return; }
            var context=SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(usuario.idUsuario(),null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            SecurityContextHolder.setContext(context);
        } catch(FeignException ex) { error(res,ex.status()==401?401:503); return; }
        chain.doFilter(req,res);
    }
}
