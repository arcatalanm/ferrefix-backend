package ferrefix.auth.service;

import ferrefix.auth.dto.AuthDtos.*;
import ferrefix.auth.service.JwtService;
import ferrefix.auth.model.Usuario;
import ferrefix.auth.repository.UsuarioRepository;
import ferrefix.auth.model.Rut;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final String dummyHash;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwords, JwtService jwt) {
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.jwt = jwt;
        this.dummyHash = passwords.encode("dummy-password-for-timing");
    }

    @Transactional
    public AuthResponse registrar(Registro request) {
        validarPassword(request.password());
        String email = normalizarEmail(request.email());
        String rut = validarRut(request.rut());
        if (usuarios.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con este correo.");
        }
        if (usuarios.existsByRut(rut))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con este RUT.");
        var usuario = new Usuario(request.nombre().strip(), email, passwords.encode(request.password()));
        usuario.actualizarPerfil(request.nombre(), request.segNombre(), request.apellidoPaterno(),
                request.apellidoMaterno(), rut, request.telefono(),
                request.direccion(), request.region(), request.comuna());

        usuarios.saveAndFlush(usuario);
        return autenticar(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(Login request) {
        validarPassword(request.password());
        String identificador = request.identificador().strip();
        var usuario = identificador.contains("@")
                ? usuarios.findByEmail(normalizarEmail(identificador))
                : usuarios.findByRut(validarRut(identificador));
        boolean coincide = passwords.matches(request.password(),
                usuario.map(Usuario::getPasswordHash).orElse(dummyHash));
        if (usuario.isEmpty() || usuario.get().getPasswordHash() == null || !coincide) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }
        return autenticar(usuario.get());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String id) {
        return UsuarioResponse.from(buscarUsuario(id));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(String id, Perfil request) {
        var usuario = buscarUsuario(id);
        String rut = validarRut(request.rut());
        if (usuarios.existsByRutAndIdNot(rut, id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con este RUT.");
        usuario.actualizarPerfil(request.nombre(), request.segNombre(), request.apellidoPaterno(),
                request.apellidoMaterno(), rut, request.telefono(),
                request.direccion(), request.region(), request.comuna());
        usuarios.flush();
        return UsuarioResponse.from(usuario);
    }

    private Usuario buscarUsuario(String id) {
        return usuarios.findById(id).orElseThrow(() -> new BadCredentialsException("Sesión inválida."));
    }

    private AuthResponse autenticar(Usuario usuario) {
        return new AuthResponse(jwt.emitir(usuario), "Bearer", jwt.expiresIn(), UsuarioResponse.from(usuario));
    }

    private static String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static String validarRut(String valor) {
        try {
            return Rut.normalizar(valor);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    private static void validarPassword(String password) {

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña no puede superar 72 bytes UTF-8.");
        }
    }
}
