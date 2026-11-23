package clases;

/**
 * Línea de pedido por modelo, color (tela) y talla.
 */
public class DetallePrenda extends ItemPedido {

    private ModeloPrenda modelo;
    private Tela tela;      // tela del tipo del modelo en el color elegido
    private String talla;

    public DetallePrenda(ModeloPrenda modelo, Tela tela, String talla, int cantidad) {
        super(cantidad);
        this.modelo = modelo;
        this.tela = tela;
        this.talla = talla;
    }

    @Override
    public String getTipoItem() {
        return "Prenda";
    }

    @Override
    public String getDescripcion() {
        return modelo.getNombre() + " - " + tela.getColor() + " - Talla " + talla;
    }

    @Override
    public double getPrecioUnitario() {
        return modelo.getPrecioVenta();
    }

    @Override
    public double calcularSubtotal() {
        return modelo.getPrecioVenta() * cantidad;
    }

    // Usa el método polimórfico del modelo (Polo, Jean o Casaca)
    @Override
    public double calcularCosto() {
        return modelo.calcularCostoProduccion(talla, tela.getCostoMetro()) * cantidad;
    }

    @Override
    public DetallePrenda[] getPrendas() {
        DetallePrenda[] prendas = {this};
        return prendas;
    }

    @Override
    public boolean requiereAcabado() {
        return false;
    }

    @Override
    public boolean incluyeLogo() {
        return false;
    }

    public ModeloPrenda getModelo() { return modelo; }
    public Tela getTela() { return tela; }
    public String getTalla() { return talla; }
    public String getColor() { return tela.getColor(); }
}
