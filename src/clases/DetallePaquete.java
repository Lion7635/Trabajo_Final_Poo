package clases;

/**
 * Paquete promocional pedido en una talla y un color.
 */
public class DetallePaquete extends ItemPedido {

    private Paquete paquete;
    private String talla;
    private String color;
    private Tela[] telas;   // tela de cada modelo del paquete en el color elegido (mismo orden)

    public DetallePaquete(Paquete paquete, String talla, String color, Tela[] telas, int cantidad) {
        super(cantidad);
        this.paquete = paquete;
        this.talla = talla;
        this.color = color;
        this.telas = telas;
    }

    @Override
    public String getTipoItem() {
        return "Paquete";
    }

    @Override
    public String getDescripcion() {
        return paquete.getNombre() + " - " + color + " - Talla " + talla + " [" + paquete.getContenido() + "]";
    }

    @Override
    public double getPrecioUnitario() {
        return paquete.getPrecioPromocional();
    }

    // Se respeta el precio promocional cerrado
    @Override
    public double calcularSubtotal() {
        return paquete.getPrecioPromocional() * cantidad;
    }

    @Override
    public double calcularCosto() {
        double costo = 0;
        DetallePrenda[] prendas = getPrendas();
        for (int i = 0; i < prendas.length; i++) {
            costo += prendas[i].calcularCosto();
        }
        int totalPrendas = getTotalPrendas();
        for (int i = 0; i < paquete.getTotalServicios(); i++) {
            costo += paquete.getServicios()[i].getCostoPorPrenda() * totalPrendas;
        }
        return costo;
    }

    // Convierte el paquete en sus prendas: cantidad de paquetes x prendas de cada modelo
    @Override
    public DetallePrenda[] getPrendas() {
        DetallePrenda[] prendas = new DetallePrenda[paquete.getTotalModelos()];
        for (int i = 0; i < paquete.getTotalModelos(); i++) {
            prendas[i] = new DetallePrenda(paquete.getModelos()[i], telas[i], talla,
                    paquete.getCantidades()[i] * cantidad);
        }
        return prendas;
    }

    @Override
    public boolean requiereAcabado() {
        for (int i = 0; i < paquete.getTotalServicios(); i++) {
            if (paquete.getServicios()[i].isRequiereEtapaAcabado()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean incluyeLogo() {
        for (int i = 0; i < paquete.getTotalServicios(); i++) {
            if (paquete.getServicios()[i].isConLogo()) {
                return true;
            }
        }
        return false;
    }

    public Paquete getPaquete() { return paquete; }
    public String getTalla() { return talla; }
    public String getColor() { return color; }
}
