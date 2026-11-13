package ferrefix.ordenes.controller;

import ferrefix.ordenes.dto.CotizacionRequest;
import ferrefix.ordenes.dto.PerfilUsuario;
import ferrefix.ordenes.model.Cotizacion;
import ferrefix.ordenes.service.CotizacionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ordenes/cotizaciones")
public class CotizacionController {
    private final CotizacionService cotizaciones;
    public CotizacionController(CotizacionService cotizaciones) { this.cotizaciones = cotizaciones; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@Valid @RequestBody CotizacionRequest datos, @AuthenticationPrincipal PerfilUsuario perfil) {
        return Map.of("idCotizacion", cotizaciones.crear(datos, perfil));
    }

    @GetMapping
    public List<Cotizacion> listar() { return cotizaciones.listar(); }
}
