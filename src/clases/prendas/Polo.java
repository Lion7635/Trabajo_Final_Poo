package clases.prendas;

public class Polo extends ModeloPrenda {

    public Polo(String codigo, String nombre, String tipoTela, double[] consumoPorTalla,
            int tiempoConfeccion, double precioVenta, double costoCostura, boolean activo) {
        super(codigo, nombre, tipoTela, consumoPorTalla, tiempoConfeccion, precioVenta, costoCostura, activo);
    }

    @Override
    public String getTipo() {
        return "Polo";
    }

    // Polo: tela + costura (+ insumos asignados)
    @Override
    public double calcularCostoProduccion(String talla, double costoMetroTela) {
        return calcularCostoTelaYCostura(talla, costoMetroTela) + calcularCostoInsumos();
    }
}
