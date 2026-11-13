package ferrefix.catalogo.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "ofertas") @Getter @Setter
public class Oferta {
    @Id private Integer indice;
    @Column(nullable = false, length = 255) private String imagen;
    @Column(nullable = false, length = 500) private String enlace;
    @Column(nullable = false, length = 200) private String alt;
    @Column(nullable = false) private Instant fecha;
}
