package utilitario;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JDesktopPane;

/**
 * Fondo de la ventana principal (igual idea que el Lab de la semana 13,
 * pero dibujado con código en lugar de una imagen).
 */
public class EscritorioPersonalizado extends JDesktopPane {

    private String titulo;
    private String subtitulo;

    public EscritorioPersonalizado(String titulo, String subtitulo) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(24, 44, 74), getWidth(), getHeight(), new Color(52, 99, 133)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Patrón de líneas tipo tejido
        g2.setColor(new Color(255, 255, 255, 18));
        for (int x = -getHeight(); x < getWidth(); x += 28) {
            g2.drawLine(x, getHeight(), x + getHeight(), 0);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        g2.drawString(titulo, 60, getHeight() / 2 - 10);
        g2.setColor(new Color(200, 220, 240));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2.drawString(subtitulo, 62, getHeight() / 2 + 28);
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
        repaint();
    }
}
