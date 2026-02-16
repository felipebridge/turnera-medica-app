package Modelo;

public class Medico extends Usuario {

    private String nombre;
    private String apellido;
    private String matricula;
    private String especialidad;
    private double honorario;

    public Medico() { }

    // CREATE
    public Medico(boolean activo,
                  String nombre,
                  String apellido,
                  String matricula,
                  String especialidad,
                  double honorario) {

        super(activo);
        this.nombre = nombre;
        this.apellido = apellido;
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.honorario = honorario;
    }

    // UPDATE / READ
    public Medico(int id,
                  boolean activo,
                  String nombre,
                  String apellido,
                  String matricula,
                  String especialidad,
                  double honorario) {

        super(id, activo);
        this.nombre = nombre;
        this.apellido = apellido;
        this.matricula = matricula;
        this.especialidad = especialidad;
        this.honorario = honorario;
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getMatricula() { return matricula; }
    public String getEspecialidad() { return especialidad; }
    public double getHonorario() { return honorario; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public void setHonorario(double honorario) { this.honorario = honorario; }

    @Override
    public String getTipoUsuario() {
        return "MEDICO";
    }

    @Override
    public String toString() {
        return apellido + ", " + nombre;
    }
}
