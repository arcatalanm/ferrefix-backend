package ferrefix.catalogo.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity @Table(name="productos") @Getter @Setter @NoArgsConstructor
public class Producto {
    @Id @Column(length=36) private String idProducto;
    @Column(nullable=false, length=160) private String nombre;
    @Column(nullable=false, length=100) private String marca;
    @Column(nullable=false, length=80) private String categoria;
    @Column(nullable=false, precision=12, scale=0) private BigDecimal precioVenta;
    @Column(nullable=false) private int stock;
    @Column(nullable=false, length=20) private String unidadVenta = "unidad";
    @Column(nullable=false, length=120) private String contenidoPresentacion = "1 unidad";
    @Column(nullable=false, length=300) private String descripcionCorta;
    @Lob @Column(name="descripcion_detalle", nullable=false, columnDefinition="LONGTEXT") private String descripcionDetalle;
    @Column(nullable=false, length=255) private String imagen;
    @Column(nullable=false) private boolean activo = true;
    @Column(nullable=false) private Instant fechaCreacion = Instant.now();
}
