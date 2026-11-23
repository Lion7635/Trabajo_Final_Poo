package clases;

public class Jean extends ModeloPrenda {

    private double costoLavado;   // proceso de lavado por prenda

    public Jean(String codigo, String nombre, String tipoTela, double[] consumoPorTalla,
            int tiempoConfeccion, double precioVenta, double costoCostura, boolean activo, double costoLavado) {
        super(codigo, nombre, tipoTela, consumoPorTalla, tiempoConfeccion, precioVenta, costoCostura, activo);
        this.costoLavado = costoLavado;
    }

    @Override
    public String getTipo() {
        return "Jean";
    }

    // Jean: tela + costura + lavado (+ insumos asignados)
    @Override
    public double calcularCostoProduccion(String talla, double costoMetroTela) {
        return calcularCostoTelaYCostura(talla, costoMetroTela) + costoLavado + calcularCostoInsumos();
    }

    public double getCostoLavado() { return costoLavado; }
    public void setCostoLavado(double costoLavado) { this.costoLavado = costoLavado; }
}
