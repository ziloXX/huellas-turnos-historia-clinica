import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
final class Jdbc {
    private Jdbc() {}
    static long idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs=ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getLong(1);
        }
        throw new SQLException("La base no devolvió el número del registro creado.");
    }
    static LocalDate fecha(ResultSet rs,String campo) throws SQLException {
        Date fecha=rs.getDate(campo);
        return fecha==null ? null : fecha.toLocalDate();
    }
}
