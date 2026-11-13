package ferrefix.arriendos.controller;

import ferrefix.arriendos.service.ArriendosService;
import ferrefix.arriendos.dto.ArriendosDtos;
import ferrefix.arriendos.dto.PerfilUsuario;

import static ferrefix.arriendos.dto.ArriendosDtos.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/arriendos")
public class ArriendosController {
    private final ArriendosService service;

    public ArriendosController(ArriendosService service) { this.service = service; }

    @GetMapping("/maquinas")
    public List<MaquinaResponse> maquinas(@AuthenticationPrincipal PerfilUsuario perfil) { return service.listarMaquinas(perfil); }

    @PostMapping("/maquinas")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public MaquinaResponse crearMaquina(@Valid @RequestBody MaquinaRequest datos) { return service.crearMaquina(datos); }

    @PutMapping("/maquinas/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public MaquinaResponse actualizarMaquina(@PathVariable String id, @Valid @RequestBody MaquinaRequest datos) {
        return service.actualizarMaquina(id, datos);
    }

    @GetMapping("/contratos")
    @SecurityRequirement(name = "bearerAuth")
    public List<ArriendoResponse> arriendos(@AuthenticationPrincipal PerfilUsuario perfil) { return service.listarArriendos(perfil); }

    @PostMapping("/contratos")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public ArriendoResponse crearArriendo(@Valid @RequestBody ArriendoRequest datos, @AuthenticationPrincipal PerfilUsuario perfil) {
        return service.crearArriendo(datos, perfil);
    }
}
