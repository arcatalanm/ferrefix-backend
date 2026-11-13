package ferrefix.arriendos.repository;

import ferrefix.arriendos.model.Maquina;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MaquinaRepository extends JpaRepository<Maquina, String> {
    List<Maquina> findAllByOrderByNombreAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Maquina m where m.id = :id")
    Optional<Maquina> bloquear(String id);
}
