package gui.produccion;

import clases.empleados.JefeProduccion;
import clases.inventario.Insumo;
import clases.inventario.Tela;
import clases.pedidos.Pedido;
import clases.prendas.ModeloPrenda;
import gestion.GestionPedidos;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF08 - Avance de producción y control de calidad (Jefe de Producción).
 * Formulario creado con el GUI Builder de NetBeans (pestaña Design).
 */
public class frmProduccion extends FormularioBase {

    private GestionPedidos gestion = SistemaTextil.getGestionPedidos();
    private JefeProduccion jefe;
    private DefaultTableModel modelo;

    /**
     * Creates new form frmProduccion
     */
    public frmProduccion(JefeProduccion jefe) {
        initComponents();
        this.jefe = jefe;
        lblSubtitulo.setText("Jefe de producción: " + jefe.getNombreCompleto()
                + "   ·   Flujo: Confirmado → En Corte → En Confección → (En Acabado) → Control de calidad → Listo para Despacho");
        modelo = crearModelo(new String[]{"N°", "Cliente", "Entrega", "Días restantes", "Prendas", "Estampado/Bordado", "Defectuosas", "Estado"});
        configurarTabla(tblPedidos, modelo);
        tblPedidos.getColumnModel().getColumn(1).setPreferredWidth(240);
        listar();
        setLocationRelativeTo(null);
    }

    @Override
    protected void refrescar() {
        int numero = numeroSeleccionado();
        listar();
        seleccionar(numero);
    }

    // Pedidos que están en planta: desde Confirmado hasta En Acabado
    void listar() {
        modelo.setRowCount(0);
        String[] estados = {Pedido.CONFIRMADO, Pedido.EN_CORTE, Pedido.EN_CONFECCION, Pedido.EN_ACABADO};
        LocalDate hoy = LocalDate.now();
        for (int e = 0; e < estados.length; e++) {
            Pedido[] lista = gestion.listarPorEstado(estados[e]);
            for (int i = 0; i < lista.length; i++) {
                Pedido p = lista[i];
                modelo.addRow(new Object[]{p.getNumero(), p.getCliente().getNombreCompleto(), Validador.fecha(p.getFechaEntrega()),
                    ChronoUnit.DAYS.between(hoy, p.getFechaEntrega()), p.getTotalPrendas(), p.requiereAcabado() ? "Sí" : "No",
                    p.getTotalDefectuosos(), p.getEstado()});
            }
        }
        txtDetalle.setText("");
    }

    private int numeroSeleccionado() {
        int fila = tblPedidos.getSelectedRow();
        return fila < 0 ? -1 : (Integer) modelo.getValueAt(fila, 0);
    }

    private void seleccionar(int numero) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if ((Integer) modelo.getValueAt(i, 0) == numero) {
                tblPedidos.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private Pedido pedidoSeleccionado() {
        Pedido p = gestion.buscarPorNumero(numeroSeleccionado());
        if (p == null) {
            error("Seleccione un pedido de la tblPedidos.");
        }
        return p;
    }

    // Detalle + materiales que se descontarán al cortar
    private void mostrarDetalle() {
        Pedido p = gestion.buscarPorNumero(numeroSeleccionado());
        if (p == null) {
            return;
        }
        String t = "MATERIALES DEL PEDIDO (tela con 5% de merma)\n";
        for (int i = 0; i < SistemaTextil.getGestionTelas().getTotalTelas(); i++) {
            Tela tela = SistemaTextil.getGestionTelas().getTelas()[i];
            double m = p.getMetrosConMerma(tela);
            if (m > 0) {
                t += String.format(" - %-38s %8.1f m   (stock actual %.1f m)%n", tela.getDescripcion(), m, tela.getStock());
            }
        }
        for (int i = 0; i < SistemaTextil.getGestionInsumos().getTotalInsumos(); i++) {
            Insumo in = SistemaTextil.getGestionInsumos().getInsumos()[i];
            double c = p.getCantidadInsumo(in);
            if (c > 0) {
                t += String.format(" - %-38s %8.1f %s (stock actual %.1f)%n", in.getNombre(), c, in.getUnidad(), in.getStock());
            }
        }
        txtDetalle.setText(t + "\n" + textoPedido(p));
        txtDetalle.setCaretPosition(0);
    }

    private void despuesDeAccion(Pedido p, String texto) {
        listar();
        seleccionar(p.getNumero());
        mensaje(texto);
    }

    private void iniciarCorte() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        if (!confirmar("¿Iniciar el corte del pedido " + p.getNumeroTexto() + "?\nSe descontarán la tela y los insumos del inventario.")) {
            return;
        }
        String r = gestion.iniciarCorte(p, jefe, LocalDate.now());
        if (r != null) {
            error(r);
            return;
        }
        despuesDeAccion(p, String.format("Corte iniciado. Se descontaron %.1f m de tela (merma %.1f m).",
                p.getMetrosConsumidos(), p.getMetrosMerma()));
    }

