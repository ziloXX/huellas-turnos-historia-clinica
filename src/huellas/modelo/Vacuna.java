package huellas.modelo;
import java.time.LocalDate;
public final class Vacuna {
    private final long id, consultaId;
    private final String nombre;
    private final LocalDate aplicada, refuerzo;
    public Vacuna(long id, long consultaId, String nombre, LocalDate aplicada, LocalDate refuerzo) {
        this.id=Validacion.idNuevo(id); this.consultaId=Validacion.id(consultaId,"consulta");
        this.nombre=Validacion.texto(nombre,"vacuna",100);
        if (aplicada == null || aplicada.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de aplicación debe ser hoy o anterior.");
        if (refuerzo != null && refuerzo.isBefore(aplicada)) throw new IllegalArgumentException("El refuerzo no puede ser anterior a la aplicación.");
        this.aplicada=aplicada; this.refuerzo=refuerzo;
    }
    public long getId() { return id; }
    public long getConsultaId() { return consultaId; }
    public String getNombre() { return nombre; }
    public LocalDate getAplicada() { return aplicada; }
    public LocalDate getRefuerzo() { return refuerzo; }
}
