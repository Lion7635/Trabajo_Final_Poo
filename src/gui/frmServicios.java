package gui;

import clases.ServicioAcabado;
import gestion.GestionServicios;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF04 - Mantenimiento de servicios de acabado (solo Administrador).
 */
public class frmServicios extends FormularioBase {

    private GestionServicios gestion = SistemaTextil.getGestionServicios();
    private DefaultTableModel modelo;
    private JTable tabla;

    private JTextField txtCodigo = new JTextField(8);
    private JTextField txtNombre = new JTextField(24);
    private JTextField txtPrecio = new JTextField(8);
    private JTextField txtCosto = new JTextField(8);
    private JCheckBox chkAcabado = new JCheckBox("Es estampado o bordado (el pedido pasa por la etapa En Acabado)");
    private JCheckBox chkLogo = new JCheckBox("Lleva logo (precio especial para clientes institucionales)");

    public frmServicios() {
        super("Servicios de Acabado", 880, 560);
        JPanel form = panelFormulario(new String[]{"Código:", "Nombre:", "Precio por prenda (S/):", "Costo por prenda (S/):", "Etapa:", "Logo:"},
                new JComponent[]{txtCodigo, txtNombre, txtPrecio, txtCosto, chkAcabado, chkLogo});

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

        modelo = crearModelo(new String[]{"Código", "Nombre", "Precio/prenda", "Costo/prenda", "Etapa de acabado", "Con logo"});
        tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Servicios de acabado"), BorderLayout.NORTH);
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
        for (int i = 0; i < gestion.getTotalServicios(); i++) {
            ServicioAcabado s = gestion.getServicios()[i];
            modelo.addRow(new Object[]{s.getCodigo(), s.getNombre(), s.getPrecioPorPrenda(), s.getCostoPorPrenda(),
                s.isRequiereEtapaAcabado() ? "Sí" : "No", s.isConLogo() ? "Sí" : "No"});
        }
    }

    void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCosto.setText("");
        chkAcabado.setSelected(false);
        chkLogo.setSelected(false);
        txtCodigo.setEditable(true);
        tabla.clearSelection();
        txtCodigo.requestFocus();
    }

    private ServicioAcabado leerFormulario() {
        if (!Validador.esDecimal(txtPrecio.getText()) || !Validador.esDecimal(txtCosto.getText())) {
            error("Precio y costo deben ser números.");
            return null;
        }
        return new ServicioAcabado(txtCodigo.getText().trim().toUpperCase(), txtNombre.getText().trim(),
                Validador.leerDecimal(txtPrecio.getText()), Validador.leerDecimal(txtCosto.getText()),
                chkAcabado.isSelected(), chkLogo.isSelected());
    }

    private void mostrar(ServicioAcabado s) {
        txtCodigo.setText(s.getCodigo());
        txtNombre.setText(s.getNombre());
        txtPrecio.setText(String.valueOf(s.getPrecioPorPrenda()));
        txtCosto.setText(String.valueOf(s.getCostoPorPrenda()));
        chkAcabado.setSelected(s.isRequiereEtapaAcabado());
        chkLogo.setSelected(s.isConLogo());
        txtCodigo.setEditable(false);
    }

    void buscar() {
        ServicioAcabado s = gestion.buscarPorCodigo(txtCodigo.getText().trim());
        if (s == null) {
            error("No se encontró un servicio con ese código.");
        } else {
            mostrar(s);
        }
    }

    void registrar() {
        ServicioAcabado s = leerFormulario();
        if (s == null) {
            return;
        }
        String r = gestion.registrar(s);
        if (r == null) {
            listar();
            limpiar();
            mensaje("Servicio registrado con éxito.");
        } else {
            error(r);
        }
    }

    void actualizar() {
        ServicioAcabado s = leerFormulario();
        if (s == null) {
            return;
        }
        String r = gestion.actualizar(s);
        if (r == null) {
            listar();
            mensaje("Servicio actualizado con éxito.");
        } else {
            error(r);
        }
    }

    void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (!confirmar("¿Eliminar el servicio " + codigo + "?")) {
            return;
        }
        String r = gestion.eliminar(codigo, SistemaTextil.getGestionPedidos(), SistemaTextil.getGestionPaquetes());
        if (r == null) {
            listar();
            limpiar();
            mensaje("Servicio eliminado.");
        } else {
            error(r);
        }
    }

    void navegar() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            ServicioAcabado s = gestion.buscarPorCodigo(modelo.getValueAt(fila, 0).toString());
            if (s != null) {
                mostrar(s);
            }
        }
    }
}
