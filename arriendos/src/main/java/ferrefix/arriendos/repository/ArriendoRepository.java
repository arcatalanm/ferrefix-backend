package ferrefix.arriendos.repository;

import ferrefix.arriendos.model.Arriendo;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArriendoRepository extends JpaRepository<Arriendo, String> {
    @Query("select a from Arriendo a join fetch a.maquina order by a.fechaContrato desc")
    List<Arriendo> listarTodos();

    @Query("select a from Arriendo a join fetch a.maquina where a.idUsuario = :idUsuario order by a.fechaContrato desc")
    List<Arriendo> listarPorUsuario(String idUsuario);

    List<Arriendo> findAllByEstadoAndFechaTerminoGreaterThanEqual(Arriendo.Estado estado, LocalDate hoy);

    boolean existsByMaquinaIdAndEstadoAndFechaTerminoGreaterThanEqual(String id, Arriendo.Estado estado, LocalDate hoy);

    @Modifying
    @Query("update Arriendo a set a.estado = :finalizado where a.estado = :vigente and a.fechaTermino < :hoy")
    int finalizarVencidos(LocalDate hoy, Arriendo.Estado vigente, Arriendo.Estado finalizado);
}
