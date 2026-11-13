package ferrefix.arriendos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "arriendos")
@Getter
@Setter
public class Arriendo {
    public enum Estado { VIGENTE, FINALIZADO }

    @Id
    @Column(length = 36)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_maquina", nullable = false)
    private Maquina maquina;
    @Column(name = "id_usuario", nullable = false, length = 36)
    private String idUsuario;
    @Column(name = "nombre_usuario", nullable = false, length = 500)
    private String nombreUsuario;
    @Column(nullable = false, length = 12)
    private String rut;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(nullable = false, length = 20)
    private String telefono;
    @Column(nullable = false, length = 250)
    private String direccion;
    @Column(nullable = false, length = 120)
    private String region;
    @Column(nullable = false, length = 120)
    private String comuna;
    @Column(name = "fecha_contrato", nullable = false)
    private Instant fechaContrato;
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
    @Column(name = "fecha_termino", nullable = false)
    private LocalDate fechaTermino;
    @Column(name = "tarifa_dia", nullable = false, precision = 12, scale = 0)
    private BigDecimal tarifaDia;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.VIGENTE;
}
