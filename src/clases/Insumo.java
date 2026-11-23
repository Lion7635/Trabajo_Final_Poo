package clases;

/**
 * Insumo o avío: botones, cierres, etiquetas, hilos, etc.
 */
public class Insumo extends MateriaPrima {

    private String nombre;
    private String unidad;  // unid, cono, m

    public Insumo(String codigo, String nombre, String unidad, double stock, double stockMinimo, double costoUnitario) {
        super(codigo, stock, stockMinimo, costoUnitario);
        this.nombre = nombre;
        this.unidad = unidad;
    }

    @Override
    public String getDescripcion() {
        return nombre;
    }

    @Override
    public String getUnidad() {
        return unidad;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}
