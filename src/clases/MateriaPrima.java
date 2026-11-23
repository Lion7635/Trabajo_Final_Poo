package clases;

/**
 * Clase abstracta para los recursos del almacén (RF04).
 * Implementa la interfaz Inventariable: el control de stock es igual para telas e insumos.
 */
public abstract class MateriaPrima implements Inventariable {

    protected String codigo;
    protected double stock;          // metros (tela) o unidades (insumo)
    protected double stockMinimo;
    protected double costoUnitario;  // costo por metro o por unidad

    public MateriaPrima(String codigo, double stock, double stockMinimo, double costoUnitario) {
        this.codigo = codigo;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.costoUnitario = costoUnitario;
    }

    public abstract String getDescripcion();

    public abstract String getUnidad();

    @Override
    public boolean haySuficiente(double cantidad) {
        return stock >= cantidad;
    }

    @Override
    public void descontar(double cantidad) {
        stock = stock - cantidad;
    }

    @Override
    public boolean estaBajoMinimo() {
        return stock < stockMinimo;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }
    public double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(double stockMinimo) { this.stockMinimo = stockMinimo; }
    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }

    @Override
    public String toString() {
        return codigo + " - " + getDescripcion();
    }
}
