package gui;

import clases.Pedido;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * RF10 - Tablero de pedidos activos agrupados por etapa de producción,
 * con su fecha de entrega (en rojo si ya venció).
 */
public class frmTablero extends FormularioBase {

    private DefaultTableModel[] modelos = new DefaultTableModel[Pedido.ETAPAS_ACTIVAS.length];
    private JPanel[] columnas = new JPanel[Pedido.ETAPAS_ACTIVAS.length];
    private JLabel lblResumen = new JLabel();

    public frmTablero() {
        super("Tablero de Pedidos", 1300, 600);
        JPanel tablero = new JPanel(new GridLayout(1, Pedido.ETAPAS_ACTIVAS.length, 6, 0));
        tablero.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        for (int i = 0; i < Pedido.ETAPAS_ACTIVAS.length; i++) {
            modelos[i] = crearModelo(new String[]{"N°", "Cliente", "Entrega"});
            JTable t = crearTabla(modelos[i]);
            t.getColumnModel().getColumn(0).setMaxWidth(45);
            t.getColumnModel().getColumn(2).setMaxWidth(80);
            t.setDefaultRenderer(Object.class, new RenderVencidos());
            columnas[i] = new JPanel(new BorderLayout());
            columnas[i].add(new JScrollPane(t), BorderLayout.CENTER);
            tablero.add(columnas[i]);
        }
        JButton btnActualizar = boton("Actualizar", "update_rotation.png");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnActualizar);
        sur.add(lblResumen);

        add(titulo("Pedidos activos por etapa"), BorderLayout.NORTH);
        add(tablero, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
        btnActualizar.addActionListener(e -> refrescar());
        refrescar();
    }

    @Override
    protected void refrescar() {
        int total = 0;
        int vencidos = 0;
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < Pedido.ETAPAS_ACTIVAS.length; i++) {
            Pedido[] lista = SistemaTextil.getGestionPedidos().listarPorEstado(Pedido.ETAPAS_ACTIVAS[i]);
            modelos[i].setRowCount(0);
            for (int j = 0; j < lista.length; j++) {
                modelos[i].addRow(new Object[]{lista[j].getNumero(), lista[j].getCliente().getNombreCompleto(),
                    Validador.fecha(lista[j].getFechaEntrega())});
                if (lista[j].getFechaEntrega().isBefore(hoy)) {
                    vencidos++;
                }
            }
            total += lista.length;
            columnas[i].setBorder(BorderFactory.createTitledBorder(Pedido.ETAPAS_ACTIVAS[i] + " (" + lista.length + ")"));
        }
        lblResumen.setText("   Pedidos activos: " + total + "   ·   Con fecha de entrega vencida (en rojo): " + vencidos);
    }

    // Pinta de rojo las filas cuya fecha de entrega ya pasó
    private class RenderVencidos extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
            LocalDate entrega = Validador.leerFecha(String.valueOf(tabla.getValueAt(fila, 2)));
            boolean vencido = entrega != null && entrega.isBefore(LocalDate.now());
            if (!seleccionado) {
                c.setForeground(vencido ? new Color(190, 30, 30) : Color.BLACK);
            }
            return c;
        }
    }
}
