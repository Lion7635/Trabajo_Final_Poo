package gui;

import clases.Cliente;
import clases.EjecutivoVentas;
import clases.ItemPedido;
import clases.ModeloPrenda;
import clases.Paquete;
import clases.Pedido;
import clases.ServicioAcabado;
import clases.Tela;
import gestion.GestionPedidos;
import gestion.GestionTelas;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF05 - Registro de pedidos de producción (Ejecutivo de Ventas).
 * El pedido se arma en memoria y solo se guarda si pasa todas las validaciones.
 */
public class frmNuevoPedido extends FormularioBase {

    private GestionPedidos gestion = SistemaTextil.getGestionPedidos();
    private GestionTelas gestionTelas = SistemaTextil.getGestionTelas();
    private EjecutivoVentas ejecutivo;
    private Pedido pedido;   // pedido en construcción

    private JComboBox<Cliente> cboCliente = new JComboBox<>();
    private JLabel lblTipoCliente = new JLabel();
    private JTextField txtFechaEntrega = new JTextField(10);

    // Pestaña prenda
    private JComboBox<ModeloPrenda> cboModelo = new JComboBox<>();
    private JComboBox<String> cboColor = new JComboBox<>();
    private JComboBox<String> cboTalla = new JComboBox<>(ModeloPrenda.TALLAS);
    private JTextField txtCantPrenda = new JTextField(6);
    private JLabel lblStock = new JLabel(" ");
    // Pestaña servicio
    private JComboBox<ServicioAcabado> cboServicio = new JComboBox<>();
    private JTextField txtCantServicio = new JTextField(6);
    // Pestaña paquete
    private JComboBox<Paquete> cboPaquete = new JComboBox<>();
    private JComboBox<String> cboColorPaquete = new JComboBox<>();
    private JComboBox<String> cboTallaPaquete = new JComboBox<>(ModeloPrenda.TALLAS);
    private JTextField txtCantPaquete = new JTextField(6);
    private JLabel lblContenidoPaquete = new JLabel(" ");

    private DefaultTableModel modeloItems;
    private JTable tablaItems;
    private JTextArea txtResumen = new JTextArea(9, 30);

