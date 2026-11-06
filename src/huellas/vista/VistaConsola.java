package huellas.vista;
import huellas.modelo.*;
import java.util.List;
import java.util.Scanner;
public final class VistaConsola {
    private final Scanner teclado=new Scanner(System.in);
    private static final String[] OPCIONES={"1. Registrar cliente","2. Registrar mascota","3. Ver veterinarios","4. Agendar turno","5. Ver turnos de una fecha","6. Registrar consulta","7. Ver historia clínica","8. Buscar mascotas por nombre","9. Registrar vacuna","10. Buscar clientes por nombre o DNI","11. Cancelar turno","12. Reprogramar turno","13. Ver reporte de consultas","14. Guardar reporte en archivo","15. Abrir reporte guardado","0. Salir"};
    public void menu(){println("");for(String opcion:OPCIONES)println(opcion);}
    public void println(String texto){System.out.println(texto);}
    public boolean hayEntrada(){return teclado.hasNextLine();}
    public String pedir(String etiqueta){System.out.print(etiqueta+": ");return teclado.nextLine().trim();}
    public void mostrarPersona(Persona persona){println(persona.descripcion());}
    public void mostrarPersonas(List<? extends Persona> personas) {
        if(personas.isEmpty())println("No encontré personas con esos datos.");
        for(Persona persona:personas)mostrarPersona(persona);
    }
    public void mostrarMascota(Mascota mascota){println("#"+mascota.getId()+" | "+mascota.getNombre()+" | "+mascota.getEspecie()+" | "+(mascota.isActivo()?"activa":"inactiva"));}
    public void mostrarAgenda(List<Turno> agenda) {
        if(agenda.isEmpty())println("No hay turnos para esa fecha.");
        for(Turno t:agenda)println("#"+t.getId()+" | "+t.getInicio()+" | "+t.getMascota()+" | "+t.getVeterinario()+" | "+t.getEstado()+" | "+t.getMotivo());
    }
    public void mostrarHistoria(HistoriaClinica historia) {
        Mascota m=historia.getMascota();println("Historia de "+m.getNombre()+(m.isActivo()?"":" (inactiva)"));
        println("Alergias: "+(m.getAlergias().isBlank()?"sin registro":m.getAlergias()));println("Consultas:");
        if(historia.getConsultas().isEmpty())println("Todavía no hay consultas registradas.");
        for(Consulta c:historia.getConsultas()) {
            println("Consulta #"+c.getId()+" | "+c.getAtendida()+" | "+c.getVeterinario());
            println("Motivo: "+c.getMotivo()+". Diagnóstico: "+c.getDiagnostico()+". Tratamiento: "+c.getTratamiento()+". Peso: "+c.getPeso()+" kg.");
        }
        println("Vacunas:");if(historia.getVacunas().isEmpty())println("Todavía no hay vacunas registradas.");
        for(Vacuna v:historia.getVacunas())println(v.getNombre()+" | aplicada: "+v.getAplicada()+" | refuerzo: "+(v.getRefuerzo()==null?"sin fecha":v.getRefuerzo()));
    }
    public void mostrarReporte(ReporteConsultas reporte) {
        println("Consultas atendidas entre "+reporte.getDesde()+" y "+reporte.getHasta());
        if(reporte.getFilas().isEmpty())println("No hay consultas en ese período.");
        for(FilaConsulta f:reporte.getFilas())println("Consulta #"+f.consultaId()+" | mascota #"+f.mascotaId()+" "+f.mascota()+" | "+f.veterinario()+" | "+f.atendida()+" | "+f.peso()+" kg");
        String[] dias={"lunes","martes","miércoles","jueves","viernes","sábado","domingo"};
        long[] cantidades=reporte.getPorDia();
        for(int i=0;i<cantidades.length;i++)println(dias[i]+": "+cantidades[i]);
        println("Total: "+reporte.getFilas().size());
    }
}
