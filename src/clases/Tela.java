package clases;

public class Tela extends MateriaPrima {

    private String tipo;   // Ej. Jersey Algodón Pima, Denim 12 oz
    private String color;

    public Tela(String codigo, String tipo, String color, double metrosStock, double stockMinimo, double costoMetro) {
        super(codigo, metrosStock, stockMinimo, costoMetro);
        this.tipo = tipo;
        this.color = color;
    }

    @Override
    public String getDescripcion() {
        return tipo + " color " + color;
    }

    @Override
    public String getUnidad() {
        return "m";
    }

    public double getCostoMetro() { return costoUnitario; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
