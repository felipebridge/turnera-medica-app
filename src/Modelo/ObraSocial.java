package Modelo;

public class ObraSocial {
    private int id;
    private String nombre;
    private double descuento; // Ej: 0 a 100 (porcentaje)

    public ObraSocial() {}

    public ObraSocial(String nombre, double descuento) {
        this.nombre = nombre;
        this.descuento = descuento;
    }

    public ObraSocial(int id, String nombre, double descuento) {
        this.id = id;
        this.nombre = nombre;
        this.descuento = descuento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }

    @Override
    public String toString() {
        return nombre + " (" + descuento + "%)";
    }
}
