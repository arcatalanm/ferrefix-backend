package ferrefix.arriendos.service;

import ferrefix.arriendos.model.Maquina;
import ferrefix.arriendos.model.Arriendo;
import ferrefix.arriendos.repository.MaquinaRepository;
import ferrefix.arriendos.repository.ArriendoRepository;
import ferrefix.arriendos.dto.ArriendosDtos;
import ferrefix.arriendos.dto.PerfilUsuario;

import static ferrefix.arriendos.dto.ArriendosDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ArriendosService {
    private final MaquinaRepository maquinas;
    private final ArriendoRepository arriendos;
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    public ArriendosService(MaquinaRepository maquinas, ArriendoRepository arriendos) {
        this.maquinas = maquinas;
        this.arriendos = arriendos;
    }

    @Scheduled(fixedDelayString = "${app.arriendos.actualizacion-ms:60000}")
    public void finalizarVencidos() {
        arriendos.finalizarVencidos(LocalDate.now(ZONA), Arriendo.Estado.VIGENTE, Arriendo.Estado.FINALIZADO);
    }

    @Transactional(readOnly = true)
    public List<MaquinaResponse> listarMaquinas(PerfilUsuario perfil) {
        var vigentes = arriendos.findAllByEstadoAndFechaTerminoGreaterThanEqual(Arriendo.Estado.VIGENTE, LocalDate.now(ZONA)).stream()
                .collect(Collectors.toMap(a -> a.getMaquina().getId(), Function.identity()));
        return maquinas.findAllByOrderByNombreAsc().stream()
                .map(m -> maquinaResponse(m, vigentes.get(m.getId()), perfil)).toList();
    }

    @Transactional(readOnly = true)
    public List<ArriendoResponse> listarArriendos(PerfilUsuario perfil) {
        var registros = perfil.administrador() ? arriendos.listarTodos() : arriendos.listarPorUsuario(perfil.idUsuario());
        return registros.stream().map(this::arriendoResponse).toList();
    }

    public ArriendoResponse crearArriendo(ArriendoRequest datos, PerfilUsuario perfil) {
        LocalDate hoy = LocalDate.now(ZONA);
        long dias = ChronoUnit.DAYS.between(datos.fechaInicio(), datos.fechaTermino()) + 1;
        if (datos.fechaInicio().isBefore(hoy) || dias < 1 || dias > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El arriendo debe comenzar desde hoy y durar entre 1 y 90 días, incluyendo la fecha de término.");
        }
        if (perfil.rut() == null || perfil.rut().isBlank() || perfil.apellidoPaterno() == null || perfil.apellidoPaterno().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completa tu RUT y apellido paterno en Mi cuenta antes de arrendar.");
        }
        Maquina maquina = bloquear(datos.idMaquina());
        if (maquina.getEstado() != Maquina.Estado.DISPONIBLE || tieneArriendo(maquina)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta máquina no está disponible para arriendo.");
        }
        var arriendo = new Arriendo();
        arriendo.setId(UUID.randomUUID().toString());
        arriendo.setMaquina(maquina);
        arriendo.setIdUsuario(perfil.idUsuario());
        arriendo.setNombreUsuario(perfil.nombreCompleto());
        arriendo.setRut(perfil.rut());
        arriendo.setEmail(perfil.email());
        arriendo.setTelefono(datos.telefono().strip());
        arriendo.setDireccion(datos.direccion().strip());
        arriendo.setRegion(datos.region().strip());
        arriendo.setComuna(datos.comuna().strip());
        arriendo.setFechaContrato(Instant.now());
        arriendo.setFechaInicio(datos.fechaInicio());
        arriendo.setFechaTermino(datos.fechaTermino());
        arriendo.setTarifaDia(maquina.getTarifaDia());
        return arriendoResponse(arriendos.save(arriendo));
    }

    public MaquinaResponse crearMaquina(MaquinaRequest datos) {
        var maquina = new Maquina();
        maquina.setId(UUID.randomUUID().toString());
        aplicar(maquina, datos);
        return maquinaResponse(maquinas.save(maquina), null, null);
    }

    public MaquinaResponse actualizarMaquina(String id, MaquinaRequest datos) {
        Maquina maquina = bloquear(id);
        if (tieneArriendo(maquina)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes modificar una máquina con un arriendo vigente.");
        }
        aplicar(maquina, datos);
        return maquinaResponse(maquina, null, null);
    }

    private Maquina bloquear(String id) {
        return maquinas.bloquear(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La máquina no existe."));
    }

    private boolean tieneArriendo(Maquina maquina) {
        return arriendos.existsByMaquinaIdAndEstadoAndFechaTerminoGreaterThanEqual(
                maquina.getId(), Arriendo.Estado.VIGENTE, LocalDate.now(ZONA));
    }

    private void aplicar(Maquina maquina, MaquinaRequest datos) {
        maquina.setNombre(datos.nombre().strip());
        maquina.setMarca(datos.marca().strip());
        maquina.setCategoria(datos.categoria().strip());
        maquina.setTarifaDia(datos.tarifaDia());
        maquina.setImagen(datos.imagen());
        maquina.setIncluye(datos.incluye());
        maquina.setEstado(datos.estado());
        maquina.setDescripcionDetalle(datos.descripcionDetalle().strip());
    }

    private MaquinaResponse maquinaResponse(Maquina m, Arriendo vigente, PerfilUsuario perfil) {
        boolean puedeVerUsuario = vigente != null && perfil != null
                && (perfil.administrador() || perfil.idUsuario().equals(vigente.getIdUsuario()));
        String estado = vigente != null ? "Arrendada" : m.getEstado() == Maquina.Estado.MANTENCION ? "Mantención" : "Disponible";
        return new MaquinaResponse(m.getId(), m.getNombre(), m.getMarca(), m.getCategoria(), m.getTarifaDia(),
                m.getImagen(), m.getIncluye(), m.getDescripcionDetalle(), estado,
                puedeVerUsuario ? vigente.getIdUsuario() : null, puedeVerUsuario ? vigente.getNombreUsuario() : null,
                vigente == null ? null : vigente.getFechaInicio(), vigente == null ? null : vigente.getFechaTermino());
    }

    private ArriendoResponse arriendoResponse(Arriendo a) {
        long dias = ChronoUnit.DAYS.between(a.getFechaInicio(), a.getFechaTermino()) + 1;
        boolean finalizado = a.getEstado() == Arriendo.Estado.FINALIZADO || a.getFechaTermino().isBefore(LocalDate.now(ZONA));
        var usuario = new UsuarioArriendo(a.getIdUsuario(), a.getNombreUsuario(), a.getRut(), a.getEmail(),
                a.getTelefono(), a.getDireccion(), a.getRegion(), a.getComuna());
        return new ArriendoResponse(a.getId(), a.getFechaContrato(), a.getMaquina().getId(), a.getMaquina().getNombre(),
                a.getMaquina().getMarca(), a.getTarifaDia(), dias, a.getFechaInicio(), a.getFechaTermino(),
                a.getTarifaDia().multiply(BigDecimal.valueOf(dias)), usuario, finalizado ? "Finalizado" : "Vigente");
    }
}
