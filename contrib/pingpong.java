import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class pingpong extends JPanel implements ActionListener, Runnable {

    // =======================================================
    // VALORES AJUSTABLES
    // =======================================================
    private static final int ANCHO = 800;
    private static final int ALTO = 600;
    private static final float VELOCIDAD_PALETA = 6f;
    private static final float VELOCIDAD_BOMBA_X = 4f;
    private static final float VELOCIDAD_BOMBA_Y = 3f;
    private static final int PALETA_ANCHO = 15;
    private static final int PALETA_ALTO = 80;
    private static final int RADIO_BOMBA = 15;

    // Estado del juego
    private boolean enJuego = true;
    private int puntosP1 = 0;
    private int puntosP2 = 0;

    // Posiciones de las paletas
    private float p1Y = 160;
    private float p2Y = 160;
    private boolean p1Arriba = false, p1Abajo = false;
    private boolean p2Arriba = false, p2Abajo = false;

    // Posición y velocidad de la bomba
    private float bombaX = 300;
    private float bombaY = 200;
    private float bombaVelX = VELOCIDAD_BOMBA_X;
    private float bombaVelY = VELOCIDAD_BOMBA_Y;

    private Thread gameThread;
    private Random random;

    public pingpong() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(30, 30, 45));
        setFocusable(true);
        addKeyListener(new TAdapter());

        random = new Random();
        reiniciarRonda(1);
        
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void reiniciarRonda(int quienSaca) {
        p1Y = 160;
        p2Y = 160;
        bombaX = ANCHO / 2;
        bombaY = ALTO / 2;
        bombaVelX = (quienSaca == 1) ? VELOCIDAD_BOMBA_X : -VELOCIDAD_BOMBA_X;
        bombaVelY = (random.nextBoolean() ? 1 : -1) * VELOCIDAD_BOMBA_Y;
        enJuego = true;
    }

    @Override
    public void run() {
        while (true) {
            if (enJuego) {
                actualizarLogica();
            }
            repaint();
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarLogica() {
        // Movimiento Paleta Jugador 1 (W y S)
        if (p1Arriba) p1Y -= VELOCIDAD_PALETA;
        if (p1Abajo) p1Y += VELOCIDAD_PALETA;
        if (p1Y < 0) p1Y = 0;
        if (p1Y > ALTO - PALETA_ALTO) p1Y = ALTO - PALETA_ALTO;

        // Movimiento Paleta Jugador 2 (Flecha Arriba y Flecha Abajo)
        if (p2Arriba) p2Y -= VELOCIDAD_PALETA;
        if (p2Abajo) p2Y += VELOCIDAD_PALETA;
        if (p2Y < 0) p2Y = 0;
        if (p2Y > ALTO - PALETA_ALTO) p2Y = ALTO - PALETA_ALTO;

        // Movimiento de la bomba
        bombaX += bombaVelX;
        bombaY += bombaVelY;

        // Rebote en paredes superior e inferior
        if (bombaY - RADIO_BOMBA < 0 || bombaY + RADIO_BOMBA > ALTO) {
            bombaVelY *= -1;
        }

        // Colisión con Paleta Jugador 1 (Izquierda)
        Rectangle rectP1 = new Rectangle(30, (int)p1Y, PALETA_ANCHO, PALETA_ALTO);
        Rectangle rectBomba = new Rectangle((int)(bombaX - RADIO_BOMBA), (int)(bombaY - RADIO_BOMBA), RADIO_BOMBA * 2, RADIO_BOMBA * 2);

        if (rectP1.intersects(rectBomba)) {
            bombaVelX *= -1.05f;
            bombaX = 30 + PALETA_ANCHO + RADIO_BOMBA;
        }

        // Colisión con Paleta Jugador 2 (Derecha)
        Rectangle rectP2 = new Rectangle(ANCHO - 45, (int)p2Y, PALETA_ANCHO, PALETA_ALTO);
        if (rectP2.intersects(rectBomba)) {
            bombaVelX *= -1.05f;
            bombaX = ANCHO - 45 - RADIO_BOMBA;
        }

        // Si la bomba pasa al Jugador 1 -> Punto para P2
        if (bombaX < 0) {
            puntosP2++;
            enJuego = false;
        }
        // Si la bomba pasa al Jugador 2 -> Punto para P1
        else if (bombaX > ANCHO) {
            puntosP1++;
            enJuego = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Línea central punteada
        g2d.setColor(new Color(60, 60, 80));
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(ANCHO / 2, 0, ANCHO / 2, ALTO);

        // Paleta Jugador 1 (Roja)
        g2d.setColor(new Color(255, 75, 120));
        g2d.fillRoundRect(30, (int)p1Y, PALETA_ANCHO, PALETA_ALTO, 10, 10);

        // Paleta Jugador 2 (Celeste)
        g2d.setColor(new Color(0, 204, 255));
        g2d.fillRoundRect(ANCHO - 45, (int)p2Y, PALETA_ANCHO, PALETA_ALTO, 10, 10);

        // Bomba (Naranja)
        g2d.setColor(new Color(255, 140, 0));
        g2d.fillOval((int)(bombaX - RADIO_BOMBA), (int)(bombaY - RADIO_BOMBA), RADIO_BOMBA * 2, RADIO_BOMBA * 2);

        // HUD Superior
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("P1 (W/S): " + puntosP1, 50, 30);
        g2d.drawString("P2 (Flechas): " + puntosP2, ANCHO - 180, 30);

        if (!enJuego) {
            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.fillRect(0, 0, ANCHO, ALTO);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.drawString("¡PUNTO PARA EL RIVAL!", 135, 180);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Presiona [ESPACIO] para la siguiente ronda", 130, 240);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_W) p1Arriba = true;
            if (key == KeyEvent.VK_S) p1Abajo = true;
            if (key == KeyEvent.VK_UP) p2Arriba = true;
            if (key == KeyEvent.VK_DOWN) p2Abajo = true;

            if (key == KeyEvent.VK_SPACE && !enJuego) {
                reiniciarRonda(puntosP1 > puntosP2 ? 2 : 1);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_W) p1Arriba = false;
            if (key == KeyEvent.VK_S) p1Abajo = false;
            if (key == KeyEvent.VK_UP) p2Arriba = false;
            if (key == KeyEvent.VK_DOWN) p2Abajo = false;
        }
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("pingpong - 2 Jugadores");
        ventana.add(new pingpong());
        ventana.pack();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.setResizable(false);
        ventana.setVisible(true);
    }
}
