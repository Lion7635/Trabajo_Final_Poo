package clases.catalogo;

import clases.prendas.ModeloPrenda;

/**
 * Paquete promocional (RF04): se compone de modelos de prendas y servicios
 * ya existentes (agregación) y tiene un precio promocional cerrado.
 */
public class Paquete {

    private String codigo;
    private String nombre;
    private double precioPromocional;   // precio cerrado por paquete (sin IGV)

    private ModeloPrenda[] modelos;     // prendas incluidas
    private int[] cantidades;           // cuántas prendas de cada modelo
    private int totalModelos;

    private ServicioAcabado[] servicios;  // servicios incluidos (se aplican a todas las prendas)
    private int totalServicios;

    public Paquete(String codigo, String nombre, double precioPromocional) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioPromocional = precioPromocional;
        this.modelos = new ModeloPrenda[10];
        this.cantidades = new int[10];
        this.totalModelos = 0;
        this.servicios = new ServicioAcabado[10];
        this.totalServicios = 0;
    }

    public boolean agregarModelo(ModeloPrenda modelo, int cantidad) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i] == modelo) {
                cantidades[i] = cantidad;
                return true;
            }
        }
        if (totalModelos == modelos.length) {
            return false;
        }
        modelos[totalModelos] = modelo;
        cantidades[totalModelos] = cantidad;
        totalModelos++;
        return true;
    }

    public boolean quitarModelo(String codigoModelo) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getCodigo().equalsIgnoreCase(codigoModelo)) {
                for (int j = i; j < totalModelos - 1; j++) {
                    modelos[j] = modelos[j + 1];
                    cantidades[j] = cantidades[j + 1];
                }
                modelos[totalModelos - 1] = null;
                totalModelos--;
                return true;
            }
        }
        return false;
    }

    public boolean agregarServicio(ServicioAcabado servicio) {
        for (int i = 0; i < totalServicios; i++) {
            if (servicios[i] == servicio) {
                return true;
            }
        }
        if (totalServicios == servicios.length) {
            return false;
        }
        servicios[totalServicios] = servicio;
        totalServicios++;
        return true;
    }

    public boolean quitarServicio(String codigoServicio) {
        for (int i = 0; i < totalServicios; i++) {
            if (servicios[i].getCodigo().equalsIgnoreCase(codigoServicio)) {
                for (int j = i; j < totalServicios - 1; j++) {
                    servicios[j] = servicios[j + 1];
                }
                servicios[totalServicios - 1] = null;
                totalServicios--;
                return true;
            }
        }
        return false;
    }

    // Prendas que trae un paquete
    public int getPrendasPorPaquete() {
        int total = 0;
        for (int i = 0; i < totalModelos; i++) {
            total += cantidades[i];
        }
        return total;
    }

    // Un paquete no se puede vender si alguno de sus modelos está descontinuado
    public boolean estaDisponible() {
        if (totalModelos == 0) {
            return false;
        }
        for (int i = 0; i < totalModelos; i++) {
            if (!modelos[i].isActivo()) {
                return false;
            }
        }
        return true;
    }

    public boolean contieneModelo(ModeloPrenda modelo) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i] == modelo) {
                return true;
            }
        }
        return false;
    }

    public boolean contieneServicio(ServicioAcabado servicio) {
        for (int i = 0; i < totalServicios; i++) {
            if (servicios[i] == servicio) {
                return true;
            }
        }
        return false;
    }

    // Precio que tendrían las prendas y servicios si se compraran por separado
    public double getPrecioRegular() {
        double total = 0;
        for (int i = 0; i < totalModelos; i++) {
            total += modelos[i].getPrecioVenta() * cantidades[i];
        }
        for (int i = 0; i < totalServicios; i++) {
            total += servicios[i].getPrecioPorPrenda() * getPrendasPorPaquete();
        }
        return total;
    }

    public String getContenido() {
        String texto = "";
        for (int i = 0; i < totalModelos; i++) {
            texto += cantidades[i] + " " + modelos[i].getNombre();
            if (i < totalModelos - 1) {
                texto += ", ";
            }
        }
        for (int i = 0; i < totalServicios; i++) {
            texto += " + " + servicios[i].getNombre();
        }
        return texto;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecioPromocional() { return precioPromocional; }
    public void setPrecioPromocional(double precioPromocional) { this.precioPromocional = precioPromocional; }
    public ModeloPrenda[] getModelos() { return modelos; }
    public int[] getCantidades() { return cantidades; }
    public int getTotalModelos() { return totalModelos; }
    public ServicioAcabado[] getServicios() { return servicios; }
    public int getTotalServicios() { return totalServicios; }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
