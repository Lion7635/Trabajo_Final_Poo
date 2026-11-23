package gui;

import clases.Administrador;
import clases.EjecutivoVentas;
import clases.Empleado;
import clases.JefeProduccion;
import gestion.GestionEmpleados;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * RF01 - Mantenimiento de empleados (solo Administrador).
 */
public class frmEmpleados extends FormularioBase {

    private GestionEmpleados gestion = SistemaTextil.getGestionEmpleados();
    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtDni = new JTextField(10);
    private JTextField txtNombres = new JTextField(20);
    private JTextField txtApellidos = new JTextField(20);
    private JTextField txtUsuario = new JTextField(12);
    private JPasswordField txtContrasena = new JPasswordField(12);
    private JComboBox<String> cboRol = new JComboBox<>(new String[]{"Administrador", "Ejecutivo de Ventas", "Jefe de Producción"});

    public frmEmpleados() {
        super("Gestión de Personal", 820, 600);
        JPanel form = panelFormulario(new String[]{"DNI:", "Nombres:", "Apellidos:", "Usuario:", "Contraseña:", "Rol:"},
                new JComponent[]{txtDni, txtNombres, txtApellidos, txtUsuario, txtContrasena, cboRol});

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

        modelo = crearModelo(new String[]{"DNI", "Nombres", "Apellidos", "Usuario", "Rol"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Empleados del sistema"), BorderLayout.NORTH);
        norte.add(arriba, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> navegar());
        listar();
    }

    @Override
    protected void refrescar() {
        listar();
    }

    void listar() {
        modelo.setRowCount(0);
        for (int i = 0; i < gestion.getTotalEmpleados(); i++) {
            Empleado e = gestion.getEmpleados()[i];
            modelo.addRow(new Object[]{e.getDni(), e.getNombres(), e.getApellidos(), e.getUsuario(), e.getRol()});
        }
    }

    void limpiar() {
        txtDni.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtUsuario.setText("");
        txtContrasena.setText("");
        cboRol.setSelectedIndex(0);
        txtDni.setEditable(true);
        tabla.clearSelection();
        txtDni.requestFocus();
    }

    // Polimorfismo: según el rol elegido se instancia una subclase distinta de Empleado
    private Empleado leerFormulario() {
        String dni = txtDni.getText().trim();
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        switch (cboRol.getSelectedIndex()) {
            case 0:
                return new Administrador(dni, nombres, apellidos, usuario, contrasena);
            case 1:
                return new EjecutivoVentas(dni, nombres, apellidos, usuario, contrasena);
            default:
                return new JefeProduccion(dni, nombres, apellidos, usuario, contrasena);
        }
    }

    private void mostrar(Empleado e) {
        txtDni.setText(e.getDni());
        txtNombres.setText(e.getNombres());
        txtApellidos.setText(e.getApellidos());
        txtUsuario.setText(e.getUsuario());
        txtContrasena.setText(e.getContrasena());
        cboRol.setSelectedItem(e.getRol());
        txtDni.setEditable(false);
    }

    void buscar() {
        Empleado e = gestion.buscarPorDni(txtDni.getText().trim());
        if (e == null) {
            error("No se encontró un empleado con ese DNI.");
        } else {
            mostrar(e);
        }
    }

    void registrar() {
        String resultado = gestion.registrar(leerFormulario());
        if (resultado == null) {
            listar();
            limpiar();
            mensaje("Empleado registrado con éxito.");
        } else {
            error(resultado);
        }
    }

    void actualizar() {
        Empleado datos = leerFormulario();
        Empleado actual = SistemaTextil.getUsuarioActual();
        if (actual != null && actual.getDni().equals(datos.getDni()) && !actual.getRol().equals(datos.getRol())) {
            error("No puede cambiar su propio rol mientras tiene la sesión abierta.");
            return;
        }
        String resultado = gestion.actualizar(datos);
        if (resultado == null) {
            listar();
            mensaje("Empleado actualizado con éxito.");
        } else {
            error(resultado);
        }
    }

    void eliminar() {
        String dni = txtDni.getText().trim();
        Empleado actual = SistemaTextil.getUsuarioActual();
        if (actual != null && actual.getDni().equals(dni)) {
            error("No puede eliminar su propio usuario.");
            return;
        }
        if (!confirmar("¿Eliminar al empleado con DNI " + dni + "?")) {
            return;
        }
        String resultado = gestion.eliminar(dni);
        if (resultado == null) {
            listar();
            limpiar();
            mensaje("Empleado eliminado.");
        } else {
            error(resultado);
        }
    }

    void navegar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            Empleado e = gestion.buscarPorDni(modelo.getValueAt(fila, 0).toString());
            if (e != null) {
                mostrar(e);
            }
        }
    }
}
