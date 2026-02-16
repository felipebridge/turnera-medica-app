package Modelo;

public abstract class Usuario {
    protected int id;
    protected boolean activo;

    public Usuario() {}

    public Usuario(boolean activo) {
        this.activo = activo;
    }

    public Usuario(int id, boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public abstract String getTipoUsuario();
}
