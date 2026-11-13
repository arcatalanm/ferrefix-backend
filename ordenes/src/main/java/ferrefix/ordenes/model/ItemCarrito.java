package ferrefix.ordenes.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ItemCarrito(String idProducto, String nombre, String marca, String categoria, BigDecimal precioVenta,
            int stock, String imagen, String unidadVenta, String contenidoPresentacion, int cantidad, boolean disponible) {}
