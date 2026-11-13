package ferrefix.auth.controller;

import ferrefix.auth.dto.PaginaUsuarios;
import ferrefix.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@Tag(name = "Administración de usuarios")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminUsuarioController {
    private final UsuarioService usuarios;

    public AdminUsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public PaginaUsuarios listar(@RequestParam(defaultValue = "0") @Min(0) int pagina,
                                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamano) {
        return usuarios.listar(pagina, tamano);
    }

}
