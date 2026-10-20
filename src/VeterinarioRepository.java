import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
public final class VeterinarioRepository {
    public List<Veterinario> listarActivos() throws SQLException {
        List<Veterinario> lista=new ArrayList<>();
        try (Connection con=Conexion.abrir(); PreparedStatement ps=con.prepareStatement("SELECT id,nombre,matricula FROM veterinarios WHERE activo=TRUE ORDER BY nombre"); ResultSet rs=ps.executeQuery()) {
            while (rs.next()) lista.add(new Veterinario(rs.getLong("id"),rs.getString("nombre"),rs.getString("matricula")));
        }
        return lista;
    }
}
