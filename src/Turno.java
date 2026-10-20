import java.time.LocalDateTime;
public final class Turno {
    private final long id, mascotaId, veterinarioId;
    private final LocalDateTime inicio;
    private final String motivo, mascota, veterinario;
    private EstadoTurno estado;
    public Turno(long id, long mascotaId, long veterinarioId, LocalDateTime inicio,
                 String motivo, EstadoTurno estado, String mascota, String veterinario) {
        this.id=Validacion.idNuevo(id);
        this.mascotaId=Validacion.id(mascotaId,"mascota");
        this.veterinarioId=Validacion.id(veterinarioId,"veterinario");
        if (inicio == null || inicio.getMinute()%30 != 0 || inicio.getSecond()!=0 || inicio.getNano()!=0)
            throw new IllegalArgumentException("Cada turno comienza en punto o y media, sin segundos.");
        if (estado == null) throw new IllegalArgumentException("Falta el estado del turno.");
        this.inicio=inicio; this.estado=estado;
        this.motivo=Validacion.texto(motivo,"motivo",250);
        this.mascota=mascota == null ? "" : mascota;
        this.veterinario=veterinario == null ? "" : veterinario;
    }
    public void atender() {
        if (estado != EstadoTurno.CONFIRMADO) throw new IllegalArgumentException("El turno ya fue cerrado.");
        estado=EstadoTurno.ATENDIDO;
    }
    public long getId() { return id; }
    public long getMascotaId() { return mascotaId; }
    public long getVeterinarioId() { return veterinarioId; }
    public LocalDateTime getInicio() { return inicio; }
    public String getMotivo() { return motivo; }
    public EstadoTurno getEstado() { return estado; }
    public String getMascota() { return mascota; }
    public String getVeterinario() { return veterinario; }
}
