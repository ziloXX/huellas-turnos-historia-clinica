import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
public final class VacunaRepository {
    public long guardar(Vacuna vacuna) throws SQLException {
        String sql="INSERT INTO vacunas (consulta_id,nombre,aplicada_el,refuerzo_el) SELECT id,?,?,? FROM consultas WHERE id=?";
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,vacuna.getNombre()); ps.setDate(2,Date.valueOf(vacuna.getAplicada()));
            ps.setDate(3,vacuna.getRefuerzo()==null?null:Date.valueOf(vacuna.getRefuerzo()));
            ps.setLong(4,vacuna.getConsultaId());
            if (ps.executeUpdate()==0) throw new IllegalArgumentException("No encontré esa consulta. Primero registrá la atención.");
            return Jdbc.idGenerado(ps);
        }
    }
    public List<Vacuna> deMascota(long mascotaId) throws SQLException {
        String sql="SELECT va.* FROM vacunas va JOIN consultas c ON c.id=va.consulta_id "
            + "JOIN turnos t ON t.id=c.turno_id WHERE t.mascota_id=? ORDER BY va.aplicada_el DESC,va.id DESC";
        List<Vacuna> lista=new ArrayList<>();
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setLong(1,mascotaId);
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()) lista.add(new Vacuna(rs.getLong("id"),rs.getLong("consulta_id"),rs.getString("nombre"),Jdbc.fecha(rs,"aplicada_el"),Jdbc.fecha(rs,"refuerzo_el")));
            }
        }
        return lista;
    }
}
