package ferrefix.ordenes.service;

import ferrefix.ordenes.dto.CotizacionRequest;
import ferrefix.ordenes.dto.PerfilUsuario;
import ferrefix.ordenes.model.Cotizacion;
import ferrefix.ordenes.repository.CotizacionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CotizacionService {
    private final CotizacionRepository cotizaciones;
    public CotizacionService(CotizacionRepository cotizaciones) { this.cotizaciones = cotizaciones; }

    public String crear(CotizacionRequest d, PerfilUsuario perfil) {
        String id = UUID.randomUUID().toString();
        cotizaciones.guardar(new Cotizacion(id, perfil == null ? null : perfil.idUsuario(), Instant.now(),
                d.nombre().strip(), d.rut(), d.email().strip(), d.telefono(), d.tipoProyecto(), d.region(), d.detalle(), d.despachoObra()));
        return id;
    }

    @Transactional(readOnly = true)
    public List<Cotizacion> listar() { return cotizaciones.listar(); }
}
