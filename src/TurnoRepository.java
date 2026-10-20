import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
public class TurnoRepository {
    public long guardar(Turno turno) throws SQLException {
        String sql="INSERT INTO turnos (mascota_id,veterinario_id,inicio,motivo) "
            + "SELECT m.id,v.id,?,? FROM mascotas m CROSS JOIN veterinarios v "
            + "WHERE m.id=? AND m.activo=TRUE AND v.id=? AND v.activo=TRUE";
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1,Timestamp.valueOf(turno.getInicio())); ps.setString(2,turno.getMotivo());
            ps.setLong(3,turno.getMascotaId()); ps.setLong(4,turno.getVeterinarioId());
            if (ps.executeUpdate()==0) throw new IllegalArgumentException("Revisá los números de mascota y veterinario activos.");
            return Jdbc.idGenerado(ps);
        }
    }
    public List<Turno> delDia(LocalDate fecha,long veterinarioId) throws SQLException {
        String sql="SELECT t.*,m.nombre AS mascota,v.nombre AS veterinario FROM turnos t "
            + "JOIN mascotas m ON m.id=t.mascota_id JOIN veterinarios v ON v.id=t.veterinario_id "
            + "WHERE t.inicio>=? AND t.inicio<? AND (?=0 OR t.veterinario_id=?) ORDER BY t.id";
        List<Turno> lista=new ArrayList<>();
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setTimestamp(1,Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setTimestamp(2,Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
            ps.setLong(3,veterinarioId); ps.setLong(4,veterinarioId);
            try (ResultSet rs=ps.executeQuery()) { while (rs.next()) lista.add(leer(rs,rs.getString("mascota"),rs.getString("veterinario"))); }
        }
        return lista;
    }
    // Comparte la conexión de la consulta: el bloqueo dura hasta commit o rollback.
    public Turno bloquear(Connection con,long id) throws SQLException {
        try (PreparedStatement ps=con.prepareStatement("SELECT * FROM turnos WHERE id=? FOR UPDATE")) {
            ps.setLong(1,id);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("No encontré ese turno.");
                return leer(rs,"","");
            }
        }
    }
    public void actualizarEstado(Connection con,Turno turno) throws SQLException {
        try (PreparedStatement ps=con.prepareStatement("UPDATE turnos SET estado=? WHERE id=?")) {
            ps.setString(1,turno.getEstado().name()); ps.setLong(2,turno.getId());
            if (ps.executeUpdate()!=1) throw new SQLException("No se pudo actualizar el turno.");
        }
    }
    private Turno leer(ResultSet rs,String mascota,String veterinario) throws SQLException {
        return new Turno(rs.getLong("id"),rs.getLong("mascota_id"),rs.getLong("veterinario_id"),
            rs.getTimestamp("inicio").toLocalDateTime(),rs.getString("motivo"),EstadoTurno.valueOf(rs.getString("estado")),mascota,veterinario);
    }
}
