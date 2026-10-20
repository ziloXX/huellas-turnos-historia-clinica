import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class Consulta {
    private final long id, turnoId;
    private final String motivo, diagnostico, tratamiento, veterinario;
    private final BigDecimal peso;
    private final LocalDateTime atendida;
    public Consulta(long id, long turnoId, String motivo, String diagnostico, String tratamiento,
                    BigDecimal peso, LocalDateTime atendida, String veterinario) {
        this.id=Validacion.idNuevo(id); this.turnoId=Validacion.id(turnoId,"turno");
        this.motivo=Validacion.texto(motivo,"motivo",250);
        this.diagnostico=Validacion.texto(diagnostico,"diagnóstico",16000);
        this.tratamiento=Validacion.texto(tratamiento,"tratamiento",16000);
        this.peso=Validacion.peso(peso);
        if (atendida == null) throw new IllegalArgumentException("Falta la fecha de atención.");
        this.atendida=atendida; this.veterinario=veterinario == null ? "" : veterinario;
    }
    public long getId() { return id; }
    public long getTurnoId() { return turnoId; }
    public String getMotivo() { return motivo; }
    public String getDiagnostico() { return diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public BigDecimal getPeso() { return peso; }
    public LocalDateTime getAtendida() { return atendida; }
    public String getVeterinario() { return veterinario; }
}
