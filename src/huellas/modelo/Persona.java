package huellas.modelo;
public abstract class Persona {
    private final long id;
    private final String nombre;
    protected Persona(long id, String nombre) {
        this.id = Validacion.idNuevo(id);
        this.nombre = Validacion.texto(nombre, "nombre", 100);
    }
    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public abstract String descripcion();
}
