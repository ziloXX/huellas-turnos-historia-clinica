package huellas.modelo;
import java.time.LocalDate;
public final class Mascota {
    private final long id, clienteId;
    private final String nombre, raza, alergias;
    private final Especie especie;
    private final Sexo sexo;
    private final LocalDate nacimiento;
    private final boolean activo;
    public Mascota(long id, long clienteId, String nombre, Especie especie, String raza,
                   Sexo sexo, LocalDate nacimiento, String alergias, boolean activo) {
        this.id = Validacion.idNuevo(id);
        this.clienteId = Validacion.id(clienteId, "cliente");
        this.nombre = Validacion.texto(nombre, "nombre de la mascota", 80);
        if (especie == null || sexo == null) throw new IllegalArgumentException("Completá especie y sexo.");
        if (nacimiento != null && nacimiento.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        this.especie=especie; this.sexo=sexo; this.nacimiento=nacimiento; this.activo=activo;
        this.raza=Validacion.opcional(raza,"raza",80);
        this.alergias=Validacion.opcional(alergias,"alergias",16000);
    }
    public long getId() { return id; }
    public long getClienteId() { return clienteId; }
    public String getNombre() { return nombre; }
    public Especie getEspecie() { return especie; }
    public String getRaza() { return raza; }
    public Sexo getSexo() { return sexo; }
    public LocalDate getNacimiento() { return nacimiento; }
    public String getAlergias() { return alergias; }
    public boolean isActivo() { return activo; }
}
