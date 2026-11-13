package ferrefix.ordenes.dto;

import jakarta.validation.constraints.*;

public record CotizacionRequest(@NotBlank @Size(max=200) String nombre,
            @NotBlank @Pattern(regexp="[0-9.Kk-]{8,12}") String rut,
            @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=20) String telefono,
            @NotBlank @Size(max=120) String tipoProyecto, @NotBlank @Size(max=120) String region,
            @NotBlank @Size(max=4000) String detalle, boolean despachoObra) {}
