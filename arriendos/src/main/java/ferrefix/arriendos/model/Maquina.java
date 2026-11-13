package ferrefix.arriendos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.DISPONIBLE;
    @ElementCollection
    @CollectionTable(name = "maquina_especificaciones", joinColumns = @JoinColumn(name = "id_maquina"))
    @MapKeyColumn(name = "atributo", length = 80)
    @Column(name = "valor", nullable = false, length = 200)
    private Map<String, String> especificaciones = new LinkedHashMap<>();
}
