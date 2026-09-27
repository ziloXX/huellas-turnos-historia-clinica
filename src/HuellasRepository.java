import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class HuellasRepository {
    public long registrarCliente(String nombre, String dni, String telefono, String correo)
        throws SQLException {
        String sql = "INSERT INTO clientes (nombre, dni, telefono, correo) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setString(4, correo.isBlank() ? null : correo);
            ps.executeUpdate();
            return idGenerado(ps);
        }
    }

    public long registrarMascota(long clienteId, String nombre, String especie, String raza,
                                String sexo, LocalDate nacimiento, String alergias)
        throws SQLException {
        String sql = "INSERT INTO mascotas (cliente_id, nombre, especie, raza, sexo, "
            + "fecha_nacimiento, alergias) "
            + "SELECT id, ?, ?, ?, ?, ?, ? FROM clientes WHERE id = ? AND activo = TRUE";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, especie);
            ps.setString(3, raza.isBlank() ? null : raza);
            ps.setString(4, sexo);
            ps.setDate(5, nacimiento == null ? null : Date.valueOf(nacimiento));
            ps.setString(6, alergias.isBlank() ? null : alergias);
            ps.setLong(7, clienteId);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No encontré un cliente activo con ese número.");
            }
            return idGenerado(ps);
        }
    }

    public String listarVeterinarios() throws SQLException {
        String sql = "SELECT id, nombre, matricula FROM veterinarios WHERE activo = TRUE ORDER BY nombre";
        StringBuilder salida = new StringBuilder();
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salida.append(rs.getLong("id")).append(" | ")
                    .append(rs.getString("nombre")).append(" | matrícula ")
                    .append(rs.getString("matricula")).append(System.lineSeparator());
            }
        }
        return salida.length() == 0 ? "Todavía no hay veterinarios cargados." : salida.toString();
    }

    public long agendarTurno(long mascotaId, long veterinarioId, LocalDateTime inicio,
                            String motivo) throws SQLException {
        String sql = "INSERT INTO turnos (mascota_id, veterinario_id, inicio, motivo) "
            + "SELECT m.id, v.id, ?, ? FROM mascotas m CROSS JOIN veterinarios v "
            + "WHERE m.id = ? AND m.activo = TRUE AND v.id = ? AND v.activo = TRUE";
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setString(2, motivo);
            ps.setLong(3, mascotaId);
            ps.setLong(4, veterinarioId);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Revisá los números de mascota y veterinario.");
            }
            return idGenerado(ps);
        }
    }

    public String turnosDelDia(LocalDate fecha, long veterinarioId) throws SQLException {
        String sql = "SELECT t.id, t.inicio, t.estado, m.nombre AS mascota, "
            + "v.nombre AS veterinario, t.motivo FROM turnos t "
            + "JOIN mascotas m ON m.id = t.mascota_id "
            + "JOIN veterinarios v ON v.id = t.veterinario_id "
            + "WHERE t.inicio >= ? AND t.inicio < ? "
            + "AND (? = 0 OR t.veterinario_id = ?) ORDER BY t.inicio, v.nombre";
        StringBuilder salida = new StringBuilder();
        try (Connection con = Conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
            ps.setLong(3, veterinarioId);
            ps.setLong(4, veterinarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    salida.append("#").append(rs.getLong("id")).append(" | ")
                        .append(rs.getTimestamp("inicio").toLocalDateTime())
                        .append(" | ").append(rs.getString("mascota"))
                        .append(" | ").append(rs.getString("veterinario"))
                        .append(" | ").append(rs.getString("estado"))
                        .append(" | ").append(rs.getString("motivo"))
                        .append(System.lineSeparator());
                }
            }
        }
        return salida.length() == 0 ? "No hay turnos para esa fecha." : salida.toString();
    }

    public long registrarConsulta(long turnoId, String motivo, String diagnostico,
                                 String tratamiento, BigDecimal peso) throws SQLException {
        String buscar = "SELECT estado FROM turnos WHERE id = ? FOR UPDATE";
        String insertar = "INSERT INTO consultas (turno_id, motivo, diagnostico, tratamiento, peso_kg) "
            + "VALUES (?, ?, ?, ?, ?)";
        String actualizar = "UPDATE turnos SET estado = 'ATENDIDO' WHERE id = ?";
        try (Connection con = Conexion.abrir()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(buscar)) {
                    ps.setLong(1, turnoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"CONFIRMADO".equals(rs.getString("estado"))) {
                            throw new IllegalArgumentException("El turno no existe o ya fue cerrado.");
                        }
                    }
                }
                long consultaId;
                try (PreparedStatement ps = con.prepareStatement(insertar,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, turnoId);
                    ps.setString(2, motivo);
                    ps.setString(3, diagnostico);
                    ps.setString(4, tratamiento);
                    ps.setBigDecimal(5, peso);
                    ps.executeUpdate();
                    consultaId = idGenerado(ps);
                }
                try (PreparedStatement ps = con.prepareStatement(actualizar)) {
                    ps.setLong(1, turnoId);
                    ps.executeUpdate();
                }
                con.commit();
                return consultaId;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public String historial(long mascotaId) throws SQLException {
        String mascotaSql = "SELECT nombre, alergias FROM mascotas WHERE id = ?";
        String sql = "SELECT t.inicio, v.nombre AS veterinario, c.motivo, "
            + "c.diagnostico, c.tratamiento, c.peso_kg FROM consultas c "
            + "JOIN turnos t ON t.id = c.turno_id "
            + "JOIN veterinarios v ON v.id = t.veterinario_id "
            + "WHERE t.mascota_id = ? ORDER BY t.inicio DESC";
        String vacunasSql = "SELECT va.nombre, va.aplicada_el, va.refuerzo_el "
            + "FROM vacunas va JOIN consultas c ON c.id = va.consulta_id "
            + "JOIN turnos t ON t.id = c.turno_id "
            + "WHERE t.mascota_id = ? ORDER BY va.aplicada_el DESC";
        StringBuilder salida = new StringBuilder();
        try (Connection con = Conexion.abrir()) {
            try (PreparedStatement ps = con.prepareStatement(mascotaSql)) {
                ps.setLong(1, mascotaId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("No encontré una mascota con ese número.");
                    }
                    salida.append("Historia de ").append(rs.getString("nombre"))
                        .append(System.lineSeparator());
                    String alergias = rs.getString("alergias");
                    salida.append("Alergias: ")
                        .append(alergias == null || alergias.isBlank() ? "sin registro" : alergias)
                        .append(System.lineSeparator());
                }
            }
            salida.append("Consultas:").append(System.lineSeparator());
            boolean tieneConsultas = false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, mascotaId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tieneConsultas = true;
                        salida.append(rs.getTimestamp("inicio").toLocalDateTime())
                            .append(" | ").append(rs.getString("veterinario"))
                            .append(System.lineSeparator())
                            .append("Motivo: ").append(rs.getString("motivo"))
                            .append(". Diagnóstico: ").append(rs.getString("diagnostico"))
                            .append(". Tratamiento: ").append(rs.getString("tratamiento"))
                            .append(". Peso: ").append(rs.getBigDecimal("peso_kg"))
                            .append(" kg.").append(System.lineSeparator());
                    }
                }
            }
            if (!tieneConsultas) {
                salida.append("Todavía no hay consultas registradas.")
                    .append(System.lineSeparator());
            }
            salida.append("Vacunas:").append(System.lineSeparator());
            boolean tieneVacunas = false;
            try (PreparedStatement ps = con.prepareStatement(vacunasSql)) {
                ps.setLong(1, mascotaId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tieneVacunas = true;
                        salida.append(rs.getString("nombre"))
                            .append(" | aplicada: ").append(rs.getDate("aplicada_el"))
                            .append(" | refuerzo: ")
                            .append(rs.getDate("refuerzo_el") == null ? "sin fecha" : rs.getDate("refuerzo_el"))
                            .append(System.lineSeparator());
                    }
                }
            }
            if (!tieneVacunas) {
                salida.append("Todavía no hay vacunas registradas.")
                    .append(System.lineSeparator());
            }
        }
        return salida.toString();
    }

    private static long idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new SQLException("La base no devolvió el número del registro creado.");
    }
}
