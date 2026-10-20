import java.util.List;
public final class HistoriaClinica {
    private final Mascota mascota;
    private final List<Consulta> consultas;
    private final List<Vacuna> vacunas;
    public HistoriaClinica(Mascota mascota, List<Consulta> consultas, List<Vacuna> vacunas) {
        this.mascota=mascota; this.consultas=List.copyOf(consultas); this.vacunas=List.copyOf(vacunas);
    }
    public Mascota getMascota() { return mascota; }
    public List<Consulta> getConsultas() { return consultas; }
    public List<Vacuna> getVacunas() { return vacunas; }
}
