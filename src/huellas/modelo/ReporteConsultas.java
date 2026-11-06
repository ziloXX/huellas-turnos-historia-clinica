package huellas.modelo;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
public final class ReporteConsultas {
    private final LocalDate desde,hasta;
    private final ArrayList<FilaConsulta> filas;
    private final long[] porDia=new long[7];
    public ReporteConsultas(LocalDate desde,LocalDate hasta,List<FilaConsulta> filas) {
        if(desde==null||hasta==null||hasta.isBefore(desde))throw new IllegalArgumentException("El período debe tener fechas válidas y ordenadas.");
        this.desde=desde;this.hasta=hasta;this.filas=new ArrayList<>(filas);
        for(FilaConsulta fila:this.filas) {
            LocalDate fecha=fila.atendida().toLocalDate();
            if(fecha.isBefore(desde)||fecha.isAfter(hasta))throw new IllegalArgumentException("Una atención no pertenece al período indicado.");
            porDia[fecha.getDayOfWeek().getValue()-1]++;
        }
    }
    public LocalDate getDesde(){return desde;}
    public LocalDate getHasta(){return hasta;}
    public List<FilaConsulta> getFilas(){return List.copyOf(filas);}
    public long[] getPorDia(){return porDia.clone();}
}
