package ferrefix.ordenes.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Pedido(String idOrden, String idUsuario, Instant fecha, ClientePedido cliente, List<ItemPedido> items,
            BigDecimal subtotal, BigDecimal despacho, BigDecimal total, String estado, String metodoPago,
            String metodoEntrega, String indicaciones) {}
