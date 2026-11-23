package gestion;

import clases.Cliente;
import utilitario.Validador;

/**
 * RF03 - Gestión de clientes con arreglo de objetos.
 */
public class GestionClientes {

    private Cliente[] clientes;
    private int totalClientes;

    public GestionClientes() {
        clientes = new Cliente[100];
        totalClientes = 0;
    }

    public Cliente buscarPorDocumento(String documento) {
        for (int i = 0; i < totalClientes; i++) {
            if (clientes[i].getDocumento().equals(documento)) {
                return clientes[i];
            }
        }
        return null;
    }

    private String validarDatos(Cliente c) {
        if (!Validador.esDni(c.getDocumento()) && !Validador.esRuc(c.getDocumento())) {
            return "El documento debe ser un DNI (8 dígitos) o un RUC (11 dígitos, empieza en 10 o 20).";
        }
        if (Validador.esVacio(c.getNombres())) {
            return "Los nombres o la razón social son obligatorios.";
        }
        if (!Validador.esTelefono(c.getTelefono())) {
            return "El teléfono debe tener 9 dígitos (celular) o 7 (fijo).";
        }
        if (!Validador.esCorreo(c.getCorreo())) {
            return "El correo electrónico no es válido.";
        }
        if (Validador.esVacio(c.getDireccion())) {
            return "La dirección es obligatoria.";
        }
        return null;
    }

    public String registrar(Cliente c) {
        String error = validarDatos(c);
        if (error != null) {
            return error;
        }
        if (totalClientes == clientes.length) {
            return "No hay espacio para más clientes.";
        }
        if (buscarPorDocumento(c.getDocumento()) != null) {
            return "Ya existe un cliente con el documento " + c.getDocumento() + ".";
        }
        clientes[totalClientes] = c;
        totalClientes++;
        return null;
    }

    public String actualizar(Cliente datos, GestionPedidos gestionPedidos) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        for (int i = 0; i < totalClientes; i++) {
            if (clientes[i].getDocumento().equals(datos.getDocumento())) {
                if (clientes[i].getClass() == datos.getClass()) {
                    clientes[i].setNombres(datos.getNombres());
                    clientes[i].setTelefono(datos.getTelefono());
                    clientes[i].setCorreo(datos.getCorreo());
                    clientes[i].setDireccion(datos.getDireccion());
                } else {
                    // Cambio de tipo: no se permite si tiene pedidos en curso (cambiaría su descuento)
                    if (gestionPedidos.tienePedidosActivos(clientes[i])) {
                        return "No se puede cambiar el tipo de cliente: tiene pedidos en curso.";
                    }
                    clientes[i] = datos;
                }
                return null;
            }
        }
        return "No existe un cliente con el documento " + datos.getDocumento() + ".";
    }

    public String eliminar(String documento, GestionPedidos gestionPedidos) {
        for (int i = 0; i < totalClientes; i++) {
            if (clientes[i].getDocumento().equals(documento)) {
                if (gestionPedidos.tienePedidosActivos(clientes[i])) {
                    return "No se puede eliminar: el cliente tiene pedidos en curso.";
                }
                for (int j = i; j < totalClientes - 1; j++) {
                    clientes[j] = clientes[j + 1];
                }
                clientes[totalClientes - 1] = null;
                totalClientes--;
                return null;
            }
        }
        return "No existe un cliente con el documento " + documento + ".";
    }

    public Cliente[] getClientes() { return clientes; }
    public int getTotalClientes() { return totalClientes; }
}
