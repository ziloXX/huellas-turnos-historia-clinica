package huellas.persistencia;
import huellas.modelo.*;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
public final class ConsultaRepository {
    private final TurnoRepository turnos;
    public ConsultaRepository() { this(new TurnoRepository()); }
    // Permite comprobar fallas de actualización sin reemplazar la conexión real.
    public ConsultaRepository(TurnoRepository turnos) { this.turnos=turnos; }
    public long guardar(Consulta consulta) throws SQLException {
        String sql="INSERT INTO consultas (turno_id,motivo,diagnostico,tratamiento,peso_kg) VALUES (?,?,?,?,?)";
        try (Connection con=Conexion.abrir()) {
            con.setAutoCommit(false);
            try {
                Turno turno=turnos.bloquear(con,consulta.getTurnoId());
                turno.atender();
                long id;
                try (PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1,consulta.getTurnoId()); ps.setString(2,consulta.getMotivo());
                    ps.setString(3,consulta.getDiagnostico()); ps.setString(4,consulta.getTratamiento());
                    ps.setBigDecimal(5,consulta.getPeso()); ps.executeUpdate(); id=Jdbc.idGenerado(ps);
                }
                turnos.actualizarEstado(con,turno);
                con.commit(); return id;
            } catch (SQLException | RuntimeException e) {
                try { con.rollback(); } catch (SQLException fallo) { e.addSuppressed(fallo); }
                throw e;
            }
        }
    }
    public List<Consulta> deMascota(long mascotaId) throws SQLException {
        String sql="SELECT c.*,v.nombre AS veterinario FROM consultas c JOIN turnos t ON t.id=c.turno_id "
            + "JOIN veterinarios v ON v.id=t.veterinario_id WHERE t.mascota_id=? ORDER BY c.atendida_en DESC,c.id DESC";
        List<Consulta> lista=new ArrayList<>();
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setLong(1,mascotaId);
            try (ResultSet rs=ps.executeQuery()) {
                while (rs.next()) lista.add(new Consulta(rs.getLong("id"),rs.getLong("turno_id"),rs.getString("motivo"),
                    rs.getString("diagnostico"),rs.getString("tratamiento"),rs.getBigDecimal("peso_kg"),
                    rs.getTimestamp("atendida_en").toLocalDateTime(),rs.getString("veterinario")));
            }
        }
        return lista;
    }
    public List<FilaConsulta> porPeriodo(LocalDate desde,LocalDate hasta)throws SQLException {
        String sql="SELECT c.id,t.mascota_id,m.nombre AS mascota,v.nombre AS veterinario,c.atendida_en,c.peso_kg FROM consultas c JOIN turnos t ON t.id=c.turno_id JOIN mascotas m ON m.id=t.mascota_id JOIN veterinarios v ON v.id=t.veterinario_id WHERE DATE(c.atendida_en)>=? AND DATE(c.atendida_en)<=? ORDER BY c.atendida_en,c.id";
        List<FilaConsulta> filas=new ArrayList<>();
        try(Connection con=Conexion.abrir();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setDate(1,Date.valueOf(desde));ps.setDate(2,Date.valueOf(hasta));
            try(ResultSet rs=ps.executeQuery()){while(rs.next())filas.add(new FilaConsulta(rs.getLong("id"),rs.getLong("mascota_id"),rs.getString("mascota"),rs.getString("veterinario"),rs.getTimestamp("atendida_en").toLocalDateTime(),rs.getBigDecimal("peso_kg")));}
        }
        return filas;
    }
}