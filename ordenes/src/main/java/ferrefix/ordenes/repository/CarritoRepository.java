package ferrefix.ordenes.repository;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CarritoRepository {
    private final JdbcTemplate jdbc;

    public CarritoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void bloquear(String propietario) {
        jdbc.update("INSERT IGNORE INTO carritos (propietario) VALUES (?)", propietario);
        jdbc.queryForObject("SELECT propietario FROM carritos WHERE propietario = ? FOR UPDATE", String.class, propietario);
    }

    public Map<String, Integer> cantidades(String propietario) {
        Map<String, Integer> items = new TreeMap<>();
        jdbc.query("SELECT id_producto, cantidad FROM carrito_items WHERE propietario = ? ORDER BY id_producto",
                rs -> { items.put(rs.getString(1), rs.getInt(2)); }, propietario);
        return items;
    }

    public void guardarCantidad(String propietario, String id, int cantidad) {
        jdbc.update("INSERT INTO carrito_items (propietario,id_producto,cantidad) VALUES (?,?,?) ON DUPLICATE KEY UPDATE cantidad = ?",
                propietario, id, cantidad, cantidad);
    }

    public void eliminar(String propietario, String id) {
        jdbc.update("DELETE FROM carrito_items WHERE propietario = ? AND id_producto = ?", propietario, id);
    }

    public void vaciar(String propietario) {
        jdbc.update("DELETE FROM carrito_items WHERE propietario = ?", propietario);
    }
}
