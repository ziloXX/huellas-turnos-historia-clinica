import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
public final class PruebasConsola {
    private static int total;
    private static void probar(String entrada,String esperado,String caso) throws Exception {
        String java=Path.of(System.getProperty("java.home"),"bin","java").toString();
        Process proceso=new ProcessBuilder(java,"-Dstdout.encoding=UTF-8","-Dstderr.encoding=UTF-8","-cp",System.getProperty("java.class.path"),"HuellasApp").redirectErrorStream(true).start();
        try(var salida=proceso.getOutputStream()) { salida.write(entrada.getBytes(StandardCharsets.UTF_8)); }
        String texto=new String(proceso.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
        if(proceso.waitFor()!=0 || !texto.contains(esperado) || texto.contains("Exception in thread")) throw new AssertionError(caso + "\n" + texto);
        System.out.println("OK " + (++total) + " | " + caso);
    }
    public static void main(String[] args) throws Exception {
        probar("5\n2026-02-30\n0\n","Hasta luego.","Fecha inválida recupera el menú");
        probar("2\nabc\n0\n","Hasta luego.","ID no numérico recupera el menú");
        probar("6\n1\nControl\nSano\nSeguimiento\nabc\n0\n","Hasta luego.","Peso no numérico recupera el menú");
        probar("99\n0\n","Elegí una opción del menú.","Opción inexistente");
        probar("1\n","Fin de la entrada.","Fin de entrada durante una operación");
        probar("","Fin de la entrada.","Fin de entrada en el menú");
        probar("9\n1\nVacuna\n2026-02-30\n0\n","Hasta luego.","Fecha inválida en vacuna");
        probar("0\n","9. Registrar vacuna","Nuevas opciones disponibles");
        System.out.println("TOTAL: " + total + " pruebas de consola correctas.");
    }
}
