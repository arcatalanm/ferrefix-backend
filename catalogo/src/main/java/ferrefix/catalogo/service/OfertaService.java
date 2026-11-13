package ferrefix.catalogo.service;

import ferrefix.catalogo.dto.OfertaRequest;
import ferrefix.catalogo.model.Oferta;
import ferrefix.catalogo.repository.OfertaRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OfertaService {
    private final OfertaRepository ofertas;
    private final ImagenService imagenes;
    public OfertaService(OfertaRepository ofertas, ImagenService imagenes) {
        this.ofertas = ofertas;
        this.imagenes = imagenes;
    }

    @Transactional(readOnly = true)
    public List<Oferta> listar() { return ofertas.findAll(); }

    public Oferta guardar(int indice, OfertaRequest datos) {
        imagenes.validarReferencia(datos.imagen());
        var oferta = new Oferta();
        oferta.setIndice(indice);
        oferta.setImagen(datos.imagen());
        oferta.setEnlace(datos.enlace());
        oferta.setAlt(datos.alt());
        oferta.setFecha(Instant.now());
        return ofertas.save(oferta);
    }

    public void eliminar(int indice) { ofertas.deleteById(indice); }
}
