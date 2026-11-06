import huellas.modelo.*;
import huellas.servicio.*;
import huellas.persistencia.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
public final class PruebasModelo {
    private static int total;
    static void comprobar(boolean valor, String caso) {
        if (!valor) throw new AssertionError(caso);
        System.out.println("OK " + (++total) + " | " + caso);
    }
    static void rechaza(Runnable accion, String caso) {
        try { accion.run(); } catch (IllegalArgumentException e) { comprobar(true, caso); return; }
        throw new AssertionError(caso);
    }
    public static void main(String[] args) {
        Persona cliente = new Cliente(1, " Ana Pérez ", "123", "351123", "");
        Persona vet = new Veterinario(1, "Juan López", "MP10");
        comprobar(cliente.descripcion().contains("DNI 123"), "Polimorfismo de Cliente");
        comprobar(vet.descripcion().contains("matrícula MP10"), "Polimorfismo de Veterinario");
        comprobar(cliente.getNombre().equals("Ana Pérez"), "Constructor normaliza nombre");
        rechaza(() -> new Cliente(0," ","1","2",""), "Nombre obligatorio");
        rechaza(() -> new Cliente(-1,"Ana","1","2",""), "ID negativo rechazado");
        Mascota m = new Mascota(1,1,"Luna",Especie.GATO,"",Sexo.HEMBRA,null,"Polen",true);
        rechaza(() -> new Mascota(0,1,"Luna",Especie.GATO,"",Sexo.HEMBRA,LocalDate.now().plusDays(1),"",true), "Nacimiento futuro rechazado");
        Turno t = new Turno(1,1,1,LocalDateTime.of(2026,1,1,10,0),"Control",EstadoTurno.CONFIRMADO,"Luna","Juan");
        t.atender();
        comprobar(t.getEstado()==EstadoTurno.ATENDIDO, "Transición a atendido");
        rechaza(t::atender, "No atender dos veces");
        rechaza(() -> new Consulta(0,1,"Control","Sano","Control anual",BigDecimal.ZERO,LocalDateTime.now(),"Juan"), "Peso cero rechazado");
        rechaza(() -> new Consulta(0,1,"Control","Sano","Control anual",new BigDecimal("10.123"),LocalDateTime.now(),"Juan"), "No redondear peso de tres decimales");
        rechaza(() -> new Vacuna(0,1,"Antirrábica",LocalDate.now(),LocalDate.now().minusDays(1)), "Refuerzo anterior rechazado");
        rechaza(() -> new Vacuna(0,1,"Antirrábica",LocalDate.now().plusDays(1),null), "Aplicación futura rechazada");
        comprobar(new Vacuna(0,1,"Antirrábica",LocalDate.now(),null).getRefuerzo()==null, "Refuerzo opcional");
        List<Mascota> mascotas=List.of(m,new Mascota(2,1,"Toby",Especie.PERRO,"",Sexo.MACHO,null,"",true));
        comprobar(Algoritmos.buscarMascotas(mascotas,"LUN").size()==1, "Búsqueda parcial sin distinguir mayúsculas");
        comprobar(Algoritmos.buscarMascotas(mascotas,"XXX").isEmpty(), "Búsqueda sin resultados");
        Turno temprano = new Turno(2,1,1,LocalDateTime.of(2026,1,1,9,0),"Control",EstadoTurno.CONFIRMADO,"Luna","Juan");
        List<Turno> agenda=new ArrayList<>(List.of(t,temprano));
        Algoritmos.ordenarAgenda(agenda);
        comprobar(agenda.get(0).getId()==2, "Ordenamiento por inserción de agenda");
        comprobar(Fechas.fechaHora("28/02/2027 10:30").getDayOfMonth()==28, "Fecha y hora válida");
        try { Fechas.fechaHora("30/02/2027 10:30"); throw new AssertionError("Fecha imposible"); }
        catch (DateTimeException e) { comprobar(true,"Fecha imposible rechazada sin ajuste"); }
        System.out.println("TOTAL: " + total + " pruebas correctas.");
    }
}
