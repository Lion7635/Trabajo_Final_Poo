package gui.mantenimiento;

import clases.inventario.Insumo;
import clases.prendas.Casaca;
import clases.prendas.Jean;
import clases.prendas.ModeloPrenda;
import clases.prendas.Polo;
import gestion.GestionModelos;
import gestion.GestionTelas;
import gestion.SistemaTextil;
import gui.FormularioBase;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF02 - Mantenimiento de modelos de prendas (Polo, Jean, Casaca).
 * Formulario creado con el GUI Builder de NetBeans (pestaña Design).
 */
public class frmModelos extends FormularioBase {

    private GestionModelos gestion = SistemaTextil.getGestionModelos();
    private GestionTelas gestionTelas = SistemaTextil.getGestionTelas();
    private DefaultTableModel modelo;
    private DefaultTableModel modeloInsumos;
    private JTextField[] txtConsumo;   // las 4 cajas de consumo (S, M, L, XL) en un arreglo

    /**
     * Creates new form frmModelos
     */
    public frmModelos() {
        initComponents();
        txtConsumo = new JTextField[]{txtConsumoS, txtConsumoM, txtConsumoL, txtConsumoXL};
        modelo = crearModelo(new String[]{"Código", "Nombre", "Tipo", "Tela principal", "Consumo S/M/L/XL (m)",
            "Tiempo (min)", "Precio", "Costo prod. M", "Margen M", "Estado"});
        configurarTabla(tblModelos, modelo);
        modeloInsumos = crearModelo(new String[]{"Código", "Insumo", "Cantidad por prenda", "Unidad"});
        configurarTabla(tblInsumos, modeloInsumos);
        cargarCombos();
        habilitarCostos();
        listar();
        setLocationRelativeTo(null);
    }

    @Override
    protected void refrescar() {
        listar();
    }

    private void cargarCombos() {
        cboTela.removeAllItems();
        String[] tipos = gestionTelas.getTiposDistintos();
        for (int i = 0; i < tipos.length; i++) {
            cboTela.addItem(tipos[i]);
        }
        cboInsumo.removeAllItems();
        for (int i = 0; i < SistemaTextil.getGestionInsumos().getTotalInsumos(); i++) {
            cboInsumo.addItem(SistemaTextil.getGestionInsumos().getInsumos()[i]);
        }
    }

    // Solo se habilitan los costos que corresponden al tipo elegido
    private void habilitarCostos() {
        String tipo = (String) cboTipo.getSelectedItem();
        txtLavado.setEnabled(tipo.equals("Jean"));
        txtForro.setEnabled(tipo.equals("Casaca"));
        txtAvios.setEnabled(tipo.equals("Casaca"));
    }

    // Costo promedio por metro de un tipo de tela (referencia para la tblModelos)
    private double costoMetroPromedio(String tipoTela) {
        double suma = 0;
        int n = 0;
        for (int i = 0; i < gestionTelas.getTotalTelas(); i++) {
            if (gestionTelas.getTelas()[i].getTipo().equalsIgnoreCase(tipoTela)) {
                suma += gestionTelas.getTelas()[i].getCostoMetro();
                n++;
            }
        }
        return n == 0 ? 0 : suma / n;
    }

    void listar() {
        modelo.setRowCount(0);
        for (int i = 0; i < gestion.getTotalModelos(); i++) {
            ModeloPrenda m = gestion.getModelos()[i];
            double[] c = m.getConsumoPorTalla();
            double costo = m.calcularCostoProduccion("M", costoMetroPromedio(m.getTipoTela()));  // polimorfismo
            modelo.addRow(new Object[]{m.getCodigo(), m.getNombre(), m.getTipo(), m.getTipoTela(),
                c[0] + " / " + c[1] + " / " + c[2] + " / " + c[3], m.getTiempoConfeccion(), m.getPrecioVenta(),
                Validador.redondear(costo), Validador.redondear(m.getPrecioVenta() - costo), m.getEstado()});
        }
    }

