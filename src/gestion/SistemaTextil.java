package gestion;

import clases.Empleado;

/**
 * Punto central de datos del sistema: guarda una sola instancia de cada gestor
 * para que todas las ventanas trabajen con los mismos arreglos en memoria.
 */
public class SistemaTextil {

    private static GestionEmpleados gestionEmpleados = new GestionEmpleados();
    private static GestionClientes gestionClientes = new GestionClientes();
    private static GestionTelas gestionTelas = new GestionTelas();
    private static GestionInsumos gestionInsumos = new GestionInsumos();
    private static GestionModelos gestionModelos = new GestionModelos();
    private static GestionServicios gestionServicios = new GestionServicios();
    private static GestionPaquetes gestionPaquetes = new GestionPaquetes();
    private static GestionComprobantes gestionComprobantes = new GestionComprobantes();
    private static GestionPedidos gestionPedidos = new GestionPedidos(gestionTelas, gestionInsumos, gestionComprobantes);
    private static GestionReportes gestionReportes = new GestionReportes(gestionPedidos, gestionTelas, gestionInsumos, gestionEmpleados);

    private static Empleado usuarioActual;   // quien inició sesión
    private static boolean datosCargados = false;

    public static void cargarDatosIniciales() {
        if (!datosCargados) {
            DatosIniciales.cargar();
            datosCargados = true;
        }
    }

    public static GestionEmpleados getGestionEmpleados() { return gestionEmpleados; }
    public static GestionClientes getGestionClientes() { return gestionClientes; }
    public static GestionTelas getGestionTelas() { return gestionTelas; }
    public static GestionInsumos getGestionInsumos() { return gestionInsumos; }
    public static GestionModelos getGestionModelos() { return gestionModelos; }
    public static GestionServicios getGestionServicios() { return gestionServicios; }
    public static GestionPaquetes getGestionPaquetes() { return gestionPaquetes; }
    public static GestionComprobantes getGestionComprobantes() { return gestionComprobantes; }
    public static GestionPedidos getGestionPedidos() { return gestionPedidos; }
    public static GestionReportes getGestionReportes() { return gestionReportes; }

    public static Empleado getUsuarioActual() { return usuarioActual; }
    public static void setUsuarioActual(Empleado usuario) { usuarioActual = usuario; }
}
