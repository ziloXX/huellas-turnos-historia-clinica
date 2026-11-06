package huellas.modelo;
public final class Veterinario extends Persona {
    private final String matricula;
    public Veterinario(long id, String nombre, String matricula) {
        super(id, nombre);
        this.matricula = Validacion.texto(matricula, "matrícula", 30);
    }
    public String getMatricula() { return matricula; }
    @Override public String descripcion() { return getId() + " | " + getNombre() + " | matrícula " + matricula; }
}
