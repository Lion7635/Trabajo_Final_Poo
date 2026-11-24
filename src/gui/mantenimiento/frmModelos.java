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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF02 - Mantenimiento de modelos de prendas (solo Administrador).
 * La tabla muestra el costo de producción calculado con el método polimórfico de cada tipo.
 */
public class frmModelos extends FormularioBase {

    private GestionModelos gestion = SistemaTextil.getGestionModelos();
    private GestionTelas gestionTelas = SistemaTextil.getGestionTelas();
    private DefaultTableModel modelo;
    private DefaultTableModel modeloInsumos;
    private JTable tabla;
    private JTable tablaInsumos;

    private JTextField txtCodigo = new JTextField(8);
    private JTextField txtNombre = new JTextField(20);
    private JComboBox<String> cboTipo = new JComboBox<>(new String[]{"Polo", "Jean", "Casaca"});
    private JComboBox<String> cboTela = new JComboBox<>();
    private JTextField[] txtConsumo = {new JTextField(4), new JTextField(4), new JTextField(4), new JTextField(4)};
    private JTextField txtTiempo = new JTextField(6);
    private JTextField txtPrecio = new JTextField(8);
    private JTextField txtCostura = new JTextField(8);
    private JComboBox<String> cboEstado = new JComboBox<>(new String[]{"Activo", "Descontinuado"});
    private JTextField txtLavado = new JTextField(8);
    private JTextField txtForro = new JTextField(8);
    private JTextField txtAvios = new JTextField(8);
    private JComboBox<Insumo> cboInsumo = new JComboBox<>();
    private JTextField txtCantInsumo = new JTextField(5);

    public frmModelos() {
        super("Modelos de Prendas", 1150, 720);

        JPanel consumo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (int i = 0; i < txtConsumo.length; i++) {
            consumo.add(new JLabel(ModeloPrenda.TALLAS[i]));
            consumo.add(txtConsumo[i]);
        }
        JPanel formGeneral = panelFormulario(
                new String[]{"Código:", "Nombre:", "Tipo:", "Tela principal:", "Consumo tela (m) por talla:",
                    "Tiempo de confección (min):", "Precio de venta (S/):", "Costo de costura (S/):", "Estado:"},
                new JComponent[]{txtCodigo, txtNombre, cboTipo, cboTela, consumo, txtTiempo, txtPrecio, txtCostura, cboEstado});
        formGeneral.setBorder(BorderFactory.createTitledBorder("Datos del modelo"));

        JPanel formCostos = panelFormulario(new String[]{"Costo de lavado (Jean):", "Costo de forro (Casaca):", "Costo de avíos - cierres y broches (Casaca):"},
                new JComponent[]{txtLavado, txtForro, txtAvios});
        formCostos.setBorder(BorderFactory.createTitledBorder("Costos propios del tipo de prenda"));

        // Insumos que consume cada prenda
        modeloInsumos = crearModelo(new String[]{"Código", "Insumo", "Cantidad por prenda", "Unidad"});
        tablaInsumos = crearTabla(modeloInsumos);
        JButton btnAgregarInsumo = boton("Agregar", null);
        JButton btnQuitarInsumo = boton("Quitar", null);
        JPanel filaInsumo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaInsumo.add(cboInsumo);
        filaInsumo.add(new JLabel("Cant./prenda:"));
        filaInsumo.add(txtCantInsumo);
        filaInsumo.add(btnAgregarInsumo);
        filaInsumo.add(btnQuitarInsumo);
        JScrollPane scrollInsumos = new JScrollPane(tablaInsumos);
        scrollInsumos.setPreferredSize(new Dimension(420, 110));
        JPanel panelInsumos = new JPanel(new BorderLayout());
        panelInsumos.setBorder(BorderFactory.createTitledBorder("Insumos por prenda"));
        panelInsumos.add(filaInsumo, BorderLayout.NORTH);
        panelInsumos.add(scrollInsumos, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(formCostos, BorderLayout.NORTH);
        derecha.add(panelInsumos, BorderLayout.CENTER);

        JPanel formularios = new JPanel(new GridLayout(1, 2, 8, 0));
        formularios.add(formGeneral);
        formularios.add(derecha);

        JButton btnBuscar = boton("Buscar", "consultar.png");
        JButton btnRegistrar = boton("Registrar", "vcsnormal.png");
        JButton btnActualizar = boton("Actualizar", "update_rotation.png");
        JButton btnEliminar = boton("Eliminar", "delete_remove.png");
        JButton btnLimpiar = boton("Limpiar", "archivo.png");
        JPanel botones = new JPanel(new FlowLayout());
        botones.add(btnBuscar);
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));
        arriba.add(formularios, BorderLayout.CENTER);
        arriba.add(botones, BorderLayout.SOUTH);

        modelo = crearModelo(new String[]{"Código", "Nombre", "Tipo", "Tela principal", "Consumo S/M/L/XL (m)",
            "Tiempo (min)", "Precio", "Costo prod. M", "Margen M", "Estado"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Modelos de prendas"), BorderLayout.NORTH);
        norte.add(arriba, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JLabel nota = new JLabel("  Costo prod. M = costo de una prenda talla M con el costo promedio por metro de su tela (cálculo polimórfico por tipo).");
        add(nota, BorderLayout.SOUTH);

        cboTipo.addActionListener(e -> habilitarCostos());
        btnAgregarInsumo.addActionListener(e -> agregarInsumo());
        btnQuitarInsumo.addActionListener(e -> quitarInsumo());
        btnBuscar.addActionListener(e -> buscar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> navegar());

        cargarCombos();
        habilitarCostos();
        listar();
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

    // Costo promedio por metro de un tipo de tela (referencia para la tabla)
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
        tabla.clearSelection();
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
        int fila = tablaInsumos.getSelectedRow();
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
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            ModeloPrenda m = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (m != null) {
                mostrar(m);
            }
        }
    }
}
