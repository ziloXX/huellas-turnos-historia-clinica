import huellas.modelo.*;
import huellas.archivos.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
public final class PruebasTP4 {
 private static int total;
 private static void ok(boolean b,String caso) { if(!b)throw new AssertionError(caso);System.out.println("OK "+(++total)+" | "+caso); }
 interface Accion {void ejecutar() throws Exception;}
 private static void rechaza(Accion a,String caso) throws Exception {try{a.ejecutar();}catch(IllegalArgumentException|java.io.IOException e){ok(true,caso);return;}throw new AssertionError(caso);}
 public static void main(String[] args)throws Exception {
  var fecha=LocalDate.now().plusDays(10).atTime(10,0);
  var t=new Turno(1,1,1,fecha,"Control",EstadoTurno.CONFIRMADO,"Nero","Ana");
  t.reprogramar(fecha.plusHours(1)); ok(t.getInicio().equals(fecha.plusHours(1)),"Reprogramación conserva turno");
  rechaza(()->t.reprogramar(fecha.plusMinutes(15)),"Reprogramación fuera de grilla");
  t.cancelar("No puede asistir");ok(t.getEstado()==EstadoTurno.CANCELADO,"Cancelación cambia estado");
  rechaza(()->t.cancelar("Motivo"),"No cancelar turno cerrado");rechaza(()->t.reprogramar(fecha),"No reprogramar cancelado");
  var cerrado=new Turno(2,1,1,fecha,"Control",EstadoTurno.ATENDIDO,"Nero","Ana");
  rechaza(()->cerrado.reprogramar(fecha),"No reprogramar atendido");
  var filas=new ArrayList<FilaConsulta>();
  filas.add(new FilaConsulta(1,1,"Nero\tFiel","Ana\nPérez",LocalDate.of(2026,11,2).atTime(10,0),new BigDecimal("4.50")));
  filas.add(new FilaConsulta(2,2,"Luna","Juan",LocalDate.of(2026,11,2).atTime(11,0),new BigDecimal("5.20")));
  var reporte=new ReporteConsultas(LocalDate.of(2026,11,1),LocalDate.of(2026,11,7),filas);
  ok(reporte.getPorDia().length==7&&reporte.getPorDia()[0]==2,"Arreglo cuenta consultas del lunes");
  long[] copia=reporte.getPorDia();copia[0]=99;ok(reporte.getPorDia()[0]==2,"Arreglo protegido con copia");
  filas.clear();ok(reporte.getFilas().size()==2,"ArrayList copiado al construir reporte");
  rechaza(()->new ReporteConsultas(LocalDate.now(),LocalDate.now().minusDays(1),List.of()),"Rango invertido rechazado");
  rechaza(()->new ReporteConsultas(LocalDate.of(2026,11,9),LocalDate.of(2026,11,10),reporte.getFilas()),"Filas fuera del período rechazadas");
  Path carpeta=Files.createTempDirectory("huellas-pruebas-");
  try {
   Path archivo=carpeta.resolve("reporte.tsv");var gestor=new ArchivoReporte();gestor.guardar(archivo,reporte);
   var leido=gestor.leer(archivo);ok(leido.getFilas().size()==2,"Recuperación de filas desde archivo");
   ok(leido.getFilas().get(0).mascota().equals("Nero\tFiel")&&leido.getFilas().get(0).veterinario().equals("Ana\nPérez"),"Archivo conserva UTF-8 y separadores");
   ok(leido.getPorDia()[0]==2,"Resumen reconstruido desde archivo");
   rechaza(()->gestor.guardar(archivo,reporte),"No sobrescribir archivo existente");
   Path vacio=carpeta.resolve("vacio.tsv");gestor.guardar(vacio,new ReporteConsultas(LocalDate.now(),LocalDate.now(),List.of()));ok(gestor.leer(vacio).getFilas().isEmpty(),"Reporte vacío válido");
   Path malo=carpeta.resolve("malo.tsv");Files.writeString(malo,"cualquier texto");rechaza(()->gestor.leer(malo),"Archivo inválido rechazado");
   Files.writeString(malo,Files.readString(archivo)+"fila incompleta\n");rechaza(()->gestor.leer(malo),"Fila incompleta rechazada");
   String original=Files.readString(archivo);
   String alterado=original.replace("TmVybwlGaWVs","/w==");
   Files.writeString(malo,alterado);
   rechaza(()->gestor.leer(malo),"Texto Base64 con UTF-8 dañado rechazado");
   rechaza(()->gestor.leer(carpeta.resolve("ausente.tsv")),"Archivo inexistente controlado");
  }finally{try(var stream=Files.list(carpeta)){for(Path p:stream.toList())Files.delete(p);}Files.delete(carpeta);}
  System.out.println("TOTAL: "+total+" pruebas TP4 correctas.");
 }
}
