package ferrefix.arriendos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "maquinas")
@Getter
@Setter
public class Maquina {
    public enum Estado { DISPONIBLE, MANTENCION }

    @Id
    @Column(length = 40)
    private String id;
    @Column(nullable = false, length = 200)
    private String nombre;
    @Column(nullable = false, length = 120)
    private String marca;
    @Column(nullable = false, length = 120)
    private String categoria;
    @Column(name = "tarifa_dia", nullable = false, precision = 12, scale = 0)
    private BigDecimal tarifaDia;
    @Column(length = 500)
    private String imagen;
    @Column(length = 500)
    private String incluye;
    @Lob
    @Column(name = "descripcion_detalle", nullable = false, columnDefinition = "LONGTEXT")
    private String descripcionDetalle = "";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.DISPONIBLE;
}
