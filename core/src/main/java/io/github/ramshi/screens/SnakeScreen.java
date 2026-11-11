package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class SnakeScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final float INTERVALO_INICIAL = 0.098f;
    private static final float INTERVALO_FINAL = 0.065f;
    private static final int DIFERENCIA_PARA_3 = 4;
    private static final int DIFERENCIA_PARA_2 = 2;
    private static final int GRILLA = 14;
    private static final float TAMANO_CASILLA = 16f;
    private static final float[] ZONA_X = {88f, 488f};
    private static final float ZONA_Y = 108f;
    private static final int LARGO_INICIAL = 3;
    private static final int[] INICIO_X = {4, GRILLA - 5};
    private static final int[] DIR_INICIAL = {3, 1};
    private static final int[][] TECLAS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };

    private static final int[] DX = {0, -1, 0, 1};
    private static final int[] DY = {1, 0, -1, 0};
    private static final String[] NOMBRES_DIR = {"Up", "Left", "Down", "Right"};
    private static final int[] MASCARAS_CUERPO = {5, 10, 6, 3, 12, 9};
    private static final String[] SUFIJOS_CUERPO = {"", "_1", "_2", "_3", "_4", "_5"};

    private final List<Texture> texturas = new ArrayList<>();
    private final Texture casilla1 = cargar("snake/tiles/Tile1.png");
    private final Texture casilla2 = cargar("snake/tiles/Tile2.png");
    private final Texture huevo = cargar("snake/egg.png");
    private final Texture[][] cabezas = new Texture[2][4];
    private final Texture[][] colas = new Texture[2][4];
    private final Texture[][] cuerpos = new Texture[2][16];

    private final List<List<int[]>> serpientes = new ArrayList<>();
    private final int[] dir = new int[2];
    private final int[] sigDir = new int[2];
    private final int[] crecer = new int[2];
    private final int[] puntaje = new int[2];
    private final int[] record = new int[2];
    private final int[] comidaX = new int[2];
    private final int[] comidaY = new int[2];
    private float tiempoMovimiento;

    public SnakeScreen(Game juego) {
        super(juego);
        for (int p = 0; p < 2; p++) {
            String n = "P" + (p + 1);
            for (int d = 0; d < 4; d++) {
                cabezas[p][d] = cargar("snake/head/Cabeza" + n + "_" + NOMBRES_DIR[d] + ".png");
                colas[p][d] = cargar("snake/tail/Cola" + n + "_" + NOMBRES_DIR[d] + ".png");
            }
            for (int i = 0; i < MASCARAS_CUERPO.length; i++) {
                cuerpos[p][MASCARAS_CUERPO[i]] = cargar("snake/Body/Body" + n + SUFIJOS_CUERPO[i] + ".png");
            }
            serpientes.add(new ArrayList<int[]>());
            reaparecer(p);
        }
    }

    private Texture cargar(String ruta) {
        Texture t = new Texture(ruta);
        texturas.add(t);
        return t;
    }

    @Override protected String getConsigna() { return "¡COMÉ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.cruz("mover")
        };
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        for (int p = 0; p < 2; p++) {
            for (int d = 0; d < 4; d++) {
                if (Gdx.input.isKeyJustPressed(TECLAS[p][d]) && d != (dir[p] + 2) % 4) sigDir[p] = d;
            }
        }
        float intervalo = MathUtils.lerp(INTERVALO_INICIAL, INTERVALO_FINAL, 1f - tiempoRestante / DURACION_RONDA);
        tiempoMovimiento += delta;
        if (tiempoMovimiento >= intervalo) {
            tiempoMovimiento -= intervalo;
            avanzar(0);
            avanzar(1);
        }
    }

    private void avanzar(int p) {
        List<int[]> s = serpientes.get(p);
        dir[p] = sigDir[p];
        int[] cabeza = s.get(s.size() - 1);
        int x = cabeza[0] + DX[dir[p]];
        int y = cabeza[1] + DY[dir[p]];
        s.add(new int[]{x, y});
        if (crecer[p] > 0) crecer[p]--;
        else s.remove(0);

        if (x == comidaX[p] && y == comidaY[p]) {
            crecer[p]++;
            puntaje[p]++;
            record[p] = Math.max(record[p], puntaje[p]);
            ubicarComida(p);
        }
        boolean afuera = x < 0 || y < 0 || x >= GRILLA || y >= GRILLA;
        if (afuera || seChoca(s)) reaparecer(p);
    }

    private boolean seChoca(List<int[]> s) {
        int[] cabeza = s.get(s.size() - 1);
        for (int i = 0; i < s.size() - 1; i++) {
            if (s.get(i)[0] == cabeza[0] && s.get(i)[1] == cabeza[1]) return true;
        }
        return false;
    }

    private void reaparecer(int p) {
        List<int[]> s = serpientes.get(p);
        s.clear();
        dir[p] = sigDir[p] = DIR_INICIAL[p];
        for (int i = LARGO_INICIAL - 1; i >= 0; i--) {
            s.add(new int[]{INICIO_X[p] - DX[dir[p]] * i, GRILLA / 2 - DY[dir[p]] * i});
        }
        crecer[p] = 0;
        puntaje[p] = 0;
        ubicarComida(p);
    }

    private void ubicarComida(int p) {
        int intentos = 0;
        do {
            comidaX[p] = MathUtils.random(GRILLA - 1);
            comidaY[p] = MathUtils.random(GRILLA - 1);
        } while (ocupa(p, comidaX[p], comidaY[p]) && ++intentos < 100);
    }

    private boolean ocupa(int p, int x, int y) {
        for (int[] c : serpientes.get(p)) {
            if (c[0] == x && c[1] == y) return true;
        }
        return false;
    }

    private int direccionHacia(int[] desde, int[] hacia) {
        for (int d = 0; d < 4; d++) {
            if (desde[0] + DX[d] == hacia[0] && desde[1] + DY[d] == hacia[1]) return d;
        }
        return 0;
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        for (int p = 0; p < 2; p++) {
            for (int x = 0; x < GRILLA; x++) {
                for (int y = 0; y < GRILLA; y++) dibujarCasilla(batch, (x + y) % 2 == 0 ? casilla1 : casilla2, p, x, y);
            }
            dibujarCasilla(batch, huevo, p, comidaX[p], comidaY[p]);

            List<int[]> s = serpientes.get(p);
            int ultimo = s.size() - 1;
            for (int i = 0; i <= ultimo; i++) {
                int[] c = s.get(i);
                Texture t;
                if (i == ultimo) t = cabezas[p][dir[p]];
                else if (i == 0) t = colas[p][direccionHacia(c, s.get(1))];
                else t = cuerpos[p][(1 << direccionHacia(c, s.get(i - 1))) | (1 << direccionHacia(c, s.get(i + 1)))];
                if (t != null) dibujarCasilla(batch, t, p, c[0], c[1]);
            }
        }
        batch.end();

        float size = GRILLA * TAMANO_CASILLA;
        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(0.25f, 0.25f, 0.25f, 1f);
        for (float x : ZONA_X) formas.rect(x - 1f, ZONA_Y - 1f, size + 2f, size + 2f);
        formas.setColor(0.35f, 0.35f, 0.35f, 1f);
        formas.line(ANCHO_VIRTUAL / 2f, ZONA_Y, ANCHO_VIRTUAL / 2f, ZONA_Y + size);
        formas.end();
    }

    private void dibujarCasilla(SpriteBatch batch, Texture t, int p, int x, int y) {
        batch.draw(t, ZONA_X[p] + x * TAMANO_CASILLA, ZONA_Y + y * TAMANO_CASILLA, TAMANO_CASILLA, TAMANO_CASILLA);
    }

    @Override
    protected String determinarGanador() {
        if (record[0] == record[1]) return "¡EMPATE EN LA RONDA!";
        int diferencia = Math.abs(record[0] - record[1]);
        int pts = 1;
        if (diferencia >= DIFERENCIA_PARA_3) pts = 3;
        else if (diferencia >= DIFERENCIA_PARA_2) pts = 2;
        explicacion = "Récord de " + Math.max(record[0], record[1]) + " huevos contra " + Math.min(record[0], record[1]) + " (ventaja de " + diferencia + "): 4 o más = 3 pts, 2 o 3 = 2, 1 = 1.";
        return sumarPuntos(record[0] > record[1] ? 1 : 2, pts);
    }

    @Override
    protected void reiniciarRonda() {
        tiempoMovimiento = 0f;
        record[0] = record[1] = 0;
        reaparecer(0);
        reaparecer(1);
    }

    @Override protected String getTextoP1() { return "P1: " + puntaje[0] + "  Récord: " + record[0] + "  Victorias: " + victoriasP1; }
    @Override protected String getTextoP2() { return "P2: " + puntaje[1] + "  Récord: " + record[1] + "  Victorias: " + victoriasP2; }
    @Override protected String getResumen() { return "Récord de huevos: P1 (" + record[0] + ") vs P2 (" + record[1] + ")"; }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : texturas) t.dispose();
    }
}
