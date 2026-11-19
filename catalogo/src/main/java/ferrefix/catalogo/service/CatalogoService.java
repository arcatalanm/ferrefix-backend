package ferrefix.catalogo.service;

import ferrefix.catalogo.model.Producto;
import ferrefix.catalogo.repository.ProductoRepository;
import ferrefix.catalogo.dto.CatalogoDtos;
import static ferrefix.catalogo.dto.CatalogoDtos.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogoService {
    private final ProductoRepository productos;
    private final ImagenService imagenes;
    public CatalogoService(ProductoRepository productos, ImagenService imagenes) {
        this.productos=productos; this.imagenes=imagenes;
    }
    @Transactional(readOnly=true)
    public Pagina listar(int pagina,int tamano) {
        var result=productos.findByActivoTrue(PageRequest.of(pagina,tamano,Sort.by("fechaCreacion","idProducto")));
        return new Pagina(result.stream().map(Vista::from).toList(),pagina,result.getTotalPages(),result.getTotalElements());
    }
    @Transactional(readOnly=true)
    public Vista detalle(String id) { return Vista.from(buscarActivo(id)); }
    private Producto buscarActivo(String id) {
        return productos.findById(id).filter(p -> p.isActivo())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Producto no disponible."));
    }
    @Transactional
    public Vista crear(Escritura datos) {
        var producto=new Producto();
        producto.setIdProducto(UUID.randomUUID().toString());
        asignar(producto,datos);
        return Vista.from(productos.saveAndFlush(producto));
    }
    @Transactional
    public Vista actualizar(String id,Escritura datos) {
        var producto=buscarActivo(id);
        asignar(producto,datos);
        productos.flush();
        return Vista.from(producto);
    }
    @Transactional
    public void desactivar(String id) {
        var producto=buscarActivo(id);
        producto.setActivo(false);
    }
    private void asignar(Producto p,Escritura d) {
        imagenes.validarReferencia(d.imagen());
        p.setNombre(d.nombre().strip()); p.setMarca(d.marca().strip()); p.setCategoria(d.categoria().strip());
        p.setPrecioVenta(d.precioVenta()); p.setStock(d.stock());
        p.setUnidadVenta(d.unidadVenta()); p.setContenidoPresentacion(d.contenidoPresentacion().strip());
        p.setDescripcionCorta(d.descripcionCorta().strip()); p.setDescripcionDetalle(d.descripcionDetalle().strip());
        p.setImagen(d.imagen());
    }
}
