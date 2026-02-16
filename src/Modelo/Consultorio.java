package Modelo;

public class Consultorio {
    private int id;
    private String numero;
    private String piso;
    private String descripcion;

    public Consultorio() {}

    public Consultorio(String numero, String piso, String descripcion) {
        this.numero = numero;
        this.piso = piso;
        this.descripcion = descripcion;
    }

    public Consultorio(int id, String numero, String piso, String descripcion) {
        this.id = id;
        this.numero = numero;
        this.piso = piso;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "Consultorio " + numero + (piso != null && !piso.isBlank() ? " (Piso " + piso + ")" : "");
    }
}