    private void registrarAvance() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        String r = gestion.registrarAvance(p, jefe, LocalDate.now());
        if (r != null) {
            error(r);
            return;
        }
        despuesDeAccion(p, "El pedido " + p.getNumeroTexto() + " pasó a «" + p.getEstado() + "».");
    }

    // Se registran las prendas defectuosas por talla
    private void controlCalidad() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        if (!gestion.puedeControlCalidad(p)) {
            error(p.getEstado().equals(Pedido.EN_CONFECCION)
                    ? "Este pedido lleva estampado/bordado: primero registre el avance a «En Acabado»."
                    : "El control de calidad se registra al terminar la confección o el acabado.");
            return;
        }
        JTextField[] campos = new JTextField[ModeloPrenda.TALLAS.length];
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Talla (prendas)"));
        panel.add(new JLabel("Defectuosas"));
        for (int i = 0; i < campos.length; i++) {
            campos[i] = new JTextField("0", 5);
            campos[i].setEnabled(p.getPrendasPorTalla(i) > 0);
            panel.add(new JLabel(ModeloPrenda.TALLAS[i] + " (" + p.getPrendasPorTalla(i) + ")"));
            panel.add(campos[i]);
        }
        int op = JOptionPane.showConfirmDialog(this, panel, "Control de calidad " + p.getNumeroTexto(), JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) {
            return;
        }
        int[] defectuosos = new int[campos.length];
        for (int i = 0; i < campos.length; i++) {
            if (!Validador.esEntero(campos[i].getText())) {
                error("Ingrese cantidades enteras.");
                return;
            }
            defectuosos[i] = Validador.leerEntero(campos[i].getText());
        }
        String r = gestion.registrarControlCalidad(p, defectuosos, jefe, LocalDate.now());
        if (r != null) {
            error(r);
            return;
        }
        if (p.getEstado().equals(Pedido.LISTO_DESPACHO)) {
            despuesDeAccion(p, "Todas las prendas fueron aprobadas. El pedido está «Listo para Despacho».");
        } else {
            despuesDeAccion(p, "Se registraron prendas defectuosas: vuelven a «En Confección» para rehacerse.");
        }
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
        pnlAcciones = new javax.swing.JPanel();
        btnCorte = new javax.swing.JButton();
        btnAvance = new javax.swing.JButton();
        btnCalidad = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        scrPedidos = new javax.swing.JScrollPane();
        tblPedidos = new javax.swing.JTable();
        pnlDetalle = new javax.swing.JPanel();
        scrDetalle = new javax.swing.JScrollPane();
        txtDetalle = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Avance de Producción y Control de Calidad - Sistema Textil Gamarra");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(30, 58, 95));
        lblTitulo.setText("Avance de Producción y Control de Calidad");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 112, 128));
        lblSubtitulo.setText("Jefe de producción");

        btnCorte.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnCorte.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/edit.png"))); // NOI18N
        btnCorte.setText("Iniciar corte");
        btnCorte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCorteActionPerformed(evt);
            }
        });

        btnAvance.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAvance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/update_rotation.png"))); // NOI18N
        btnAvance.setText("Registrar avance");
        btnAvance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAvanceActionPerformed(evt);
            }
        });

        btnCalidad.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnCalidad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnCalidad.setText("Control de calidad");
        btnCalidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalidadActionPerformed(evt);
            }
        });

        btnSalir.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnSalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/cerrar.png"))); // NOI18N
        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        pnlAcciones.setBackground(new java.awt.Color(236, 242, 249));
        pnlAcciones.setBorder(javax.swing.BorderFactory.createTitledBorder("Acciones"));
        javax.swing.GroupLayout pnlAccionesLayout = new javax.swing.GroupLayout(pnlAcciones);
        pnlAcciones.setLayout(pnlAccionesLayout);
        pnlAccionesLayout.setHorizontalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(btnCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnAvance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 20, Short.MAX_VALUE)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        pnlAccionesLayout.setVerticalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCorte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAvance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCalidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7))
        );

        tblPedidos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblPedidos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPedidosMouseClicked(evt);
            }
        });

        scrPedidos.setViewportView(tblPedidos);

        txtDetalle.setEditable(false);
        txtDetalle.setColumns(20);
        txtDetalle.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtDetalle.setRows(5);

        scrDetalle.setViewportView(txtDetalle);

        pnlDetalle.setBackground(new java.awt.Color(236, 242, 249));
        pnlDetalle.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalle del pedido seleccionado"));
        javax.swing.GroupLayout pnlDetalleLayout = new javax.swing.GroupLayout(pnlDetalle);
        pnlDetalle.setLayout(pnlDetalleLayout);
        pnlDetalleLayout.setHorizontalGroup(
            pnlDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(scrDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );
        pnlDetalleLayout.setVerticalGroup(
            pnlDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(scrDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlAcciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrPedidos, javax.swing.GroupLayout.DEFAULT_SIZE, 1150, Short.MAX_VALUE)
                    .addComponent(pnlDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(pnlAcciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrPedidos, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(pnlDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblPedidosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPedidosMouseClicked
        mostrarDetalle();
    }//GEN-LAST:event_tblPedidosMouseClicked

    private void btnCorteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCorteActionPerformed
        iniciarCorte();
    }//GEN-LAST:event_btnCorteActionPerformed

    private void btnAvanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAvanceActionPerformed
        registrarAvance();
    }//GEN-LAST:event_btnAvanceActionPerformed

    private void btnCalidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalidadActionPerformed
        controlCalidad();
    }//GEN-LAST:event_btnCalidadActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAvance;
    private javax.swing.JButton btnCalidad;
    private javax.swing.JButton btnCorte;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JScrollPane scrDetalle;
    private javax.swing.JScrollPane scrPedidos;
    private javax.swing.JTable tblPedidos;
    private javax.swing.JTextArea txtDetalle;
    // End of variables declaration//GEN-END:variables
}
