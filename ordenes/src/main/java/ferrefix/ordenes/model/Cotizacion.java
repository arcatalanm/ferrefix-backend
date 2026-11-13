package ferrefix.ordenes.model;

import java.time.Instant;

public record Cotizacion(String idCotizacion, String idUsuario, Instant fecha, String nombre,
        String rut, String email, String telefono, String tipoProyecto, String region,
        String detalle, boolean despachoObra) {}
