package ferrefix.ordenes.repository;

import ferrefix.ordenes.model.Cotizacion;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CotizacionRepository {
    private final JdbcTemplate jdbc;
    public CotizacionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void guardar(Cotizacion c) {
        jdbc.update("INSERT INTO cotizaciones (id,id_usuario,fecha,nombre,rut,email,telefono,tipo_proyecto,region,detalle,despacho_obra) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                c.idCotizacion(), c.idUsuario(), Timestamp.from(c.fecha()), c.nombre(), c.rut(), c.email(),
                c.telefono(), c.tipoProyecto(), c.region(), c.detalle(), c.despachoObra());
    }

    public List<Cotizacion> listar() {
        return jdbc.query("SELECT * FROM cotizaciones ORDER BY fecha DESC", (rs, row) -> new Cotizacion(
                rs.getString("id"), rs.getString("id_usuario"), rs.getTimestamp("fecha").toInstant(),
                rs.getString("nombre"), rs.getString("rut"), rs.getString("email"), rs.getString("telefono"),
                rs.getString("tipo_proyecto"), rs.getString("region"), rs.getString("detalle"), rs.getBoolean("despacho_obra")));
    }
}
