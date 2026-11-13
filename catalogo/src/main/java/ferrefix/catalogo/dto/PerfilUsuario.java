package ferrefix.catalogo.dto;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record PerfilUsuario(String idUsuario, String rol) {}
