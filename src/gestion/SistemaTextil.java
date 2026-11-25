package gestion;

import clases.catalogo.Paquete;
import clases.catalogo.ServicioAcabado;
import clases.clientes.Cliente;
import clases.empleados.EjecutivoVentas;
import clases.empleados.Empleado;
import clases.empleados.JefeProduccion;
import clases.pedidos.Pedido;
import clases.prendas.ModeloPrenda;
import java.time.LocalDate;

/**
 * Punto central de datos del sistema: guarda una sola instancia de cada gestión
 * para que todas las ventanas trabajen con los mismos arreglos en memoria.
 * Cada gestión registra sus propios datos iniciales en su constructor.
 */
public class SistemaTextil {

    // El orden importa: los modelos usan insumos y los paquetes usan modelos y servicios
    private static GestionEmpleados gestionEmpleados = new GestionEmpleados();
    private static GestionClientes gestionClientes = new GestionClientes();
    private static GestionTelas gestionTelas = new GestionTelas();
    private static GestionInsumos gestionInsumos = new GestionInsumos();
    private static GestionModelos gestionModelos = new GestionModelos(gestionInsumos);
    private static GestionServicios gestionServicios = new GestionServicios();
    private static GestionPaquetes gestionPaquetes = new GestionPaquetes(gestionModelos, gestionServicios);
    private static GestionComprobantes gestionComprobantes = new GestionComprobantes();
    private static GestionPedidos gestionPedidos = new GestionPedidos(gestionTelas, gestionInsumos, gestionComprobantes);
    private static GestionReportes gestionReportes = new GestionReportes(gestionPedidos, gestionTelas, gestionInsumos, gestionEmpleados);

    private static Empleado usuarioActual;   // quien inició sesión
    private static boolean datosCargados = false;

