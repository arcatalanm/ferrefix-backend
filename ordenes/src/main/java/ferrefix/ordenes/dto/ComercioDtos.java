package ferrefix.ordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ComercioDtos {
    private ComercioDtos() {}
    public record Cantidad(@Min(1) @Max(999) int cantidad) {}
    public record ItemSolicitud(@NotBlank @Size(max=36) String idProducto, @Min(1) @Max(999) int cantidad) {}
    public record Cliente(@NotBlank @Size(max=500) String nombre, @NotBlank @Size(max=12) String rut,
            @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=20) String telefono,
            @NotBlank @Size(max=250) String direccion, @NotBlank @Size(max=120) String region,
            @NotBlank @Size(max=120) String comuna) {}
    public record CrearPedido(@NotNull java.util.UUID idSolicitud, @NotNull @Valid Cliente cliente,
            @NotEmpty @Size(max=100) List<@Valid ItemSolicitud> items,
            @NotBlank @Pattern(regexp="despacho|retiro") String metodoEntrega,
            @NotBlank @Pattern(regexp="WebPay Plus \\(Débito\\)|Tarjeta de Crédito|Transferencia Bancaria") String metodoPago,
            @Size(max=500) String indicaciones) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true)
    public record Producto(String idProducto, String nombre, String marca, String categoria, BigDecimal precioVenta,
            int stock, String imagen, String unidadVenta, String contenidoPresentacion) {}



}