    public frmNuevoPedido(EjecutivoVentas ejecutivo) {
        super("Nuevo Pedido de Producción", 1150, 720);
        this.ejecutivo = ejecutivo;

        // ---- Cabecera ----
        txtFechaEntrega.setText(Validador.fecha(LocalDate.now().plusDays(14)));
        JPanel cabecera = panelFormulario(new String[]{"Cliente:", "Tipo / beneficio:", "Fecha de entrega (dd/mm/aaaa):"},
                new JComponent[]{cboCliente, lblTipoCliente, txtFechaEntrega});
        cabecera.setBorder(BorderFactory.createTitledBorder("Datos del pedido   ·   Mínimo "
                + GestionPedidos.PRENDAS_MINIMAS + " prendas y " + GestionPedidos.DIAS_MINIMOS_ENTREGA + " días de anticipación"));

        // ---- Pestañas para agregar ítems ----
        JButton btnAgregarPrenda = boton("Agregar prenda", "vcsnormal.png");
        JPanel tabPrenda = panelFormulario(new String[]{"Modelo:", "Color:", "Talla:", "Cantidad:", "Tela:", ""},
                new JComponent[]{cboModelo, cboColor, cboTalla, txtCantPrenda, lblStock, btnAgregarPrenda});
        JButton btnAgregarServicio = boton("Agregar servicio", "vcsnormal.png");
        JPanel tabServicio = panelFormulario(new String[]{"Servicio:", "Cantidad de prendas:", ""},
                new JComponent[]{cboServicio, txtCantServicio, btnAgregarServicio});
        JButton btnAgregarPaquete = boton("Agregar paquete", "vcsnormal.png");
        JPanel tabPaquete = panelFormulario(new String[]{"Paquete:", "Contenido:", "Color:", "Talla:", "Cantidad de paquetes:", ""},
                new JComponent[]{cboPaquete, lblContenidoPaquete, cboColorPaquete, cboTallaPaquete, txtCantPaquete, btnAgregarPaquete});
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Prendas", tabPrenda);
        pestanas.addTab("Servicios de acabado", tabServicio);
        pestanas.addTab("Paquetes promocionales", tabPaquete);
        pestanas.setBorder(BorderFactory.createTitledBorder("Agregar al pedido"));

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(cabecera, BorderLayout.NORTH);
        izquierda.add(pestanas, BorderLayout.CENTER);

        // ---- Resumen de montos ----
        txtResumen.setEditable(false);
        txtResumen.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtResumen.setBackground(new Color(245, 248, 252));
        JPanel panelResumen = new JPanel(new BorderLayout());
        panelResumen.setBorder(BorderFactory.createTitledBorder("Resumen (montos sin IGV + IGV)"));
        panelResumen.add(new JScrollPane(txtResumen), BorderLayout.CENTER);

        JPanel arriba = new JPanel(new GridLayout(1, 2, 8, 0));
        arriba.add(izquierda);
        arriba.add(panelResumen);

        // ---- Tabla de ítems ----
        modeloItems = crearModelo(new String[]{"#", "Tipo", "Descripción", "Cantidad", "Prendas", "P. Unit.", "Subtotal"});
        tablaItems = crearTabla(modeloItems);
        tablaItems.getColumnModel().getColumn(0).setMaxWidth(35);
        tablaItems.getColumnModel().getColumn(2).setPreferredWidth(420);

        JButton btnQuitar = boton("Quitar ítem", "delete_remove.png");
        JButton btnStock = boton("Ver stock de telas", "consultar.png");
        JButton btnValidar = boton("Validar", "update_rotation.png");
        JButton btnRegistrar = boton("Registrar pedido", "vcsnormal.png");
        JButton btnNuevo = boton("Nuevo", "archivo.png");
        JPanel botones = new JPanel(new FlowLayout());
        botones.add(btnQuitar);
        botones.add(btnStock);
        botones.add(btnValidar);
        botones.add(btnRegistrar);
        botones.add(btnNuevo);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createTitledBorder("Detalle del pedido"));
        centro.add(new JScrollPane(tablaItems), BorderLayout.CENTER);
        centro.add(botones, BorderLayout.SOUTH);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Nuevo pedido · Vendedor: " + ejecutivo.getNombreCompleto()), BorderLayout.NORTH);
        norte.add(arriba, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        // ---- Eventos ----
        cboCliente.addActionListener(e -> cambiarCliente());
        cboModelo.addActionListener(e -> cargarColoresModelo());
        cboColor.addActionListener(e -> mostrarStockTela());
        cboPaquete.addActionListener(e -> cargarColoresPaquete());
        btnAgregarPrenda.addActionListener(e -> agregarPrenda());
        btnAgregarServicio.addActionListener(e -> agregarServicio());
        btnAgregarPaquete.addActionListener(e -> agregarPaquete());
        btnQuitar.addActionListener(e -> quitarItem());
        btnStock.addActionListener(e -> new frmStockTelas().setVisible(true));
        btnValidar.addActionListener(e -> validar());
        btnRegistrar.addActionListener(e -> registrar());
        btnNuevo.addActionListener(e -> nuevoPedido());

        cargarCombos();
        nuevoPedido();
    }

    private void cargarCombos() {
        cboCliente.removeAllItems();
        for (int i = 0; i < SistemaTextil.getGestionClientes().getTotalClientes(); i++) {
            cboCliente.addItem(SistemaTextil.getGestionClientes().getClientes()[i]);
        }
        cboModelo.removeAllItems();
        ModeloPrenda[] activos = SistemaTextil.getGestionModelos().listarActivos();   // descontinuados no se ofrecen
        for (int i = 0; i < activos.length; i++) {
            cboModelo.addItem(activos[i]);
        }
        cboServicio.removeAllItems();
        for (int i = 0; i < SistemaTextil.getGestionServicios().getTotalServicios(); i++) {
            cboServicio.addItem(SistemaTextil.getGestionServicios().getServicios()[i]);
        }
        cboPaquete.removeAllItems();
        for (int i = 0; i < SistemaTextil.getGestionPaquetes().getTotalPaquetes(); i++) {
            Paquete p = SistemaTextil.getGestionPaquetes().getPaquetes()[i];
            if (p.estaDisponible()) {
                cboPaquete.addItem(p);
            }
        }
    }

