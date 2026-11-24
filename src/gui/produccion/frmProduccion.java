package gui.produccion;

import clases.empleados.JefeProduccion;
import clases.inventario.Insumo;
import clases.inventario.Tela;
import clases.pedidos.Pedido;
import clases.prendas.ModeloPrenda;
import gestion.GestionPedidos;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.JButton;
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
 * RF06 - El Jefe de Producción registra el corte, el avance y el control de calidad.
 */
public class frmProduccion extends FormularioBase {

    private GestionPedidos gestion = SistemaTextil.getGestionPedidos();
    private JefeProduccion jefe;
    private DefaultTableModel modelo;
    private JTable tabla;
    private JTextArea txtDetalle = new JTextArea();

    public frmProduccion(JefeProduccion jefe) {
        super("Avance de Producción y Control de Calidad", 1200, 720);
        this.jefe = jefe;

        JButton btnCorte = boton("Iniciar corte", "edit.png");
        JButton btnAvance = boton("Registrar avance", "update_rotation.png");
        JButton btnCalidad = boton("Control de calidad", "vcsnormal.png");
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(btnCorte);
        acciones.add(btnAvance);
        acciones.add(btnCalidad);
        acciones.add(new JLabel("   Flujo: Confirmado → En Corte → En Confección → (En Acabado) → Control de calidad → Listo para Despacho"));

        modelo = crearModelo(new String[]{"N°", "Cliente", "Entrega", "Días restantes", "Prendas", "Estampado/Bordado", "Defectuosas", "Estado"});
        tabla = crearTabla(modelo);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(240);

        txtDetalle.setEditable(false);
        txtDetalle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane spDetalle = new JScrollPane(txtDetalle);
        spDetalle.setPreferredSize(new Dimension(1100, 300));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabla), spDetalle);
        split.setResizeWeight(0.45);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Producción · " + jefe.getNombreCompleto()), BorderLayout.NORTH);
        norte.add(acciones, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> mostrarDetalle());
        btnCorte.addActionListener(e -> iniciarCorte());
        btnAvance.addActionListener(e -> registrarAvance());
        btnCalidad.addActionListener(e -> controlCalidad());
        listar();
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
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : (Integer) modelo.getValueAt(fila, 0);
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
}
