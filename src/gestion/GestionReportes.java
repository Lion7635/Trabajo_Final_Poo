package gestion;

import clases.DetallePrenda;
import clases.Empleado;
import clases.EjecutivoVentas;
import clases.ItemPedido;
import clases.JefeProduccion;
import clases.MateriaPrima;
import clases.ModeloPrenda;
import clases.Pedido;
import java.time.LocalDate;

/**
 * RF08 - Reportes gerenciales. Todo se calcula recorriendo los arreglos de objetos.
 * Cada método devuelve las filas listas para mostrarse en un JTable.
 */
public class GestionReportes {

    private GestionPedidos gestionPedidos;
    private GestionTelas gestionTelas;
    private GestionInsumos gestionInsumos;
    private GestionEmpleados gestionEmpleados;

    public GestionReportes(GestionPedidos gestionPedidos, GestionTelas gestionTelas,
            GestionInsumos gestionInsumos, GestionEmpleados gestionEmpleados) {
        this.gestionPedidos = gestionPedidos;
        this.gestionTelas = gestionTelas;
        this.gestionInsumos = gestionInsumos;
        this.gestionEmpleados = gestionEmpleados;
    }

    private boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    // Recorta una matriz al número de filas usadas
    private Object[][] recortar(Object[][] filas, int total) {
        Object[][] r = new Object[total][];
        for (int i = 0; i < total; i++) {
            r[i] = filas[i];
        }
        return r;
    }

    // 1) Producción por modelo, talla y color (pedidos registrados en el rango, sin cancelados)
    //    Columnas: Modelo, Tipo, Color, Talla, Prendas
    public Object[][] produccionPorModelo(LocalDate desde, LocalDate hasta) {
        Object[][] filas = new Object[500][];
        int total = 0;
        for (int i = 0; i < gestionPedidos.getTotalPedidos(); i++) {
            Pedido p = gestionPedidos.getPedidos()[i];
            if (p.getEstado().equals(Pedido.CANCELADO) || !enRango(p.getFechaRegistro(), desde, hasta)) {
                continue;
            }
            for (int j = 0; j < p.getTotalItems(); j++) {
                DetallePrenda[] prendas = p.getItems()[j].getPrendas();
                for (int k = 0; k < prendas.length; k++) {
                    DetallePrenda d = prendas[k];
                    boolean encontrado = false;
                    for (int f = 0; f < total; f++) {
                        if (filas[f][0].equals(d.getModelo().getNombre()) && filas[f][2].equals(d.getColor())
                                && filas[f][3].equals(d.getTalla())) {
                            filas[f][4] = (Integer) filas[f][4] + d.getCantidad();
                            encontrado = true;
                        }
                    }
                    if (!encontrado && total < filas.length) {
                        filas[total] = new Object[]{d.getModelo().getNombre(), d.getModelo().getTipo(),
                            d.getColor(), d.getTalla(), d.getCantidad()};
                        total++;
                    }
                }
            }
        }
        return recortar(filas, total);
    }

    // 2a) Consumo de tela, merma y defectuosas por pedido (pedidos ya cortados)
    //     Columnas: Pedido, Cliente, Estado, Prendas, Metros netos, Merma, Total consumido, Defectuosas, % Defectuosas
    public Object[][] consumoPorPedido() {
        Object[][] filas = new Object[gestionPedidos.getTotalPedidos()][];
        int total = 0;
        for (int i = 0; i < gestionPedidos.getTotalPedidos(); i++) {
            Pedido p = gestionPedidos.getPedidos()[i];
            if (p.getMetrosConsumidos() > 0) {
                double porcentaje = p.getTotalDefectuosos() * 100.0 / p.getTotalPrendas();
                filas[total] = new Object[]{p.getNumeroTexto(), p.getCliente().getNombreCompleto(), p.getEstado(),
                    p.getTotalPrendas(), redondear(p.getMetrosConsumidos() - p.getMetrosMerma()),
                    redondear(p.getMetrosMerma()), redondear(p.getMetrosConsumidos()),
                    p.getTotalDefectuosos(), redondear(porcentaje)};
                total++;
            }
        }
        return recortar(filas, total);
    }

    // 2b) Telas e insumos por debajo del stock mínimo.
    //     Se juntan en un arreglo de la clase padre MateriaPrima y se recorren con polimorfismo.
    //     Columnas: Clase, Código, Descripción, Stock, Mínimo, Faltante, Unidad
    public Object[][] stockBajoMinimo() {
        MateriaPrima[] materiales = new MateriaPrima[gestionTelas.getTotalTelas() + gestionInsumos.getTotalInsumos()];
        int n = 0;
        for (int i = 0; i < gestionTelas.getTotalTelas(); i++) {
            materiales[n] = gestionTelas.getTelas()[i];
            n++;
        }
        for (int i = 0; i < gestionInsumos.getTotalInsumos(); i++) {
            materiales[n] = gestionInsumos.getInsumos()[i];
            n++;
        }
        Object[][] filas = new Object[n][];
        int total = 0;
        for (int i = 0; i < n; i++) {
            MateriaPrima m = materiales[i];
            if (m.estaBajoMinimo()) {
                filas[total] = new Object[]{m.getClass().getSimpleName(), m.getCodigo(), m.getDescripcion(),
                    redondear(m.getStock()), redondear(m.getStockMinimo()),
                    redondear(m.getStockMinimo() - m.getStock()), m.getUnidad()};
                total++;
            }
        }
        return recortar(filas, total);
    }