    private void nuevoPedido() {
        pedido = new Pedido((Cliente) cboCliente.getSelectedItem(), ejecutivo, LocalDate.now(), null);
        cambiarCliente();
        txtFechaEntrega.setText(Validador.fecha(LocalDate.now().plusDays(14)));
        actualizarVista();
    }

    private void cambiarCliente() {
        Cliente c = (Cliente) cboCliente.getSelectedItem();
        if (c != null && pedido != null) {
            pedido.setCliente(c);
            lblTipoCliente.setText(c.getTipo() + " · " + c.getBeneficio());
            actualizarVista();
        }
    }

    // Colores disponibles para la tela principal del modelo
    private void cargarColoresModelo() {
        cboColor.removeAllItems();
        ModeloPrenda m = (ModeloPrenda) cboModelo.getSelectedItem();
        if (m != null) {
            String[] colores = gestionTelas.getColores(m.getTipoTela());
            for (int i = 0; i < colores.length; i++) {
                cboColor.addItem(colores[i]);
            }
        }
        mostrarStockTela();
    }

    // RF10: muestra el stock de la tela antes de agregar la prenda
    private void mostrarStockTela() {
        ModeloPrenda m = (ModeloPrenda) cboModelo.getSelectedItem();
        String color = (String) cboColor.getSelectedItem();
        if (m == null || color == null) {
            lblStock.setText(" ");
            return;
        }
        Tela t = gestionTelas.buscarPorTipoYColor(m.getTipoTela(), color);
        double disponible = t.getStock() - gestion.metrosComprometidos(t, null);
        lblStock.setText(String.format("%s · stock %.1f m, disponible %.1f m", t.getDescripcion(), t.getStock(), disponible));
    }

    // Para un paquete solo se ofrecen colores que existen en la tela de TODOS sus modelos
    private void cargarColoresPaquete() {
        cboColorPaquete.removeAllItems();
        Paquete p = (Paquete) cboPaquete.getSelectedItem();
        if (p == null) {
            lblContenidoPaquete.setText(" ");
            return;
        }
        lblContenidoPaquete.setText(p.getContenido() + " · " + Validador.dinero(p.getPrecioPromocional()));
        String[] colores = gestionTelas.getColores(null);
        for (int i = 0; i < colores.length; i++) {
            boolean sirve = true;
            for (int j = 0; j < p.getTotalModelos(); j++) {
                if (gestionTelas.buscarPorTipoYColor(p.getModelos()[j].getTipoTela(), colores[i]) == null) {
                    sirve = false;
                }
            }
            if (sirve) {
                cboColorPaquete.addItem(colores[i]);
            }
        }
    }

    private int leerCantidad(JTextField campo) {
        if (!Validador.esEntero(campo.getText()) || Validador.leerEntero(campo.getText()) <= 0) {
            error("Ingrese una cantidad entera mayor a 0.");
            return -1;
        }
        return Validador.leerEntero(campo.getText());
    }

    private void agregarPrenda() {
        ModeloPrenda m = (ModeloPrenda) cboModelo.getSelectedItem();
        String color = (String) cboColor.getSelectedItem();
        int cantidad = leerCantidad(txtCantPrenda);
        if (m == null || color == null || cantidad < 0) {
            return;
        }
        String r = gestion.crearDetallePrenda(pedido, m, color, (String) cboTalla.getSelectedItem(), cantidad);
        if (r != null) {
            error(r);
            return;
        }
        txtCantPrenda.setText("");
        actualizarVista();
    }

    private void agregarServicio() {
        ServicioAcabado s = (ServicioAcabado) cboServicio.getSelectedItem();
        int cantidad = leerCantidad(txtCantServicio);
        if (s == null || cantidad < 0) {
            return;
        }
        String r = gestion.crearDetalleServicio(pedido, s, cantidad);
        if (r != null) {
            error(r);
            return;
        }
        actualizarVista();
    }

