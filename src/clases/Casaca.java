package clases;

public class Casaca extends ModeloPrenda {

    private double costoForro;   // forro por prenda
    private double costoAvios;   // cierres y broches por prenda

    public Casaca(String codigo, String nombre, String tipoTela, double[] consumoPorTalla,
            int tiempoConfeccion, double precioVenta, double costoCostura, boolean activo,
            double costoForro, double costoAvios) {
        super(codigo, nombre, tipoTela, consumoPorTalla, tiempoConfeccion, precioVenta, costoCostura, activo);
        this.costoForro = costoForro;
        this.costoAvios = costoAvios;
    }

    @Override
    public String getTipo() {
        return "Casaca";
    }

    // Casaca: tela + costura + forro + avíos (+ insumos asignados)
    @Override
    public double calcularCostoProduccion(String talla, double costoMetroTela) {
        return calcularCostoTelaYCostura(talla, costoMetroTela) + costoForro + costoAvios + calcularCostoInsumos();
    }

    public double getCostoForro() { return costoForro; }
    public void setCostoForro(double costoForro) { this.costoForro = costoForro; }
    public double getCostoAvios() { return costoAvios; }
    public void setCostoAvios(double costoAvios) { this.costoAvios = costoAvios; }
}
