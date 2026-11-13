package ferrefix.catalogo.repository;

import ferrefix.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ProductoRepository extends JpaRepository<Producto,String> {
    Page<Producto> findByActivoTrue(Pageable pageable);
}
