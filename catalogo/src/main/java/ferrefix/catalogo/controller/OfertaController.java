package ferrefix.catalogo.controller;

import ferrefix.catalogo.model.Oferta;
import ferrefix.catalogo.dto.OfertaRequest;
import ferrefix.catalogo.service.OfertaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/catalogo/ofertas") @Validated
public class OfertaController {
    private final OfertaService ofertas;
    public OfertaController(OfertaService ofertas) { this.ofertas = ofertas; }


    @GetMapping public List<Oferta> listar() { return ofertas.listar(); }

    @PutMapping("/{indice}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
    public Oferta guardar(@PathVariable @Min(0) @Max(3) int indice, @Valid @RequestBody OfertaRequest datos) {
        return ofertas.guardar(indice, datos);
    }

    @DeleteMapping("/{indice}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
    public void eliminar(@PathVariable @Min(0) @Max(3) int indice) { ofertas.eliminar(indice); }
}
