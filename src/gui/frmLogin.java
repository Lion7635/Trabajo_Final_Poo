package gui;

import clases.Empleado;
import gestion.SistemaTextil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * RF09 - Ventana de inicio de sesión. Es la clase principal del proyecto (main).
 */
public class frmLogin extends FormularioBase {

    private static final int INTENTOS_MAXIMOS = 3;

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private int intentos = 0;

    public frmLogin() {
        super("Iniciar sesión", 430, 330);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initComponentes();
    }

    private void initComponentes() {
        txtUsuario = new JTextField(16);
        txtContrasena = new JPasswordField(16);
        JButton btnIngresar = boton("Ingresar", "vcsnormal.png");
        JButton btnSalir = boton("Salir", "cerrar.png");

        JPanel formulario = panelFormulario(new String[]{"Usuario:", "Contraseña:"},
                new JComponent[]{txtUsuario, txtContrasena});
        formulario.setBorder(BorderFactory.createEmptyBorder(18, 30, 6, 30));

        JLabel ayuda = new JLabel("<html><center>Usuarios de prueba: admin / admin123 · "
                + "cmendoza / ventas123 · rhuaman / prod123</center></html>");
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ayuda.setForeground(Color.GRAY);
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 20, 6, 20));

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(btnIngresar);
        botones.add(btnSalir);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(ayuda, BorderLayout.NORTH);
        sur.add(botones, BorderLayout.SOUTH);

        add(titulo("Confecciones Gamarra · Acceso"), BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));
        txtContrasena.addActionListener(e -> ingresar());   // Enter en la contraseña
        txtUsuario.addActionListener(e -> txtContrasena.requestFocus());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            error("Ingrese usuario y contraseña.");
            return;
        }
        Empleado empleado = SistemaTextil.getGestionEmpleados().autenticar(usuario, contrasena);
        if (empleado == null) {
            intentos++;
            txtContrasena.setText("");
            if (intentos >= INTENTOS_MAXIMOS) {
                error("Acceso denegado. Superó los " + INTENTOS_MAXIMOS + " intentos permitidos.");
                System.exit(0);
            }
            error("Acceso denegado: usuario o contraseña incorrectos.\nIntentos restantes: "
                    + (INTENTOS_MAXIMOS - intentos));
            return;
        }
        SistemaTextil.setUsuarioActual(empleado);
        frmPrincipal principal = new frmPrincipal(empleado);
        principal.setVisible(true);
        dispose();
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SistemaTextil.cargarDatosIniciales();
        java.awt.EventQueue.invokeLater(() -> new frmLogin().setVisible(true));
    }
}
