package huellas.controlador;
import huellas.modelo.*;
import huellas.servicio.*;
import huellas.vista.VistaConsola;
import huellas.archivos.ArchivoReporte;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Scanner;

public final class HuellasController {
    private final VistaConsola vista = new VistaConsola();
    private final ArchivoReporte archivos = new ArchivoReporte();
    private final HuellasService servicio = new HuellasService();


    public void iniciar() {
        vista.println("Clínica Veterinaria Huellas");
        while (true) {
            vista.menu();
            if (!vista.hayEntrada()) { vista.println("Fin de la entrada."); return; }
            String opcion = pedir("Opción");
            if ("0".equals(opcion)) {
                vista.println("Hasta luego.");
                return;
            }
            try {
                ejecutar(opcion);
            } catch (DateTimeException e) {
                vista.println("Revisá la fecha: usá el formato indicado y una fecha que exista.");
            } catch (NumberFormatException e) {
                vista.println("Ingresá un número válido. Para el peso, usá punto decimal.");
            } catch (NoSuchElementException e) {
                vista.println("Fin de la entrada. La operación quedó sin completar.");
                return;
            } catch (IllegalArgumentException e) {
                vista.println("Revisá los datos: " + e.getMessage());
            } catch (java.io.IOException e) {
                vista.println("No se pudo guardar o leer el archivo: " + e.getMessage());
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    vista.println("Ese dato ya existe o el veterinario tiene ocupado el horario.");
                } else {
                    vista.println("No se pudo completar la operación en MySQL. Revisá la conexión y los permisos. Código: "
                        + e.getErrorCode());
                }
            }
        }
    }

    private void ejecutar(String opcion) throws SQLException, java.io.IOException {
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
            case "10" -> buscarClientes();
            case "11" -> cancelarTurno();
            case "12" -> reprogramarTurno();
            case "13" -> verReporte();
            case "14" -> guardarReporte();
            case "15" -> leerReporte();
            default -> vista.println("Elegí una opción del menú.");
        }
    }

    private void registrarCliente() throws SQLException {
        long id = servicio.registrarCliente(pedir("Nombre y apellido"), pedir("DNI"),
            pedir("Teléfono"), pedir("Correo electrónico (opcional)"));
        vista.println("Cliente registrado con el número " + id + ".");
    }

    private void registrarMascota() throws SQLException {
        long cliente = numero("ID de cliente (el asignado al registrarlo; opción 10 para buscarlo)");
        String nombre = pedir("Nombre de la mascota");
        String especie = pedir("Especie (PERRO/GATO)").toUpperCase();
        String raza = pedir("Raza (opcional)");
        String sexo = pedir("Sexo (MACHO/HEMBRA)").toUpperCase();
        String fecha = pedir("Nacimiento (AAAA-MM-DD, opcional)");
        LocalDate nacimiento = fecha.isBlank() ? null : LocalDate.parse(fecha);
        long id = servicio.registrarMascota(cliente, nombre, especie, raza, sexo,
            nacimiento, pedir("Alergias (opcional)"));
        vista.println("Mascota registrada con el número " + id + ".");
    }

    private void agendarTurno() throws SQLException {
        verVeterinarios();
        long mascota = numero("Número de mascota");
        long veterinario = numero("Número de veterinario");
        LocalDateTime inicio = Fechas.fechaHora(pedir("Fecha y hora (DD/MM/AAAA HH:MM)"));
        long id = servicio.agendarTurno(mascota, veterinario, inicio, pedir("Motivo"));
        vista.println("Turno confirmado con el número " + id + ".");
    }

    private void verTurnos() throws SQLException {
        LocalDate fecha = LocalDate.parse(pedir("Fecha (AAAA-MM-DD)"));
        long veterinario = numero("Número de veterinario (0 para todos)");
        List<Turno> agenda = servicio.turnosDelDia(fecha, veterinario);
        vista.mostrarAgenda(agenda);
    }

    private void registrarConsulta() throws SQLException {
        long turno = numero("Número de turno confirmado");
        String motivo = pedir("Motivo de consulta");
        String diagnostico = pedir("Diagnóstico");
        String tratamiento = pedir("Tratamiento indicado");
        BigDecimal peso = new BigDecimal(pedir("Peso en kg (usar punto decimal)"));
        long id = servicio.registrarConsulta(turno, motivo, diagnostico, tratamiento, peso);
        vista.println("Consulta registrada con el número " + id + ".");
    }

    private void verHistorial() throws SQLException {
        HistoriaClinica historia = servicio.historial(numero("Número de mascota"));
        vista.mostrarHistoria(historia);
    }

    private void verVeterinarios() throws SQLException {
        List<Veterinario> lista = servicio.listarVeterinarios();
        vista.mostrarPersonas(lista);
    }

    private void buscarMascotas() throws SQLException {
        List<Mascota> lista = servicio.buscarMascotas(pedir("Nombre o parte del nombre"));
        if (lista.isEmpty()) vista.println("No encontré mascotas con ese nombre.");
        for (Mascota mascota : lista) {
            vista.mostrarMascota(mascota);
            vista.mostrarPersona(servicio.cliente(mascota.getClienteId()));
        }
    }

    private void registrarVacuna() throws SQLException {
        long consulta = numero("Número de consulta (figura en la historia clínica)");
        String nombre = pedir("Nombre de la vacuna");
        LocalDate aplicada = LocalDate.parse(pedir("Aplicación (AAAA-MM-DD)"));
        String fecha = pedir("Refuerzo (AAAA-MM-DD, opcional)");
        LocalDate refuerzo = fecha.isBlank() ? null : LocalDate.parse(fecha);
        long id = servicio.registrarVacuna(consulta, nombre, aplicada, refuerzo);
        vista.println("Vacuna registrada con el número " + id + ".");
    }

    private void buscarClientes() throws SQLException {
        vista.mostrarPersonas(servicio.buscarClientes(pedir("Nombre o DNI del cliente")));
    }
    private void cancelarTurno() throws SQLException {
        servicio.cancelarTurno(numero("ID de turno confirmado"),pedir("Motivo de cancelación"));
        vista.println("Turno cancelado. El horario quedó disponible.");
    }
    private void reprogramarTurno() throws SQLException {
        long id=numero("ID de turno confirmado");
        servicio.reprogramarTurno(id,Fechas.fechaHora(pedir("Nueva fecha y hora (DD/MM/AAAA HH:MM)")));
        vista.println("Turno reprogramado con el mismo número de registro.");
    }
    private ReporteConsultas solicitarReporte() throws SQLException {
        LocalDate desde=LocalDate.parse(pedir("Desde (AAAA-MM-DD)"));
        LocalDate hasta=LocalDate.parse(pedir("Hasta (AAAA-MM-DD, inclusive)"));
        return servicio.reporte(desde,hasta);
    }
    private void verReporte() throws SQLException { vista.mostrarReporte(solicitarReporte()); }
    private void guardarReporte() throws SQLException, java.io.IOException {
        ReporteConsultas reporte=solicitarReporte();
        java.nio.file.Path ruta=java.nio.file.Path.of(pedir("Ruta del archivo nuevo .tsv"));
        archivos.guardar(ruta,reporte);
        vista.println("Reporte guardado en " + ruta.toAbsolutePath());
    }
    private void leerReporte() throws java.io.IOException {
        vista.mostrarReporte(archivos.leer(java.nio.file.Path.of(pedir("Ruta del reporte .tsv"))));
    }
    private long numero(String etiqueta) {
        return Long.parseLong(pedir(etiqueta));
    }

    private String pedir(String etiqueta) {
        return vista.pedir(etiqueta);
    }
}
