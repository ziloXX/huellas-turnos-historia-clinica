import java.math.BigDecimal;
public final class Validacion {
    private Validacion() {}
    public static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Falta completar " + campo + ".");
        String limpio = valor.trim();
        if (limpio.codePointCount(0, limpio.length()) > maximo) throw new IllegalArgumentException("El campo " + campo + " admite hasta " + maximo + " caracteres.");
        return limpio;
    }
    public static String opcional(String valor, String campo, int maximo) {
        return valor == null || valor.isBlank() ? "" : texto(valor, campo, maximo);
    }
    public static long id(long valor, String campo) {
        if (valor <= 0) throw new IllegalArgumentException("El número de " + campo + " debe ser positivo.");
        return valor;
    }
    public static long idNuevo(long valor) {
        if (valor < 0) throw new IllegalArgumentException("El número de registro no puede ser negativo.");
        return valor;
    }
    public static BigDecimal peso(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0 || valor.compareTo(new BigDecimal("9999.99")) > 0 || valor.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Ingresá un peso mayor que cero, hasta 9999.99 kg y con hasta dos decimales.");
        return valor;
    }
}
