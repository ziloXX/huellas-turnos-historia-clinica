package huellas.persistencia;
import huellas.modelo.*;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
public final class ClienteRepository {
    public long guardar(Cliente cliente) throws SQLException {
        String sql="INSERT INTO clientes (nombre,dni,telefono,correo) VALUES (?,?,?,?)";
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,cliente.getNombre()); ps.setString(2,cliente.getDni());
            ps.setString(3,cliente.getTelefono()); ps.setString(4,cliente.getCorreo().isBlank()?null:cliente.getCorreo());
            ps.executeUpdate(); return Jdbc.idGenerado(ps);
        }
    }
    public Cliente buscar(long id) throws SQLException {
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement("SELECT * FROM clientes WHERE id=?")) {
            ps.setLong(1,id);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("No encontré ese cliente.");
                return new Cliente(rs.getLong("id"),rs.getString("nombre"),rs.getString("dni"),rs.getString("telefono"),rs.getString("correo"));
            }
        }
    }
    public List<Cliente> buscarPorNombreODni(String criterio)throws SQLException {
        List<Cliente> lista=new ArrayList<>();
        String sql="SELECT * FROM clientes WHERE LOCATE(?,nombre)>0 OR dni=? ORDER BY nombre,id";
        try(Connection con=Conexion.abrir();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,criterio);ps.setString(2,criterio);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())lista.add(new Cliente(rs.getLong("id"),rs.getString("nombre"),rs.getString("dni"),rs.getString("telefono"),rs.getString("correo")));}
        }
        return lista;
    }
}