package gestion;

import clases.inventario.Insumo;
import utilitario.Validador;

/**
 * RF04 - Gestión de insumos y avíos.
 */
public class GestionInsumos {

    private Insumo[] insumos;
    private int totalInsumos;

    public GestionInsumos() {
        insumos = new Insumo[100];
        totalInsumos = 0;
    }

    public Insumo buscarPorCodigo(String codigo) {
        for (int i = 0; i < totalInsumos; i++) {
            if (insumos[i].getCodigo().equalsIgnoreCase(codigo)) {
                return insumos[i];
            }
        }
        return null;
    }

    private String validarDatos(Insumo in) {
        if (Validador.esVacio(in.getCodigo()) || Validador.esVacio(in.getNombre()) || Validador.esVacio(in.getUnidad())) {
            return "Código, nombre y unidad son obligatorios.";
        }
        if (in.getStock() < 0 || in.getStockMinimo() < 0) {
            return "El stock y el stock mínimo no pueden ser negativos.";
        }
        if (in.getCostoUnitario() < 0) {
            return "El costo unitario no puede ser negativo.";
        }
        return null;
    }

    public String registrar(Insumo in) {
        String error = validarDatos(in);
        if (error != null) {
            return error;
        }
        if (totalInsumos == insumos.length) {
            return "No hay espacio para más insumos.";
        }
        if (buscarPorCodigo(in.getCodigo()) != null) {
            return "Ya existe un insumo con el código " + in.getCodigo() + ".";
        }
        insumos[totalInsumos] = in;
        totalInsumos++;
        return null;
    }

    public String actualizar(Insumo datos) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        Insumo in = buscarPorCodigo(datos.getCodigo());
        if (in == null) {
            return "No existe un insumo con el código " + datos.getCodigo() + ".";
        }
        in.setNombre(datos.getNombre());
        in.setUnidad(datos.getUnidad());
        in.setStock(datos.getStock());
        in.setStockMinimo(datos.getStockMinimo());
        in.setCostoUnitario(datos.getCostoUnitario());
        return null;
    }

    public String eliminar(String codigo, GestionModelos gestionModelos) {
        for (int i = 0; i < totalInsumos; i++) {
            if (insumos[i].getCodigo().equalsIgnoreCase(codigo)) {
                for (int k = 0; k < gestionModelos.getTotalModelos(); k++) {
                    if (gestionModelos.getModelos()[k].usaInsumo(insumos[i])) {
                        return "No se puede eliminar: lo usa el modelo " + gestionModelos.getModelos()[k].getNombre() + ".";
                    }
                }
                for (int j = i; j < totalInsumos - 1; j++) {
                    insumos[j] = insumos[j + 1];
                }
                insumos[totalInsumos - 1] = null;
                totalInsumos--;
                return null;
            }
        }
        return "No existe un insumo con el código " + codigo + ".";
    }

    public Insumo[] getInsumos() { return insumos; }
    public int getTotalInsumos() { return totalInsumos; }
}