    private void agregarPaquete() {
        Paquete p = (Paquete) cboPaquete.getSelectedItem();
        String color = (String) cboColorPaquete.getSelectedItem();
        int cantidad = leerCantidad(txtCantPaquete);
        if (p == null || color == null || cantidad < 0) {
            if (p != null && color == null) {
                error("No hay un color común para todas las telas del paquete.");
            }
            return;
        }
        String r = gestion.crearDetallePaquete(pedido, p, color, (String) cboTallaPaquete.getSelectedItem(), cantidad);
        if (r != null) {
            error(r);
            return;
        }
        txtCantPaquete.setText("");
        actualizarVista();
    }

    private void quitarItem() {
        int fila = tablaItems.getSelectedRow();
        if (fila < 0) {
            error("Seleccione el ítem a quitar.");
            return;
        }
        pedido.quitarItem(fila);
        actualizarVista();
    }

    // Recorre los ítems del pedido (polimorfismo: cada ítem calcula su subtotal)
    private void actualizarVista() {
        modeloItems.setRowCount(0);
        for (int i = 0; i < pedido.getTotalItems(); i++) {
            ItemPedido item = pedido.getItems()[i];
            modeloItems.addRow(new Object[]{i + 1, item.getTipoItem(), item.getDescripcion(), item.getCantidad(),
                item.getTotalPrendas(), item.getPrecioUnitario(), Validador.redondear(item.calcularSubtotal())});
        }
        txtCantServicio.setText(String.valueOf(pedido.getTotalPrendas()));
        String r = "";
        r += String.format("Total de prendas ........ %10d%n", pedido.getTotalPrendas());
        r += String.format("Prendas ................. %14s%n", Validador.dinero(pedido.getSubtotalPorTipo("Prenda")));
        r += String.format("(-) Descuento cliente ... %14s%n", Validador.dinero(pedido.calcularDescuento()));
        r += String.format("Servicios de acabado .... %14s%n", Validador.dinero(pedido.getSubtotalPorTipo("Servicio")));
        r += String.format("Paquetes ................ %14s%n", Validador.dinero(pedido.getSubtotalPorTipo("Paquete")));
        r += String.format("Subtotal ................ %14s%n", Validador.dinero(pedido.calcularSubtotal()));
        r += String.format("IGV 18%% ................. %14s%n", Validador.dinero(pedido.calcularIgv()));
        r += String.format("TOTAL ................... %14s%n", Validador.dinero(pedido.calcularTotal()));
        r += String.format("Adelanto 50%% ............ %14s%n", Validador.dinero(pedido.calcularAdelantoRequerido()));
        r += pedido.requiereAcabado() ? "Pasará por la etapa En Acabado." : "";
        txtResumen.setText(r);
    }

    private boolean leerFechaEntrega() {
        LocalDate fecha = Validador.leerFecha(txtFechaEntrega.getText());
        if (fecha == null) {
            error("Fecha de entrega inválida. Use el formato dd/mm/aaaa.");
            return false;
        }
        pedido.setFechaEntrega(fecha);
        return true;
    }

    private void validar() {
        if (!leerFechaEntrega()) {
            return;
        }
        String r = gestion.validar(pedido, LocalDate.now());
        if (r == null) {
            mensaje("El pedido cumple todas las validaciones (tela, insumos, capacidad de planta, mínimo de prendas y fecha).");
        } else {
            JOptionPane.showMessageDialog(this, "El pedido NO puede registrarse:\n\n" + r, "Pedido rechazado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void registrar() {
        if (!leerFechaEntrega()) {
            return;
        }
        String r = gestion.registrar(pedido, LocalDate.now());
        if (r != null) {
            JOptionPane.showMessageDialog(this, "El pedido NO se registró:\n\n" + r, "Pedido rechazado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        mensaje("Pedido " + pedido.getNumeroTexto() + " registrado en estado «Registrado».\n"
                + "Total: " + Validador.dinero(pedido.calcularTotal()) + "\n"
                + "Adelanto requerido (50%) para confirmarlo: " + Validador.dinero(pedido.calcularAdelantoRequerido()));
        nuevoPedido();
    }
}
