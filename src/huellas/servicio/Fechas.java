package huellas.servicio;
import huellas.modelo.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
public final class Fechas {
    private static final DateTimeFormatter FORMATO=DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private Fechas() {}
    public static LocalDateTime fechaHora(String texto) { return LocalDateTime.parse(texto,FORMATO); }
}
