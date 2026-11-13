package ferrefix.catalogo.service;
import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImagenService {
    private final Path directorio;
    public ImagenService(@Value("${app.media.directory}") String ruta) throws IOException {
        directorio=Path.of(ruta).toAbsolutePath().normalize().resolve("productos");
        Files.createDirectories(directorio);
    }
    public void validarReferencia(String url) {
        if(url==null || (!url.equals("/media/productos/sin-imagen.svg") && !url.matches("/media/productos/[a-zA-Z0-9_-]+\\.(webp|png|jpg|jpeg)"))
            || !Files.isRegularFile(directorio.resolve(url.substring("/media/productos/".length()))))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona una imagen del catálogo o sube una nueva.");
    }
    public String guardar(MultipartFile archivo) {
        if(archivo.isEmpty() || archivo.getSize()>6*1024*1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La imagen debe pesar entre 1 byte y 6 MB.");

        try(var input=new MemoryCacheImageInputStream(archivo.getInputStream())) {
            var readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw invalida();
            var reader=readers.next();
            try {
                if(!Set.of("png","jpeg","jpg","webp").contains(reader.getFormatName().toLowerCase(Locale.ROOT))) throw invalida();
                reader.setInput(input,true,true);
                int w=reader.getWidth(0), h=reader.getHeight(0);
                if(w<=0 || h<=0 || (long)w*h>16_000_000) throw invalida();
                var imagen=reader.read(0);
                String nombre=UUID.randomUUID()+".png";
                Path temporal=Files.createTempFile(directorio,"upload-",".tmp");
                try {
                    if(!ImageIO.write(imagen,"png",temporal.toFile())) throw invalida();
                    Files.move(temporal,directorio.resolve(nombre),StandardCopyOption.ATOMIC_MOVE);
                } finally { Files.deleteIfExists(temporal); }
                return "/media/productos/"+nombre;
            } finally { reader.dispose(); }
        } catch(IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No se pudo procesar la imagen.");
        }
    }
    private ResponseStatusException invalida() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Usa una imagen JPG, PNG o WebP de hasta 16 megapíxeles.");
    }
}
