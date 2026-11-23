package gui;

import clases.ModeloPrenda;
import clases.Paquete;
import clases.ServicioAcabado;
import gestion.GestionPaquetes;
import gestion.SistemaTextil;
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
 * RF04 - Paquetes promocionales: se arman con modelos y servicios ya registrados.
 */
public class frmPaquetes extends FormularioBase {

    private GestionPaquetes gestion = SistemaTextil.getGestionPaquetes();
    private DefaultTableModel modelo;
    private DefaultTableModel modeloPrendas;
    private DefaultTableModel modeloServicios;
    private JTable tabla;
    private JTable tablaPrendas;
    private JTable tablaServicios;

    private JTextField txtCodigo = new JTextField(8);
    private JTextField txtNombre = new JTextField(22);
    private JTextField txtPrecio = new JTextField(8);
    private JLabel lblRegular = new JLabel("-");
    private JComboBox<ModeloPrenda> cboModelo = new JComboBox<>();
    private JTextField txtCantidad = new JTextField(4);
    private JComboBox<ServicioAcabado> cboServicio = new JComboBox<>();

    public frmPaquetes() {
        super("Paquetes Promocionales", 1100, 700);
        JPanel formDatos = panelFormulario(new String[]{"Código:", "Nombre:", "Precio promocional (S/):", "Precio si se compra por separado:"},
                new JComponent[]{txtCodigo, txtNombre, txtPrecio, lblRegular});
        formDatos.setBorder(BorderFactory.createTitledBorder("Datos del paquete"));

        modeloPrendas = crearModelo(new String[]{"Código", "Modelo", "Cantidad"});
        tablaPrendas = crearTabla(modeloPrendas);
        JButton btnAgregarPrenda = boton("Agregar", null);
        JButton btnQuitarPrenda = boton("Quitar", null);
        JPanel filaPrenda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaPrenda.add(cboModelo);
        filaPrenda.add(new JLabel("Cant.:"));
        filaPrenda.add(txtCantidad);
        filaPrenda.add(btnAgregarPrenda);
        filaPrenda.add(btnQuitarPrenda);
        JPanel panelPrendas = new JPanel(new BorderLayout());
        panelPrendas.setBorder(BorderFactory.createTitledBorder("Prendas incluidas"));
        panelPrendas.add(filaPrenda, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(tablaPrendas);
        sp.setPreferredSize(new Dimension(380, 100));
        panelPrendas.add(sp, BorderLayout.CENTER);

        modeloServicios = crearModelo(new String[]{"Código", "Servicio", "Precio/prenda"});
        tablaServicios = crearTabla(modeloServicios);
        JButton btnAgregarServicio = boton("Agregar", null);
        JButton btnQuitarServicio = boton("Quitar", null);
        JPanel filaServicio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaServicio.add(cboServicio);
        filaServicio.add(btnAgregarServicio);
        filaServicio.add(btnQuitarServicio);
        JPanel panelServicios = new JPanel(new BorderLayout());
        panelServicios.setBorder(BorderFactory.createTitledBorder("Servicios incluidos (se aplican a todas las prendas)"));
        panelServicios.add(filaServicio, BorderLayout.NORTH);
        JScrollPane ss = new JScrollPane(tablaServicios);
        ss.setPreferredSize(new Dimension(380, 100));
        panelServicios.add(ss, BorderLayout.CENTER);

        JPanel formularios = new JPanel(new GridLayout(1, 3, 8, 0));
        formularios.add(formDatos);
        formularios.add(panelPrendas);
        formularios.add(panelServicios);

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

        modelo = crearModelo(new String[]{"Código", "Nombre", "Contenido", "Prendas", "Precio por separado", "Precio promocional", "Disponible"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Paquetes promocionales"), BorderLayout.NORTH);
        norte.add(arriba, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnAgregarPrenda.addActionListener(e -> agregarPrenda());
        btnQuitarPrenda.addActionListener(e -> quitarFila(tablaPrendas, modeloPrendas));
        btnAgregarServicio.addActionListener(e -> agregarServicio());
        btnQuitarServicio.addActionListener(e -> quitarFila(tablaServicios, modeloServicios));
        btnBuscar.addActionListener(e -> buscar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> navegar());

        cargarCombos();
        listar();
    }

    @Override
    protected void refrescar() {
        listar();
    }

    private void cargarCombos() {
        cboModelo.removeAllItems();
        ModeloPrenda[] activos = SistemaTextil.getGestionModelos().listarActivos();
        for (int i = 0; i < activos.length; i++) {
            cboModelo.addItem(activos[i]);
        }
        cboServicio.removeAllItems();
        for (int i = 0; i < SistemaTextil.getGestionServicios().getTotalServicios(); i++) {
            cboServicio.addItem(SistemaTextil.getGestionServicios().getServicios()[i]);
        }
    }

    void listar() {
        modelo.setRowCount(0);
        for (int i = 0; i < gestion.getTotalPaquetes(); i++) {
            Paquete p = gestion.getPaquetes()[i];
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getContenido(), p.getPrendasPorPaquete(),
                Validador.redondear(p.getPrecioRegular()), p.getPrecioPromocional(), p.estaDisponible() ? "Sí" : "No (modelo descontinuado)"});
        }
    }

    private void agregarPrenda() {
        ModeloPrenda m = (ModeloPrenda) cboModelo.getSelectedItem();
        if (m == null || !Validador.esEntero(txtCantidad.getText()) || Validador.leerEntero(txtCantidad.getText()) <= 0) {
            error("Seleccione un modelo e ingrese una cantidad entera mayor a 0.");
            return;
        }
        int cantidad = Validador.leerEntero(txtCantidad.getText());
        for (int i = 0; i < modeloPrendas.getRowCount(); i++) {
            if (modeloPrendas.getValueAt(i, 0).equals(m.getCodigo())) {
                modeloPrendas.setValueAt(cantidad, i, 2);
                actualizarPrecioRegular();
                return;
            }
        }
        modeloPrendas.addRow(new Object[]{m.getCodigo(), m.getNombre(), cantidad});
        txtCantidad.setText("");
        actualizarPrecioRegular();
    }

    private void agregarServicio() {
        ServicioAcabado s = (ServicioAcabado) cboServicio.getSelectedItem();
        if (s == null) {
            return;
        }
        for (int i = 0; i < modeloServicios.getRowCount(); i++) {
            if (modeloServicios.getValueAt(i, 0).equals(s.getCodigo())) {
                return;
            }
        }
        modeloServicios.addRow(new Object[]{s.getCodigo(), s.getNombre(), s.getPrecioPorPrenda()});
        actualizarPrecioRegular();
    }

    private void quitarFila(JTable t, DefaultTableModel m) {
        int fila = t.getSelectedRow();
        if (fila < 0) {
            error("Seleccione la fila a quitar.");
            return;
        }
        m.removeRow(fila);
        actualizarPrecioRegular();
    }

    // Construye un objeto Paquete con lo que hay en pantalla (agregación de objetos existentes)
    private Paquete leerFormulario() {
        if (!Validador.esDecimal(txtPrecio.getText())) {
            error("El precio promocional debe ser un número.");
            return null;
        }
        Paquete p = new Paquete(txtCodigo.getText().trim().toUpperCase(), txtNombre.getText().trim(),
                Validador.leerDecimal(txtPrecio.getText()));
        for (int i = 0; i < modeloPrendas.getRowCount(); i++) {
            ModeloPrenda m = SistemaTextil.getGestionModelos().buscarPorCodigo(modeloPrendas.getValueAt(i, 0).toString());
            p.agregarModelo(m, Integer.parseInt(modeloPrendas.getValueAt(i, 2).toString()));
        }
        for (int i = 0; i < modeloServicios.getRowCount(); i++) {
            p.agregarServicio(SistemaTextil.getGestionServicios().buscarPorCodigo(modeloServicios.getValueAt(i, 0).toString()));
        }
        if (p.getPrecioPromocional() >= p.getPrecioRegular() && p.getTotalModelos() > 0) {
            if (!confirmar("El precio promocional no es menor al precio por separado ("
                    + Validador.dinero(p.getPrecioRegular()) + "). ¿Guardar de todos modos?")) {
                return null;
            }
        }
        return p;
    }

    private void actualizarPrecioRegular() {
        Paquete temp = new Paquete("", "", 0);
        for (int i = 0; i < modeloPrendas.getRowCount(); i++) {
            ModeloPrenda m = SistemaTextil.getGestionModelos().buscarPorCodigo(modeloPrendas.getValueAt(i, 0).toString());
            if (m != null) {
                temp.agregarModelo(m, Integer.parseInt(modeloPrendas.getValueAt(i, 2).toString()));
            }
        }
        for (int i = 0; i < modeloServicios.getRowCount(); i++) {
            ServicioAcabado s = SistemaTextil.getGestionServicios().buscarPorCodigo(modeloServicios.getValueAt(i, 0).toString());
            if (s != null) {
                temp.agregarServicio(s);
            }
        }
        lblRegular.setText(Validador.dinero(temp.getPrecioRegular()) + " (" + temp.getPrendasPorPaquete() + " prendas)");
    }

    private void mostrar(Paquete p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecioPromocional()));
        modeloPrendas.setRowCount(0);
        for (int i = 0; i < p.getTotalModelos(); i++) {
            modeloPrendas.addRow(new Object[]{p.getModelos()[i].getCodigo(), p.getModelos()[i].getNombre(), p.getCantidades()[i]});
        }
        modeloServicios.setRowCount(0);
        for (int i = 0; i < p.getTotalServicios(); i++) {
            ServicioAcabado s = p.getServicios()[i];
            modeloServicios.addRow(new Object[]{s.getCodigo(), s.getNombre(), s.getPrecioPorPrenda()});
        }
        actualizarPrecioRegular();
        txtCodigo.setEditable(false);
    }

    void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCantidad.setText("");
        modeloPrendas.setRowCount(0);
        modeloServicios.setRowCount(0);
        lblRegular.setText("-");
        txtCodigo.setEditable(true);
        tabla.clearSelection();
        cargarCombos();
        txtCodigo.requestFocus();
    }

    void buscar() {
        Paquete p = gestion.buscarPorCodigo(txtCodigo.getText().trim());
        if (p == null) {
            error("No se encontró un paquete con ese código.");
        } else {
            mostrar(p);
        }
    }

    void registrar() {
        Paquete p = leerFormulario();
        if (p == null) {
            return;
        }
        String r = gestion.registrar(p);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Paquete registrado con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        Paquete p = leerFormulario();
        if (p == null) {
            return;
        }
        String r = gestion.actualizar(p);
        if (r == null) {
            listar();
            mensaje("Paquete actualizado con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (!confirmar("¿Eliminar el paquete " + codigo + "?")) {
            return;
        }
        String r = gestion.eliminar(codigo);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Paquete eliminado.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            Paquete p = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (p != null) {
                mostrar(p);
            }
        }
    }
}
