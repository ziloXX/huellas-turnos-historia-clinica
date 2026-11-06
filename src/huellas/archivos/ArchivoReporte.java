package huellas.archivos;
import huellas.modelo.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
public final class ArchivoReporte {
    private static final String CABECERA="consulta_id\tmascota_id\tmascota_base64\tveterinario_base64\tatendida\tpeso_kg";
    private static String codificar(String texto){return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));}
    private static String decodificar(String texto)throws IOException {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(Base64.getDecoder().decode(texto))).toString();
        } catch(java.nio.charset.CharacterCodingException e) {
            throw new IOException("El reporte contiene texto UTF-8 dañado.",e);
        }
    }
    public void guardar(Path archivo,ReporteConsultas reporte)throws IOException {
        // CREATE_NEW evita reemplazar por error un reporte o cualquier otro archivo.
        try(BufferedWriter w=Files.newBufferedWriter(archivo,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW)) {
            w.write("HUELLAS_CONSULTAS_V1\t"+reporte.getDesde()+"\t"+reporte.getHasta());w.newLine();
            w.write(CABECERA);w.newLine();
            for(FilaConsulta f:reporte.getFilas()) {
                w.write(f.consultaId()+"\t"+f.mascotaId()+"\t"+codificar(f.mascota())+"\t"+codificar(f.veterinario())+"\t"+f.atendida()+"\t"+f.peso().toPlainString());w.newLine();
            }
        }
    }
    public ReporteConsultas leer(Path archivo)throws IOException {
        try(BufferedReader lector=Files.newBufferedReader(archivo,StandardCharsets.UTF_8)) {
            String primera=lector.readLine();
            if(primera==null)throw new IOException("El archivo está vacío.");
            String[] inicio=primera.split("\t",-1);
            if(inicio.length!=3||!inicio[0].equals("HUELLAS_CONSULTAS_V1")||!CABECERA.equals(lector.readLine()))throw new IOException("El archivo no tiene el formato de un reporte Huellas.");
            try {
                LocalDate desde=LocalDate.parse(inicio[1]),hasta=LocalDate.parse(inicio[2]);
                ArrayList<FilaConsulta> filas=new ArrayList<>();Set<Long> ids=new HashSet<>();String linea;
                while((linea=lector.readLine())!=null) {
                    String[] campos=linea.split("\t",-1);
                    if(campos.length!=6)throw new IllegalArgumentException("Fila incompleta.");
                    long id=Long.parseLong(campos[0]);
                    if(!ids.add(id))throw new IllegalArgumentException("Consulta repetida en el archivo.");
                    filas.add(new FilaConsulta(id,Long.parseLong(campos[1]),decodificar(campos[2]),decodificar(campos[3]),LocalDateTime.parse(campos[4]),new BigDecimal(campos[5])));
                }
                return new ReporteConsultas(desde,hasta,filas);
            } catch(RuntimeException e){throw new IOException("El reporte contiene datos inválidos: "+e.getMessage(),e);}
        }
    }
}
