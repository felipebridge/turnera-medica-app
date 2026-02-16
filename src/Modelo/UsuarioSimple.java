package Modelo;

public class UsuarioSimple extends Usuario {

    private String tipo;

    public UsuarioSimple() { }

    public UsuarioSimple(String tipo, boolean activo) {
        super(activo);
        this.tipo = tipo;
    }

    public UsuarioSimple(int id, String tipo, boolean activo) {
        super(id, activo);
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String getTipoUsuario() {
        return tipo;
    }
}
