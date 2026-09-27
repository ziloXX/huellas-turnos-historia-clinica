import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class HuellasService {
    private final HuellasRepository datos = new HuellasRepository();

    public long registrarCliente(String nombre, String dni, String telefono, String correo)
        throws SQLException {
        obligatorio(nombre, "nombre");
        obligatorio(dni, "DNI");
        obligatorio(telefono, "teléfono");
        return datos.registrarCliente(nombre.trim(), dni.trim(), telefono.trim(), correo.trim());
    }

    public long registrarMascota(long clienteId, String nombre, String especie, String raza,
                                String sexo, LocalDate nacimiento, String alergias)
        throws SQLException {
        positivo(clienteId, "cliente");
        obligatorio(nombre, "nombre de la mascota");
        if (!"PERRO".equals(especie) && !"GATO".equals(especie)) {
            throw new IllegalArgumentException("La especie debe ser PERRO o GATO.");
        }
        if (!"MACHO".equals(sexo) && !"HEMBRA".equals(sexo)) {
            throw new IllegalArgumentException("El sexo debe ser MACHO o HEMBRA.");
        }
        if (nacimiento != null && nacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        return datos.registrarMascota(clienteId, nombre.trim(), especie, raza.trim(), sexo,
            nacimiento, alergias.trim());
    }

    public String listarVeterinarios() throws SQLException {
        return datos.listarVeterinarios();
    }

    public long agendarTurno(long mascotaId, long veterinarioId, LocalDateTime inicio,
                            String motivo) throws SQLException {
        positivo(mascotaId, "mascota");
        positivo(veterinarioId, "veterinario");
        obligatorio(motivo, "motivo");
        if (inicio.isBefore(LocalDateTime.now()) || inicio.getMinute() % 30 != 0
                || inicio.getSecond() != 0) {
            throw new IllegalArgumentException(
                "Elegí una fecha futura y un horario en punto o y media. Cada turno dura 30 minutos."
            );
        }
        return datos.agendarTurno(mascotaId, veterinarioId, inicio, motivo.trim());
    }

    public String turnosDelDia(LocalDate fecha, long veterinarioId) throws SQLException {
        if (veterinarioId < 0) {
            throw new IllegalArgumentException("El número de veterinario no puede ser negativo.");
        }
        return datos.turnosDelDia(fecha, veterinarioId);
    }

    public long registrarConsulta(long turnoId, String motivo, String diagnostico,
                                 String tratamiento, BigDecimal peso) throws SQLException {
        positivo(turnoId, "turno");
        obligatorio(motivo, "motivo");
        obligatorio(diagnostico, "diagnóstico");
        obligatorio(tratamiento, "tratamiento");
        if (peso == null || peso.signum() <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que cero.");
        }
        return datos.registrarConsulta(turnoId, motivo.trim(), diagnostico.trim(),
            tratamiento.trim(), peso);
    }

    public String historial(long mascotaId) throws SQLException {
        positivo(mascotaId, "mascota");
        return datos.historial(mascotaId);
    }

    private static void obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta completar " + campo + ".");
        }
    }

    private static void positivo(long valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El número de " + campo + " debe ser positivo.");
        }
    }
}
