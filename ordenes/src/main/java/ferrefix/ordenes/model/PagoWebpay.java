package ferrefix.ordenes.model;

import java.time.Instant;

public record PagoWebpay(String idPedido, String buyOrder, String token, String sessionId, long monto,
        String estado, Integer codigoRespuesta, String codigoAutorizacion, String tipoPago, Integer cuotas,
        String tarjetaUltimos4, Instant fechaTransaccion, Instant fechaCreacion, Instant fechaActualizacion) {}