package ferrefix.catalogo.controller;

import ferrefix.catalogo.dto.CatalogoDtos;
import ferrefix.catalogo.service.CatalogoService;
import ferrefix.catalogo.service.ImagenService;
import static ferrefix.catalogo.dto.CatalogoDtos.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/v1/catalogo") @Validated
public class CatalogoController {
    private final CatalogoService catalogo;
    private final ImagenService imagenes;
    public CatalogoController(CatalogoService catalogo,ImagenService imagenes) {
        this.catalogo=catalogo; this.imagenes=imagenes;
    }
    @GetMapping("/productos")
    public Pagina listar(@RequestParam(defaultValue="0") @Min(0) int pagina,
                         @RequestParam(defaultValue="100") @Min(1) @Max(100) int tamano) {
        return catalogo.listar(pagina,tamano);
    }
    @GetMapping("/productos/{id}") public Vista detalle(@PathVariable String id) { return catalogo.detalle(id); }
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
    @PostMapping("/productos") public ResponseEntity<Vista> crear(@Valid @RequestBody Escritura datos) {
        var producto=catalogo.crear(datos);
        return ResponseEntity.created(URI.create("/api/v1/catalogo/productos/"+producto.idProducto())).body(producto);
    }
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
    @PutMapping("/productos/{id}") public Vista actualizar(@PathVariable String id,@Valid @RequestBody Escritura datos) {
        return catalogo.actualizar(id,datos);
    }
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
    @DeleteMapping("/productos/{id}") public ResponseEntity<Void> desactivar(
        @PathVariable String id) {
        catalogo.desactivar(id);
        return ResponseEntity.noContent().build();
    }
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name="bearerAuth")
    @PostMapping(value="/imagenes",consumes="multipart/form-data")
    public ResponseEntity<Map<String,String>> subir(@RequestParam MultipartFile archivo) {
        String url=imagenes.guardar(archivo);
        return ResponseEntity.created(URI.create(url)).body(Map.of("url",url));
    }
}
