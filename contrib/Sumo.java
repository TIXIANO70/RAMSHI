import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Sumo extends JPanel implements ActionListener, Runnable {

    // =======================================================
    // VALORES AJUSTABLES (Resolución exacta 800x600)
    // =======================================================
    private static final int ANCHO = 800;                 
    private static final int ALTO = 600;                  
    private static final float IMPULSO = 12f;             
    private static final int RADIO = 20;                  

    // Plataforma
    private static final int PLAT_X = 200;  
    private static final int PLAT_Y = 350;  
    private static final int PLAT_ANCHO = 400;            
    private static final int PLAT_ALTO = 20;              

    // Estado del juego
    private boolean enJuego = true;
    private int puntosP1 = 0;
    private int puntosP2 = 0;
    private int tiempo = 30;
    private long ultimoSegundo = System.currentTimeMillis();

    // Posiciones iniciales 
    private float p1X = 340;
    private float p1Y = 330; // PLAT_Y - RADIO
    private float p2X = 460;
    private float p2Y = 330; // PLAT_Y - RADIO

    private Thread hilo;

    public Sumo() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(30, 30, 45));
        setFocusable(true);
        addKeyListener(new Teclado());

        reiniciar();
        hilo = new Thread(this);
        hilo.start();
    }

    private void reiniciar() {
        p1X = 340;
        p1Y = 330;
        p2X = 460;
        p2Y = 330;
        tiempo = 10;
        ultimoSegundo = System.currentTimeMillis();
        enJuego = true;
    }

    @Override
    public void run() {
        while (true) {
            if (enJuego) {
                if (System.currentTimeMillis() - ultimoSegundo >= 1000) {
                    tiempo--;
                    ultimoSegundo = System.currentTimeMillis();
                    if (tiempo <= 0) {
                        enJuego = false;
                    }
                }
            }
            repaint();
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibujar Plataforma
        g2d.setColor(new Color(0, 204, 255));
        g2d.fillRoundRect(PLAT_X, PLAT_Y, PLAT_ANCHO, PLAT_ALTO, 10, 10);

        // Jugador 1 (Rojo)
        g2d.setColor(new Color(255, 75, 120));
        g2d.fillOval((int)(p1X - RADIO), (int)(p1Y - RADIO), RADIO * 2, RADIO * 2);

        // Jugador 2 (Celeste)
        g2d.setColor(new Color(0, 255, 180));
        g2d.fillOval((int)(p2X - RADIO), (int)(p2Y - RADIO), RADIO * 2, RADIO * 2);

        // HUD Superior
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("P1 (A/D): " + puntosP1, 40, 40);
        g2d.drawString("Tiempo: " + tiempo + "s", ANCHO / 2 - 50, 40);
        g2d.drawString("P2 (Flechas): " + puntosP2, ANCHO - 180, 40);

        // Fin de ronda
        if (!enJuego) {
            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.fillRect(0, 0, ANCHO, ALTO);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 32));
            g2d.drawString("¡FIN DE LA RONDA!", ANCHO / 2 - 140, ALTO / 2 - 30);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Presiona [ESPACIO] para continuar", ANCHO / 2 - 150, ALTO / 2 + 20);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    private class Teclado extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int tecla = e.getKeyCode();

            if (enJuego) {
                if (tecla == KeyEvent.VK_A) p1X -= IMPULSO;
                if (tecla == KeyEvent.VK_D) p1X += IMPULSO;
                if (tecla == KeyEvent.VK_LEFT) p2X -= IMPULSO;
                if (tecla == KeyEvent.VK_RIGHT) p2X += IMPULSO;

                // Colisión y empuje mutuo
                float difX = p2X - p1X;
                float difY = p2Y - p1Y;
                float distancia = (float) Math.sqrt(difX * difX + difY * difY);
                
                if (distancia < RADIO * 2) {
                    if (tecla == KeyEvent.VK_D || tecla == KeyEvent.VK_RIGHT) {
                        p2X += 12;
                        p1X -= 6;
                    } else if (tecla == KeyEvent.VK_A || tecla == KeyEvent.VK_LEFT) {
                        p1X -= 12;
                        p2X += 6;
                    }
                }

                // Control de caída fuera de la plataforma
                boolean p1Fuera = (p1X < PLAT_X - RADIO || p1X > PLAT_X + PLAT_ANCHO);
                boolean p2Fuera = (p2X < PLAT_X - RADIO || p2X > PLAT_X + PLAT_ANCHO);

                if (p1Fuera && p2Fuera) {
                    enJuego = false;
                } else if (p1Fuera) {
                    puntosP2++;
                    enJuego = false;
                } else if (p2Fuera) {
                    puntosP1++;
                    enJuego = false;
                }
            }

            if (tecla == KeyEvent.VK_SPACE && !enJuego) {
                reiniciar();
            }
        }
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Sumo - 2 Jugadores");
        ventana.add(new Sumo());
        ventana.pack();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.setResizable(false);
        ventana.setVisible(true);
    }
}
