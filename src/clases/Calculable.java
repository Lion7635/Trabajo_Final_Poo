package clases;

/**
 * Interfaz para todo concepto de un pedido que tiene precio y costo.
 * La implementan los ítems del pedido (prendas, servicios y paquetes).
 */
public interface Calculable {

    double calcularSubtotal();   // precio de venta (sin IGV)

    double calcularCosto();      // costo de producción
}
