package gui.consultas;

import clases.inventario.Tela;
import gestion.GestionTelas;
import gestion.SistemaTextil;
import gui.FormularioBase;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF10 - Consulta del stock de telas por tipo y color antes de registrar un pedido.
 * "Comprometido" = tela reservada por pedidos registrados o confirmados que aún no se cortan.
 */
public class frmStockTelas extends FormularioBase {

    private GestionTelas gestion = SistemaTextil.getGestionTelas();
    private DefaultTableModel modelo;
    private JComboBox<String> cboTipo = new JComboBox<>();
    private JComboBox<String> cboColor = new JComboBox<>();
    private JLabel lblTotal = new JLabel();
    private boolean cargando = false;

    public frmStockTelas() {
        super("Stock de Telas", 950, 500);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Tipo:"));
        filtros.add(cboTipo);
        filtros.add(new JLabel("Color:"));
        filtros.add(cboColor);

        modelo = crearModelo(new String[]{"Código", "Tipo", "Color", "Stock (m)", "Comprometido (m)", "Disponible (m)", "Mínimo (m)", "Estado"});
        JTable tabla = crearTabla(modelo);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(titulo("Consulta de stock de telas"), BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblTotal, BorderLayout.SOUTH);

        cboTipo.addActionListener(e -> {
            cargarColores();
            listar();
        });
        cboColor.addActionListener(e -> listar());
        cargarTipos();
    }

    @Override
    protected void refrescar() {
        listar();
    }

    private void cargarTipos() {
        cargando = true;
        cboTipo.removeAllItems();
        cboTipo.addItem("Todos");
        String[] tipos = gestion.getTiposDistintos();
        for (int i = 0; i < tipos.length; i++) {
            cboTipo.addItem(tipos[i]);
        }
        cargando = false;
        cargarColores();
        listar();
    }

    private void cargarColores() {
        cargando = true;
        cboColor.removeAllItems();
        cboColor.addItem("Todos");
        String tipo = (String) cboTipo.getSelectedItem();
        String[] colores = gestion.getColores("Todos".equals(tipo) ? null : tipo);
        for (int i = 0; i < colores.length; i++) {
            cboColor.addItem(colores[i]);
        }
        cargando = false;
    }

    void listar() {
        if (cargando || cboTipo.getSelectedItem() == null || cboColor.getSelectedItem() == null) {
            return;
        }
        String tipo = (String) cboTipo.getSelectedItem();
        String color = (String) cboColor.getSelectedItem();
        modelo.setRowCount(0);
        double totalStock = 0;
        double totalDisponible = 0;
        for (int i = 0; i < gestion.getTotalTelas(); i++) {
            Tela t = gestion.getTelas()[i];
            if ((tipo.equals("Todos") || t.getTipo().equalsIgnoreCase(tipo))
                    && (color.equals("Todos") || t.getColor().equalsIgnoreCase(color))) {
                double comprometido = SistemaTextil.getGestionPedidos().metrosComprometidos(t, null);
                double disponible = t.getStock() - comprometido;
                modelo.addRow(new Object[]{t.getCodigo(), t.getTipo(), t.getColor(), Validador.redondear(t.getStock()),
                    Validador.redondear(comprometido), Validador.redondear(disponible), t.getStockMinimo(),
                    t.estaBajoMinimo() ? "BAJO MÍNIMO" : "OK"});
                totalStock += t.getStock();
                totalDisponible += disponible;
            }
        }
        lblTotal.setText(String.format("   Total en stock: %.1f m   ·   Total disponible para nuevos pedidos: %.1f m",
                totalStock, totalDisponible));
    }
}
