package ferrefix.ordenes.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ItemPedido(String idProducto, String nombre, int cantidad, BigDecimal precioUnitario) {}
