import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public final class Algoritmos {
    private Algoritmos() {}
    public static List<Mascota> buscarMascotas(List<Mascota> mascotas, String texto) {
        String criterio=Validacion.texto(texto,"nombre a buscar",80).toLowerCase(Locale.ROOT);
        List<Mascota> resultado=new ArrayList<>();
        for (Mascota mascota : mascotas) {
            if (mascota.getNombre().toLowerCase(Locale.ROOT).contains(criterio)) resultado.add(mascota);
        }
        return resultado;
    }
    public static void ordenarAgenda(List<Turno> turnos) {
        // Inserción: el tramo anterior a i ya está ordenado por fecha y hora.
        for (int i=1; i<turnos.size(); i++) {
            Turno actual=turnos.get(i);
            int j=i-1;
            while (j>=0 && turnos.get(j).getInicio().isAfter(actual.getInicio())) {
                turnos.set(j+1,turnos.get(j));
                j--;
            }
            turnos.set(j+1,actual);
        }
    }
}
