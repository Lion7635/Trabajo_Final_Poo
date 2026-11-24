package clases.prendas;

import clases.inventario.Insumo;

/**
 * Clase abstracta para los modelos de prendas (RF02).
 * El costo de producción se calcula de forma distinta en cada subclase (polimorfismo).
 */
public abstract class ModeloPrenda {

    public static final String[] TALLAS = {"S", "M", "L", "XL"};

    private String codigo;
    private String nombre;
    private String tipoTela;            // tela principal (tipo); el color se elige en el pedido
    private double[] consumoPorTalla;   // metros de tela por prenda: S, M, L, XL
    private int tiempoConfeccion;       // minutos por prenda
    private double precioVenta;
    private double costoCostura;        // mano de obra por prenda
    private boolean activo;             // true = Activo, false = Descontinuado

    private InsumoRequerido[] insumos;  // arreglo de insumos que usa cada prenda
    private int totalInsumos;

    public ModeloPrenda(String codigo, String nombre, String tipoTela, double[] consumoPorTalla,
            int tiempoConfeccion, double precioVenta, double costoCostura, boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoTela = tipoTela;
        this.consumoPorTalla = consumoPorTalla;
        this.tiempoConfeccion = tiempoConfeccion;
        this.precioVenta = precioVenta;
        this.costoCostura = costoCostura;
        this.activo = activo;
        this.insumos = new InsumoRequerido[10];
        this.totalInsumos = 0;
    }

    public abstract String getTipo();

    /**
     * Método polimórfico: cada tipo de prenda calcula su costo a su manera.
     * @param talla S, M, L o XL
     * @param costoMetroTela costo por metro de la tela usada
     */
    public abstract double calcularCostoProduccion(String talla, double costoMetroTela);

    // Parte común del costo: tela + costura (lo usan las subclases con super)
    protected double calcularCostoTelaYCostura(String talla, double costoMetroTela) {
        return getConsumo(talla) * costoMetroTela + costoCostura;
    }

    // Costo de los insumos (botones, hilos, etiquetas...) de una prenda
    public double calcularCostoInsumos() {
        double costo = 0;
        for (int i = 0; i < totalInsumos; i++) {
            costo += insumos[i].getCantidadPorPrenda() * insumos[i].getInsumo().getCostoUnitario();
        }
        return costo;
    }

    public static int indiceTalla(String talla) {
        for (int i = 0; i < TALLAS.length; i++) {
            if (TALLAS[i].equalsIgnoreCase(talla)) {
                return i;
            }
        }
        return -1;
    }

    public double getConsumo(String talla) {
        int i = indiceTalla(talla);
        if (i == -1) {
            return 0;
        }
        return consumoPorTalla[i];
    }

    // ---- Insumos requeridos (arreglo de objetos) ----
    public boolean agregarInsumo(Insumo insumo, double cantidadPorPrenda) {
        for (int i = 0; i < totalInsumos; i++) {
            if (insumos[i].getInsumo().getCodigo().equalsIgnoreCase(insumo.getCodigo())) {
                insumos[i].setCantidadPorPrenda(cantidadPorPrenda);  // si ya está, se actualiza
                return true;
            }
        }
        if (totalInsumos == insumos.length) {
            return false;
        }
        insumos[totalInsumos] = new InsumoRequerido(insumo, cantidadPorPrenda);
        totalInsumos++;
        return true;
    }

    public boolean quitarInsumo(String codigoInsumo) {
        for (int i = 0; i < totalInsumos; i++) {
            if (insumos[i].getInsumo().getCodigo().equalsIgnoreCase(codigoInsumo)) {
                for (int j = i; j < totalInsumos - 1; j++) {
                    insumos[j] = insumos[j + 1];
                }
                insumos[totalInsumos - 1] = null;
                totalInsumos--;
                return true;
            }
        }
        return false;
    }

    public double getCantidadInsumo(Insumo insumo) {
        for (int i = 0; i < totalInsumos; i++) {
            if (insumos[i].getInsumo() == insumo) {
                return insumos[i].getCantidadPorPrenda();
            }
        }
        return 0;
    }

    public void limpiarInsumos() {
        for (int i = 0; i < totalInsumos; i++) {
            insumos[i] = null;
        }
        totalInsumos = 0;
    }

    public boolean usaInsumo(Insumo insumo) {
        return getCantidadInsumo(insumo) > 0;
    }

    public void copiarInsumosDe(ModeloPrenda otro) {
        for (int i = 0; i < otro.getTotalInsumos(); i++) {
            agregarInsumo(otro.getInsumos()[i].getInsumo(), otro.getInsumos()[i].getCantidadPorPrenda());
        }
    }

    public String getEstado() {
        return activo ? "Activo" : "Descontinuado";
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoTela() { return tipoTela; }
    public void setTipoTela(String tipoTela) { this.tipoTela = tipoTela; }
    public double[] getConsumoPorTalla() { return consumoPorTalla; }
    public void setConsumoPorTalla(double[] consumoPorTalla) { this.consumoPorTalla = consumoPorTalla; }
    public int getTiempoConfeccion() { return tiempoConfeccion; }
    public void setTiempoConfeccion(int tiempoConfeccion) { this.tiempoConfeccion = tiempoConfeccion; }
    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public double getCostoCostura() { return costoCostura; }
    public void setCostoCostura(double costoCostura) { this.costoCostura = costoCostura; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public InsumoRequerido[] getInsumos() { return insumos; }
    public int getTotalInsumos() { return totalInsumos; }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + getTipo() + ")";
    }
}
