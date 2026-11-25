package gui.consultas;

import gestion.GestionReportes;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.Font;
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
 * RF10 - Reportes gerenciales.
 * Formulario creado con el GUI Builder de NetBeans (pestaña Design).
 */
public class frmReportes extends FormularioBase {

    private GestionReportes reportes = SistemaTextil.getGestionReportes();

    private DefaultTableModel mProduccion;
    private DefaultTableModel mConsumo;
    private DefaultTableModel mStock;
    private DefaultTableModel mVentas;
    private DefaultTableModel mJefes;
    private DefaultTableModel mIngresos;

    /**
     * Creates new form frmReportes
     */
    public frmReportes() {
        initComponents();
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
        configurarTabla(tblProduccion, mProduccion);
        configurarTabla(tblConsumo, mConsumo);
        configurarTabla(tblStock, mStock);
        configurarTabla(tblVentas, mVentas);
        configurarTabla(tblJefes, mJefes);
        configurarTabla(tblIngresos, mIngresos);
        generarTodos();
        setLocationRelativeTo(null);
    }

    @Override
    protected void refrescar() {
        generarTodos();
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        tabReportes = new javax.swing.JTabbedPane();
        pnlProduccion = new javax.swing.JPanel();
        lblDesdeProd = new javax.swing.JLabel();
        txtDesdeProd = new javax.swing.JTextField();
        lblHastaProd = new javax.swing.JLabel();
        txtHastaProd = new javax.swing.JTextField();
        btnGenerarProd = new javax.swing.JButton();
        lblDescProd = new javax.swing.JLabel();
        scrProduccion = new javax.swing.JScrollPane();
        tblProduccion = new javax.swing.JTable();
        pnlConsumoStock = new javax.swing.JPanel();
        pnlConsumo = new javax.swing.JPanel();
        scrConsumo = new javax.swing.JScrollPane();
        tblConsumo = new javax.swing.JTable();
        pnlStock = new javax.swing.JPanel();
        scrStock = new javax.swing.JScrollPane();
        tblStock = new javax.swing.JTable();
        pnlProductividad = new javax.swing.JPanel();
        pnlVentas = new javax.swing.JPanel();
        scrVentas = new javax.swing.JScrollPane();
        tblVentas = new javax.swing.JTable();
        pnlJefes = new javax.swing.JPanel();
        scrJefes = new javax.swing.JScrollPane();
        tblJefes = new javax.swing.JTable();
        pnlIngresos = new javax.swing.JPanel();
        lblDesdeIng = new javax.swing.JLabel();
        txtDesdeIng = new javax.swing.JTextField();
        lblHastaIng = new javax.swing.JLabel();
        txtHastaIng = new javax.swing.JTextField();
        btnGenerarIng = new javax.swing.JButton();
        lblDescIng = new javax.swing.JLabel();
        scrIngresos = new javax.swing.JScrollPane();
        tblIngresos = new javax.swing.JTable();
        btnSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Reportes Gerenciales - Sistema Textil Gamarra");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(30, 58, 95));
        lblTitulo.setText("Reportes Gerenciales");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 112, 128));
        lblSubtitulo.setText("Información para la toma de decisiones. Los totales aparecen en la última fila de cada tabla.");

        lblDesdeProd.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDesdeProd.setText("Desde (dd/mm/aaaa):");

        txtDesdeProd.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblHastaProd.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblHastaProd.setText("Hasta:");

        txtHastaProd.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnGenerarProd.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnGenerarProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte.png"))); // NOI18N
        btnGenerarProd.setText("Generar");
        btnGenerarProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarProdActionPerformed(evt);
            }
        });

        lblDescProd.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblDescProd.setForeground(new java.awt.Color(100, 112, 128));
        lblDescProd.setText("Pedidos registrados en el rango (sin cancelados), por modelo, color y talla");

        tblProduccion.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblProduccion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrProduccion.setViewportView(tblProduccion);

        pnlProduccion.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlProduccionLayout = new javax.swing.GroupLayout(pnlProduccion);
        pnlProduccion.setLayout(pnlProduccionLayout);
        pnlProduccionLayout.setHorizontalGroup(
            pnlProduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProduccionLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlProduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProduccionLayout.createSequentialGroup()
                        .addComponent(lblDesdeProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtDesdeProd, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(lblHastaProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtHastaProd, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(btnGenerarProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(lblDescProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrProduccion, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        pnlProduccionLayout.setVerticalGroup(
            pnlProduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProduccionLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlProduccionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDesdeProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDesdeProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHastaProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHastaProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDescProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(scrProduccion, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        tblConsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblConsumo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrConsumo.setViewportView(tblConsumo);

        pnlConsumo.setBackground(new java.awt.Color(236, 242, 249));
        pnlConsumo.setBorder(javax.swing.BorderFactory.createTitledBorder("Consumo de tela, merma y prendas defectuosas por pedido"));
        javax.swing.GroupLayout pnlConsumoLayout = new javax.swing.GroupLayout(pnlConsumo);
        pnlConsumo.setLayout(pnlConsumoLayout);
        pnlConsumoLayout.setHorizontalGroup(
            pnlConsumoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsumoLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(scrConsumo, javax.swing.GroupLayout.DEFAULT_SIZE, 1040, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        pnlConsumoLayout.setVerticalGroup(
            pnlConsumoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsumoLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(scrConsumo, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        tblStock.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrStock.setViewportView(tblStock);

        pnlStock.setBackground(new java.awt.Color(236, 242, 249));
        pnlStock.setBorder(javax.swing.BorderFactory.createTitledBorder("Telas e insumos por debajo del stock mínimo"));
        javax.swing.GroupLayout pnlStockLayout = new javax.swing.GroupLayout(pnlStock);
        pnlStock.setLayout(pnlStockLayout);
        pnlStockLayout.setHorizontalGroup(
            pnlStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStockLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(scrStock, javax.swing.GroupLayout.DEFAULT_SIZE, 1040, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        pnlStockLayout.setVerticalGroup(
            pnlStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStockLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(scrStock, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        pnlConsumoStock.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlConsumoStockLayout = new javax.swing.GroupLayout(pnlConsumoStock);
        pnlConsumoStock.setLayout(pnlConsumoStockLayout);
        pnlConsumoStockLayout.setHorizontalGroup(
            pnlConsumoStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsumoStockLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlConsumoStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlConsumo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );
        pnlConsumoStockLayout.setVerticalGroup(
            pnlConsumoStockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsumoStockLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(pnlConsumo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(pnlStock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        tblVentas.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrVentas.setViewportView(tblVentas);

        pnlVentas.setBackground(new java.awt.Color(236, 242, 249));
        pnlVentas.setBorder(javax.swing.BorderFactory.createTitledBorder("Pedidos vendidos por ejecutivo"));
        javax.swing.GroupLayout pnlVentasLayout = new javax.swing.GroupLayout(pnlVentas);
        pnlVentas.setLayout(pnlVentasLayout);
        pnlVentasLayout.setHorizontalGroup(
            pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVentasLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(scrVentas, javax.swing.GroupLayout.DEFAULT_SIZE, 1040, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        pnlVentasLayout.setVerticalGroup(
            pnlVentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVentasLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(scrVentas, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        tblJefes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblJefes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrJefes.setViewportView(tblJefes);

        pnlJefes.setBackground(new java.awt.Color(236, 242, 249));
        pnlJefes.setBorder(javax.swing.BorderFactory.createTitledBorder("Pedidos entregados a tiempo por jefe de producción"));
        javax.swing.GroupLayout pnlJefesLayout = new javax.swing.GroupLayout(pnlJefes);
        pnlJefes.setLayout(pnlJefesLayout);
        pnlJefesLayout.setHorizontalGroup(
            pnlJefesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlJefesLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(scrJefes, javax.swing.GroupLayout.DEFAULT_SIZE, 1040, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        pnlJefesLayout.setVerticalGroup(
            pnlJefesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlJefesLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(scrJefes, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        pnlProductividad.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlProductividadLayout = new javax.swing.GroupLayout(pnlProductividad);
        pnlProductividad.setLayout(pnlProductividadLayout);
        pnlProductividadLayout.setHorizontalGroup(
            pnlProductividadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductividadLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlProductividadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlJefes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );
        pnlProductividadLayout.setVerticalGroup(
            pnlProductividadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProductividadLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(pnlVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(pnlJefes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        lblDesdeIng.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDesdeIng.setText("Desde (dd/mm/aaaa):");

        txtDesdeIng.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblHastaIng.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblHastaIng.setText("Hasta:");

        txtHastaIng.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnGenerarIng.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnGenerarIng.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte.png"))); // NOI18N
        btnGenerarIng.setText("Generar");
        btnGenerarIng.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarIngActionPerformed(evt);
            }
        });

        lblDescIng.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblDescIng.setForeground(new java.awt.Color(100, 112, 128));
        lblDescIng.setText("Pedidos entregados (fecha de despacho) en el rango");

        tblIngresos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblIngresos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrIngresos.setViewportView(tblIngresos);

        pnlIngresos.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlIngresosLayout = new javax.swing.GroupLayout(pnlIngresos);
        pnlIngresos.setLayout(pnlIngresosLayout);
        pnlIngresosLayout.setHorizontalGroup(
            pnlIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIngresosLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlIngresosLayout.createSequentialGroup()
                        .addComponent(lblDesdeIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtDesdeIng, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(lblHastaIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtHastaIng, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(btnGenerarIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(lblDescIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        pnlIngresosLayout.setVerticalGroup(
            pnlIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIngresosLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlIngresosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDesdeIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDesdeIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHastaIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHastaIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDescIng, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(scrIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        tabReportes.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tabReportes.addTab("Producción por modelo", pnlProduccion);
        tabReportes.addTab("Consumo de tela y stock", pnlConsumoStock);
        tabReportes.addTab("Productividad del personal", pnlProductividad);
        tabReportes.addTab("Ingresos y margen", pnlIngresos);

        btnSalir.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnSalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/cerrar.png"))); // NOI18N
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabReportes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 20, Short.MAX_VALUE)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(tabReportes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarProdActionPerformed
        generarTodos();
    }//GEN-LAST:event_btnGenerarProdActionPerformed

    private void btnGenerarIngActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarIngActionPerformed
        generarTodos();
    }//GEN-LAST:event_btnGenerarIngActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerarIng;
    private javax.swing.JButton btnGenerarProd;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel lblDescIng;
    private javax.swing.JLabel lblDescProd;
    private javax.swing.JLabel lblDesdeIng;
    private javax.swing.JLabel lblDesdeProd;
    private javax.swing.JLabel lblHastaIng;
    private javax.swing.JLabel lblHastaProd;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlConsumo;
    private javax.swing.JPanel pnlConsumoStock;
    private javax.swing.JPanel pnlIngresos;
    private javax.swing.JPanel pnlJefes;
    private javax.swing.JPanel pnlProduccion;
    private javax.swing.JPanel pnlProductividad;
    private javax.swing.JPanel pnlStock;
    private javax.swing.JPanel pnlVentas;
    private javax.swing.JScrollPane scrConsumo;
    private javax.swing.JScrollPane scrIngresos;
    private javax.swing.JScrollPane scrJefes;
    private javax.swing.JScrollPane scrProduccion;
    private javax.swing.JScrollPane scrStock;
    private javax.swing.JScrollPane scrVentas;
    private javax.swing.JTabbedPane tabReportes;
    private javax.swing.JTable tblConsumo;
    private javax.swing.JTable tblIngresos;
    private javax.swing.JTable tblJefes;
    private javax.swing.JTable tblProduccion;
    private javax.swing.JTable tblStock;
    private javax.swing.JTable tblVentas;
    private javax.swing.JTextField txtDesdeIng;
    private javax.swing.JTextField txtDesdeProd;
    private javax.swing.JTextField txtHastaIng;
    private javax.swing.JTextField txtHastaProd;
    // End of variables declaration//GEN-END:variables
}
