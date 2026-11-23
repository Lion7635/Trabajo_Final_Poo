package gui;

import clases.Empleado;
import clases.EjecutivoVentas;
import clases.JefeProduccion;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import utilitario.EscritorioPersonalizado;

/**
 * Ventana principal. RF09: cada usuario solo ve los menús de su rol.
 * El permiso se pregunta al objeto Empleado (método polimórfico tienePermiso).
 */
public class frmPrincipal extends JFrame {

    private Empleado usuario;

    public frmPrincipal(Empleado usuario) {
        this.usuario = usuario;
        setTitle("Sistema Textil Gamarra - " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);

        EscritorioPersonalizado escritorio = new EscritorioPersonalizado("Confecciones Gamarra",
                "Bienvenido(a), " + usuario.getNombres() + " · " + usuario.getRol());
        setLayout(new BorderLayout());
        add(escritorio, BorderLayout.CENTER);

        JLabel barraEstado = new JLabel("  Usuario: " + usuario.getUsuario() + "   |   Rol: " + usuario.getRol()
                + "   |   Use el menú superior para acceder a sus opciones");
        barraEstado.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        barraEstado.setOpaque(true);
        barraEstado.setBackground(new Color(225, 232, 240));
        add(barraEstado, BorderLayout.SOUTH);

        crearMenu();
    }

    private void crearMenu() {
        JMenuBar barra = new JMenuBar();

        // Sesión (todos)
        JMenu mnSesion = new JMenu("Sesión");
        mnSesion.setIcon(icono("archivo.png"));
        JMenuItem itmCerrarSesion = new JMenuItem("Cerrar sesión");
        JMenuItem itmSalir = new JMenuItem("Salir", icono("cerrar.png"));
        itmCerrarSesion.addActionListener(e -> cerrarSesion());
        itmSalir.addActionListener(e -> System.exit(0));
        mnSesion.add(itmCerrarSesion);
        mnSesion.add(itmSalir);
        barra.add(mnSesion);

        // Mantenimiento (Administrador)
        JMenu mnMantenimiento = new JMenu("Mantenimiento");
        mnMantenimiento.setIcon(icono("mantenimiento.png"));
        agregar(mnMantenimiento, "Empleados", Empleado.MOD_EMPLEADOS);
        agregar(mnMantenimiento, "Modelos de prendas", Empleado.MOD_MODELOS);
        agregar(mnMantenimiento, "Telas", Empleado.MOD_MATERIA_PRIMA);
        agregar(mnMantenimiento, "Insumos y avíos", Empleado.MOD_MATERIA_PRIMA);
        agregar(mnMantenimiento, "Servicios de acabado", Empleado.MOD_MATERIA_PRIMA);
        agregar(mnMantenimiento, "Paquetes promocionales", Empleado.MOD_MATERIA_PRIMA);
        agregarSiTieneOpciones(barra, mnMantenimiento);

        // Ventas (Ejecutivo de Ventas)
        JMenu mnVentas = new JMenu("Ventas");
        mnVentas.setIcon(icono("book.png"));
        agregar(mnVentas, "Clientes", Empleado.MOD_CLIENTES);
        agregar(mnVentas, "Nuevo pedido", Empleado.MOD_PEDIDOS);
        agregar(mnVentas, "Pedidos: adelantos, despacho y cobro", Empleado.MOD_PEDIDOS);
        agregarSiTieneOpciones(barra, mnVentas);

        // Producción (Jefe de Producción)
        JMenu mnProduccion = new JMenu("Producción");
        mnProduccion.setIcon(icono("edit.png"));
        agregar(mnProduccion, "Avance de producción y calidad", Empleado.MOD_PRODUCCION);
        agregarSiTieneOpciones(barra, mnProduccion);

        // Consultas (todos los roles)
        JMenu mnConsultas = new JMenu("Consultas");
        mnConsultas.setIcon(icono("consultar.png"));
        agregar(mnConsultas, "Tablero de pedidos", Empleado.MOD_TABLERO);
        agregar(mnConsultas, "Stock de telas", Empleado.MOD_STOCK);
        agregarSiTieneOpciones(barra, mnConsultas);

        // Reportes (Administrador)
        JMenu mnReportes = new JMenu("Reportes");
        mnReportes.setIcon(icono("reporte.png"));
        agregar(mnReportes, "Reportes gerenciales", Empleado.MOD_REPORTES);
        agregarSiTieneOpciones(barra, mnReportes);

        setJMenuBar(barra);
    }

    // Agrega la opción solo si el rol del usuario tiene permiso para ese módulo
    private void agregar(JMenu menu, String texto, String modulo) {
        if (usuario.tienePermiso(modulo)) {
            JMenuItem item = new JMenuItem(texto);
            item.addActionListener(e -> abrirVentana(texto));
            menu.add(item);
        }
    }

    // Crea la ventana que corresponde a la opción elegida
    private void abrirVentana(String opcion) {
        JFrame ventana = null;
        switch (opcion) {
            case "Empleados": ventana = new frmEmpleados(); break;
            case "Modelos de prendas": ventana = new frmModelos(); break;
            case "Telas": ventana = new frmTelas(); break;
            case "Insumos y avíos": ventana = new frmInsumos(); break;
            case "Servicios de acabado": ventana = new frmServicios(); break;
            case "Paquetes promocionales": ventana = new frmPaquetes(); break;
            case "Clientes": ventana = new frmClientes(); break;
            case "Nuevo pedido": ventana = new frmNuevoPedido((EjecutivoVentas) usuario); break;
            case "Pedidos: adelantos, despacho y cobro": ventana = new frmGestionPedidos(); break;
            case "Avance de producción y calidad": ventana = new frmProduccion((JefeProduccion) usuario); break;
            case "Tablero de pedidos": ventana = new frmTablero(); break;
            case "Stock de telas": ventana = new frmStockTelas(); break;
            case "Reportes gerenciales": ventana = new frmReportes(); break;
        }
        if (ventana != null) {
            ventana.setVisible(true);
        }
    }

    private void agregarSiTieneOpciones(JMenuBar barra, JMenu menu) {
        if (menu.getItemCount() > 0) {
            barra.add(menu);
        }
    }

    private ImageIcon icono(String nombre) {
        if (getClass().getResource("/imagenes/" + nombre) == null) {
            return null;
        }
        return new ImageIcon(getClass().getResource("/imagenes/" + nombre));
    }

    private void cerrarSesion() {
        // Cierra todas las ventanas abiertas y vuelve al login
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            w.dispose();
        }
        SistemaTextil.setUsuarioActual(null);
        new frmLogin().setVisible(true);
    }
}
