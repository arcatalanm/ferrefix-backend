package ferrefix.catalogo.dto;

import ferrefix.catalogo.model.Producto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public final class CatalogoDtos {
    private CatalogoDtos() {}
    public record Escritura(
        @NotBlank @Size(max=160) String nombre,
        @NotBlank @Size(max=100) String marca,
        @NotBlank @Size(max=80) String categoria,
        @NotNull @DecimalMin("0") @Digits(integer=12,fraction=0) BigDecimal precioVenta,
        @NotNull @Min(0) Integer stock,
        @NotBlank @Pattern(regexp="unidad|caja|bolsa|kilo|pack|saco|rollo|metro|litro") String unidadVenta,
        @NotBlank @Size(max=120) String contenidoPresentacion,
        @NotNull @Size(max=300) String descripcionCorta,
        @NotNull @Size(max=4000) String descripcionLarga,
        @NotBlank @Size(max=255) String imagen,
        @NotNull @Size(max=30) Map<@NotBlank @Size(max=120) String,@NotNull @Size(max=500) String> especificaciones) {}
        
    public record Vista(String idProducto, String nombre, String marca, String categoria,
        BigDecimal precioVenta, int stock, String unidadVenta, String contenidoPresentacion,
        String descripcionCorta, String descripcionLarga, String imagen,
        Map<String,String> especificaciones, boolean activo, Instant fechaCreacion) {
        public static Vista from(Producto p) {
            return new Vista(p.getIdProducto(),p.getNombre(),p.getMarca(),p.getCategoria(),p.getPrecioVenta(),p.getStock(),
                p.getUnidadVenta(),p.getContenidoPresentacion(),
                p.getDescripcionCorta(),p.getDescripcionLarga(),p.getImagen(),Map.copyOf(p.getEspecificaciones()),p.isActivo(),p.getFechaCreacion());
        }
    }
    public record Pagina(List<Vista> contenido,int pagina,int totalPaginas,long total) {}
}
