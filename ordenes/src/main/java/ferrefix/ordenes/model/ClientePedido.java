package ferrefix.ordenes.model;

public record ClientePedido(String nombre, String rut, String email, String telefono,
                            String direccion, String region, String comuna) {}
