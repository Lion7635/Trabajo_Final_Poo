package gestion;

import clases.catalogo.ServicioAcabado;
import utilitario.Validador;

/**
 * RF04 - Gestión de servicios de acabado.
 */
public class GestionServicios {

    private ServicioAcabado[] servicios;
    private int totalServicios;

    public GestionServicios() {
        servicios = new ServicioAcabado[50];
        totalServicios = 0;
    }

    public ServicioAcabado buscarPorCodigo(String codigo) {
        for (int i = 0; i < totalServicios; i++) {
            if (servicios[i].getCodigo().equalsIgnoreCase(codigo)) {
                return servicios[i];
            }
        }
        return null;
    }

    private String validarDatos(ServicioAcabado s) {
        if (Validador.esVacio(s.getCodigo()) || Validador.esVacio(s.getNombre())) {
            return "Código y nombre son obligatorios.";
        }
        if (s.getPrecioPorPrenda() <= 0 || s.getCostoPorPrenda() < 0) {
            return "El precio por prenda debe ser mayor a 0 y el costo no negativo.";
        }
        return null;
    }

    public String registrar(ServicioAcabado s) {
        String error = validarDatos(s);
        if (error != null) {
            return error;
        }
        if (totalServicios == servicios.length) {
            return "No hay espacio para más servicios.";
        }
        if (buscarPorCodigo(s.getCodigo()) != null) {
            return "Ya existe un servicio con el código " + s.getCodigo() + ".";
        }
        servicios[totalServicios] = s;
        totalServicios++;
        return null;
    }

    public String actualizar(ServicioAcabado datos) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        ServicioAcabado s = buscarPorCodigo(datos.getCodigo());
        if (s == null) {
            return "No existe un servicio con el código " + datos.getCodigo() + ".";
        }
        s.setNombre(datos.getNombre());
        s.setPrecioPorPrenda(datos.getPrecioPorPrenda());
        s.setCostoPorPrenda(datos.getCostoPorPrenda());
        s.setRequiereEtapaAcabado(datos.isRequiereEtapaAcabado());
        s.setConLogo(datos.isConLogo());
        return null;
    }

    public String eliminar(String codigo, GestionPedidos gestionPedidos, GestionPaquetes gestionPaquetes) {
        for (int i = 0; i < totalServicios; i++) {
            if (servicios[i].getCodigo().equalsIgnoreCase(codigo)) {
                if (gestionPaquetes.usaServicio(servicios[i])) {
                    return "No se puede eliminar: el servicio forma parte de un paquete.";
                }
                if (gestionPedidos.usaServicio(servicios[i])) {
                    return "No se puede eliminar: el servicio está en pedidos.";
                }
                for (int j = i; j < totalServicios - 1; j++) {
                    servicios[j] = servicios[j + 1];
                }
                servicios[totalServicios - 1] = null;
                totalServicios--;
                return null;
            }
        }
        return "No existe un servicio con el código " + codigo + ".";
    }

    public ServicioAcabado[] getServicios() { return servicios; }
    public int getTotalServicios() { return totalServicios; }
}
