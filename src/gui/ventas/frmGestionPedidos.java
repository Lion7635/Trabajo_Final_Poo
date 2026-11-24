package gui.ventas;

import clases.pedidos.Pedido;
import gestion.GestionPedidos;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF06 y RF07 - El Ejecutivo de Ventas confirma pedidos (adelanto), los cancela
 * y los despacha emitiendo la boleta o factura.
 */
public class frmGestionPedidos extends FormularioBase {

    private GestionPedidos gestion = SistemaTextil.getGestionPedidos();
    private DefaultTableModel modelo;
    private JTable tabla;
    private JTextArea txtDetalle = new JTextArea();
    private JComboBox<String> cboFiltro = new JComboBox<>();

    public frmGestionPedidos() {
        super("Pedidos: adelantos, despacho y cobro", 1250, 720);
        cboFiltro.addItem("Activos");
        for (int i = 0; i < Pedido.ETAPAS_ACTIVAS.length; i++) {
            cboFiltro.addItem(Pedido.ETAPAS_ACTIVAS[i]);
        }
        cboFiltro.addItem(Pedido.ENTREGADO);
        cboFiltro.addItem(Pedido.CANCELADO);
        cboFiltro.addItem("Todos");

        JButton btnConfirmar = boton("Confirmar (adelanto 50%)", "vcsnormal.png");
        JButton btnDespachar = boton("Despachar y facturar", "reporte.png");
        JButton btnCancelar = boton("Cancelar pedido", "delete_remove.png");
        JButton btnComprobante = boton("Ver comprobante", "consultar.png");
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Mostrar:"));
        filtros.add(cboFiltro);
        filtros.add(btnConfirmar);
        filtros.add(btnDespachar);
        filtros.add(btnCancelar);
        filtros.add(btnComprobante);

        modelo = crearModelo(new String[]{"N°", "Registro", "Cliente", "Tipo", "Entrega", "Prendas", "Total", "Adelanto", "Estado", "Vendedor"});
        tabla = crearTabla(modelo);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(230);

        txtDetalle.setEditable(false);
        txtDetalle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane spDetalle = new JScrollPane(txtDetalle);
        spDetalle.setPreferredSize(new Dimension(1200, 260));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabla), spDetalle);
        split.setResizeWeight(0.55);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Pedidos de producción"), BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        cboFiltro.addActionListener(e -> listar());
        tabla.getSelectionModel().addListSelectionListener(e -> mostrarDetalle());
        btnConfirmar.addActionListener(e -> confirmarPedido());
        btnDespachar.addActionListener(e -> despachar());
        btnCancelar.addActionListener(e -> cancelar());
        btnComprobante.addActionListener(e -> verComprobante());
        listar();
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
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return -1;
        }
        return (Integer) modelo.getValueAt(fila, 0);
    }

    private void seleccionar(int numero) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if ((Integer) modelo.getValueAt(i, 0) == numero) {
                tabla.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private Pedido pedidoSeleccionado() {
        Pedido p = gestion.buscarPorNumero(numeroSeleccionado());
        if (p == null) {
            error("Seleccione un pedido de la tabla.");
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
}
