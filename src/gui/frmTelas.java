package gui;

import clases.Tela;
import gestion.GestionTelas;
import gestion.SistemaTextil;
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
 * RF04 - Mantenimiento de telas (solo Administrador).
 */
public class frmTelas extends FormularioBase {

    private GestionTelas gestion = SistemaTextil.getGestionTelas();
    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtCodigo = new JTextField(8);
    private JComboBox<String> cboTipo = new JComboBox<>();   // editable: se puede escribir un tipo nuevo
    private JTextField txtColor = new JTextField(14);
    private JTextField txtStock = new JTextField(8);
    private JTextField txtMinimo = new JTextField(8);
    private JTextField txtCosto = new JTextField(8);

    public frmTelas() {
        super("Gestión de Telas", 900, 600);
        cboTipo.setEditable(true);
        JPanel form = panelFormulario(new String[]{"Código:", "Tipo de tela:", "Color:", "Metros en stock:", "Stock mínimo (m):", "Costo por metro (S/):"},
                new JComponent[]{txtCodigo, cboTipo, txtColor, txtStock, txtMinimo, txtCosto});

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

        modelo = crearModelo(new String[]{"Código", "Tipo", "Color", "Stock (m)", "Mínimo (m)", "Costo/m", "Estado"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Telas"), BorderLayout.NORTH);
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
        for (int i = 0; i < gestion.getTotalTelas(); i++) {
            Tela t = gestion.getTelas()[i];
            modelo.addRow(new Object[]{t.getCodigo(), t.getTipo(), t.getColor(), Validador.redondear(t.getStock()),
                t.getStockMinimo(), t.getCostoMetro(), t.estaBajoMinimo() ? "BAJO MÍNIMO" : "OK"});
        }
        Object tipoActual = cboTipo.getEditor().getItem();
        cboTipo.removeAllItems();
        String[] tipos = gestion.getTiposDistintos();
        for (int i = 0; i < tipos.length; i++) {
            cboTipo.addItem(tipos[i]);
        }
        cboTipo.getEditor().setItem(tipoActual);
    }

    void limpiar() {
        txtCodigo.setText("");
        cboTipo.getEditor().setItem("");
        txtColor.setText("");
        txtStock.setText("");
        txtMinimo.setText("");
        txtCosto.setText("");
        txtCodigo.setEditable(true);
        tabla.clearSelection();
        txtCodigo.requestFocus();
    }

    private Tela leerFormulario() {
        if (!Validador.esDecimal(txtStock.getText()) || !Validador.esDecimal(txtMinimo.getText())
                || !Validador.esDecimal(txtCosto.getText())) {
            error("Stock, stock mínimo y costo deben ser números.");
            return null;
        }
        String tipo = String.valueOf(cboTipo.getEditor().getItem()).trim();
        return new Tela(txtCodigo.getText().trim().toUpperCase(), tipo, txtColor.getText().trim(),
                Validador.leerDecimal(txtStock.getText()), Validador.leerDecimal(txtMinimo.getText()),
                Validador.leerDecimal(txtCosto.getText()));
    }

    private void mostrar(Tela t) {
        txtCodigo.setText(t.getCodigo());
        cboTipo.getEditor().setItem(t.getTipo());
        txtColor.setText(t.getColor());
        txtStock.setText(String.valueOf(Validador.redondear(t.getStock())));
        txtMinimo.setText(String.valueOf(t.getStockMinimo()));
        txtCosto.setText(String.valueOf(t.getCostoMetro()));
        txtCodigo.setEditable(false);
    }

    void buscar() {
        Tela t = gestion.buscarPorCodigo(txtCodigo.getText().trim());
        if (t == null) {
            error("No se encontró una tela con ese código.");
        } else {
            mostrar(t);
        }
    }

    void registrar() {
        Tela t = leerFormulario();
        if (t == null) {
            return;
        }
        String r = gestion.registrar(t);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Tela registrada con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        Tela t = leerFormulario();
        if (t == null) {
            return;
        }
        String r = gestion.actualizar(t);
        if (r == null) {
            listar();
            mensaje("Tela actualizada con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (!confirmar("¿Eliminar la tela " + codigo + "?")) {
            return;
        }
        String r = gestion.eliminar(codigo, SistemaTextil.getGestionPedidos());
        if (r == null) {
            listar();
            limpiar();
            mensaje("Tela eliminada.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            Tela t = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (t != null) {
                mostrar(t);
            }
        }
    }
}
