package huellas.modelo;
import java.time.LocalDateTime;
import java.math.BigDecimal;
public record FilaConsulta(long consultaId,long mascotaId,String mascota,String veterinario,LocalDateTime atendida,BigDecimal peso) {
    public FilaConsulta {
        Validacion.id(consultaId,"consulta");Validacion.id(mascotaId,"mascota");
        mascota=Validacion.texto(mascota,"mascota",80);veterinario=Validacion.texto(veterinario,"veterinario",100);
        if(atendida==null)throw new IllegalArgumentException("Falta la fecha de atención.");
        peso=Validacion.peso(peso);
    }
}
