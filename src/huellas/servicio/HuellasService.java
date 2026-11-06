package huellas.servicio;
import huellas.persistencia.*;
import huellas.modelo.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.Locale;
public final class HuellasService {
    private final ClienteRepository clientes=new ClienteRepository();
    private final MascotaRepository mascotas=new MascotaRepository();
    private final VeterinarioRepository veterinarios=new VeterinarioRepository();
    private final TurnoRepository turnos=new TurnoRepository();
    private final ConsultaRepository consultas=new ConsultaRepository();
    private final VacunaRepository vacunas=new VacunaRepository();
    public long registrarCliente(String nombre,String dni,String telefono,String correo) throws SQLException {
        return clientes.guardar(new Cliente(0,nombre,dni,telefono,correo));
    }
    public Cliente cliente(long id) throws SQLException { return clientes.buscar(Validacion.id(id,"cliente")); }
    public long registrarMascota(long clienteId,String nombre,String especie,String raza,String sexo,LocalDate nacimiento,String alergias) throws SQLException {
        Especie tipo; Sexo genero;
        try { tipo=Especie.valueOf(especie.trim().toUpperCase(Locale.ROOT)); genero=Sexo.valueOf(sexo.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException | NullPointerException e) { throw new IllegalArgumentException("Completá especie PERRO/GATO y sexo MACHO/HEMBRA."); }
        return mascotas.guardar(new Mascota(0,clienteId,nombre,tipo,raza,genero,nacimiento,alergias,true));
    }
    public List<Veterinario> listarVeterinarios() throws SQLException { return veterinarios.listarActivos(); }
    public List<Mascota> buscarMascotas(String nombre) throws SQLException {
        Validacion.texto(nombre,"nombre a buscar",80);
        return Algoritmos.buscarMascotas(mascotas.listar(),nombre);
    }
    public long agendarTurno(long mascotaId,long veterinarioId,LocalDateTime inicio,String motivo) throws SQLException {
        if (inicio==null || !inicio.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Elegí una fecha y hora futuras.");
        return turnos.guardar(new Turno(0,mascotaId,veterinarioId,inicio,motivo,EstadoTurno.CONFIRMADO,"",""));
    }
    public List<Turno> turnosDelDia(LocalDate fecha,long veterinarioId) throws SQLException {
        if (fecha==null || veterinarioId<0) throw new IllegalArgumentException("Revisá la fecha y el número de veterinario.");
        List<Turno> agenda=turnos.delDia(fecha,veterinarioId);
        Algoritmos.ordenarAgenda(agenda); return agenda;
    }
    public long registrarConsulta(long turnoId,String motivo,String diagnostico,String tratamiento,BigDecimal peso) throws SQLException {
        return consultas.guardar(new Consulta(0,turnoId,motivo,diagnostico,tratamiento,peso,LocalDateTime.now(),""));
    }
    public long registrarVacuna(long consultaId,String nombre,LocalDate aplicada,LocalDate refuerzo) throws SQLException {
        return vacunas.guardar(new Vacuna(0,consultaId,nombre,aplicada,refuerzo));
    }
    public HistoriaClinica historial(long mascotaId) throws SQLException {
        Validacion.id(mascotaId,"mascota");
        return new HistoriaClinica(mascotas.buscar(mascotaId),consultas.deMascota(mascotaId),vacunas.deMascota(mascotaId));
    }
    public List<Cliente> buscarClientes(String criterio)throws SQLException {
        return clientes.buscarPorNombreODni(Validacion.texto(criterio,"nombre o DNI",100));
    }
    public void cancelarTurno(long id,String motivo)throws SQLException {
        turnos.cancelar(Validacion.id(id,"turno"),Validacion.texto(motivo,"motivo de cancelación",250));
    }
    public void reprogramarTurno(long id,LocalDateTime inicio)throws SQLException {
        if(inicio==null)throw new IllegalArgumentException("Falta la nueva fecha del turno.");
        turnos.reprogramar(Validacion.id(id,"turno"),inicio);
    }
    public ReporteConsultas reporte(LocalDate desde,LocalDate hasta)throws SQLException {
        new ReporteConsultas(desde,hasta,List.of());
        return new ReporteConsultas(desde,hasta,consultas.porPeriodo(desde,hasta));
    }
}