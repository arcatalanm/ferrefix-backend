package ferrefix.catalogo.exception;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ApiSecurityError {
    private ApiSecurityError() {}
    public static void error(HttpServletResponse response,int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        if(status==401) response.setHeader("WWW-Authenticate","Bearer");
        response.getWriter().write("{\"status\":"+status+",\"detail\":\""+
            (status==503?"Autenticación temporalmente no disponible.":status==401?"Inicia sesión para continuar.":"Se requieren permisos de administrador.")+"\"}");
    }
}
