package gestion;

import clases.Administrador;
import clases.Casaca;
import clases.ClienteInstitucional;
import clases.ClienteMayorista;
import clases.ClienteMinorista;
import clases.EjecutivoVentas;
import clases.Insumo;
import clases.Jean;
import clases.JefeProduccion;
import clases.ModeloPrenda;
import clases.Paquete;
import clases.Pedido;
import clases.Polo;
import clases.ServicioAcabado;
import clases.Tela;
import java.time.LocalDate;

/**
 * Datos de prueba que se cargan al iniciar el programa (persistencia temporal en memoria).
 * Los pedidos de ejemplo pasan por los mismos métodos de GestionPedidos que usa la GUI,
 * con fechas pasadas, para que los reportes y el tablero tengan información.
 */
public class DatosIniciales {

    public static void cargar() {
        GestionEmpleados ge = SistemaTextil.getGestionEmpleados();
        GestionClientes gc = SistemaTextil.getGestionClientes();
        GestionTelas gt = SistemaTextil.getGestionTelas();
        GestionInsumos gi = SistemaTextil.getGestionInsumos();
        GestionModelos gm = SistemaTextil.getGestionModelos();
        GestionServicios gs = SistemaTextil.getGestionServicios();
        GestionPaquetes gp = SistemaTextil.getGestionPaquetes();
        GestionPedidos gped = SistemaTextil.getGestionPedidos();

        // ---------------- Empleados (usuario / contraseña) ----------------
        ge.registrar(new Administrador("41234567", "Jorge", "Quispe Ramos", "admin", "admin123"));
        EjecutivoVentas carla = new EjecutivoVentas("45678901", "Carla", "Mendoza Ríos", "cmendoza", "ventas123");
        EjecutivoVentas diego = new EjecutivoVentas("46789012", "Diego", "Salas Paredes", "dsalas", "ventas123");
        JefeProduccion rosa = new JefeProduccion("42345678", "Rosa", "Huamán Torres", "rhuaman", "prod123");
        JefeProduccion luis = new JefeProduccion("43456789", "Luis", "Chávez Lino", "lchavez", "prod123");
        ge.registrar(carla);
        ge.registrar(diego);
        ge.registrar(rosa);
        ge.registrar(luis);

        // ---------------- Telas: código, tipo, color, metros, mínimo, costo/m ----------------
        gt.registrar(new Tela("T01", "Jersey Algodón Pima", "Negro", 1400, 250, 14.50));
        gt.registrar(new Tela("T02", "Jersey Algodón Pima", "Blanco", 1200, 250, 14.50));
        gt.registrar(new Tela("T03", "Jersey Algodón Pima", "Azul marino", 300, 150, 14.50));
        gt.registrar(new Tela("T04", "Piqué Algodón", "Blanco", 900, 200, 12.00));
        gt.registrar(new Tela("T05", "Piqué Algodón", "Azul marino", 1000, 200, 12.00));
        gt.registrar(new Tela("T06", "Denim 12 oz", "Azul índigo", 1100, 250, 18.00));
        gt.registrar(new Tela("T07", "Denim 12 oz", "Negro", 300, 200, 18.00));
        gt.registrar(new Tela("T08", "Drill Poliéster", "Negro", 800, 150, 11.00));
        gt.registrar(new Tela("T09", "Drill Poliéster", "Azul marino", 400, 150, 11.00));
        gt.registrar(new Tela("T10", "Polar", "Azul marino", 250, 120, 15.00));

        // ---------------- Insumos: código, nombre, unidad, stock, mínimo, costo ----------------
        Insumo hilo = new Insumo("I01", "Hilo poliéster 40/2", "cono", 300, 60, 6.00);
        Insumo etiqueta = new Insumo("I02", "Etiqueta tejida", "unid", 3000, 500, 0.15);
        Insumo boton = new Insumo("I03", "Botón 4 huecos", "unid", 2000, 400, 0.10);
        Insumo cierre = new Insumo("I04", "Cierre metálico 18 cm", "unid", 500, 120, 1.20);
        Insumo broche = new Insumo("I05", "Broche de presión", "unid", 900, 300, 0.25);
        Insumo remache = new Insumo("I06", "Remache para jean", "unid", 2200, 400, 0.12);
        Insumo bolsa = new Insumo("I07", "Bolsa de empaque", "unid", 150, 300, 0.30);
        gi.registrar(hilo);
        gi.registrar(etiqueta);
        gi.registrar(boton);
        gi.registrar(cierre);
        gi.registrar(broche);
        gi.registrar(remache);
        gi.registrar(bolsa);

        // ---------------- Modelos: consumo de tela por talla S, M, L, XL ----------------
        Polo poloPima = new Polo("M01", "Polo Pima Clásico", "Jersey Algodón Pima",
                new double[]{1.10, 1.20, 1.30, 1.45}, 25, 35.00, 4.50, true);
        poloPima.agregarInsumo(hilo, 0.04);
        poloPima.agregarInsumo(etiqueta, 1);

        Polo poloPique = new Polo("M02", "Polo Piqué Cuello Camisero", "Piqué Algodón",
                new double[]{1.20, 1.30, 1.40, 1.55}, 35, 42.00, 6.00, true);
        poloPique.agregarInsumo(hilo, 0.05);
        poloPique.agregarInsumo(etiqueta, 1);
        poloPique.agregarInsumo(boton, 3);

        Jean jean = new Jean("M03", "Jean Clásico 5 Bolsillos", "Denim 12 oz",
                new double[]{1.30, 1.40, 1.50, 1.65}, 45, 89.00, 9.00, true, 6.50);
        jean.agregarInsumo(hilo, 0.08);
        jean.agregarInsumo(etiqueta, 1);
        jean.agregarInsumo(boton, 1);
        jean.agregarInsumo(remache, 6);

        Casaca casacaDrill = new Casaca("M04", "Casaca Drill Corporativa", "Drill Poliéster",
                new double[]{1.80, 1.95, 2.10, 2.30}, 70, 129.00, 14.00, true, 9.50, 4.00);
        casacaDrill.agregarInsumo(hilo, 0.10);
        casacaDrill.agregarInsumo(etiqueta, 1);
        casacaDrill.agregarInsumo(cierre, 1);
        casacaDrill.agregarInsumo(broche, 4);

        Casaca casacaPolar = new Casaca("M05", "Casaca Polar Escolar", "Polar",
                new double[]{1.60, 1.70, 1.85, 2.00}, 55, 95.00, 10.00, true, 0.00, 3.00);
        casacaPolar.agregarInsumo(hilo, 0.08);
        casacaPolar.agregarInsumo(etiqueta, 1);
        casacaPolar.agregarInsumo(cierre, 1);

        Polo poloBasico = new Polo("M06", "Polo Básico Cuello Redondo", "Jersey Algodón Pima",
                new double[]{1.00, 1.10, 1.20, 1.30}, 20, 25.00, 3.50, false);   // Descontinuado
        poloBasico.agregarInsumo(etiqueta, 1);

        gm.registrar(poloPima);
        gm.registrar(poloPique);
        gm.registrar(jean);
        gm.registrar(casacaDrill);
        gm.registrar(casacaPolar);
        gm.registrar(poloBasico);

        // ---------------- Servicios de acabado ----------------
        ServicioAcabado estampado = new ServicioAcabado("S01", "Estampado serigrafía 1 color", 3.50, 1.40, true, false);
        ServicioAcabado bordado = new ServicioAcabado("S02", "Bordado de logo", 6.00, 2.50, true, true);
        ServicioAcabado empaque = new ServicioAcabado("S03", "Empaque personalizado", 1.20, 0.50, false, false);
        ServicioAcabado estampadoLogo = new ServicioAcabado("S04", "Estampado de logo full color", 5.00, 2.00, true, true);
        gs.registrar(estampado);
        gs.registrar(bordado);
        gs.registrar(empaque);
        gs.registrar(estampadoLogo);

        // ---------------- Paquetes promocionales ----------------
        Paquete uniforme = new Paquete("P01", "Uniforme Escolar Completo", 175.00);
        uniforme.agregarModelo(poloPique, 2);
        uniforme.agregarModelo(casacaPolar, 1);
        uniforme.agregarServicio(bordado);
        gp.registrar(uniforme);

        Paquete kit = new Paquete("P02", "Kit Corporativo", 199.00);
        kit.agregarModelo(poloPima, 2);
        kit.agregarModelo(casacaDrill, 1);
        kit.agregarServicio(bordado);
        kit.agregarServicio(empaque);
        gp.registrar(kit);

        // ---------------- Clientes ----------------
        ClienteMinorista lucia = new ClienteMinorista("45612378", "Lucía Fernández (Bodega Moda Lucía)",
                "987654321", "lucia.moda@gmail.com", "Jr. Gamarra 650, La Victoria");
        ClienteMinorista valeria = new ClienteMinorista("10712345671", "Boutique Valeria E.I.R.L.",
                "956123478", "ventas@boutiquevaleria.pe", "Av. Larco 345, Miraflores");
        ClienteMayorista andina = new ClienteMayorista("20512345678", "Distribuidora Textil Andina S.A.C.",
                "014567890", "compras@textilandina.pe", "Av. Argentina 1520, Cercado de Lima");
        ClienteInstitucional colegio = new ClienteInstitucional("20498765432", "Colegio San Martín de Porres",
                "012345678", "administracion@csmp.edu.pe", "Av. Brasil 2100, Pueblo Libre");
        ClienteInstitucional minera = new ClienteInstitucional("20601234567", "Corporación Minera del Sur S.A.",
                "016549870", "logistica@minerasur.com.pe", "Av. Javier Prado Este 4200, Surco");
        gc.registrar(lucia);
        gc.registrar(valeria);
        gc.registrar(andina);
        gc.registrar(colegio);
        gc.registrar(minera);

        // ---------------- Pedidos de ejemplo en distintas etapas ----------------
        LocalDate hoy = LocalDate.now();

        // 1) Colegio: uniformes escolares. Entregado a tiempo, con defectuosas en el control de calidad.
        LocalDate f = hoy.minusDays(50);
        Pedido p1 = new Pedido(colegio, carla, f, f.plusDays(16));
        gped.crearDetallePaquete(p1, uniforme, "Azul marino", "M", 60);
        gped.crearDetallePaquete(p1, uniforme, "Azul marino", "S", 40);
        verificar(gped.registrar(p1, f), "pedido 1");
        verificar(gped.confirmar(p1, redondearArriba(p1.calcularAdelantoRequerido()), carla, f.plusDays(1)), "confirmar 1");
        verificar(gped.iniciarCorte(p1, rosa, f.plusDays(3)), "corte 1");
        verificar(gped.registrarAvance(p1, rosa, f.plusDays(5)), "avance 1");
        verificar(gped.registrarAvance(p1, rosa, f.plusDays(10)), "acabado 1");
        verificar(gped.registrarControlCalidad(p1, new int[]{3, 5, 0, 0}, rosa, f.plusDays(12)), "calidad 1");
        verificar(gped.registrarAvance(p1, rosa, f.plusDays(13)), "acabado 1b");
        verificar(gped.registrarControlCalidad(p1, new int[]{0, 0, 0, 0}, rosa, f.plusDays(14)), "calidad 1b");
        verificar(gped.despachar(p1, "Factura", colegio.getDocumento(), colegio.getNombres(), carla, f.plusDays(15)), "despacho 1");

        // 2) Mayorista: más de 200 prendas (descuento por volumen). Entregado con 2 días de retraso.
        f = hoy.minusDays(40);
        Pedido p2 = new Pedido(andina, diego, f, f.plusDays(12));
        gped.crearDetallePrenda(p2, jean, "Azul índigo", "M", 120);
        gped.crearDetallePrenda(p2, jean, "Azul índigo", "L", 100);
        gped.crearDetallePrenda(p2, poloPima, "Negro", "M", 60);
        verificar(gped.registrar(p2, f), "pedido 2");
        verificar(gped.confirmar(p2, redondearArriba(p2.calcularAdelantoRequerido()), diego, f.plusDays(1)), "confirmar 2");
        verificar(gped.iniciarCorte(p2, luis, f.plusDays(2)), "corte 2");
        verificar(gped.registrarAvance(p2, luis, f.plusDays(4)), "avance 2");
        verificar(gped.registrarControlCalidad(p2, new int[]{0, 0, 0, 0}, luis, f.plusDays(13)), "calidad 2");
        verificar(gped.despachar(p2, "Factura", andina.getDocumento(), andina.getNombres(), diego, f.plusDays(14)), "despacho 2");

        // 3) Minorista: polos con estampado. Entregado a tiempo con boleta.
        f = hoy.minusDays(30);
        Pedido p3 = new Pedido(lucia, carla, f, f.plusDays(10));
        gped.crearDetallePrenda(p3, poloPima, "Blanco", "S", 30);
        gped.crearDetallePrenda(p3, poloPima, "Blanco", "M", 30);
        gped.crearDetalleServicio(p3, estampado, 60);
        verificar(gped.registrar(p3, f), "pedido 3");
        verificar(gped.confirmar(p3, redondearArriba(p3.calcularAdelantoRequerido()), carla, f));
        verificar(gped.iniciarCorte(p3, rosa, f.plusDays(1)));
        verificar(gped.registrarAvance(p3, rosa, f.plusDays(3)));
        verificar(gped.registrarAvance(p3, rosa, f.plusDays(6)));
        verificar(gped.registrarControlCalidad(p3, new int[]{0, 0, 0, 0}, rosa, f.plusDays(8)));
        verificar(gped.despachar(p3, "Boleta", "", "", carla, f.plusDays(9)));

        // 4) Corporativo: kit con bordado. En Acabado.
        f = hoy.minusDays(20);
        Pedido p4 = new Pedido(minera, diego, f, hoy.plusDays(5));
        gped.crearDetallePaquete(p4, kit, "Negro", "L", 40);
        gped.crearDetallePrenda(p4, casacaDrill, "Negro", "XL", 20);
        gped.crearDetalleServicio(p4, bordado, 20);
        verificar(gped.registrar(p4, f), "pedido 4");
        verificar(gped.confirmar(p4, redondearArriba(p4.calcularAdelantoRequerido()), diego, f.plusDays(1)));
        verificar(gped.iniciarCorte(p4, luis, f.plusDays(4)));
        verificar(gped.registrarAvance(p4, luis, f.plusDays(8)));
        verificar(gped.registrarAvance(p4, luis, hoy.minusDays(2)));

        // 5) Mayorista: polos piqué. En Corte.
        f = hoy.minusDays(10);
        Pedido p5 = new Pedido(andina, carla, f, hoy.plusDays(10));
        gped.crearDetallePrenda(p5, poloPique, "Blanco", "M", 150);
        gped.crearDetallePrenda(p5, poloPique, "Blanco", "L", 100);
        verificar(gped.registrar(p5, f), "pedido 5");
        verificar(gped.confirmar(p5, redondearArriba(p5.calcularAdelantoRequerido()), carla, f.plusDays(1)));
        verificar(gped.iniciarCorte(p5, rosa, hoy.minusDays(3)));

        // 6) Boutique: polos piqué azul marino. Listo para despacho.
        f = hoy.minusDays(15);
        Pedido p6 = new Pedido(valeria, carla, f, hoy.plusDays(2));
        gped.crearDetallePrenda(p6, poloPique, "Azul marino", "S", 25);
        gped.crearDetallePrenda(p6, poloPique, "Azul marino", "M", 35);
        gped.crearDetalleServicio(p6, empaque, 60);
        verificar(gped.registrar(p6, f), "pedido 6");
        verificar(gped.confirmar(p6, redondearArriba(p6.calcularAdelantoRequerido()), carla, f.plusDays(1)));
        verificar(gped.iniciarCorte(p6, luis, f.plusDays(2)));
        verificar(gped.registrarAvance(p6, luis, f.plusDays(5)));
        verificar(gped.registrarControlCalidad(p6, new int[]{0, 0, 0, 0}, luis, hoy.minusDays(1)));

        // 7) Minorista: jeans negros. Confirmado.
        f = hoy.minusDays(5);
        Pedido p7 = new Pedido(lucia, diego, f, hoy.plusDays(12));
        gped.crearDetallePrenda(p7, jean, "Negro", "M", 60);
        verificar(gped.registrar(p7, f), "pedido 7");
        verificar(gped.confirmar(p7, redondearArriba(p7.calcularAdelantoRequerido()), diego, hoy.minusDays(4)));

        // 8) Colegio: polos con estampado de logo. Registrado (falta adelanto).
        f = hoy.minusDays(2);
        Pedido p8 = new Pedido(colegio, carla, f, hoy.plusDays(15));
        gped.crearDetallePrenda(p8, poloPima, "Blanco", "M", 80);
        gped.crearDetalleServicio(p8, estampadoLogo, 80);
        verificar(gped.registrar(p8, f), "pedido 8");

        // 9) Cancelado antes del corte: se devuelve el adelanto.
        f = hoy.minusDays(25);
        Pedido p9 = new Pedido(valeria, diego, f, f.plusDays(14));
        gped.crearDetallePrenda(p9, poloPima, "Negro", "L", 50);
        verificar(gped.registrar(p9, f), "pedido 9");
        verificar(gped.confirmar(p9, redondearArriba(p9.calcularAdelantoRequerido()), diego, f.plusDays(1)));
        verificar(gped.cancelar(p9, "El cliente cambió de proveedor", diego, f.plusDays(2)));
    }

    private static double redondearArriba(double monto) {
        return Math.ceil(monto * 100) / 100.0;
    }

    private static void verificar(String error) {
        verificar(error, "paso");
    }

    // Si un dato de prueba no pasa las validaciones se avisa en la consola
    private static void verificar(String error, String paso) {
        if (error != null) {
            System.out.println("[Datos iniciales] " + paso + ": " + error);
        }
    }
}
