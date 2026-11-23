package clases;

/**
 * Servicio de acabado: estampado, bordado de logo, empaque personalizado (RF04).
 */
public class ServicioAcabado {

    private String codigo;
    private String nombre;
    private double precioPorPrenda;
    private double costoPorPrenda;
    private boolean requiereEtapaAcabado;  // estampado o bordado -> el pedido pasa por "En Acabado"
    private boolean conLogo;               // aplica el precio especial institucional

    public ServicioAcabado(String codigo, String nombre, double precioPorPrenda, double costoPorPrenda,
            boolean requiereEtapaAcabado, boolean conLogo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioPorPrenda = precioPorPrenda;
        this.costoPorPrenda = costoPorPrenda;
        this.requiereEtapaAcabado = requiereEtapaAcabado;
        this.conLogo = conLogo;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecioPorPrenda() { return precioPorPrenda; }
    public void setPrecioPorPrenda(double precioPorPrenda) { this.precioPorPrenda = precioPorPrenda; }
    public double getCostoPorPrenda() { return costoPorPrenda; }
    public void setCostoPorPrenda(double costoPorPrenda) { this.costoPorPrenda = costoPorPrenda; }
    public boolean isRequiereEtapaAcabado() { return requiereEtapaAcabado; }
    public void setRequiereEtapaAcabado(boolean requiereEtapaAcabado) { this.requiereEtapaAcabado = requiereEtapaAcabado; }
    public boolean isConLogo() { return conLogo; }
    public void setConLogo(boolean conLogo) { this.conLogo = conLogo; }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
