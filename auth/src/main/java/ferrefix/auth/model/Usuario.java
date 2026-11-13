package ferrefix.auth.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "seg_nombre", length = 120)
    private String segNombre;
    @Column(name = "apellido_paterno", length = 120)
    private String apellidoPaterno;
    @Column(name = "apellido_materno", length = 120)
    private String apellidoMaterno;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(length = 12, unique = true)
    private String rut;
    @Column(length = 20)
    private String telefono;
    @Column(length = 250)
    private String direccion;
    @Column(length = 120)
    private String region;
    @Column(length = 120)
    private String comuna;

    public Usuario(String nombre, String email, String passwordHash) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = Rol.CLIENTE;
    }

    public void actualizarPerfil(String nombre, String segNombre, String apellidoPaterno,
                                 String apellidoMaterno, String rut, String telefono,
                                 String direccion, String region, String comuna) {
        this.nombre = nombre.strip();
        this.segNombre = limpiar(segNombre);
        this.apellidoPaterno = apellidoPaterno.strip();
        this.apellidoMaterno = limpiar(apellidoMaterno);
        this.rut = rut;
        this.telefono = limpiar(telefono);
        this.direccion = limpiar(direccion);
        this.region = limpiar(region);
        this.comuna = limpiar(comuna);
    }

    public static Usuario inicial(String nombre, String email, String passwordHash, Rol rol) {
        var usuario = new Usuario(nombre, email, passwordHash);
        usuario.rol = rol;
        return usuario;
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.strip();
    }
}
