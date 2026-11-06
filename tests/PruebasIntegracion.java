import huellas.modelo.*;
import huellas.servicio.*;
import huellas.persistencia.*;
import java.sql.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public final class PruebasIntegracion {
    private static final HuellasService servicio=new HuellasService();
    private static int total;
    private static long cliente, mascota, veterinario, otroVeterinario;
    private static final List<Long> turnos=new ArrayList<>();
    private static final List<Long> consultas=new ArrayList<>();
    private static final List<Long> vacunas=new ArrayList<>();
    interface Operacion { void ejecutar() throws Exception; }
    private static void verificar(boolean valor,String caso) {
        if (!valor) throw new AssertionError(caso);
        System.out.println("OK " + (++total) + " | " + caso);
    }
    private static void rechazar(Operacion accion,String caso) throws Exception {
        try { accion.ejecutar(); } catch (IllegalArgumentException e) { verificar(true,caso); return; }
        throw new AssertionError(caso);
    }
    private static void duplicado(Operacion accion,String caso) throws Exception {
        try { accion.ejecutar(); } catch (SQLException e) {
            if(e.getErrorCode()!=1062) throw e;
            verificar(true,caso); return;
        }
        throw new AssertionError(caso);
    }
    private static long altaVeterinario(String nombre,String matricula) throws SQLException {
        try(Connection con=Conexion.abrir();PreparedStatement ps=con.prepareStatement("INSERT INTO veterinarios(nombre,matricula) VALUES (?,?)",Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,nombre); ps.setString(2,matricula); ps.executeUpdate(); return Jdbc.idGenerado(ps);
        }
    }
    private static void borrar(String tabla,long id) throws SQLException {
        if(id==0) return;
        try(Connection con=Conexion.abrir();PreparedStatement ps=con.prepareStatement("DELETE FROM " + tabla + " WHERE id=?")) { ps.setLong(1,id); ps.executeUpdate(); }
    }
    private static void activo(boolean valor) throws SQLException {
        try(Connection con=Conexion.abrir();PreparedStatement ps=con.prepareStatement("UPDATE mascotas SET activo=? WHERE id=?")) { ps.setBoolean(1,valor); ps.setLong(2,mascota); ps.executeUpdate(); }
    }
    public static void main(String[] args) throws Exception {
        String marca=Long.toString(System.currentTimeMillis());
        LocalDateTime inicio=LocalDate.now().plusDays(10).atTime(10,0);
        try {
            try(Connection con=Conexion.abrir()) { verificar(con.isValid(5),"Conexión real a MySQL"); }
            cliente=servicio.registrarCliente("Cliente de prueba TP4",marca,"3510000000","");
            verificar(cliente>0 && servicio.cliente(cliente).getDni().equals(marca),"Alta y lectura de cliente");
            duplicado(()->servicio.registrarCliente("Repetido",marca,"3510000000",""),"DNI duplicado rechazado");
            mascota=servicio.registrarMascota(cliente,"Prueba"+marca,"GATO","","HEMBRA",null,"Polen");
            verificar(mascota>0,"Alta de mascota");
            rechazar(()->servicio.registrarMascota(cliente,"Ave","AVE","","HEMBRA",null,""),"Especie inválida rechazada");
            verificar(servicio.buscarMascotas("prueba"+marca).stream().anyMatch(m->m.getId()==mascota),"Búsqueda por nombre desde MySQL");
            veterinario=altaVeterinario("Veterinario de prueba TP4","T"+marca);
            otroVeterinario=altaVeterinario("Otro veterinario de prueba TP4","U"+marca);
            verificar(servicio.listarVeterinarios().stream().anyMatch(v->v.getId()==veterinario),"Lista de objetos Veterinario");
            long turno=servicio.agendarTurno(mascota,veterinario,inicio,"Control"); turnos.add(turno);
            verificar(turno>0,"Reserva futura");
            duplicado(()->servicio.agendarTurno(mascota,veterinario,inicio,"Repetido"),"Horario ocupado rechazado");
            long otro=servicio.agendarTurno(mascota,otroVeterinario,inicio,"Control"); turnos.add(otro);
            verificar(otro>0,"Mismo horario con otro veterinario");
            rechazar(()->servicio.agendarTurno(mascota,veterinario,inicio.plusMinutes(15),"Control"),"Horario fuera de grilla rechazado");
            rechazar(()->servicio.agendarTurno(mascota,veterinario,inicio.minusYears(1),"Control"),"Turno pasado rechazado");
            long temprano=servicio.agendarTurno(mascota,veterinario,inicio.minusHours(1),"Control"); turnos.add(temprano);
            List<Turno> agenda=servicio.turnosDelDia(inicio.toLocalDate(),veterinario);
            verificar(agenda.size()==2 && agenda.get(0).getId()==temprano && agenda.stream().allMatch(t->t.getVeterinarioId()==veterinario),"Agenda filtrada y ordenada por inserción");
            rechazar(()->servicio.registrarConsulta(temprano,"Control","Sano","Seguimiento",BigDecimal.ZERO),"Peso cero rechazado antes de guardar");
            verificar(servicio.turnosDelDia(inicio.toLocalDate(),veterinario).stream().filter(t->t.getId()==temprano).findFirst().orElseThrow().getEstado()==EstadoTurno.CONFIRMADO,"Turno conservado tras validación fallida");
            long consulta=servicio.registrarConsulta(turno,"Control","Sin alteraciones","Control anual",new BigDecimal("4.50")); consultas.add(consulta);
            verificar(servicio.turnosDelDia(inicio.toLocalDate(),veterinario).stream().filter(t->t.getId()==turno).findFirst().orElseThrow().getEstado()==EstadoTurno.ATENDIDO,"Transacción: consulta y turno atendido");
            rechazar(()->servicio.registrarConsulta(turno,"Control","Sano","Control anual",new BigDecimal("4.50")),"Segunda atención rechazada dentro de transacción");
            verificar(servicio.historial(mascota).getConsultas().size()==1,"Segunda atención: no se duplicó la consulta");
            ConsultaRepository repositorioConFalla=new ConsultaRepository(new TurnoRepository() {
                @Override public void actualizarEstado(Connection con,Turno turno) throws SQLException {
                    throw new SQLException("Fallo controlado después de insertar", "ZZ001");
                }
            });
            boolean falloPosterior=false;
            try {
                repositorioConFalla.guardar(new Consulta(0,temprano,"Control","Sano","Seguimiento",new BigDecimal("4.50"),LocalDateTime.now(),""));
            } catch (SQLException e) {
                if (!"ZZ001".equals(e.getSQLState())) throw e;
                falloPosterior=true;
            }
            verificar(falloPosterior,"Fallo controlado después del INSERT de consulta");
            verificar(servicio.historial(mascota).getConsultas().size()==1
                && servicio.turnosDelDia(inicio.toLocalDate(),veterinario).stream().filter(t->t.getId()==temprano).findFirst().orElseThrow().getEstado()==EstadoTurno.CONFIRMADO,
                "Rollback real: sin consulta parcial y turno confirmado desde otra conexión");
            verificar(servicio.buscarClientes(marca).stream().anyMatch(c->c.getId()==cliente),"Buscar cliente por DNI");
            verificar(servicio.buscarClientes("Cliente de prueba TP4").stream().anyMatch(c->c.getId()==cliente),"Buscar cliente por nombre");
            duplicado(()->servicio.reprogramarTurno(temprano,inicio),"Reprogramación a horario ocupado rechazada");
            verificar(servicio.turnosDelDia(inicio.toLocalDate(),veterinario).stream().anyMatch(t->t.getId()==temprano&&t.getInicio().equals(inicio.minusHours(1))),"Rollback de reprogramación conserva horario anterior");
            servicio.reprogramarTurno(otro,inicio.plusHours(2));
            verificar(servicio.turnosDelDia(inicio.toLocalDate(),otroVeterinario).stream().anyMatch(t->t.getId()==otro&&t.getInicio().equals(inicio.plusHours(2))),"Reprogramación conserva el ID y cambia horario en MySQL");
            servicio.cancelarTurno(otro,"No puede asistir");
            verificar(servicio.turnosDelDia(inicio.toLocalDate(),otroVeterinario).stream().anyMatch(t->t.getId()==otro&&t.getEstado()==EstadoTurno.CANCELADO),"Cancelación persistida");
            long reemplazo=servicio.agendarTurno(mascota,otroVeterinario,inicio.plusHours(2),"Otro control");turnos.add(reemplazo);
            verificar(reemplazo>0,"Cancelación libera el horario para otra reserva");
            rechazar(()->servicio.cancelarTurno(turno,"Motivo"),"No cancelar atención cerrada");
            rechazar(()->servicio.reprogramarTurno(turno,inicio.plusHours(3)),"No reprogramar atención cerrada");
            ReporteConsultas reporte=servicio.reporte(LocalDate.now(),LocalDate.now());
            verificar(reporte.getFilas().stream().anyMatch(f->f.consultaId()==consulta)&&reporte.getPorDia()[LocalDate.now().getDayOfWeek().getValue()-1]>=1,"Reporte MySQL por fecha real de atención y arreglo semanal");
            String entrada="3\n8\nPrueba"+marca+"\n10\n"+marca+"\n5\n"+inicio.toLocalDate()+"\n"+veterinario+"\n13\n"+LocalDate.now()+"\n"+LocalDate.now()+"\n0\n";
            Process proceso=new ProcessBuilder(java.nio.file.Path.of(System.getProperty("java.home"),"bin","java").toString(),"-Dstdout.encoding=UTF-8","-Dstderr.encoding=UTF-8","-cp",System.getProperty("java.class.path"),"HuellasApp").redirectErrorStream(true).start();
            try(var out=proceso.getOutputStream()){out.write(entrada.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
            String pantalla=new String(proceso.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            verificar(proceso.waitFor()==0&&pantalla.contains("Prueba"+marca)&&pantalla.contains("DNI "+marca)&&pantalla.contains("Consultas atendidas entre")&&pantalla.contains("Hasta luego."),"Menú real: veterinarios, búsqueda, cliente, agenda y reporte con MySQL");
            long vacuna=servicio.registrarVacuna(consulta,"Antirrábica",LocalDate.now(),LocalDate.now().plusYears(1)); vacunas.add(vacuna);
            verificar(vacuna>0,"Alta de vacuna desde servicio");
            rechazar(()->servicio.registrarVacuna(consulta,"Antirrábica",LocalDate.now(),LocalDate.now().minusDays(1)),"Refuerzo anterior rechazado");
            rechazar(()->servicio.registrarVacuna(Long.MAX_VALUE,"Antirrábica",LocalDate.now(),null),"Consulta inexistente rechazada");
            HistoriaClinica historia=new HuellasService().historial(mascota);
            verificar(historia.getConsultas().size()==1 && historia.getVacunas().size()==1 && historia.getMascota().getAlergias().equals("Polen"),"Historia persistida leída con nuevas conexiones");
            activo(false);
            verificar(servicio.historial(mascota).getConsultas().size()==1 && !servicio.historial(mascota).getMascota().isActivo(),"Historia conservada tras baja lógica");
            rechazar(()->servicio.agendarTurno(mascota,veterinario,inicio.plusHours(2),"Control"),"No reservar para mascota inactiva");
            verificar(servicio.turnosDelDia(inicio.toLocalDate().plusDays(1),veterinario).isEmpty(),"Agenda vacía");
            System.out.println("TOTAL: " + total + " pruebas de integración correctas.");
        } finally {
            for(long id:vacunas) borrar("vacunas",id);
            for(long id:consultas) borrar("consultas",id);
            for(long id:turnos) borrar("turnos",id);
            borrar("mascotas",mascota); borrar("clientes",cliente);
            borrar("veterinarios",veterinario); borrar("veterinarios",otroVeterinario);
            System.out.println("Limpieza terminada: se quitaron solamente los registros creados por esta prueba.");
        }
    }
}
