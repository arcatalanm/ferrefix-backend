package ferrefix.auth.repository;

import ferrefix.auth.model.Usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByRut(String rut);
    boolean existsByEmail(String email);
    boolean existsByRut(String rut);
    boolean existsByRutAndIdNot(String rut, String id);
}
