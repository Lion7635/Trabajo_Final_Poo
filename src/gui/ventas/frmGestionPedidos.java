package gui.ventas;

import clases.pedidos.Pedido;
import gestion.GestionPedidos;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * RF06/RF07 - Confirmación con adelanto, despacho, facturación y cancelación de pedidos.
 * Formulario creado con el GUI Builder de NetBeans (pestaña Design).
 */
public class frmGestionPedidos extends FormularioBase {

    private GestionPedidos gestion = SistemaTextil.getGestionPedidos();
    private DefaultTableModel modelo;

    /**
     * Creates new form frmGestionPedidos
     */
    public frmGestionPedidos() {
        initComponents();
        // Primero el modelo de la tabla: al llenar el combo se dispara listar()
        modelo = crearModelo(new String[]{"N°", "Registro", "Cliente", "Tipo", "Entrega", "Prendas", "Total", "Adelanto", "Estado", "Vendedor"});
        configurarTabla(tblPedidos, modelo);
        tblPedidos.getColumnModel().getColumn(2).setPreferredWidth(230);
        cboFiltro.addItem("Activos");
        for (int i = 0; i < Pedido.ETAPAS_ACTIVAS.length; i++) {
            cboFiltro.addItem(Pedido.ETAPAS_ACTIVAS[i]);
        }
        cboFiltro.addItem(Pedido.ENTREGADO);
        cboFiltro.addItem(Pedido.CANCELADO);
        cboFiltro.addItem("Todos");
        cboFiltro.setSelectedIndex(0);
        listar();
        setLocationRelativeTo(null);
    }

    @Override
    protected void refrescar() {
        int numero = numeroSeleccionado();
        listar();
        seleccionar(numero);
    }

    void listar() {
        String filtro = (String) cboFiltro.getSelectedItem();
        modelo.setRowCount(0);
        for (int i = gestion.getTotalPedidos() - 1; i >= 0; i--) {   // los más recientes primero
            Pedido p = gestion.getPedidos()[i];
            boolean mostrar = filtro.equals("Todos") || p.getEstado().equals(filtro)
                    || (filtro.equals("Activos") && !p.estaFinalizado());
            if (mostrar) {
                modelo.addRow(new Object[]{p.getNumero(), Validador.fecha(p.getFechaRegistro()), p.getCliente().getNombreCompleto(),
                    p.getCliente().getTipo(), Validador.fecha(p.getFechaEntrega()), p.getTotalPrendas(),
                    Validador.redondear(p.calcularTotal()), Validador.redondear(p.getAdelanto()), p.getEstado(),
                    p.getEjecutivo().getNombres()});
            }
        }
        txtDetalle.setText("");
    }

    private int numeroSeleccionado() {
        int fila = tblPedidos.getSelectedRow();
        if (fila < 0) {
            return -1;
        }
        return (Integer) modelo.getValueAt(fila, 0);
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

    private void mostrarDetalle() {
        Pedido p = gestion.buscarPorNumero(numeroSeleccionado());
        if (p != null) {
            txtDetalle.setText(textoPedido(p));
            txtDetalle.setCaretPosition(0);
        }
    }

    private void despuesDeAccion(Pedido p) {
        listar();
        seleccionar(p.getNumero());
    }

    private void confirmarPedido() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        double sugerido = Math.ceil(p.calcularAdelantoRequerido() * 100) / 100.0;
        String texto = JOptionPane.showInputDialog(this, "Pedido " + p.getNumeroTexto() + " · Total "
                + Validador.dinero(p.calcularTotal()) + "\nMonto del adelanto (mínimo 50%):", String.format("%.2f", sugerido));
        if (texto == null) {
            return;
        }
        if (!Validador.esDecimal(texto)) {
            error("Ingrese un monto válido.");
            return;
        }
        String r = gestion.confirmar(p, Validador.leerDecimal(texto), SistemaTextil.getUsuarioActual(), LocalDate.now());
        if (r != null) {
            error(r);
            return;
        }
        mensaje("Adelanto registrado. El pedido " + p.getNumeroTexto() + " pasó a «Confirmado».");
        despuesDeAccion(p);
    }

