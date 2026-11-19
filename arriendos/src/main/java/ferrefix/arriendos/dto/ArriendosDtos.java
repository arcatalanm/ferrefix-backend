package ferrefix.arriendos.dto;

import ferrefix.arriendos.model.Maquina;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class ArriendosDtos {
    private ArriendosDtos() {}

    public record MaquinaRequest(
            @NotBlank @Size(max = 200) String nombre,
            @NotBlank @Size(max = 120) String marca,
            @NotBlank @Size(max = 120) String categoria,
            @NotNull @DecimalMin("1") @Digits(integer = 12, fraction = 0) BigDecimal tarifaDia,
            @Size(max = 500) @Pattern(regexp = "^(/media/[a-zA-Z0-9_./-]+)?$") String imagen,
            @Size(max = 500) String incluye,
            @NotNull Maquina.Estado estado,
            @NotBlank @Size(max = 100000) String descripcionDetalle) {}

    public record ArriendoRequest(
            @NotBlank @Size(max = 40) String idMaquina,
            @NotNull LocalDate fechaInicio,
            @NotNull LocalDate fechaTermino,
            @NotBlank @Size(max = 20) @Pattern(regexp = "^[+0-9 ()-]{8,20}$") String telefono,
            @NotBlank @Size(max = 250) String direccion,
            @NotBlank @Size(max = 120) String region,
            @NotBlank @Size(max = 120) String comuna) {}

    public record UsuarioArriendo(String idUsuario, String nombre, String rut, String email,
            String telefono, String direccion, String region, String comuna) {}

    public record MaquinaResponse(String idMaquina, String nombre, String marca, String categoria,
            BigDecimal tarifaDia, String imagen, String incluye, String descripcionDetalle,
            String estado, String idUsuario, String nombreUsuario, LocalDate fechaInicio, LocalDate fechaTermino) {}

    public record ArriendoResponse(String idContrato, Instant fechaContrato, String idMaquina,
            String nombreMaquina, String marcaMaquina, BigDecimal tarifaDia, long diasArriendo,
            LocalDate fechaInicio, LocalDate fechaTermino, BigDecimal totalPagar, UsuarioArriendo usuario,
            String estado) {}
}
