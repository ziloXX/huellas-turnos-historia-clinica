import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
public final class MascotaRepository {
    public long guardar(Mascota mascota) throws SQLException {
        String sql="INSERT INTO mascotas (cliente_id,nombre,especie,raza,sexo,fecha_nacimiento,alergias) "
            + "SELECT id,?,?,?,?,?,? FROM clientes WHERE id=? AND activo=TRUE";
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,mascota.getNombre()); ps.setString(2,mascota.getEspecie().name());
            ps.setString(3,mascota.getRaza()); ps.setString(4,mascota.getSexo().name());
            ps.setDate(5,mascota.getNacimiento()==null?null:Date.valueOf(mascota.getNacimiento()));
            ps.setString(6,mascota.getAlergias()); ps.setLong(7,mascota.getClienteId());
            if (ps.executeUpdate()==0) throw new IllegalArgumentException("No encontré un cliente activo con ese número.");
            return Jdbc.idGenerado(ps);
        }
    }
    public List<Mascota> listar() throws SQLException {
        List<Mascota> lista=new ArrayList<>();
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement("SELECT * FROM mascotas ORDER BY id"); ResultSet rs=ps.executeQuery()) {
            while (rs.next()) lista.add(leer(rs));
        }
        return lista;
    }
    public Mascota buscar(long id) throws SQLException {
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement("SELECT * FROM mascotas WHERE id=?")) {
            ps.setLong(1,id);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("No encontré una mascota con ese número.");
                return leer(rs);
            }
        }
    }
    private Mascota leer(ResultSet rs) throws SQLException {
        return new Mascota(rs.getLong("id"),rs.getLong("cliente_id"),rs.getString("nombre"),
            Especie.valueOf(rs.getString("especie")),rs.getString("raza"),Sexo.valueOf(rs.getString("sexo")),
            Jdbc.fecha(rs,"fecha_nacimiento"),rs.getString("alergias"),rs.getBoolean("activo"));
    }
}
