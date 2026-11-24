package clases.interfaces;

/**
 * Interfaz para los recursos que se controlan en el almacén (telas e insumos).
 * Define el comportamiento común del inventario.
 */
public interface Inventariable {

    boolean haySuficiente(double cantidad);

    void descontar(double cantidad);

    boolean estaBajoMinimo();
}
