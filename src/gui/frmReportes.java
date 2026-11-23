package gui;

import gestion.GestionReportes;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF08 - Reportes gerenciales en tablas con totales (solo Administrador).
 */
public class frmReportes extends FormularioBase {

    private GestionReportes reportes = SistemaTextil.getGestionReportes();

    private JTextField txtDesdeProd = new JTextField(9);
    private JTextField txtHastaProd = new JTextField(9);
    private JTextField txtDesdeIng = new JTextField(9);
    private JTextField txtHastaIng = new JTextField(9);

    private DefaultTableModel mProduccion;
    private DefaultTableModel mConsumo;
    private DefaultTableModel mStock;
    private DefaultTableModel mVentas;
    private DefaultTableModel mJefes;
    private DefaultTableModel mIngresos;

    public frmReportes() {
        super("Reportes Gerenciales", 1150, 680);
        String hoy = Validador.fecha(LocalDate.now());
        String hace90 = Validador.fecha(LocalDate.now().minusDays(90));
        txtDesdeProd.setText(hace90);
        txtHastaProd.setText(hoy);
        txtDesdeIng.setText(hace90);
        txtHastaIng.setText(hoy);

        mProduccion = crearModelo(new String[]{"Modelo", "Tipo", "Color", "Talla", "Prendas"});
        mConsumo = crearModelo(new String[]{"Pedido", "Cliente", "Estado", "Prendas", "Tela neta (m)", "Merma (m)", "Tela total (m)", "Defectuosas", "% Defectuosas"});
        mStock = crearModelo(new String[]{"Clase", "Código", "Descripción", "Stock", "Mínimo", "Faltante", "Unidad"});
        mVentas = crearModelo(new String[]{"Ejecutivo de ventas", "Pedidos vendidos", "Cancelados", "Monto vendido (S/ con IGV)"});
        mJefes = crearModelo(new String[]{"Jefe de producción", "Pedidos a cargo", "Entregados", "A tiempo", "Con retraso", "% a tiempo"});
        mIngresos = crearModelo(new String[]{"Concepto", "Cantidad", "Ingreso (S/ sin IGV)", "Costo (S/)", "Margen (S/)", "% Margen"});

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Producción por modelo", panelConFechas(txtDesdeProd, txtHastaProd, mProduccion,
                "Pedidos registrados en el rango (sin cancelados), por modelo, color y talla"));
        pestanas.addTab("Consumo de tela y stock", panelDoble("Consumo de tela, merma y prendas defectuosas por pedido", mConsumo,
                "Telas e insumos por debajo del stock mínimo", mStock));
        pestanas.addTab("Productividad del personal", panelDoble("Pedidos vendidos por ejecutivo", mVentas,
                "Pedidos entregados a tiempo por jefe de producción", mJefes));
        pestanas.addTab("Ingresos y margen", panelConFechas(txtDesdeIng, txtHastaIng, mIngresos,
                "Pedidos entregados (fecha de despacho) en el rango. Descuentos de cliente aplicados a las prendas."));

        add(titulo("Reportes gerenciales"), BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        generarTodos();
    }

    @Override
    protected void refrescar() {
        generarTodos();
    }

    private JPanel panelConFechas(JTextField desde, JTextField hasta, DefaultTableModel modelo, String descripcion) {
        JButton btnGenerar = boton("Generar", "reporte.png");
        btnGenerar.addActionListener(e -> generarTodos());
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Desde (dd/mm/aaaa):"));
        filtros.add(desde);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(hasta);
        filtros.add(btnGenerar);
        filtros.add(new JLabel("   " + descripcion));
        JPanel p = new JPanel(new BorderLayout());
        p.add(filtros, BorderLayout.NORTH);
        p.add(new JScrollPane(tablaReporte(modelo)), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelDoble(String titulo1, DefaultTableModel m1, String titulo2, DefaultTableModel m2) {
        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder(titulo1));
        p1.add(new JScrollPane(tablaReporte(m1)), BorderLayout.CENTER);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder(titulo2));
        p2.add(new JScrollPane(tablaReporte(m2)), BorderLayout.CENTER);
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 6));
        p.add(p1);
        p.add(p2);
        return p;
    }

    private JTable tablaReporte(DefaultTableModel modelo) {
        JTable t = crearTabla(modelo);
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
        return t;
    }

    private void llenar(DefaultTableModel modelo, Object[][] filas) {
        modelo.setRowCount(0);
        for (int i = 0; i < filas.length; i++) {
            modelo.addRow(filas[i]);
        }
    }

    private void generarTodos() {
        LocalDate d1 = Validador.leerFecha(txtDesdeProd.getText());
        LocalDate h1 = Validador.leerFecha(txtHastaProd.getText());
        LocalDate d2 = Validador.leerFecha(txtDesdeIng.getText());
        LocalDate h2 = Validador.leerFecha(txtHastaIng.getText());
        if (d1 == null || h1 == null || d2 == null || h2 == null) {
            error("Revise las fechas (formato dd/mm/aaaa).");
            return;
        }
        if (d1.isAfter(h1) || d2.isAfter(h2)) {
            error("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
            return;
        }

        // 1) Producción
        Object[][] prod = reportes.produccionPorModelo(d1, h1);
        llenar(mProduccion, prod);
        mProduccion.addRow(new Object[]{"TOTAL", "", "", "", (int) GestionReportes.sumarColumna(prod, 4)});

        // 2) Consumo y stock
        Object[][] cons = reportes.consumoPorPedido();
        llenar(mConsumo, cons);
        double prendas = GestionReportes.sumarColumna(cons, 3);
        double defect = GestionReportes.sumarColumna(cons, 7);
        mConsumo.addRow(new Object[]{"TOTAL", "", "", (int) prendas,
            Validador.redondear(GestionReportes.sumarColumna(cons, 4)), Validador.redondear(GestionReportes.sumarColumna(cons, 5)),
            Validador.redondear(GestionReportes.sumarColumna(cons, 6)), (int) defect,
            prendas == 0 ? 0 : Validador.redondear(defect * 100 / prendas)});
        Object[][] stock = reportes.stockBajoMinimo();
        llenar(mStock, stock);
        mStock.addRow(new Object[]{"TOTAL", stock.length + " materiales bajo mínimo", "", "", "", "", ""});

        // 3) Productividad
        Object[][] ventas = reportes.ventasPorEjecutivo();
        llenar(mVentas, ventas);
        mVentas.addRow(new Object[]{"TOTAL", (int) GestionReportes.sumarColumna(ventas, 1),
            (int) GestionReportes.sumarColumna(ventas, 2), Validador.redondear(GestionReportes.sumarColumna(ventas, 3))});
        Object[][] jefes = reportes.entregasPorJefe();
        llenar(mJefes, jefes);
        double entregados = GestionReportes.sumarColumna(jefes, 2);
        double aTiempo = GestionReportes.sumarColumna(jefes, 3);
        mJefes.addRow(new Object[]{"TOTAL", (int) GestionReportes.sumarColumna(jefes, 1), (int) entregados, (int) aTiempo,
            (int) GestionReportes.sumarColumna(jefes, 4), entregados == 0 ? 0 : Validador.redondear(aTiempo * 100 / entregados)});

        // 4) Ingresos y margen
        Object[][] ing = reportes.ingresosYMargen(d2, h2);
        llenar(mIngresos, ing);
        double ingreso = GestionReportes.sumarColumna(ing, 2);
        double costo = GestionReportes.sumarColumna(ing, 3);
        mIngresos.addRow(new Object[]{"TOTAL", (int) GestionReportes.sumarColumna(ing, 1), Validador.redondear(ingreso),
            Validador.redondear(costo), Validador.redondear(ingreso - costo),
            ingreso == 0 ? 0 : Validador.redondear((ingreso - costo) * 100 / ingreso)});
    }
}