    void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        cboTipo.setSelectedIndex(0);
        for (int i = 0; i < txtConsumo.length; i++) {
            txtConsumo[i].setText("");
        }
        txtTiempo.setText("");
        txtPrecio.setText("");
        txtCostura.setText("");
        txtLavado.setText("");
        txtForro.setText("");
        txtAvios.setText("");
        cboEstado.setSelectedIndex(0);
        modeloInsumos.setRowCount(0);
        txtCodigo.setEditable(true);
        tblModelos.clearSelection();
        cargarCombos();
        txtCodigo.requestFocus();
    }

    private void agregarInsumo() {
        Insumo in = (Insumo) cboInsumo.getSelectedItem();
        if (in == null || !Validador.esDecimal(txtCantInsumo.getText()) || Validador.leerDecimal(txtCantInsumo.getText()) <= 0) {
            error("Seleccione un insumo e ingrese una cantidad mayor a 0.");
            return;
        }
        double cantidad = Validador.leerDecimal(txtCantInsumo.getText());
        for (int i = 0; i < modeloInsumos.getRowCount(); i++) {
            if (modeloInsumos.getValueAt(i, 0).equals(in.getCodigo())) {
                modeloInsumos.setValueAt(cantidad, i, 2);   // ya estaba: se actualiza la cantidad
                return;
            }
        }
        modeloInsumos.addRow(new Object[]{in.getCodigo(), in.getNombre(), cantidad, in.getUnidad()});
        txtCantInsumo.setText("");
    }

    private void quitarInsumo() {
        int fila = tblInsumos.getSelectedRow();
        if (fila < 0) {
            error("Seleccione el insumo a quitar.");
            return;
        }
        modeloInsumos.removeRow(fila);
    }

    private double leerNumero(JTextField campo, boolean habilitado) {
        if (!habilitado || campo.getText().trim().isEmpty()) {
            return 0;
        }
        return Validador.leerDecimal(campo.getText());
    }

    // Crea un Polo, Jean o Casaca según el tipo elegido (polimorfismo al instanciar)
    private ModeloPrenda leerFormulario() {
        double[] consumo = new double[4];
        for (int i = 0; i < 4; i++) {
            if (!Validador.esDecimal(txtConsumo[i].getText())) {
                error("Ingrese el consumo de tela de la talla " + ModeloPrenda.TALLAS[i] + ".");
                return null;
            }
            consumo[i] = Validador.leerDecimal(txtConsumo[i].getText());
        }
        if (!Validador.esEntero(txtTiempo.getText()) || !Validador.esDecimal(txtPrecio.getText())
                || !Validador.esDecimal(txtCostura.getText())) {
            error("Tiempo (entero), precio y costo de costura deben ser números.");
            return null;
        }
        try {
            String codigo = txtCodigo.getText().trim().toUpperCase();
            String nombre = txtNombre.getText().trim();
            String tela = (String) cboTela.getSelectedItem();
            int tiempo = Validador.leerEntero(txtTiempo.getText());
            double precio = Validador.leerDecimal(txtPrecio.getText());
            double costura = Validador.leerDecimal(txtCostura.getText());
            boolean activo = cboEstado.getSelectedIndex() == 0;
            ModeloPrenda m;
            switch ((String) cboTipo.getSelectedItem()) {
                case "Jean":
                    m = new Jean(codigo, nombre, tela, consumo, tiempo, precio, costura, activo, leerNumero(txtLavado, true));
                    break;
                case "Casaca":
                    m = new Casaca(codigo, nombre, tela, consumo, tiempo, precio, costura, activo,
                            leerNumero(txtForro, true), leerNumero(txtAvios, true));
                    break;
                default:
                    m = new Polo(codigo, nombre, tela, consumo, tiempo, precio, costura, activo);
            }
            for (int i = 0; i < modeloInsumos.getRowCount(); i++) {
                Insumo in = SistemaTextil.getGestionInsumos().buscarPorCodigo(modeloInsumos.getValueAt(i, 0).toString());
                m.agregarInsumo(in, Double.parseDouble(modeloInsumos.getValueAt(i, 2).toString()));
            }
            return m;
        } catch (NumberFormatException ex) {
            error("Los costos de lavado, forro y avíos deben ser números.");
            return null;
        }
    }

    private void mostrar(ModeloPrenda m) {
        txtCodigo.setText(m.getCodigo());
        txtNombre.setText(m.getNombre());
        cboTipo.setSelectedItem(m.getTipo());
        cboTela.setSelectedItem(m.getTipoTela());
        for (int i = 0; i < 4; i++) {
            txtConsumo[i].setText(String.valueOf(m.getConsumoPorTalla()[i]));
        }
        txtTiempo.setText(String.valueOf(m.getTiempoConfeccion()));
        txtPrecio.setText(String.valueOf(m.getPrecioVenta()));
        txtCostura.setText(String.valueOf(m.getCostoCostura()));
        cboEstado.setSelectedIndex(m.isActivo() ? 0 : 1);
        txtLavado.setText("");
        txtForro.setText("");
        txtAvios.setText("");
        // Casting para leer los atributos propios de la subclase
        if (m instanceof Jean) {
            txtLavado.setText(String.valueOf(((Jean) m).getCostoLavado()));
        }
        if (m instanceof Casaca) {
            txtForro.setText(String.valueOf(((Casaca) m).getCostoForro()));
            txtAvios.setText(String.valueOf(((Casaca) m).getCostoAvios()));
        }
        modeloInsumos.setRowCount(0);
        for (int i = 0; i < m.getTotalInsumos(); i++) {
            Insumo in = m.getInsumos()[i].getInsumo();
            modeloInsumos.addRow(new Object[]{in.getCodigo(), in.getNombre(), m.getInsumos()[i].getCantidadPorPrenda(), in.getUnidad()});
        }
        txtCodigo.setEditable(false);
    }

    void buscar() {
        ModeloPrenda m = gestion.buscarPorCodigo(txtCodigo.getText().trim());
        if (m == null) {
            error("No se encontró un modelo con ese código.");
        } else {
            mostrar(m);
        }
    }

    void registrar() {
        ModeloPrenda m = leerFormulario();
        if (m == null) {
            return;
        }
        String r = gestion.registrar(m);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Modelo registrado con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        ModeloPrenda m = leerFormulario();
        if (m == null) {
            return;
        }
        String r = gestion.actualizar(m, SistemaTextil.getGestionPedidos(), SistemaTextil.getGestionPaquetes());
        if (r == null) {
            listar();
            mensaje("Modelo actualizado con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (!confirmar("¿Eliminar el modelo " + codigo + "?")) {
            return;
        }
        String r = gestion.eliminar(codigo, SistemaTextil.getGestionPedidos(), SistemaTextil.getGestionPaquetes());
        if (r == null) {
            listar();
            limpiar();
            mensaje("Modelo eliminado.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tblModelos.getSelectedRow();
        if (fila >= 0) {
            ModeloPrenda m = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (m != null) {
                mostrar(m);
            }
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
        pnlDatos = new javax.swing.JPanel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblTipo = new javax.swing.JLabel();
        cboTipo = new javax.swing.JComboBox<>();
        lblTela = new javax.swing.JLabel();
        cboTela = new javax.swing.JComboBox<>();
        lblConsumo = new javax.swing.JLabel();
        lblS = new javax.swing.JLabel();
        txtConsumoS = new javax.swing.JTextField();
        lblM = new javax.swing.JLabel();
        txtConsumoM = new javax.swing.JTextField();
        lblL = new javax.swing.JLabel();
        txtConsumoL = new javax.swing.JTextField();
        lblXL = new javax.swing.JLabel();
        txtConsumoXL = new javax.swing.JTextField();
        lblTiempo = new javax.swing.JLabel();
        txtTiempo = new javax.swing.JTextField();
        lblPrecio = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        lblCostura = new javax.swing.JLabel();
        txtCostura = new javax.swing.JTextField();
        lblEstado = new javax.swing.JLabel();
        cboEstado = new javax.swing.JComboBox<>();
        pnlCostos = new javax.swing.JPanel();
        lblLavado = new javax.swing.JLabel();
        txtLavado = new javax.swing.JTextField();
        lblForro = new javax.swing.JLabel();
        txtForro = new javax.swing.JTextField();
        lblAvios = new javax.swing.JLabel();
        txtAvios = new javax.swing.JTextField();
        pnlInsumos = new javax.swing.JPanel();
        cboInsumo = new javax.swing.JComboBox<>();
        lblCantInsumo = new javax.swing.JLabel();
        txtCantInsumo = new javax.swing.JTextField();
        btnAgregarInsumo = new javax.swing.JButton();
        btnQuitarInsumo = new javax.swing.JButton();
        scrInsumos = new javax.swing.JScrollPane();
        tblInsumos = new javax.swing.JTable();
        pnlAcciones = new javax.swing.JPanel();
        btnBuscar = new javax.swing.JButton();
        btnRegistrar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        scrModelos = new javax.swing.JScrollPane();
        tblModelos = new javax.swing.JTable();
        lblNota = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Modelos de Prendas - Sistema Textil Gamarra");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(30, 58, 95));
        lblTitulo.setText("Modelos de Prendas");

        lblSubtitulo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(100, 112, 128));
        lblSubtitulo.setText("Polo, Jean y Casaca heredan de ModeloPrenda; cada uno calcula su costo de producción a su manera.");

        lblCodigo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCodigo.setText("Código:");

        txtCodigo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblNombre.setText("Nombre:");

        txtNombre.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblTipo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTipo.setText("Tipo de prenda:");

        cboTipo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Polo", "Jean", "Casaca" }));
        cboTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoActionPerformed(evt);
            }
        });

        lblTela.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTela.setText("Tela principal:");

        cboTela.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblConsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblConsumo.setText("Consumo de tela (m):");

        lblS.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblS.setText("S");

        txtConsumoS.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblM.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblM.setText("M");

        txtConsumoM.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblL.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblL.setText("L");

        txtConsumoL.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblXL.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblXL.setText("XL");

        txtConsumoXL.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblTiempo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblTiempo.setText("Tiempo de confección (min):");

        txtTiempo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblPrecio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblPrecio.setText("Precio de venta (S/):");

        txtPrecio.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblCostura.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCostura.setText("Costo de costura (S/):");

        txtCostura.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblEstado.setText("Estado:");

        cboEstado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo", "Descontinuado" }));

        pnlDatos.setBackground(new java.awt.Color(236, 242, 249));
        pnlDatos.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del modelo"));
        javax.swing.GroupLayout pnlDatosLayout = new javax.swing.GroupLayout(pnlDatos);
        pnlDatos.setLayout(pnlDatosLayout);
        pnlDatosLayout.setHorizontalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCostura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(cboTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTela, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlDatosLayout.createSequentialGroup()
                        .addComponent(lblS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtConsumoS, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(lblM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtConsumoM, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(lblL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtConsumoL, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(lblXL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtConsumoXL, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCostura, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlDatosLayout.setVerticalGroup(
            pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatosLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConsumoS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConsumoM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConsumoL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblXL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConsumoXL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTiempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCostura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCostura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        lblLavado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblLavado.setText("Costo de lavado (solo Jean):");

        txtLavado.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblForro.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblForro.setText("Costo de forro (solo Casaca):");

        txtForro.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblAvios.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAvios.setText("Cierres y broches (solo Casaca):");

        txtAvios.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        pnlCostos.setBackground(new java.awt.Color(236, 242, 249));
        pnlCostos.setBorder(javax.swing.BorderFactory.createTitledBorder("Costos propios del tipo de prenda (herencia)"));
        javax.swing.GroupLayout pnlCostosLayout = new javax.swing.GroupLayout(pnlCostos);
        pnlCostos.setLayout(pnlCostosLayout);
        pnlCostosLayout.setHorizontalGroup(
            pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCostosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLavado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblForro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAvios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtLavado, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtForro, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAvios, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
        pnlCostosLayout.setVerticalGroup(
            pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCostosLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLavado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLavado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblForro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtForro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(pnlCostosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAvios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAvios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        cboInsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblCantInsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblCantInsumo.setText("Cant./prenda:");

        txtCantInsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnAgregarInsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAgregarInsumo.setText("Agregar");
        btnAgregarInsumo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarInsumoActionPerformed(evt);
            }
        });

        btnQuitarInsumo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnQuitarInsumo.setText("Quitar");
        btnQuitarInsumo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarInsumoActionPerformed(evt);
            }
        });

        tblInsumos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblInsumos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));

        scrInsumos.setViewportView(tblInsumos);

        pnlInsumos.setBackground(new java.awt.Color(236, 242, 249));
        pnlInsumos.setBorder(javax.swing.BorderFactory.createTitledBorder("Insumos que consume cada prenda"));
        javax.swing.GroupLayout pnlInsumosLayout = new javax.swing.GroupLayout(pnlInsumos);
        pnlInsumos.setLayout(pnlInsumosLayout);
        pnlInsumosLayout.setHorizontalGroup(
            pnlInsumosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInsumosLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlInsumosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInsumosLayout.createSequentialGroup()
                        .addComponent(cboInsumo, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                        .addGap(8, 8, 8)
                        .addComponent(lblCantInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtCantInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnAgregarInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btnQuitarInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrInsumos, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        pnlInsumosLayout.setVerticalGroup(
            pnlInsumosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInsumosLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlInsumosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAgregarInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuitarInsumo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(scrInsumos, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addGap(8, 8, 8))
        );

        btnBuscar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/search.png"))); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        btnRegistrar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnRegistrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/vcsnormal.png"))); // NOI18N
        btnRegistrar.setText("Registrar");
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
            }
        });

        btnActualizar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnActualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/update_rotation.png"))); // NOI18N
        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnEliminar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/delete_remove.png"))); // NOI18N
        btnEliminar.setText("Eliminar");
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/archivo.png"))); // NOI18N
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
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
                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 20, Short.MAX_VALUE)
                .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        pnlAccionesLayout.setVerticalGroup(
            pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccionesLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(pnlAccionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegistrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnActualizar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7))
        );

        tblModelos.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        tblModelos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblModelos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblModelosMouseClicked(evt);
            }
        });

        scrModelos.setViewportView(tblModelos);

        lblNota.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblNota.setForeground(new java.awt.Color(100, 112, 128));
        lblNota.setText("Costo prod. M = costo de una prenda talla M con el costo promedio por metro de su tela (cálculo polimórfico por tipo).");

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
                        .addComponent(pnlDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlCostos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlInsumos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(pnlAcciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrModelos, javax.swing.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
                    .addComponent(lblNota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(pnlDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlCostos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(pnlInsumos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(8, 8, 8)
                .addComponent(pnlAcciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrModelos, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addComponent(lblNota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
        registrar();
    }//GEN-LAST:event_btnRegistrarActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        actualizar();
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    private void tblModelosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblModelosMouseClicked
        // Al hacer clic en una fila se cargan sus datos arriba
        navegar();
    }//GEN-LAST:event_tblModelosMouseClicked

    private void cboTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoActionPerformed
        // Solo se habilitan los costos del tipo elegido
        habilitarCostos();
    }//GEN-LAST:event_cboTipoActionPerformed

    private void btnAgregarInsumoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarInsumoActionPerformed
        agregarInsumo();
    }//GEN-LAST:event_btnAgregarInsumoActionPerformed

    private void btnQuitarInsumoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarInsumoActionPerformed
        quitarInsumo();
    }//GEN-LAST:event_btnQuitarInsumoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAgregarInsumo;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnQuitarInsumo;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<String> cboEstado;
    private javax.swing.JComboBox<Insumo> cboInsumo;
    private javax.swing.JComboBox<String> cboTela;
    private javax.swing.JComboBox<String> cboTipo;
    private javax.swing.JLabel lblAvios;
    private javax.swing.JLabel lblCantInsumo;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblConsumo;
    private javax.swing.JLabel lblCostura;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblForro;
    private javax.swing.JLabel lblL;
    private javax.swing.JLabel lblLavado;
    private javax.swing.JLabel lblM;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblNota;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JLabel lblS;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTela;
    private javax.swing.JLabel lblTiempo;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblXL;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlCostos;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlInsumos;
    private javax.swing.JScrollPane scrInsumos;
    private javax.swing.JScrollPane scrModelos;
    private javax.swing.JTable tblInsumos;
    private javax.swing.JTable tblModelos;
    private javax.swing.JTextField txtAvios;
    private javax.swing.JTextField txtCantInsumo;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtConsumoL;
    private javax.swing.JTextField txtConsumoM;
    private javax.swing.JTextField txtConsumoS;
    private javax.swing.JTextField txtConsumoXL;
    private javax.swing.JTextField txtCostura;
    private javax.swing.JTextField txtForro;
    private javax.swing.JTextField txtLavado;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPrecio;
    private javax.swing.JTextField txtTiempo;
    // End of variables declaration//GEN-END:variables
}
