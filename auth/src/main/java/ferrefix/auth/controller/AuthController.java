package ferrefix.auth.controller;

import ferrefix.auth.service.AuthService;

import ferrefix.auth.dto.AuthDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un cliente e iniciar su sesión")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody Registro request) {
        return ResponseEntity.created(URI.create("/api/v1/auth/me"))
                .cacheControl(CacheControl.noStore()).body(auth.registrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con correo y contraseña")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody Login request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(auth.login(request));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener el perfil del usuario autenticado")
    public UsuarioResponse obtenerPerfil(Principal principal) {
        return auth.obtenerPerfil(principal.getName());
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reemplazar los datos editables del perfil propio")
    public UsuarioResponse actualizarPerfil(Principal principal, @Valid @RequestBody Perfil request) {
        return auth.actualizarPerfil(principal.getName(), request);
    }
}
