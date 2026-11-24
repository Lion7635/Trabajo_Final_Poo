package gui.mantenimiento;

import clases.inventario.Insumo;
import gestion.GestionInsumos;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF04 - Mantenimiento de insumos y avíos (solo Administrador).
 */
public class frmInsumos extends FormularioBase {

    private GestionInsumos gestion = SistemaTextil.getGestionInsumos();
    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtCodigo = new JTextField(8);
    private JTextField txtNombre = new JTextField(22);
    private JComboBox<String> cboUnidad = new JComboBox<>(new String[]{"unid", "cono", "m"});
    private JTextField txtStock = new JTextField(8);
    private JTextField txtMinimo = new JTextField(8);
    private JTextField txtCosto = new JTextField(8);

    public frmInsumos() {
        super("Gestión de Insumos y Avíos", 880, 580);
        cboUnidad.setEditable(true);
        JPanel form = panelFormulario(new String[]{"Código:", "Nombre:", "Unidad:", "Stock:", "Stock mínimo:", "Costo unitario (S/):"},
                new JComponent[]{txtCodigo, txtNombre, cboUnidad, txtStock, txtMinimo, txtCosto});

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

        modelo = crearModelo(new String[]{"Código", "Nombre", "Unidad", "Stock", "Mínimo", "Costo unit.", "Estado"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Insumos y avíos"), BorderLayout.NORTH);
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
        for (int i = 0; i < gestion.getTotalInsumos(); i++) {
            Insumo in = gestion.getInsumos()[i];
            modelo.addRow(new Object[]{in.getCodigo(), in.getNombre(), in.getUnidad(), Validador.redondear(in.getStock()),
                in.getStockMinimo(), in.getCostoUnitario(), in.estaBajoMinimo() ? "BAJO MÍNIMO" : "OK"});
        }
    }

    void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        cboUnidad.setSelectedIndex(0);
        txtStock.setText("");
        txtMinimo.setText("");
        txtCosto.setText("");
        txtCodigo.setEditable(true);
        tabla.clearSelection();
        txtCodigo.requestFocus();
    }

    private Insumo leerFormulario() {
        if (!Validador.esDecimal(txtStock.getText()) || !Validador.esDecimal(txtMinimo.getText())
                || !Validador.esDecimal(txtCosto.getText())) {
            error("Stock, stock mínimo y costo deben ser números.");
            return null;
        }
        return new Insumo(txtCodigo.getText().trim().toUpperCase(), txtNombre.getText().trim(),
                String.valueOf(cboUnidad.getEditor().getItem()).trim(),
                Validador.leerDecimal(txtStock.getText()), Validador.leerDecimal(txtMinimo.getText()),
                Validador.leerDecimal(txtCosto.getText()));
    }

    private void mostrar(Insumo in) {
        txtCodigo.setText(in.getCodigo());
        txtNombre.setText(in.getNombre());
        cboUnidad.getEditor().setItem(in.getUnidad());
        txtStock.setText(String.valueOf(Validador.redondear(in.getStock())));
        txtMinimo.setText(String.valueOf(in.getStockMinimo()));
        txtCosto.setText(String.valueOf(in.getCostoUnitario()));
        txtCodigo.setEditable(false);
    }

    void buscar() {
        Insumo in = gestion.buscarPorCodigo(txtCodigo.getText().trim());
        if (in == null) {
            error("No se encontró un insumo con ese código.");
        } else {
            mostrar(in);
        }
    }

    void registrar() {
        Insumo in = leerFormulario();
        if (in == null) {
            return;
        }
        String r = gestion.registrar(in);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Insumo registrado con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        Insumo in = leerFormulario();
        if (in == null) {
            return;
        }
        String r = gestion.actualizar(in);
        if (r == null) {
            listar();
            mensaje("Insumo actualizado con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (!confirmar("¿Eliminar el insumo " + codigo + "?")) {
            return;
        }
        String r = gestion.eliminar(codigo, SistemaTextil.getGestionModelos());
        if (r == null) {
            listar();
            limpiar();
            mensaje("Insumo eliminado.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            Insumo in = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (in != null) {
                mostrar(in);
            }
        }
    }
}
