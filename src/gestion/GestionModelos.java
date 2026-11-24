package gestion;

import clases.prendas.Casaca;
import clases.prendas.Jean;
import clases.prendas.ModeloPrenda;
import utilitario.Validador;

/**
 * RF02 - Gestión de modelos de prendas.
 * El arreglo es de tipo ModeloPrenda (clase padre) y guarda Polos, Jeans y Casacas.
 */
public class GestionModelos {

    private ModeloPrenda[] modelos;
    private int totalModelos;

    public GestionModelos() {
        modelos = new ModeloPrenda[100];
        totalModelos = 0;
    }

    public ModeloPrenda buscarPorCodigo(String codigo) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getCodigo().equalsIgnoreCase(codigo)) {
                return modelos[i];
            }
        }
        return null;
    }

    private String validarDatos(ModeloPrenda m) {
        if (Validador.esVacio(m.getCodigo()) || Validador.esVacio(m.getNombre())) {
            return "Código y nombre son obligatorios.";
        }
        if (Validador.esVacio(m.getTipoTela())) {
            return "Seleccione la tela principal.";
        }
        for (int i = 0; i < m.getConsumoPorTalla().length; i++) {
            if (m.getConsumoPorTalla()[i] <= 0) {
                return "El consumo de tela de la talla " + ModeloPrenda.TALLAS[i] + " debe ser mayor a 0.";
            }
        }
        if (m.getTiempoConfeccion() <= 0) {
            return "El tiempo de confección debe ser mayor a 0.";
        }
        if (m.getPrecioVenta() <= 0 || m.getCostoCostura() < 0) {
            return "El precio de venta debe ser mayor a 0 y el costo de costura no negativo.";
        }
        return null;
    }

    public String registrar(ModeloPrenda m) {
        String error = validarDatos(m);
        if (error != null) {
            return error;
        }
        if (totalModelos == modelos.length) {
            return "No hay espacio para más modelos.";
        }
        if (buscarPorCodigo(m.getCodigo()) != null) {
            return "Ya existe un modelo con el código " + m.getCodigo() + ".";
        }
        modelos[totalModelos] = m;
        totalModelos++;
        return null;
    }

    public String actualizar(ModeloPrenda datos, GestionPedidos gestionPedidos, GestionPaquetes gestionPaquetes) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        for (int i = 0; i < totalModelos; i++) {
            ModeloPrenda m = modelos[i];
            if (m.getCodigo().equalsIgnoreCase(datos.getCodigo())) {
                if (m.getClass() != datos.getClass()) {
                    if (gestionPedidos.usaModelo(m) || gestionPaquetes.usaModelo(m)) {
                        return "No se puede cambiar el tipo: el modelo está en pedidos o paquetes.";
                    }
                    modelos[i] = datos;
                    return null;
                }
                m.setNombre(datos.getNombre());
                m.setTipoTela(datos.getTipoTela());
                m.setConsumoPorTalla(datos.getConsumoPorTalla());
                m.setTiempoConfeccion(datos.getTiempoConfeccion());
                m.setPrecioVenta(datos.getPrecioVenta());
                m.setCostoCostura(datos.getCostoCostura());
                m.setActivo(datos.isActivo());
                m.limpiarInsumos();
                m.copiarInsumosDe(datos);
                // Atributos propios de cada subclase (casting)
                if (m instanceof Jean) {
                    ((Jean) m).setCostoLavado(((Jean) datos).getCostoLavado());
                }
                if (m instanceof Casaca) {
                    ((Casaca) m).setCostoForro(((Casaca) datos).getCostoForro());
                    ((Casaca) m).setCostoAvios(((Casaca) datos).getCostoAvios());
                }
                return null;
            }
        }
        return "No existe un modelo con el código " + datos.getCodigo() + ".";
    }

    public String eliminar(String codigo, GestionPedidos gestionPedidos, GestionPaquetes gestionPaquetes) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getCodigo().equalsIgnoreCase(codigo)) {
                if (gestionPedidos.usaModelo(modelos[i])) {
                    return "No se puede eliminar: el modelo está en pedidos. Márquelo como Descontinuado.";
                }
                if (gestionPaquetes.usaModelo(modelos[i])) {
                    return "No se puede eliminar: el modelo forma parte de un paquete.";
                }
                for (int j = i; j < totalModelos - 1; j++) {
                    modelos[j] = modelos[j + 1];
                }
                modelos[totalModelos - 1] = null;
                totalModelos--;
                return null;
            }
        }
        return "No existe un modelo con el código " + codigo + ".";
    }

    public ModeloPrenda[] listarActivos() {
        int c = 0;
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].isActivo()) {
                c++;
            }
        }
        ModeloPrenda[] activos = new ModeloPrenda[c];
        int k = 0;
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].isActivo()) {
                activos[k] = modelos[i];
                k++;
            }
        }
        return activos;
    }

    public boolean usaTipoTela(String tipoTela) {
        for (int i = 0; i < totalModelos; i++) {
            if (modelos[i].getTipoTela().equalsIgnoreCase(tipoTela)) {
                return true;
            }
        }
        return false;
    }

    public ModeloPrenda[] getModelos() { return modelos; }
    public int getTotalModelos() { return totalModelos; }
}
