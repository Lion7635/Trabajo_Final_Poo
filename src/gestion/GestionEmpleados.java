package gestion;

import clases.empleados.Administrador;
import clases.empleados.EjecutivoVentas;
import clases.empleados.Empleado;
import clases.empleados.JefeProduccion;
import utilitario.Validador;

/**
 * RF01 - Gestión de personal con arreglo de objetos.
 * Convención: los métodos que validan devuelven null si todo salió bien
 * o un texto con la causa del error.
 */
public class GestionEmpleados {

    private Empleado[] empleados;
    private int totalEmpleados;

    public GestionEmpleados() {
        empleados = new Empleado[50];
        totalEmpleados = 0;

        // Datos iniciales: se registran al crear la gestión (igual que en los laboratorios)
        registrar(new Administrador("41234567", "Jorge", "Quispe Ramos", "admin", "admin123"));
        registrar(new EjecutivoVentas("45678901", "Carla", "Mendoza Ríos", "cmendoza", "ventas123"));
        registrar(new EjecutivoVentas("46789012", "Diego", "Salas Paredes", "dsalas", "ventas123"));
        registrar(new JefeProduccion("42345678", "Rosa", "Huamán Torres", "rhuaman", "prod123"));
        registrar(new JefeProduccion("43456789", "Luis", "Chávez Lino", "lchavez", "prod123"));
    }

    public Empleado buscarPorDni(String dni) {
        for (int i = 0; i < totalEmpleados; i++) {
            if (empleados[i].getDni().equals(dni)) {
                return empleados[i];
            }
        }
        return null;
    }

    public Empleado buscarPorUsuario(String usuario) {
        for (int i = 0; i < totalEmpleados; i++) {
            if (empleados[i].getUsuario().equalsIgnoreCase(usuario)) {
                return empleados[i];
            }
        }
        return null;
    }

    private String validarDatos(Empleado e) {
        if (!Validador.esDni(e.getDni())) {
            return "El DNI debe tener 8 dígitos.";
        }
        if (Validador.esVacio(e.getNombres()) || Validador.esVacio(e.getApellidos())) {
            return "Nombres y apellidos son obligatorios.";
        }
        if (Validador.esVacio(e.getUsuario()) || e.getUsuario().contains(" ")) {
            return "El usuario es obligatorio y no debe tener espacios.";
        }
        if (e.getContrasena() == null || e.getContrasena().length() < 4) {
            return "La contraseña debe tener al menos 4 caracteres.";
        }
        return null;
    }

    public String registrar(Empleado e) {
        String error = validarDatos(e);
        if (error != null) {
            return error;
        }
        if (totalEmpleados == empleados.length) {
            return "No hay espacio para más empleados.";
        }
        if (buscarPorDni(e.getDni()) != null) {
            return "Ya existe un empleado con el DNI " + e.getDni() + ".";
        }
        if (buscarPorUsuario(e.getUsuario()) != null) {
            return "El usuario '" + e.getUsuario() + "' ya está en uso.";
        }
        empleados[totalEmpleados] = e;
        totalEmpleados++;
        return null;
    }

    // Si cambia el rol se reemplaza el objeto, porque cada rol es otra clase
    public String actualizar(Empleado datos) {
        String error = validarDatos(datos);
        if (error != null) {
            return error;
        }
        Empleado otro = buscarPorUsuario(datos.getUsuario());
        if (otro != null && !otro.getDni().equals(datos.getDni())) {
            return "El usuario '" + datos.getUsuario() + "' ya está en uso.";
        }
        for (int i = 0; i < totalEmpleados; i++) {
            if (empleados[i].getDni().equals(datos.getDni())) {
                if (empleados[i].getClass() == datos.getClass()) {
                    empleados[i].setNombres(datos.getNombres());
                    empleados[i].setApellidos(datos.getApellidos());
                    empleados[i].setUsuario(datos.getUsuario());
                    empleados[i].setContrasena(datos.getContrasena());
                } else {
                    empleados[i] = datos;
                }
                return null;
            }
        }
        return "No existe un empleado con el DNI " + datos.getDni() + ".";
    }

    public String eliminar(String dni) {
        for (int i = 0; i < totalEmpleados; i++) {
            if (empleados[i].getDni().equals(dni)) {
                for (int j = i; j < totalEmpleados - 1; j++) {
                    empleados[j] = empleados[j + 1];
                }
                empleados[totalEmpleados - 1] = null;
                totalEmpleados--;
                return null;
            }
        }
        return "No existe un empleado con el DNI " + dni + ".";
    }

    // RF09: recorre el arreglo y usa la interfaz Autenticable
    public Empleado autenticar(String usuario, String contrasena) {
        for (int i = 0; i < totalEmpleados; i++) {
            if (empleados[i].autenticar(usuario, contrasena)) {
                return empleados[i];
            }
        }
        return null;
    }

    public int contarPorRol(String rol) {
        int c = 0;
        for (int i = 0; i < totalEmpleados; i++) {
            if (empleados[i].getRol().equals(rol)) {
                c++;
            }
        }
        return c;
    }

    public Empleado[] getEmpleados() { return empleados; }
    public int getTotalEmpleados() { return totalEmpleados; }
}
