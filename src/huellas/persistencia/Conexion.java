package huellas.persistencia;
import huellas.modelo.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexion {
    private Conexion() {
    }

    public static Connection abrir() throws SQLException {
        String url = System.getenv().getOrDefault(
            "HUELLAS_DB_URL", "jdbc:mysql://localhost:3306/huellas?serverTimezone=America/Argentina/Cordoba"
        );
        String usuario = System.getenv().getOrDefault("HUELLAS_DB_USER", "huellas_app");
        String clave = System.getenv("HUELLAS_DB_PASSWORD");
        if (clave == null || clave.isBlank()) {
            throw new SQLException("Falta configurar HUELLAS_DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, usuario, clave);
    }
}
