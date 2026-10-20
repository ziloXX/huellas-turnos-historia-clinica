import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Scanner;

public final class HuellasApp {
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
            System.out.println("8. Buscar mascotas por nombre");
            System.out.println("9. Registrar vacuna");
            System.out.println("0. Salir");
            if (!teclado.hasNextLine()) { System.out.println("Fin de la entrada."); return; }
            String opcion = pedir("Opción");
            if ("0".equals(opcion)) {
                System.out.println("Hasta luego.");
                return;
            }
            try {
                ejecutar(opcion);
            } catch (DateTimeException e) {
                System.out.println("Revisá la fecha: usá el formato indicado y una fecha que exista.");
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número válido. Para el peso, usá punto decimal.");
            } catch (NoSuchElementException e) {
                System.out.println("Fin de la entrada. La operación quedó sin completar.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Revisá los datos: " + e.getMessage());
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    System.out.println("Ese dato ya existe o el veterinario tiene ocupado el horario.");
                } else {
                    System.out.println("No se pudo completar la operación en MySQL. Revisá la conexión y los permisos. Código: "
                        + e.getErrorCode());
                }
            }
        }
    }

    private void ejecutar(String opcion) throws SQLException {
        switch (opcion) {
            case "1" -> registrarCliente();
            case "2" -> registrarMascota();
            case "3" -> verVeterinarios();
            case "4" -> agendarTurno();
            case "5" -> verTurnos();
            case "6" -> registrarConsulta();
            case "7" -> verHistorial();
            case "8" -> buscarMascotas();
            case "9" -> registrarVacuna();
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
        verVeterinarios();
        long mascota = numero("Número de mascota");
        long veterinario = numero("Número de veterinario");
        LocalDateTime inicio = Fechas.fechaHora(pedir("Fecha y hora (DD/MM/AAAA HH:MM)"));
        long id = servicio.agendarTurno(mascota, veterinario, inicio, pedir("Motivo"));
        System.out.println("Turno confirmado con el número " + id + ".");
    }

    private void verTurnos() throws SQLException {
        LocalDate fecha = LocalDate.parse(pedir("Fecha (AAAA-MM-DD)"));
        long veterinario = numero("Número de veterinario (0 para todos)");
        List<Turno> agenda = servicio.turnosDelDia(fecha, veterinario);
        if (agenda.isEmpty()) System.out.println("No hay turnos para esa fecha.");
        for (Turno turno : agenda) {
            System.out.println("#" + turno.getId() + " | " + turno.getInicio() + " | "
                + turno.getMascota() + " | " + turno.getVeterinario() + " | " + turno.getEstado()
                + " | " + turno.getMotivo());
        }
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
        HistoriaClinica historia = servicio.historial(numero("Número de mascota"));
        Mascota mascota = historia.getMascota();
        System.out.println("Historia de " + mascota.getNombre() + (mascota.isActivo() ? "" : " (inactiva)"));
        System.out.println("Alergias: " + (mascota.getAlergias().isBlank() ? "sin registro" : mascota.getAlergias()));
        System.out.println("Consultas:");
        if (historia.getConsultas().isEmpty()) System.out.println("Todavía no hay consultas registradas.");
        for (Consulta consulta : historia.getConsultas()) {
            System.out.println("Consulta #" + consulta.getId() + " | " + consulta.getAtendida()
                + " | " + consulta.getVeterinario());
            System.out.println("Motivo: " + consulta.getMotivo() + ". Diagnóstico: " + consulta.getDiagnostico()
                + ". Tratamiento: " + consulta.getTratamiento() + ". Peso: " + consulta.getPeso() + " kg.");
        }
        System.out.println("Vacunas:");
        if (historia.getVacunas().isEmpty()) System.out.println("Todavía no hay vacunas registradas.");
        for (Vacuna vacuna : historia.getVacunas()) {
            System.out.println(vacuna.getNombre() + " | aplicada: " + vacuna.getAplicada()
                + " | refuerzo: " + (vacuna.getRefuerzo() == null ? "sin fecha" : vacuna.getRefuerzo()));
        }
    }

    private void verVeterinarios() throws SQLException {
        List<Veterinario> lista = servicio.listarVeterinarios();
        if (lista.isEmpty()) System.out.println("Todavía no hay veterinarios cargados.");
        for (Persona persona : lista) mostrarPersona(persona);
    }

    private void mostrarPersona(Persona persona) {
        System.out.println(persona.descripcion());
    }

    private void buscarMascotas() throws SQLException {
        List<Mascota> lista = servicio.buscarMascotas(pedir("Nombre o parte del nombre"));
        if (lista.isEmpty()) System.out.println("No encontré mascotas con ese nombre.");
        for (Mascota mascota : lista) {
            System.out.println("#" + mascota.getId() + " | " + mascota.getNombre() + " | "
                + mascota.getEspecie() + " | " + (mascota.isActivo() ? "activa" : "inactiva"));
            mostrarPersona(servicio.cliente(mascota.getClienteId()));
        }
    }

    private void registrarVacuna() throws SQLException {
        long consulta = numero("Número de consulta (figura en la historia clínica)");
        String nombre = pedir("Nombre de la vacuna");
        LocalDate aplicada = LocalDate.parse(pedir("Aplicación (AAAA-MM-DD)"));
        String fecha = pedir("Refuerzo (AAAA-MM-DD, opcional)");
        LocalDate refuerzo = fecha.isBlank() ? null : LocalDate.parse(fecha);
        long id = servicio.registrarVacuna(consulta, nombre, aplicada, refuerzo);
        System.out.println("Vacuna registrada con el número " + id + ".");
    }

    private long numero(String etiqueta) {
        return Long.parseLong(pedir(etiqueta));
    }

    private String pedir(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return teclado.nextLine().trim();
    }
}