    // RF07: si se pide factura se exige RUC y razón social
    private void despachar() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        if (!p.getEstado().equals(Pedido.LISTO_DESPACHO)) {
            error("Solo se despachan pedidos en estado «Listo para Despacho».");
            return;
        }
        JComboBox<String> cboTipo = new JComboBox<>(new String[]{"Boleta", "Factura"});
        JTextField txtRuc = new JTextField(12);
        JTextField txtRazon = new JTextField(24);
        if (p.getCliente().tieneRuc()) {
            cboTipo.setSelectedItem("Factura");
            txtRuc.setText(p.getCliente().getDocumento());
            txtRazon.setText(p.getCliente().getNombres());
        }
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Saldo a cobrar:"));
        panel.add(new JLabel(Validador.dinero(p.calcularSaldo())));
        panel.add(new JLabel("Comprobante:"));
        panel.add(cboTipo);
        panel.add(new JLabel("RUC (solo factura):"));
        panel.add(txtRuc);
        panel.add(new JLabel("Razón social (solo factura):"));
        panel.add(txtRazon);
        int op = JOptionPane.showConfirmDialog(this, panel, "Despacho de " + p.getNumeroTexto(), JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) {
            return;
        }
        String r = gestion.despachar(p, (String) cboTipo.getSelectedItem(), txtRuc.getText().trim(), txtRazon.getText(),
                SistemaTextil.getUsuarioActual(), LocalDate.now());
        if (r != null) {
            error(r);
            return;
        }
        despuesDeAccion(p);
        mostrarTexto(this, p.getComprobante().getTipo() + " " + p.getComprobante().getNumero(), p.getComprobante().generarTexto());
    }

    private void cancelar() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        if (p.estaFinalizado()) {
            error("El pedido está " + p.getEstado() + " y ya no puede modificarse.");
            return;
        }
        String aviso = p.yaFueCortado()
                ? "La tela ya fue cortada: el adelanto de " + Validador.dinero(p.getAdelanto()) + " quedará RETENIDO."
                : "Aún no se corta la tela: se DEVOLVERÁ el adelanto de " + Validador.dinero(p.getAdelanto()) + ".";
        String motivo = JOptionPane.showInputDialog(this, "¿Cancelar el pedido " + p.getNumeroTexto() + "?\n" + aviso
                + "\n\nMotivo de la cancelación:");
        if (motivo == null) {
            return;
        }
        String r = gestion.cancelar(p, motivo, SistemaTextil.getUsuarioActual(), LocalDate.now());
        if (r != null) {
            error(r);
            return;
        }
        mensaje("Pedido cancelado. Adelanto: " + p.getSituacionAdelanto() + ".");
        despuesDeAccion(p);
    }

    private void verComprobante() {
        Pedido p = pedidoSeleccionado();
        if (p == null) {
            return;
        }
        if (p.getComprobante() == null) {
            error("El pedido aún no tiene comprobante (se emite al despachar).");
            return;
        }
        mostrarTexto(this, p.getComprobante().getTipo() + " " + p.getComprobante().getNumero(), p.getComprobante().generarTexto());
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
        lblFiltro = new javax.swing.JLabel();
        cboFiltro = new javax.swing.JComboBox<>();
        btnConfirmar = new javax.swing.JButton();
        btnDespachar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnComprobante = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        scrPedidos = new javax.swing.JScrollPane();
        tblPedidos = new javax.swing.JTable();
        pnlDetalle = new javax.swing.JPanel();
        scrDetalle = new javax.swing.JScrollPane();
        txtDetalle = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pedidos: adelantos, despacho y cobro - Sistema Textil Gamarra");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(30, 58, 95));
        lblTitulo.setText("Pedidos: adelantos, despacho y cobro");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 112, 128));
        lblSubtitulo.setText("Seleccione un pedido de la tabla para ver su detalle y aplicar una acción.");

        lblFiltro.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblFiltro.setText("Mostrar:");

        cboFiltro.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboFiltroActionPerformed(evt);
            }
        });

        btnConfirmar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnConfirmar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnConfirmar.setText("Confirmar (adelanto 50%)");
        btnConfirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmarActionPerformed(evt);
            }
        });

        btnDespachar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnDespachar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/reporte.png"))); // NOI18N
        btnDespachar.setText("Despachar y facturar");
        btnDespachar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDespacharActionPerformed(evt);
            }
        });

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/delete_remove.png"))); // NOI18N
        btnCancelar.setText("Cancelar pedido");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnComprobante.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnComprobante.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/consultar.png"))); // NOI18N
        btnComprobante.setText("Ver comprobante");
        btnComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnComprobanteActionPerformed(evt);
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
                .addComponent(lblFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(cboFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnDespachar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 20, Short.MAX_VALUE)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        pnlAccionesLayout.setVerticalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDespachar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void cboFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboFiltroActionPerformed
        listar();
    }//GEN-LAST:event_cboFiltroActionPerformed

    private void tblPedidosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPedidosMouseClicked
        mostrarDetalle();
    }//GEN-LAST:event_tblPedidosMouseClicked

    private void btnConfirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmarActionPerformed
        confirmarPedido();
    }//GEN-LAST:event_btnConfirmarActionPerformed

    private void btnDespacharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDespacharActionPerformed
        despachar();
    }//GEN-LAST:event_btnDespacharActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        cancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnComprobanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnComprobanteActionPerformed
        verComprobante();
    }//GEN-LAST:event_btnComprobanteActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnComprobante;
    private javax.swing.JButton btnConfirmar;
    private javax.swing.JButton btnDespachar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<String> cboFiltro;
    private javax.swing.JLabel lblFiltro;
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
