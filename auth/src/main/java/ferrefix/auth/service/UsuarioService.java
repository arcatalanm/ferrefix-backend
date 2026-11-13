package ferrefix.auth.service;

import ferrefix.auth.dto.AuthDtos.UsuarioResponse;
import ferrefix.auth.dto.PaginaUsuarios;
import ferrefix.auth.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    public UsuarioService(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Transactional(readOnly = true)
    public PaginaUsuarios listar(int pagina, int tamano) {
        var resultado = usuarios.findAll(PageRequest.of(pagina, tamano, Sort.by("email")));
        return new PaginaUsuarios(resultado.getContent().stream().map(UsuarioResponse::from).toList(),
                pagina, tamano, resultado.getTotalElements());
    }
}
