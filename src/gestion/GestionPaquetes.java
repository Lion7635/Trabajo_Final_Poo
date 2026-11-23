package gestion;

import clases.ModeloPrenda;
import clases.Paquete;
import clases.ServicioAcabado;
import utilitario.Validador;

/**
 * RF04 - Gestión de paquetes promocionales.
 */
public class GestionPaquetes {

    private Paquete[] paquetes;
    private int totalPaquetes;

    public GestionPaquetes() {
        paquetes = new Paquete[50];
        totalPaquetes = 0;
    }

    public Paquete buscarPorCodigo(String codigo) {
        for (int i = 0; i < totalPaquetes; i++) {
            if (paquetes[i].getCodigo().equalsIgnoreCase(codigo)) {
                return paquetes[i];
            }
        }
        return null;
    }

    private String validarDatos(Paquete p) {
        if (Validador.esVacio(p.getCodigo()) || Validador.esVacio(p.getNombre())) {
            return "Código y nombre son obligatorios.";
        }
        if (p.getPrecioPromocional() <= 0) {
            return "El precio promocional debe ser mayor a 0.";
        }
        if (p.getTotalModelos() == 0) {
            return "El paquete debe incluir al menos una prenda.";
        }
        return null;
    }

    public String registrar(Paquete p) {
        String error = validarDatos(p);
        if (error != null) {
            return error;
        }
        if (totalPaquetes == paquetes.length) {
            return "No hay espacio para más paquetes.";
        }
        if (buscarPorCodigo(p.getCodigo()) != null) {
            return "Ya existe un paquete con el código " + p.getCodigo() + ".";
        }
        paquetes[totalPaquetes] = p;
        totalPaquetes++;
        return null;
    }

    // Se reemplaza el objeto: los pedidos ya registrados conservan el paquete con el que se vendieron
    public String actualizar(Paquete datos) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        for (int i = 0; i < totalPaquetes; i++) {
            if (paquetes[i].getCodigo().equalsIgnoreCase(datos.getCodigo())) {
                paquetes[i] = datos;
                return null;
            }
        }
        return "No existe un paquete con el código " + datos.getCodigo() + ".";
    }

    public String eliminar(String codigo) {
        for (int i = 0; i < totalPaquetes; i++) {
            if (paquetes[i].getCodigo().equalsIgnoreCase(codigo)) {
                for (int j = i; j < totalPaquetes - 1; j++) {
                    paquetes[j] = paquetes[j + 1];
                }
                paquetes[totalPaquetes - 1] = null;
                totalPaquetes--;
                return null;
            }
        }
        return "No existe un paquete con el código " + codigo + ".";
    }

    public boolean usaModelo(ModeloPrenda modelo) {
        for (int i = 0; i < totalPaquetes; i++) {
            if (paquetes[i].contieneModelo(modelo)) {
                return true;
            }
        }
        return false;
    }

    public boolean usaServicio(ServicioAcabado servicio) {
        for (int i = 0; i < totalPaquetes; i++) {
            if (paquetes[i].contieneServicio(servicio)) {
                return true;
            }
        }
        return false;
    }

    public Paquete[] getPaquetes() { return paquetes; }
    public int getTotalPaquetes() { return totalPaquetes; }
}
