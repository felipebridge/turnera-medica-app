package Modelo;

public class Paciente extends Usuario {

    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
    private ObraSocial obraSocial;

    public Paciente() { }

    // Para CREATE (sin id)
    public Paciente(boolean activo,
                    String nombre,
                    String apellido,
                    String dni,
                    String telefono,
                    String email,
                    ObraSocial obraSocial) {
        super(activo);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
        this.obraSocial = obraSocial;
    }

    // Para READ (con id)
    public Paciente(int id,
                    boolean activo,
                    String nombre,
                    String apellido,
                    String dni,
                    String telefono,
                    String email,
                    ObraSocial obraSocial) {
        super(id, activo);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
        this.obraSocial = obraSocial;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ObraSocial getObraSocial() { return obraSocial; }
    public void setObraSocial(ObraSocial obraSocial) { this.obraSocial = obraSocial; }

    @Override
    public String getTipoUsuario() {
        return "PACIENTE";
    }

    @Override
    public String toString() {
        return apellido + ", " + nombre + " (DNI " + dni + ")";
    }
}
