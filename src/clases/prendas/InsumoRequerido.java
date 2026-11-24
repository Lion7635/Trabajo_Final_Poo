package clases.prendas;

import clases.inventario.Insumo;

/**
 * Cantidad de un insumo que consume una prenda de un modelo.
 * Ej. una Casaca usa 1 cierre y 4 broches por prenda.
 */
public class InsumoRequerido {

    private Insumo insumo;
    private double cantidadPorPrenda;

    public InsumoRequerido(Insumo insumo, double cantidadPorPrenda) {
        this.insumo = insumo;
        this.cantidadPorPrenda = cantidadPorPrenda;
    }

    public Insumo getInsumo() { return insumo; }
    public double getCantidadPorPrenda() { return cantidadPorPrenda; }
    public void setCantidadPorPrenda(double cantidadPorPrenda) { this.cantidadPorPrenda = cantidadPorPrenda; }
}
