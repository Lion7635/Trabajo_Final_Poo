package gui;

import clases.DetallePrenda;
import clases.HistorialEstado;
import clases.ItemPedido;
import clases.ModeloPrenda;
import clases.Pedido;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import utilitario.Validador;

/**
 * Clase padre de todas las ventanas del sistema (herencia en la GUI).
 * Reúne el código que se repetía en cada formulario: botones con ícono,
 * tablas, formularios etiqueta-campo y mensajes.
 */
public class FormularioBase extends JFrame {

    public FormularioBase(String titulo, int ancho, int alto) {
        setTitle(titulo + " - Sistema Textil Gamarra");
        setSize(ancho, alto);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Cada vez que la ventana recibe el foco se vuelven a leer los arreglos
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refrescar();
            }
        });
    }

    // Las ventanas hijas lo sobrescriben para recargar sus tablas
    protected void refrescar() {
    }

    // ---------- Componentes ----------
    protected JButton boton(String texto, String icono) {
        JButton b = new JButton(texto);
        if (icono != null && getClass().getResource("/imagenes/" + icono) != null) {
            b.setIcon(new ImageIcon(getClass().getResource("/imagenes/" + icono)));
        }
        return b;
    }

    // Modelo de tabla de solo lectura (igual al DefaultTableModel del lab)
    protected DefaultTableModel crearModelo(String[] columnas) {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (int i = 0; i < columnas.length; i++) {
            modelo.addColumn(columnas[i]);
        }
        return modelo;
    }

    protected JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    // Arma un formulario de dos columnas: etiqueta | campo
    protected JPanel panelFormulario(String[] etiquetas, JComponent[] campos) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(new JLabel(etiquetas[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(campos[i], c);
        }
        return panel;
    }

    protected JPanel titulo(String texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(30, 58, 95));
        JLabel l = new JLabel(texto);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 17));
        l.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // ---------- Mensajes ----------
    protected void mensaje(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void error(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected boolean confirmar(String texto) {
        return JOptionPane.showConfirmDialog(this, texto, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // Muestra un texto largo (comprobante, detalle del pedido) con letra de ancho fijo
    public static void mostrarTexto(Component padre, String titulo, String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(700, 520));
        JOptionPane.showMessageDialog(padre, scroll, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    // Texto con el detalle completo de un pedido: ítems, montos, avance por talla e historial
    public static String textoPedido(Pedido p) {
        DateTimeFormatter fh = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String t = "PEDIDO " + p.getNumeroTexto() + "   Estado: " + p.getEstado() + "\n";
        t += "Cliente: " + p.getCliente().getNombreCompleto() + " (" + p.getCliente().getTipo() + ")\n";
        t += "Vendedor: " + p.getEjecutivo().getNombreCompleto();
        if (p.getJefeProduccion() != null) {
            t += "   Jefe de producción: " + p.getJefeProduccion().getNombreCompleto();
        }
        t += "\nRegistro: " + Validador.fecha(p.getFechaRegistro()) + "   Entrega solicitada: "
                + Validador.fecha(p.getFechaEntrega());
        if (p.getFechaDespacho() != null) {
            t += "   Despacho: " + Validador.fecha(p.getFechaDespacho());
        }
        t += "\n\nDETALLE\n";
        for (int i = 0; i < p.getTotalItems(); i++) {
            ItemPedido item = p.getItems()[i];
            t += String.format(" %2d. [%s] %s x %d = %s%n", i + 1, item.getTipoItem(), item.getDescripcion(),
                    item.getCantidad(), Validador.dinero(item.calcularSubtotal()));
        }
        t += "\nPrendas por talla: ";
        for (int i = 0; i < ModeloPrenda.TALLAS.length; i++) {
            t += ModeloPrenda.TALLAS[i] + "=" + p.getPrendasPorTalla(i) + "  ";
        }
        t += "(total " + p.getTotalPrendas() + ")\n";
        t += "Lleva estampado/bordado: " + (p.requiereAcabado() ? "Sí" : "No") + "\n";
        t += String.format("%nSubtotal %s | Descuento %s | IGV %s | TOTAL %s%n",
                Validador.dinero(p.calcularSubtotal() + p.calcularDescuento()), Validador.dinero(p.calcularDescuento()),
                Validador.dinero(p.calcularIgv()), Validador.dinero(p.calcularTotal()));
        t += "Adelanto: " + Validador.dinero(p.getAdelanto()) + " (" + p.getSituacionAdelanto() + ")   Saldo: "
                + Validador.dinero(p.calcularSaldo()) + "\n";
        if (p.getMetrosConsumidos() > 0) {
            t += String.format("Tela consumida: %.1f m (merma %.1f m)   Defectuosas registradas: %d%n",
                    p.getMetrosConsumidos(), p.getMetrosMerma(), p.getTotalDefectuosos());
        }
        if (p.getComprobante() != null) {
            t += "Comprobante: " + p.getComprobante().getTipo() + " " + p.getComprobante().getNumero() + "\n";
        }
        t += "\nHISTORIAL DE ESTADOS\n";
        for (int i = 0; i < p.getTotalHistorial(); i++) {
            HistorialEstado h = p.getHistorial()[i];
            t += " " + h.getFechaHora().format(fh) + "  " + h.getEstadoAnterior() + " -> " + h.getEstadoNuevo()
                    + "  [" + h.getUsuario() + "] " + h.getObservacion() + "\n";
        }
        return t;
    }

    // Prendas de un ítem (para mostrar el detalle de los paquetes)
    public static String prendasDe(ItemPedido item) {
        String t = "";
        DetallePrenda[] prendas = item.getPrendas();
        for (int i = 0; i < prendas.length; i++) {
            t += prendas[i].getCantidad() + " " + prendas[i].getModelo().getNombre() + "; ";
        }
        return t;
    }
}
