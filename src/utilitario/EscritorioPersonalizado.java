package utilitario;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JDesktopPane;

/**
 * Fondo de la ventana principal (igual que en el Lab de las semanas 13 y 14):
 * dibuja una imagen y, encima, el mensaje de bienvenida del usuario.
 */
public class EscritorioPersonalizado extends JDesktopPane {

    private BufferedImage img;
    private String mensaje;

    public EscritorioPersonalizado(String mensaje) {
        this.mensaje = mensaje;
        try {
            img = ImageIO.read(getClass().getResourceAsStream("/imagenes/fondoTextil.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

        // Franja inferior con el usuario que inició sesión
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(0, getHeight() - 40, getWidth(), 40);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        g2.drawString(mensaje, 20, getHeight() - 14);
    }
}