    // Se llama una vez al iniciar el programa (desde frmLogin)
    public static void cargarDatosIniciales() {
        if (!datosCargados) {
            cargarPedidosDeEjemplo();
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

    /*
     * Pedidos de ejemplo en distintas etapas, con fechas pasadas, para que el tablero y los
     * reportes tengan información. Pasan por los mismos métodos de GestionPedidos que usa la GUI.
     * No van en el constructor de GestionPedidos porque necesitan clientes, empleados, modelos, etc.
     */
    private static void cargarPedidosDeEjemplo() {
        // Objetos que ya se registraron en los constructores de cada gestión
        EjecutivoVentas carla = (EjecutivoVentas) gestionEmpleados.buscarPorDni("45678901");
        EjecutivoVentas diego = (EjecutivoVentas) gestionEmpleados.buscarPorDni("46789012");
        JefeProduccion rosa = (JefeProduccion) gestionEmpleados.buscarPorDni("42345678");
        JefeProduccion luis = (JefeProduccion) gestionEmpleados.buscarPorDni("43456789");
        Cliente lucia = gestionClientes.buscarPorDocumento("45612378");
        Cliente valeria = gestionClientes.buscarPorDocumento("10712345671");
        Cliente andina = gestionClientes.buscarPorDocumento("20512345678");
        Cliente colegio = gestionClientes.buscarPorDocumento("20498765432");
        Cliente minera = gestionClientes.buscarPorDocumento("20601234567");
        ModeloPrenda poloPima = gestionModelos.buscarPorCodigo("M01");
        ModeloPrenda poloPique = gestionModelos.buscarPorCodigo("M02");
        ModeloPrenda jean = gestionModelos.buscarPorCodigo("M03");
        ModeloPrenda casacaDrill = gestionModelos.buscarPorCodigo("M04");
        ServicioAcabado estampado = gestionServicios.buscarPorCodigo("S01");
        ServicioAcabado bordado = gestionServicios.buscarPorCodigo("S02");
        ServicioAcabado empaque = gestionServicios.buscarPorCodigo("S03");
        ServicioAcabado estampadoLogo = gestionServicios.buscarPorCodigo("S04");
        Paquete uniforme = gestionPaquetes.buscarPorCodigo("P01");
        Paquete kit = gestionPaquetes.buscarPorCodigo("P02");

        // ---------------- Pedidos de ejemplo en distintas etapas ----------------
        LocalDate hoy = LocalDate.now();

        // 1) Colegio: uniformes escolares. Entregado a tiempo, con defectuosas en el control de calidad.
        LocalDate f = hoy.minusDays(50);
        Pedido p1 = new Pedido(colegio, carla, f, f.plusDays(16));
        gestionPedidos.crearDetallePaquete(p1, uniforme, "Azul marino", "M", 60);
        gestionPedidos.crearDetallePaquete(p1, uniforme, "Azul marino", "S", 40);
        verificar(gestionPedidos.registrar(p1, f), "pedido 1");
        verificar(gestionPedidos.confirmar(p1, redondearArriba(p1.calcularAdelantoRequerido()), carla, f.plusDays(1)), "confirmar 1");
        verificar(gestionPedidos.iniciarCorte(p1, rosa, f.plusDays(3)), "corte 1");
        verificar(gestionPedidos.registrarAvance(p1, rosa, f.plusDays(5)), "avance 1");
        verificar(gestionPedidos.registrarAvance(p1, rosa, f.plusDays(10)), "acabado 1");
        verificar(gestionPedidos.registrarControlCalidad(p1, new int[]{3, 5, 0, 0}, rosa, f.plusDays(12)), "calidad 1");
        verificar(gestionPedidos.registrarAvance(p1, rosa, f.plusDays(13)), "acabado 1b");
        verificar(gestionPedidos.registrarControlCalidad(p1, new int[]{0, 0, 0, 0}, rosa, f.plusDays(14)), "calidad 1b");
        verificar(gestionPedidos.despachar(p1, "Factura", colegio.getDocumento(), colegio.getNombres(), carla, f.plusDays(15)), "despacho 1");

        // 2) Mayorista: más de 200 prendas (descuento por volumen). Entregado con 2 días de retraso.
        f = hoy.minusDays(40);
        Pedido p2 = new Pedido(andina, diego, f, f.plusDays(12));
        gestionPedidos.crearDetallePrenda(p2, jean, "Azul índigo", "M", 120);
        gestionPedidos.crearDetallePrenda(p2, jean, "Azul índigo", "L", 100);
        gestionPedidos.crearDetallePrenda(p2, poloPima, "Negro", "M", 60);
        verificar(gestionPedidos.registrar(p2, f), "pedido 2");
        verificar(gestionPedidos.confirmar(p2, redondearArriba(p2.calcularAdelantoRequerido()), diego, f.plusDays(1)), "confirmar 2");
        verificar(gestionPedidos.iniciarCorte(p2, luis, f.plusDays(2)), "corte 2");
        verificar(gestionPedidos.registrarAvance(p2, luis, f.plusDays(4)), "avance 2");
        verificar(gestionPedidos.registrarControlCalidad(p2, new int[]{0, 0, 0, 0}, luis, f.plusDays(13)), "calidad 2");
        verificar(gestionPedidos.despachar(p2, "Factura", andina.getDocumento(), andina.getNombres(), diego, f.plusDays(14)), "despacho 2");

        // 3) Minorista: polos con estampado. Entregado a tiempo con boleta.
        f = hoy.minusDays(30);
        Pedido p3 = new Pedido(lucia, carla, f, f.plusDays(10));
        gestionPedidos.crearDetallePrenda(p3, poloPima, "Blanco", "S", 30);
        gestionPedidos.crearDetallePrenda(p3, poloPima, "Blanco", "M", 30);
        gestionPedidos.crearDetalleServicio(p3, estampado, 60);
        verificar(gestionPedidos.registrar(p3, f), "pedido 3");
        verificar(gestionPedidos.confirmar(p3, redondearArriba(p3.calcularAdelantoRequerido()), carla, f));
        verificar(gestionPedidos.iniciarCorte(p3, rosa, f.plusDays(1)));
        verificar(gestionPedidos.registrarAvance(p3, rosa, f.plusDays(3)));
        verificar(gestionPedidos.registrarAvance(p3, rosa, f.plusDays(6)));
        verificar(gestionPedidos.registrarControlCalidad(p3, new int[]{0, 0, 0, 0}, rosa, f.plusDays(8)));
        verificar(gestionPedidos.despachar(p3, "Boleta", "", "", carla, f.plusDays(9)));

        // 4) Corporativo: kit con bordado. En Acabado.
        f = hoy.minusDays(20);
        Pedido p4 = new Pedido(minera, diego, f, hoy.plusDays(5));
        gestionPedidos.crearDetallePaquete(p4, kit, "Negro", "L", 40);
        gestionPedidos.crearDetallePrenda(p4, casacaDrill, "Negro", "XL", 20);
        gestionPedidos.crearDetalleServicio(p4, bordado, 20);
        verificar(gestionPedidos.registrar(p4, f), "pedido 4");
        verificar(gestionPedidos.confirmar(p4, redondearArriba(p4.calcularAdelantoRequerido()), diego, f.plusDays(1)));
        verificar(gestionPedidos.iniciarCorte(p4, luis, f.plusDays(4)));
        verificar(gestionPedidos.registrarAvance(p4, luis, f.plusDays(8)));
        verificar(gestionPedidos.registrarAvance(p4, luis, hoy.minusDays(2)));

        // 5) Mayorista: polos piqué. En Corte.
        f = hoy.minusDays(10);
        Pedido p5 = new Pedido(andina, carla, f, hoy.plusDays(10));
        gestionPedidos.crearDetallePrenda(p5, poloPique, "Blanco", "M", 150);
        gestionPedidos.crearDetallePrenda(p5, poloPique, "Blanco", "L", 100);
        verificar(gestionPedidos.registrar(p5, f), "pedido 5");
        verificar(gestionPedidos.confirmar(p5, redondearArriba(p5.calcularAdelantoRequerido()), carla, f.plusDays(1)));
        verificar(gestionPedidos.iniciarCorte(p5, rosa, hoy.minusDays(3)));

        // 6) Boutique: polos piqué azul marino. Listo para despacho.
        f = hoy.minusDays(15);
        Pedido p6 = new Pedido(valeria, carla, f, hoy.plusDays(2));
        gestionPedidos.crearDetallePrenda(p6, poloPique, "Azul marino", "S", 25);
        gestionPedidos.crearDetallePrenda(p6, poloPique, "Azul marino", "M", 35);
        gestionPedidos.crearDetalleServicio(p6, empaque, 60);
        verificar(gestionPedidos.registrar(p6, f), "pedido 6");
        verificar(gestionPedidos.confirmar(p6, redondearArriba(p6.calcularAdelantoRequerido()), carla, f.plusDays(1)));
        verificar(gestionPedidos.iniciarCorte(p6, luis, f.plusDays(2)));
        verificar(gestionPedidos.registrarAvance(p6, luis, f.plusDays(5)));
        verificar(gestionPedidos.registrarControlCalidad(p6, new int[]{0, 0, 0, 0}, luis, hoy.minusDays(1)));

        // 7) Minorista: jeans negros. Confirmado.
        f = hoy.minusDays(5);
        Pedido p7 = new Pedido(lucia, diego, f, hoy.plusDays(12));
        gestionPedidos.crearDetallePrenda(p7, jean, "Negro", "M", 60);
        verificar(gestionPedidos.registrar(p7, f), "pedido 7");
        verificar(gestionPedidos.confirmar(p7, redondearArriba(p7.calcularAdelantoRequerido()), diego, hoy.minusDays(4)));

        // 8) Colegio: polos con estampado de logo. Registrado (falta adelanto).
        f = hoy.minusDays(2);
        Pedido p8 = new Pedido(colegio, carla, f, hoy.plusDays(15));
        gestionPedidos.crearDetallePrenda(p8, poloPima, "Blanco", "M", 80);
        gestionPedidos.crearDetalleServicio(p8, estampadoLogo, 80);
        verificar(gestionPedidos.registrar(p8, f), "pedido 8");

        // 9) Cancelado antes del corte: se devuelve el adelanto.
        f = hoy.minusDays(25);
        Pedido p9 = new Pedido(valeria, diego, f, f.plusDays(14));
        gestionPedidos.crearDetallePrenda(p9, poloPima, "Negro", "L", 50);
        verificar(gestionPedidos.registrar(p9, f), "pedido 9");
        verificar(gestionPedidos.confirmar(p9, redondearArriba(p9.calcularAdelantoRequerido()), diego, f.plusDays(1)));
        verificar(gestionPedidos.cancelar(p9, "El cliente cambió de proveedor", diego, f.plusDays(2)));
    }

    private static double redondearArriba(double monto) {
        return Math.ceil(monto * 100) / 100.0;
    }

    private static void verificar(String error) {
        verificar(error, "paso");
    }

    // Si un dato de prueba no pasa las validaciones se avisa en la consola (sobrecarga)
    private static void verificar(String error, String paso) {
        if (error != null) {
            System.out.println("[Datos iniciales] " + paso + ": " + error);
        }
    }
}
