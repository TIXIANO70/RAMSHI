package io.github.ramshi.servidor;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.List;

/** La única pantalla del servidor: la IP para conectarse y el botón para apagarlo, con los colores y la fuente del juego. */
public class VentanaServidor extends JPanel {

    private static final Color FONDO = new Color(0.06f, 0.06f, 0.08f);
    private static final Color TITULO = new Color(1f, 0.85f, 0.2f);
    private static final Color SUBTITULO = new Color(0.4f, 0.8f, 1f);
    private static final Color TEXTO = new Color(0.9f, 0.9f, 0.92f);
    private static final Color NOTA = new Color(0.6f, 0.6f, 0.65f);
    private static final Color ROJO = new Color(0.75f, 0.2f, 0.2f);
    private static final Color TEXTO_BOTON = new Color(0.85f, 0.85f, 0.88f);
    private static final float BRILLO_NORMAL = 0.6f;
    private static final float BRILLO_BORDE = 0.45f;

    private static final int ANCHO = 440;
    private static final int TITULO_Y = 58;
    private static final int PUERTO_Y = 88;
    private static final int PRIMERA_IP_Y = 138;
    private static final int ALTO_IP = 36;
    private static final int NOTA_Y_DESDE_IP = 6;
    private static final int BOTON_ANCHO = 260;
    private static final int BOTON_ALTO = 50;
    private static final int MARGEN = 28;

    private final List<String> ips;
    private final int puerto;
    private final Font fuenteTitulo;
    private final Font fuenteIp;
    private final Font fuenteChica;
    private final Font fuenteBoton;
    private final Rectangle boton;
    private boolean encima;

    public VentanaServidor(List<String> ips, int puerto) {
        this.ips = ips;
        this.puerto = puerto;
        Font normal = cargarFuente("/fuentes/PixelifySans-Regular.ttf", Font.PLAIN);
        Font negrita = cargarFuente("/fuentes/PixelifySans-Bold.ttf", Font.BOLD);
        fuenteTitulo = negrita.deriveFont(30f);
        fuenteIp = negrita.deriveFont(28f);
        fuenteChica = normal.deriveFont(15f);
        fuenteBoton = negrita.deriveFont(20f);

        int botonY = notaY() + MARGEN;
        boton = new Rectangle((ANCHO - BOTON_ANCHO) / 2, botonY, BOTON_ANCHO, BOTON_ALTO);
        setPreferredSize(new Dimension(ANCHO, botonY + BOTON_ALTO + MARGEN));
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean antes = encima;
                encima = boton.contains(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(encima ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                if (antes != encima) repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (boton.contains(e.getPoint())) apagar();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) apagar();
            }
        });
    }

    public static void mostrar(List<String> ips, int puerto) {
        VentanaServidor panel = new VentanaServidor(ips, puerto);
        JFrame ventana = new JFrame("Servidor RAMSHI");
        ventana.setContentPane(panel);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        try (InputStream icono = VentanaServidor.class.getResourceAsStream("/icono.png")) {
            if (icono != null) ventana.setIconImage(ImageIO.read(icono));
        } catch (Exception e) {
            // Sin ícono no pasa nada.
        }
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        panel.requestFocusInWindow();
    }

    private static void apagar() {
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(FONDO);
        g2.fillRect(0, 0, getWidth(), getHeight());

        centrar(g2, fuenteTitulo, TITULO, "SERVIDOR RAMSHI", TITULO_Y);
        centrar(g2, fuenteChica, SUBTITULO, "Encendido", PUERTO_Y);
        for (int i = 0; i < ips.size(); i++) {
            centrar(g2, fuenteIp, TEXTO, "IP: " + ips.get(i), PRIMERA_IP_Y + i * ALTO_IP);
        }
        centrar(g2, fuenteIp, SUBTITULO, "Puerto: " + puerto, PRIMERA_IP_Y + ips.size() * ALTO_IP);
        centrar(g2, fuenteChica, NOTA, "Los jugadores lo eligen en BUSCAR SERVIDOR", notaY());

        g2.setColor(encima ? ROJO : escalar(ROJO, BRILLO_NORMAL));
        g2.fill(boton);
        g2.setColor(encima ? Color.WHITE : mezclarConBlanco(ROJO, BRILLO_BORDE));
        g2.drawRect(boton.x, boton.y, boton.width - 1, boton.height - 1);
        g2.setFont(fuenteBoton);
        FontMetrics medidas = g2.getFontMetrics();
        String texto = "APAGAR SERVIDOR";
        g2.setColor(encima ? Color.WHITE : TEXTO_BOTON);
        g2.drawString(texto, boton.x + (boton.width - medidas.stringWidth(texto)) / 2,
            boton.y + (boton.height + medidas.getAscent() - medidas.getDescent()) / 2);
    }

    /** Y de la nota que va debajo de las IP y del puerto. */
    private int notaY() {
        return PRIMERA_IP_Y + ips.size() * ALTO_IP + ALTO_IP - NOTA_Y_DESDE_IP;
    }

    private void centrar(Graphics2D g2, Font fuente, Color color, String texto, int y) {
        g2.setFont(fuente);
        g2.setColor(color);
        g2.drawString(texto, (getWidth() - g2.getFontMetrics().stringWidth(texto)) / 2, y);
    }

    private static Color escalar(Color c, float factor) {
        return new Color(Math.round(c.getRed() * factor), Math.round(c.getGreen() * factor), Math.round(c.getBlue() * factor));
    }

    private static Color mezclarConBlanco(Color c, float t) {
        return new Color(Math.round(c.getRed() + (255 - c.getRed()) * t), Math.round(c.getGreen() + (255 - c.getGreen()) * t),
            Math.round(c.getBlue() + (255 - c.getBlue()) * t));
    }

    private static Font cargarFuente(String ruta, int estiloRespaldo) {
        try (InputStream datos = VentanaServidor.class.getResourceAsStream(ruta)) {
            if (datos != null) return Font.createFont(Font.TRUETYPE_FONT, datos);
        } catch (Exception e) {
            // Si no está la fuente del juego se usa una del sistema.
        }
        return new Font(Font.SANS_SERIF, estiloRespaldo, 12);
    }
}
