package clases.pedidos;

import clases.interfaces.Calculable;
import clases.inventario.Insumo;
import clases.inventario.Tela;

/**
 * Clase abstracta para cada línea del pedido (RF05).
 * Subclases: DetallePrenda, DetalleServicio, DetallePaquete.
 * La factura recorre el arreglo de ítems y llama a sus métodos sin saber de qué tipo son (polimorfismo).
 */
public abstract class ItemPedido implements Calculable {

    protected int cantidad;

    public ItemPedido(int cantidad) {
        this.cantidad = cantidad;
    }

    public abstract String getTipoItem();          // Prenda, Servicio o Paquete

    public abstract String getDescripcion();

    public abstract double getPrecioUnitario();

    // Prendas físicas que hay que fabricar por este ítem (un servicio no tiene prendas)
    public abstract DetallePrenda[] getPrendas();

    public abstract boolean requiereAcabado();    // estampado o bordado

    public abstract boolean incluyeLogo();

    public int getTotalPrendas() {
        int total = 0;
        DetallePrenda[] prendas = getPrendas();
        for (int i = 0; i < prendas.length; i++) {
            total += prendas[i].getCantidad();
        }
        return total;
    }

    // Metros netos (sin merma) de una tela que necesita este ítem
    public double getMetrosTela(Tela tela) {
        double metros = 0;
        DetallePrenda[] prendas = getPrendas();
        for (int i = 0; i < prendas.length; i++) {
            if (prendas[i].getTela() == tela) {
                metros += prendas[i].getModelo().getConsumo(prendas[i].getTalla()) * prendas[i].getCantidad();
            }
        }
        return metros;
    }

    // Cantidad de un insumo que necesita este ítem
    public double getCantidadInsumo(Insumo insumo) {
        double total = 0;
        DetallePrenda[] prendas = getPrendas();
        for (int i = 0; i < prendas.length; i++) {
            total += prendas[i].getModelo().getCantidadInsumo(insumo) * prendas[i].getCantidad();
        }
        return total;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
