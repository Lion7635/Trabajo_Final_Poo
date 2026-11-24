package gui.ventas;

import clases.clientes.Cliente;
import clases.clientes.ClienteInstitucional;
import clases.clientes.ClienteMayorista;
import clases.clientes.ClienteMinorista;
import gestion.GestionClientes;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

/**
 * RF03 - Mantenimiento de clientes (Ejecutivo de Ventas).
 */
public class frmClientes extends FormularioBase {

    private GestionClientes gestion = SistemaTextil.getGestionClientes();
    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtDocumento = new JTextField(12);
    private JTextField txtNombres = new JTextField(28);
    private JTextField txtTelefono = new JTextField(10);
    private JTextField txtCorreo = new JTextField(24);
    private JTextField txtDireccion = new JTextField(28);
    private JComboBox<String> cboTipo = new JComboBox<>(new String[]{"Minorista", "Mayorista", "Institucional"});
    private JLabel lblBeneficio = new JLabel();

    public frmClientes() {
        super("Gestión de Clientes", 980, 620);
        JPanel form = panelFormulario(
                new String[]{"Documento (DNI o RUC):", "Nombres o razón social:", "Teléfono:", "Correo:", "Dirección:", "Tipo de cliente:", "Beneficio:"},
                new JComponent[]{txtDocumento, txtNombres, txtTelefono, txtCorreo, txtDireccion, cboTipo, lblBeneficio});

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
        arriba.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        arriba.add(form, BorderLayout.CENTER);
        arriba.add(botones, BorderLayout.SOUTH);

        modelo = crearModelo(new String[]{"Documento", "Nombre / Razón social", "Tipo", "Teléfono", "Correo", "Dirección", "Beneficio"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Clientes"), BorderLayout.NORTH);
        norte.add(arriba, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        cboTipo.addActionListener(e -> lblBeneficio.setText(leerFormulario().getBeneficio()));
        tabla.getSelectionModel().addListSelectionListener(e -> navegar());
        lblBeneficio.setText(leerFormulario().getBeneficio());
        listar();
    }

    @Override
    protected void refrescar() {
        listar();
    }

    void listar() {
        modelo.setRowCount(0);
        for (int i = 0; i < gestion.getTotalClientes(); i++) {
            Cliente c = gestion.getClientes()[i];
            modelo.addRow(new Object[]{c.getDocumento(), c.getNombreCompleto(), c.getTipo(), c.getTelefono(),
                c.getCorreo(), c.getDireccion(), c.getBeneficio()});
        }
    }

    void limpiar() {
        txtDocumento.setText("");
        txtNombres.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        cboTipo.setSelectedIndex(0);
        txtDocumento.setEditable(true);
        tabla.clearSelection();
        txtDocumento.requestFocus();
    }

    // Según el tipo se crea un objeto de la subclase correspondiente
    private Cliente leerFormulario() {
        String doc = txtDocumento.getText().trim();
        String nom = txtNombres.getText().trim();
        String tel = txtTelefono.getText().trim();
        String cor = txtCorreo.getText().trim();
        String dir = txtDireccion.getText().trim();
        switch (cboTipo.getSelectedIndex()) {
            case 1:
                return new ClienteMayorista(doc, nom, tel, cor, dir);
            case 2:
                return new ClienteInstitucional(doc, nom, tel, cor, dir);
            default:
                return new ClienteMinorista(doc, nom, tel, cor, dir);
        }
    }

    private void mostrar(Cliente c) {
        txtDocumento.setText(c.getDocumento());
        txtNombres.setText(c.getNombres());
        txtTelefono.setText(c.getTelefono());
        txtCorreo.setText(c.getCorreo());
        txtDireccion.setText(c.getDireccion());
        cboTipo.setSelectedItem(c.getTipo());
        txtDocumento.setEditable(false);
    }

    void buscar() {
        Cliente c = gestion.buscarPorDocumento(txtDocumento.getText().trim());
        if (c == null) {
            error("No se encontró un cliente con ese documento.");
        } else {
            mostrar(c);
        }
    }

    void registrar() {
        String r = gestion.registrar(leerFormulario());
        if (r == null) {
            listar();
            limpiar();
            mensaje("Cliente registrado con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        String r = gestion.actualizar(leerFormulario(), SistemaTextil.getGestionPedidos());
        if (r == null) {
            listar();
            mensaje("Cliente actualizado con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String doc = txtDocumento.getText().trim();
        if (!confirmar("¿Eliminar al cliente " + doc + "?")) {
            return;
        }
        String r = gestion.eliminar(doc, SistemaTextil.getGestionPedidos());
        if (r == null) {
            listar();
            limpiar();
            mensaje("Cliente eliminado.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            Cliente c = gestion.buscarPorDocumento(modelo.getValueAt(fila, 0).toString());
            if (c != null) {
                mostrar(c);
            }
        }
    }
}
