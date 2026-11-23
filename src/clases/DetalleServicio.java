package clases;

/**
 * Servicio de acabado aplicado a una cantidad de prendas del pedido.
 */
public class DetalleServicio extends ItemPedido {

    private ServicioAcabado servicio;

    public DetalleServicio(ServicioAcabado servicio, int cantidadPrendas) {
        super(cantidadPrendas);
        this.servicio = servicio;
    }

    @Override
    public String getTipoItem() {
        return "Servicio";
    }

    @Override
    public String getDescripcion() {
        return servicio.getNombre() + " (" + cantidad + " prendas)";
    }

    @Override
    public double getPrecioUnitario() {
        return servicio.getPrecioPorPrenda();
    }

    @Override
    public double calcularSubtotal() {
        return servicio.getPrecioPorPrenda() * cantidad;
    }

    @Override
    public double calcularCosto() {
        return servicio.getCostoPorPrenda() * cantidad;
    }

    // Un servicio no agrega prendas nuevas
    @Override
    public DetallePrenda[] getPrendas() {
        return new DetallePrenda[0];
    }

    @Override
    public boolean requiereAcabado() {
        return servicio.isRequiereEtapaAcabado();
    }

    @Override
    public boolean incluyeLogo() {
        return servicio.isConLogo();
    }

    public ServicioAcabado getServicio() { return servicio; }
}
