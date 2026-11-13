package ferrefix.auth.dto;

import ferrefix.auth.dto.AuthDtos.UsuarioResponse;
import java.util.List;

public record PaginaUsuarios(List<UsuarioResponse> contenido, int pagina, int tamano, long total) {}