    // 3a) Pedidos vendidos por ejecutivo. Columnas: Ejecutivo, Vendidos, Cancelados, Monto vendido (con IGV)
    public Object[][] ventasPorEjecutivo() {
        Object[][] filas = new Object[gestionEmpleados.getTotalEmpleados()][];
        int total = 0;
        for (int i = 0; i < gestionEmpleados.getTotalEmpleados(); i++) {
            Empleado e = gestionEmpleados.getEmpleados()[i];
            if (e instanceof EjecutivoVentas) {
                int vendidos = 0, cancelados = 0;
                double monto = 0;
                for (int j = 0; j < gestionPedidos.getTotalPedidos(); j++) {
                    Pedido p = gestionPedidos.getPedidos()[j];
                    if (p.getEjecutivo() == e) {
                        if (p.getEstado().equals(Pedido.CANCELADO)) {
                            cancelados++;
                        } else {
                            vendidos++;
                            monto += p.calcularTotal();
                        }
                    }
                }
                filas[total] = new Object[]{e.getNombreCompleto(), vendidos, cancelados, redondear(monto)};
                total++;
            }
        }
        return recortar(filas, total);
    }

    // 3b) Pedidos entregados a tiempo por jefe de producción.
    //     Columnas: Jefe, A cargo, Entregados, A tiempo, Con retraso, % a tiempo
    public Object[][] entregasPorJefe() {
        Object[][] filas = new Object[gestionEmpleados.getTotalEmpleados()][];
        int total = 0;
        for (int i = 0; i < gestionEmpleados.getTotalEmpleados(); i++) {
            Empleado e = gestionEmpleados.getEmpleados()[i];
            if (e instanceof JefeProduccion) {
                int aCargo = 0, entregados = 0, aTiempo = 0;
                for (int j = 0; j < gestionPedidos.getTotalPedidos(); j++) {
                    Pedido p = gestionPedidos.getPedidos()[j];
                    if (p.getJefeProduccion() == e) {
                        aCargo++;
                        if (p.getEstado().equals(Pedido.ENTREGADO)) {
                            entregados++;
                            if (p.fueEntregadoATiempo()) {
                                aTiempo++;
                            }
                        }
                    }
                }
                double porcentaje = entregados == 0 ? 0 : aTiempo * 100.0 / entregados;
                filas[total] = new Object[]{e.getNombreCompleto(), aCargo, entregados, aTiempo,
                    entregados - aTiempo, redondear(porcentaje)};
                total++;
            }
        }
        return recortar(filas, total);
    }

    // 4) Ingresos y margen de pedidos entregados en el rango (fecha de despacho), sin IGV.
    //    El descuento del cliente se reparte entre las prendas sueltas en proporción a su importe.
    //    Columnas: Concepto, Cantidad, Ingreso, Costo, Margen, % Margen
    public Object[][] ingresosYMargen(LocalDate desde, LocalDate hasta) {
        String[] conceptos = new String[20];
        int[] cantidades = new int[20];
        double[] ingresos = new double[20];
        double[] costos = new double[20];
        int total = 0;

        for (int i = 0; i < gestionPedidos.getTotalPedidos(); i++) {
            Pedido p = gestionPedidos.getPedidos()[i];
            if (!p.getEstado().equals(Pedido.ENTREGADO) || !enRango(p.getFechaDespacho(), desde, hasta)) {
                continue;
            }
            double subtotalPrendas = p.getSubtotalPorTipo("Prenda");
            double factor = subtotalPrendas == 0 ? 1 : (subtotalPrendas - p.calcularDescuento()) / subtotalPrendas;
            for (int j = 0; j < p.getTotalItems(); j++) {
                ItemPedido item = p.getItems()[j];
                String concepto;
                double ingreso = item.calcularSubtotal();
                if (item.getTipoItem().equals("Prenda")) {
                    concepto = ((DetallePrenda) item).getModelo().getTipo();   // Polo, Jean, Casaca
                    ingreso = ingreso * factor;
                } else if (item.getTipoItem().equals("Servicio")) {
                    concepto = "Servicios de acabado";
                } else {
                    concepto = "Paquetes promocionales";
                }
                int pos = -1;
                for (int k = 0; k < total; k++) {
                    if (conceptos[k].equals(concepto)) {
                        pos = k;
                    }
                }
                if (pos == -1) {
                    pos = total;
                    conceptos[total] = concepto;
                    total++;
                }
                cantidades[pos] += item.getCantidad();
                ingresos[pos] += ingreso;
                costos[pos] += item.calcularCosto();
            }
        }
        Object[][] filas = new Object[total][];
        for (int k = 0; k < total; k++) {
            double margen = ingresos[k] - costos[k];
            double porcentaje = ingresos[k] == 0 ? 0 : margen * 100 / ingresos[k];
            filas[k] = new Object[]{conceptos[k], cantidades[k], redondear(ingresos[k]), redondear(costos[k]),
                redondear(margen), redondear(porcentaje)};
        }
        return filas;
    }

    // Suma una columna numérica de un reporte (para la fila de totales)
    public static double sumarColumna(Object[][] filas, int columna) {
        double suma = 0;
        for (int i = 0; i < filas.length; i++) {
            suma += ((Number) filas[i][columna]).doubleValue();
        }
        return suma;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static String[] tallas() {
        return ModeloPrenda.TALLAS;
    }
}
