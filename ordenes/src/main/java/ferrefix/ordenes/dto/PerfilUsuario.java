package ferrefix.ordenes.dto;

import java.util.stream.Stream;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record PerfilUsuario(String idUsuario, String nombre, String segNombre, String apellidoPaterno,
        String apellidoMaterno, String email, String rol, String rut) {
    public boolean administrador() { return "admin".equals(rol); }
    public String nombreCompleto() {
        return Stream.of(nombre, segNombre, apellidoPaterno, apellidoMaterno)
                .filter(s -> s != null && !s.isBlank()).map(String::strip)
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
