import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public final class HuellasApp {
    private static final DateTimeFormatter FECHA_HORA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Scanner teclado = new Scanner(System.in);
    private final HuellasService servicio = new HuellasService();

    public static void main(String[] args) {
        new HuellasApp().iniciar();
    }

    private void iniciar() {
        System.out.println("Clínica Veterinaria Huellas");
        while (true) {
            System.out.println("\n1. Registrar cliente");
            System.out.println("2. Registrar mascota");
            System.out.println("3. Ver veterinarios");
            System.out.println("4. Agendar turno");
            System.out.println("5. Ver turnos de una fecha");
            System.out.println("6. Registrar consulta");
            System.out.println("7. Ver historia clínica");
            System.out.println("0. Salir");
            String opcion = pedir("Opción");
            if ("0".equals(opcion)) {
                System.out.println("Hasta luego.");
                return;
            }
            try {
                ejecutar(opcion);
            } catch (IllegalArgumentException e) {
                System.out.println("Revisá los datos: " + e.getMessage());
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    System.out.println("Ese dato ya existe o el veterinario tiene ocupado el horario.");
                } else {
                    System.out.println("No se pudo guardar o consultar la información: "
                        + e.getMessage());
                }
            }
        }
    }

    private void ejecutar(String opcion) throws SQLException {
        switch (opcion) {
            case "1" -> registrarCliente();
            case "2" -> registrarMascota();
            case "3" -> System.out.println(servicio.listarVeterinarios());
            case "4" -> agendarTurno();
            case "5" -> verTurnos();
            case "6" -> registrarConsulta();
            case "7" -> verHistorial();
            default -> System.out.println("Elegí una opción del menú.");
        }
    }

    private void registrarCliente() throws SQLException {
        long id = servicio.registrarCliente(pedir("Nombre y apellido"), pedir("DNI"),
            pedir("Teléfono"), pedir("Correo electrónico (opcional)"));
        System.out.println("Cliente registrado con el número " + id + ".");
    }

    private void registrarMascota() throws SQLException {
        long cliente = numero("Número de cliente");
        String nombre = pedir("Nombre de la mascota");
        String especie = pedir("Especie (PERRO/GATO)").toUpperCase();
        String raza = pedir("Raza (opcional)");
        String sexo = pedir("Sexo (MACHO/HEMBRA)").toUpperCase();
        String fecha = pedir("Nacimiento (AAAA-MM-DD, opcional)");
        LocalDate nacimiento = fecha.isBlank() ? null : LocalDate.parse(fecha);
        long id = servicio.registrarMascota(cliente, nombre, especie, raza, sexo,
            nacimiento, pedir("Alergias (opcional)"));
        System.out.println("Mascota registrada con el número " + id + ".");
    }

    private void agendarTurno() throws SQLException {
        System.out.println(servicio.listarVeterinarios());
        long mascota = numero("Número de mascota");
        long veterinario = numero("Número de veterinario");
        LocalDateTime inicio = LocalDateTime.parse(pedir("Fecha y hora (DD/MM/AAAA HH:MM)"),
            FECHA_HORA);
        long id = servicio.agendarTurno(mascota, veterinario, inicio, pedir("Motivo"));
        System.out.println("Turno confirmado con el número " + id + ".");
    }

    private void verTurnos() throws SQLException {
        LocalDate fecha = LocalDate.parse(pedir("Fecha (AAAA-MM-DD)"));
        long veterinario = numero("Número de veterinario (0 para todos)");
        System.out.println(servicio.turnosDelDia(fecha, veterinario));
    }

    private void registrarConsulta() throws SQLException {
        long turno = numero("Número de turno confirmado");
        String motivo = pedir("Motivo de consulta");
        String diagnostico = pedir("Diagnóstico");
        String tratamiento = pedir("Tratamiento indicado");
        BigDecimal peso = new BigDecimal(pedir("Peso en kg (usar punto decimal)"));
        long id = servicio.registrarConsulta(turno, motivo, diagnostico, tratamiento, peso);
        System.out.println("Consulta registrada con el número " + id + ".");
    }

    private void verHistorial() throws SQLException {
        System.out.println(servicio.historial(numero("Número de mascota")));
    }

    private long numero(String etiqueta) {
        return Long.parseLong(pedir(etiqueta));
    }

    private String pedir(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return teclado.nextLine().trim();
    }
}
