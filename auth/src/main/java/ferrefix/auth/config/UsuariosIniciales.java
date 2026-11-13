package ferrefix.auth.config;

import ferrefix.auth.model.Usuario;
import ferrefix.auth.model.Rol;
import ferrefix.auth.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class UsuariosIniciales implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final Environment env;

    public UsuariosIniciales(UsuarioRepository usuarios, PasswordEncoder passwords, Environment env) {
        this.usuarios = usuarios; this.passwords = passwords; this.env = env;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var emails = new HashSet<String>();
        String[] claves = {"admin", "cliente1", "cliente2"};
        for (String clave : claves) {
            String email = env.getRequiredProperty("app.seed." + clave + "-email").strip().toLowerCase(Locale.ROOT);
            String password = env.getRequiredProperty("app.seed." + clave + "-password");
            if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || email.length() > 254 || !emails.add(email))
                throw new IllegalStateException("Configura tres correos iniciales válidos y distintos.");
            if (usuarios.existsByEmail(email)) continue;
            if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
                throw new IllegalStateException("Configura SEED_" + clave.toUpperCase(Locale.ROOT) + "_PASSWORD con entre 12 y 72 bytes.");
            Rol rol = clave.equals("admin") ? Rol.ADMIN : Rol.CLIENTE;
            String nombre = clave.equals("admin") ? "Administrador Ferrefix" : "Cliente " + clave.substring(7);
            usuarios.saveAndFlush(Usuario.inicial(nombre, email, passwords.encode(password), rol));
        }
    }
}
