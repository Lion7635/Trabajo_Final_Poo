package gui.ventas;

import clases.catalogo.Paquete;
import clases.catalogo.ServicioAcabado;
import clases.clientes.Cliente;
import clases.empleados.EjecutivoVentas;
import clases.inventario.Tela;
import clases.pedidos.ItemPedido;
import clases.pedidos.Pedido;
import clases.prendas.ModeloPrenda;
import gestion.GestionPedidos;
import gestion.GestionTelas;
import gestion.SistemaTextil;
import gui.FormularioBase;
import gui.consultas.frmStockTelas;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * RF05 - Registro de un nuevo pedido de producción (Ejecutivo de Ventas).
 * Formulario creado con el GUI Builder de NetBeans (pestaña Design).
 */
public class frmNuevoPedido extends FormularioBase {

    private GestionPedidos gestion = SistemaTextil.getGestionPedidos();
    private GestionTelas gestionTelas = SistemaTextil.getGestionTelas();
    private EjecutivoVentas ejecutivo;
    private Pedido pedido;   // pedido en construcción
    private DefaultTableModel modeloItems;

    /**
     * Creates new form frmNuevoPedido
     */
    public frmNuevoPedido(EjecutivoVentas ejecutivo) {
        initComponents();
        this.ejecutivo = ejecutivo;
        lblSubtitulo.setText("Vendedor: " + ejecutivo.getNombreCompleto() + "   ·   Mínimo "
                + GestionPedidos.PRENDAS_MINIMAS + " prendas y " + GestionPedidos.DIAS_MINIMOS_ENTREGA + " días de anticipación");
        txtFechaEntrega.setText(Validador.fecha(LocalDate.now().plusDays(14)));
        modeloItems = crearModelo(new String[]{"#", "Tipo", "Descripción", "Cantidad", "Prendas", "P. Unit.", "Subtotal"});
        configurarTabla(tblItems, modeloItems);
        tblItems.getColumnModel().getColumn(0).setMaxWidth(35);
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(420);
        cargarCombos();
        nuevoPedido();
        setLocationRelativeTo(null);
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
        int fila = tblItems.getSelectedRow();
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
        pnlPedido = new javax.swing.JPanel();
        lblCliente = new javax.swing.JLabel();
        cboCliente = new javax.swing.JComboBox<>();
        lblTipo = new javax.swing.JLabel();
        lblTipoCliente = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        txtFechaEntrega = new javax.swing.JTextField();
        tabAgregar = new javax.swing.JTabbedPane();
        pnlPrenda = new javax.swing.JPanel();
        lblModelo = new javax.swing.JLabel();
        cboModelo = new javax.swing.JComboBox<>();
        lblColor = new javax.swing.JLabel();
        cboColor = new javax.swing.JComboBox<>();
        lblTalla = new javax.swing.JLabel();
        cboTalla = new javax.swing.JComboBox<>();
        lblCantPrenda = new javax.swing.JLabel();
        txtCantPrenda = new javax.swing.JTextField();
        lblTela = new javax.swing.JLabel();
        lblStock = new javax.swing.JLabel();
        lblVacio1 = new javax.swing.JLabel();
        btnAgregarPrenda = new javax.swing.JButton();
        pnlServicio = new javax.swing.JPanel();
        lblServicio = new javax.swing.JLabel();
        cboServicio = new javax.swing.JComboBox<>();
        lblCantServicio = new javax.swing.JLabel();
        txtCantServicio = new javax.swing.JTextField();
        lblVacio2 = new javax.swing.JLabel();
        btnAgregarServicio = new javax.swing.JButton();
        pnlPaquete = new javax.swing.JPanel();
        lblPaquete = new javax.swing.JLabel();
        cboPaquete = new javax.swing.JComboBox<>();
        lblContenido = new javax.swing.JLabel();
        lblContenidoPaquete = new javax.swing.JLabel();
        lblColorPaquete = new javax.swing.JLabel();
        cboColorPaquete = new javax.swing.JComboBox<>();
        lblTallaPaquete = new javax.swing.JLabel();
        cboTallaPaquete = new javax.swing.JComboBox<>();
        lblCantPaquete = new javax.swing.JLabel();
        txtCantPaquete = new javax.swing.JTextField();
        lblVacio3 = new javax.swing.JLabel();
        btnAgregarPaquete = new javax.swing.JButton();
        pnlResumen = new javax.swing.JPanel();
        scrResumen = new javax.swing.JScrollPane();
        txtResumen = new javax.swing.JTextArea();
        pnlDetalle = new javax.swing.JPanel();
        scrItems = new javax.swing.JScrollPane();
        tblItems = new javax.swing.JTable();
        btnQuitar = new javax.swing.JButton();
        btnStock = new javax.swing.JButton();
        btnValidar = new javax.swing.JButton();
        btnRegistrar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Nuevo Pedido - Sistema Textil Gamarra");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(30, 58, 95));
        lblTitulo.setText("Nuevo Pedido de Producción");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 112, 128));
        lblSubtitulo.setText("Vendedor");

        lblCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCliente.setText("Cliente:");

        cboCliente.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboClienteActionPerformed(evt);
            }
        });

        lblTipo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTipo.setText("Tipo / beneficio:");

        lblTipoCliente.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTipoCliente.setForeground(new java.awt.Color(21, 128, 61));
        lblTipoCliente.setText("-");

        lblFecha.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblFecha.setText("Fecha de entrega (dd/mm/aaaa):");

        txtFechaEntrega.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        pnlPedido.setBackground(new java.awt.Color(236, 242, 249));
        pnlPedido.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del pedido"));
        javax.swing.GroupLayout pnlPedidoLayout = new javax.swing.GroupLayout(pnlPedido);
        pnlPedido.setLayout(pnlPedidoLayout);
        pnlPedidoLayout.setHorizontalGroup(
            pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPedidoLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTipoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFechaEntrega, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlPedidoLayout.setVerticalGroup(
            pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPedidoLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTipoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFechaEntrega, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        lblModelo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblModelo.setText("Modelo:");

        cboModelo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboModelo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboModeloActionPerformed(evt);
            }
        });

        lblColor.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblColor.setText("Color:");

        cboColor.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboColorActionPerformed(evt);
            }
        });

        lblTalla.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTalla.setText("Talla:");

        cboTalla.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboTalla.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "S", "M", "L", "XL" }));

        lblCantPrenda.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCantPrenda.setText("Cantidad:");

        txtCantPrenda.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblTela.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTela.setText("Tela:");

        lblStock.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblStock.setForeground(new java.awt.Color(100, 112, 128));
        lblStock.setText(" ");

        lblVacio1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblVacio1.setText("");

        btnAgregarPrenda.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAgregarPrenda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnAgregarPrenda.setText("Agregar prenda");
        btnAgregarPrenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarPrendaActionPerformed(evt);
            }
        });

        pnlPrenda.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlPrendaLayout = new javax.swing.GroupLayout(pnlPrenda);
        pnlPrenda.setLayout(pnlPrendaLayout);
        pnlPrendaLayout.setHorizontalGroup(
            pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrendaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblModelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTalla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboColor, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTalla, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCantPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVacio1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCantPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStock, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlPrendaLayout.setVerticalGroup(
            pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrendaLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblModelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboModelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlPrendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTalla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTalla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVacio1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarPrenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        lblServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblServicio.setText("Servicio:");

        cboServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblCantServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCantServicio.setText("Cantidad de prendas:");

        txtCantServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblVacio2.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblVacio2.setText("");

        btnAgregarServicio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAgregarServicio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnAgregarServicio.setText("Agregar servicio");
        btnAgregarServicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarServicioActionPerformed(evt);
            }
        });

        pnlServicio.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlServicioLayout = new javax.swing.GroupLayout(pnlServicio);
        pnlServicio.setLayout(pnlServicioLayout);
        pnlServicioLayout.setHorizontalGroup(
            pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServicioLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVacio2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlServicioLayout.setVerticalGroup(
            pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServicioLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCantServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlServicioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVacio2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        lblPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPaquete.setText("Paquete:");

        cboPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboPaquete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboPaqueteActionPerformed(evt);
            }
        });

        lblContenido.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblContenido.setText("Contenido:");

        lblContenidoPaquete.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblContenidoPaquete.setForeground(new java.awt.Color(100, 112, 128));
        lblContenidoPaquete.setText(" ");

        lblColorPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblColorPaquete.setText("Color:");

        cboColorPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblTallaPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTallaPaquete.setText("Talla:");

        cboTallaPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboTallaPaquete.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "S", "M", "L", "XL" }));

        lblCantPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCantPaquete.setText("Cantidad:");

        txtCantPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblVacio3.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblVacio3.setText("");

        btnAgregarPaquete.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAgregarPaquete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnAgregarPaquete.setText("Agregar paquete");
        btnAgregarPaquete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarPaqueteActionPerformed(evt);
            }
        });

        pnlPaquete.setBackground(new java.awt.Color(236, 242, 249));
        javax.swing.GroupLayout pnlPaqueteLayout = new javax.swing.GroupLayout(pnlPaquete);
        pnlPaquete.setLayout(pnlPaqueteLayout);
        pnlPaqueteLayout.setHorizontalGroup(
            pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaqueteLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblContenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblColorPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblContenidoPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboColorPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTallaPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVacio3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboTallaPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlPaqueteLayout.setVerticalGroup(
            pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaqueteLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTallaPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTallaPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblContenido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblContenidoPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlPaqueteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblColorPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboColorPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVacio3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarPaquete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        tabAgregar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tabAgregar.addTab("Prendas", pnlPrenda);
        tabAgregar.addTab("Servicios de acabado", pnlServicio);
        tabAgregar.addTab("Paquetes promocionales", pnlPaquete);

        txtResumen.setEditable(false);
        txtResumen.setBackground(new java.awt.Color(245, 248, 252));
        txtResumen.setColumns(30);
        txtResumen.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        txtResumen.setRows(9);

        scrResumen.setViewportView(txtResumen);

        pnlResumen.setBackground(new java.awt.Color(236, 242, 249));
        pnlResumen.setBorder(javax.swing.BorderFactory.createTitledBorder("Resumen (montos sin IGV + IGV)"));
        javax.swing.GroupLayout pnlResumenLayout = new javax.swing.GroupLayout(pnlResumen);
        pnlResumen.setLayout(pnlResumenLayout);
        pnlResumenLayout.setHorizontalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(scrResumen, 340, 340, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );
        pnlResumenLayout.setVerticalGroup(
            pnlResumenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(scrResumen, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        tblItems.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrItems.setViewportView(tblItems);

        btnQuitar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnQuitar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/delete_remove.png"))); // NOI18N
        btnQuitar.setText("Quitar ítem");
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });

        btnStock.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/search.png"))); // NOI18N
        btnStock.setText("Ver stock de telas");
        btnStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStockActionPerformed(evt);
            }
        });

        btnValidar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnValidar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/update_rotation.png"))); // NOI18N
        btnValidar.setText("Validar");
        btnValidar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidarActionPerformed(evt);
            }
        });

        btnRegistrar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnRegistrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnRegistrar.setText("Registrar pedido");
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
            }
        });

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/archivo.png"))); // NOI18N
        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
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

        pnlDetalle.setBackground(new java.awt.Color(236, 242, 249));
        pnlDetalle.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalle del pedido"));
        javax.swing.GroupLayout pnlDetalleLayout = new javax.swing.GroupLayout(pnlDetalle);
        pnlDetalle.setLayout(pnlDetalleLayout);
        pnlDetalleLayout.setHorizontalGroup(
            pnlDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrItems, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                    .addGroup(pnlDetalleLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(btnQuitar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnValidar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 20, Short.MAX_VALUE)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)))
                .addGap(8, 8, 8))
        );
        pnlDetalleLayout.setVerticalGroup(
            pnlDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(scrItems, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addGroup(pnlDetalleLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addGroup(pnlDetalleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnQuitar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnValidar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(7, 7, 7)))
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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlPedido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tabAgregar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addComponent(pnlResumen, 360, 360, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlPedido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(tabAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlResumen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8)
                .addComponent(pnlDetalle, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboClienteActionPerformed
        cambiarCliente();
    }//GEN-LAST:event_cboClienteActionPerformed

    private void cboModeloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboModeloActionPerformed
        cargarColoresModelo();
    }//GEN-LAST:event_cboModeloActionPerformed

    private void cboColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboColorActionPerformed
        mostrarStockTela();
    }//GEN-LAST:event_cboColorActionPerformed

    private void cboPaqueteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboPaqueteActionPerformed
        cargarColoresPaquete();
    }//GEN-LAST:event_cboPaqueteActionPerformed

    private void btnAgregarPrendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarPrendaActionPerformed
        agregarPrenda();
    }//GEN-LAST:event_btnAgregarPrendaActionPerformed

    private void btnAgregarServicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarServicioActionPerformed
        agregarServicio();
    }//GEN-LAST:event_btnAgregarServicioActionPerformed

    private void btnAgregarPaqueteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarPaqueteActionPerformed
        agregarPaquete();
    }//GEN-LAST:event_btnAgregarPaqueteActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        quitarItem();
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStockActionPerformed
        frmStockTelas frm = new frmStockTelas();
        frm.setLocationRelativeTo(this);
        frm.setVisible(true);
    }//GEN-LAST:event_btnStockActionPerformed

    private void btnValidarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValidarActionPerformed
        validar();
    }//GEN-LAST:event_btnValidarActionPerformed

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
        registrar();
    }//GEN-LAST:event_btnRegistrarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        nuevoPedido();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarPaquete;
    private javax.swing.JButton btnAgregarPrenda;
    private javax.swing.JButton btnAgregarServicio;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnStock;
    private javax.swing.JButton btnValidar;
    private javax.swing.JComboBox<Cliente> cboCliente;
    private javax.swing.JComboBox<String> cboColor;
    private javax.swing.JComboBox<String> cboColorPaquete;
    private javax.swing.JComboBox<ModeloPrenda> cboModelo;
    private javax.swing.JComboBox<Paquete> cboPaquete;
    private javax.swing.JComboBox<ServicioAcabado> cboServicio;
    private javax.swing.JComboBox<String> cboTalla;
    private javax.swing.JComboBox<String> cboTallaPaquete;
    private javax.swing.JLabel lblCantPaquete;
    private javax.swing.JLabel lblCantPrenda;
    private javax.swing.JLabel lblCantServicio;
    private javax.swing.JLabel lblCliente;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblColorPaquete;
    private javax.swing.JLabel lblContenido;
    private javax.swing.JLabel lblContenidoPaquete;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblModelo;
    private javax.swing.JLabel lblPaquete;
    private javax.swing.JLabel lblServicio;
    private javax.swing.JLabel lblStock;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTalla;
    private javax.swing.JLabel lblTallaPaquete;
    private javax.swing.JLabel lblTela;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTipoCliente;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblVacio1;
    private javax.swing.JLabel lblVacio2;
    private javax.swing.JLabel lblVacio3;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JPanel pnlPaquete;
    private javax.swing.JPanel pnlPedido;
    private javax.swing.JPanel pnlPrenda;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlServicio;
    private javax.swing.JScrollPane scrItems;
    private javax.swing.JScrollPane scrResumen;
    private javax.swing.JTabbedPane tabAgregar;
    private javax.swing.JTable tblItems;
    private javax.swing.JTextField txtCantPaquete;
    private javax.swing.JTextField txtCantPrenda;
    private javax.swing.JTextField txtCantServicio;
    private javax.swing.JTextField txtFechaEntrega;
    private javax.swing.JTextArea txtResumen;
    // End of variables declaration//GEN-END:variables
}
