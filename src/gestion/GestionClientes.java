package gestion;

import clases.clientes.Cliente;
import clases.clientes.ClienteInstitucional;
import clases.clientes.ClienteMayorista;
import clases.clientes.ClienteMinorista;
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

        // Datos iniciales: se registran al crear la gestión (igual que en los laboratorios)
        registrar(new ClienteMinorista("45612378", "Lucía Fernández (Bodega Moda Lucía)",
                "987654321", "lucia.moda@gmail.com", "Jr. Gamarra 650, La Victoria"));
        registrar(new ClienteMinorista("10712345671", "Boutique Valeria E.I.R.L.",
                "956123478", "ventas@boutiquevaleria.pe", "Av. Larco 345, Miraflores"));
        registrar(new ClienteMayorista("20512345678", "Distribuidora Textil Andina S.A.C.",
                "014567890", "compras@textilandina.pe", "Av. Argentina 1520, Cercado de Lima"));
        registrar(new ClienteInstitucional("20498765432", "Colegio San Martín de Porres",
                "012345678", "administracion@csmp.edu.pe", "Av. Brasil 2100, Pueblo Libre"));
        registrar(new ClienteInstitucional("20601234567", "Corporación Minera del Sur S.A.",
                "016549870", "logistica@minerasur.com.pe", "Av. Javier Prado Este 4200, Surco"));
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

    // ---------- Búsqueda (sobrecarga de métodos, Semana 07) ----------

    // Clientes cuyo documento o nombre contiene el texto ("" devuelve todos)
    public Cliente[] buscar(String texto) {
        return buscar(texto, "Todos");
    }

    // Igual que la anterior, pero además filtra por tipo de cliente
    public Cliente[] buscar(String texto, String tipo) {
        String t = texto.trim().toLowerCase();
        int c = 0;
        for (int i = 0; i < totalClientes; i++) {
            if (coincide(clientes[i], t, tipo)) {
                c++;
            }
        }
        Cliente[] resultado = new Cliente[c];
        int k = 0;
        for (int i = 0; i < totalClientes; i++) {
            if (coincide(clientes[i], t, tipo)) {
                resultado[k] = clientes[i];
                k++;
            }
        }
        return resultado;
    }

    private boolean coincide(Cliente c, String texto, String tipo) {
        boolean textoOk = c.getNombreCompleto().toLowerCase().contains(texto) || c.getDocumento().contains(texto);
        boolean tipoOk = tipo.equals("Todos") || c.getTipo().equals(tipo);
        return textoOk && tipoOk;
    }

    // ---------- Ordenamiento burbuja por nombre (Semana 08) ----------
    public void ordenarPorNombre() {
        for (int i = 0; i < totalClientes - 1; i++) {
            for (int j = 0; j < totalClientes - 1 - i; j++) {
                if (clientes[j].getNombreCompleto().compareToIgnoreCase(clientes[j + 1].getNombreCompleto()) > 0) {
                    Cliente aux = clientes[j];
                    clientes[j] = clientes[j + 1];
                    clientes[j + 1] = aux;
                }
            }
        }
    }

    public Cliente[] getClientes() { return clientes; }
    public int getTotalClientes() { return totalClientes; }
}
