public final class Cliente extends Persona {
    private final String dni;
    private final String telefono;
    private final String correo;
    public Cliente(long id, String nombre, String dni, String telefono, String correo) {
        super(id, nombre);
        this.dni = Validacion.texto(dni, "DNI", 15);
        this.telefono = Validacion.texto(telefono, "teléfono", 30);
        this.correo = Validacion.opcional(correo, "correo", 150);
    }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    @Override public String descripcion() { return getId() + " | " + getNombre() + " | DNI " + dni; }
}
