package ferrefix.auth.dto;

import ferrefix.auth.model.Rut;

import ferrefix.auth.model.Usuario;
import jakarta.validation.constraints.*;
import java.util.Locale;

public final class AuthDtos {
    private AuthDtos() {}

    public record Registro(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 120) String segNombre,
            @NotBlank @Size(max = 120) String apellidoPaterno,
            @Size(max = 120) String apellidoMaterno,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 12) String rut,
            @Size(max = 20) String telefono,
            @Size(max = 250) String direccion,
            @Size(max = 120) String region,
            @Size(max = 120) String comuna) {}

    public record Login(
            @NotBlank(message = "El Correo o Rut es obligatorio") @Size(max = 254) String identificador,
            @NotBlank @Size(max = 72) String password) {}

    public record Perfil(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 120) String segNombre,
            @NotBlank @Size(max = 120) String apellidoPaterno,
            @Size(max = 120) String apellidoMaterno,
            @NotBlank @Size(max = 12) String rut,
            @Size(max = 20) String telefono,
            @Size(max = 250) String direccion,
            @Size(max = 120) String region,
            @Size(max = 120) String comuna) {}

    public record UsuarioResponse(String idUsuario, String nombre, String segNombre,
                                  String apellidoPaterno, String apellidoMaterno, String email,
                                  String rol, String rut, String telefono,
                                  String direccion, String region, String comuna) {
        public static UsuarioResponse from(Usuario usuario) {
            return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getSegNombre(),
                    usuario.getApellidoPaterno(), usuario.getApellidoMaterno(), usuario.getEmail(),
                    usuario.getRol().name().toLowerCase(Locale.ROOT), usuario.getRut(),
                    usuario.getTelefono(), usuario.getDireccion(), usuario.getRegion(), usuario.getComuna());
        }
    }

    public record AuthResponse(String accessToken, String tokenType, long expiresIn,
                               UsuarioResponse usuario) {}
}
