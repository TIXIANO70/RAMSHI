package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.ramshi.Main;
import java.util.ArrayList;
import java.util.List;

public class TankDuelScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final float TANQUE_TAMANO = 44f;
    private static final float VELOCIDAD_TANQUE = 210f;
    private static final float BALA_TAMANO = 14f;
    private static final float VELOCIDAD_BALA = 480f;
    private static final float TIEMPO_RECARGA = 0.30f;
    private static final int VIDA_MAXIMA = 3;
    private static final float MARGEN_ARENA = 40f;
    private static final int PUNTOS_VICTORIA = 1;
    private static final int PUNTOS_KO = 2;
    private static final int PUNTOS_KO_PERFECTO = 3;
    private static final float INICIO_X = 100f;
    private static final int[][] TECLAS = {
        {Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE},
        {Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.ENTER}
    };

    private final Rectangle[] muros = {
        new Rectangle(230f, 120f, 48f, 90f),
        new Rectangle(230f, 270f, 48f, 90f),
        new Rectangle(522f, 120f, 48f, 90f),
        new Rectangle(522f, 270f, 48f, 90f)
    };

    private static class Bala {
        float x, y, vx, vy;
        int dueno;
        final Rectangle rect = new Rectangle();
        Bala(float x, float y, float vx, float vy, int dueno) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.dueno = dueno;
            this.rect.set(x, y, BALA_TAMANO, BALA_TAMANO);
        }
    }
    private final List<Bala> balas = new ArrayList<>();

    private final Texture fondoTex = cargarTextura("tanks/arena_bg.png");
    private final Texture cajaTex = cargarTextura("tanks/crate.png");
    private final Texture[] texLaser = {
        cargarTextura("tanks/laser_p1.png"), cargarTextura("tanks/laser_p2.png")
    };
    private final Texture[] texTanque = {
        cargarTextura("tanks/tank_p1.png"), cargarTextura("tanks/tank_p2.png")
    };
    private final Sprite[] spritesTanque = new Sprite[2];

    private final float[] posX = new float[2];
    private final float[] posY = new float[2];
    private final float[] angulo = new float[2];
    private final int[] dirX = new int[2];
    private final int[] dirY = new int[2];
    private final int[] vida = new int[2];
    private final float[] recarga = new float[2];
    private final Rectangle[] rectJugador = { new Rectangle(), new Rectangle() };

    public TankDuelScreen(Game juego) {
        super(juego);
        for (int i = 0; i < 2; i++) {
            spritesTanque[i] = new Sprite(texTanque[i]);
            spritesTanque[i].setSize(TANQUE_TAMANO, TANQUE_TAMANO);
            spritesTanque[i].setOriginCenter();
        }
        reiniciarDuelo();
    }

    private Texture cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    private void reiniciarDuelo() {
        posX[0] = INICIO_X; posY[0] = (ALTO_VIRTUAL - TANQUE_TAMANO) / 2f;
        dirX[0] = 1; dirY[0] = 0; angulo[0] = 0f; vida[0] = VIDA_MAXIMA; recarga[0] = 0f;
        rectJugador[0].set(posX[0], posY[0], TANQUE_TAMANO, TANQUE_TAMANO);

        posX[1] = ANCHO_VIRTUAL - INICIO_X - TANQUE_TAMANO; posY[1] = posY[0];
        dirX[1] = -1; dirY[1] = 0; angulo[1] = 180f; vida[1] = VIDA_MAXIMA; recarga[1] = 0f;
        rectJugador[1].set(posX[1], posY[1], TANQUE_TAMANO, TANQUE_TAMANO);

        balas.clear();
    }

    @Override protected String getConsigna() { return "¡DESTRUÍ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.cruz("mover"),
            Controles.una("disparar", Controles.ESPACIO, Controles.ENTER)
        };
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        for (int p = 0; p < 2; p++) {
            if (recarga[p] > 0f) recarga[p] -= delta;
            moverTanque(p, delta);
            if (Gdx.input.isKeyJustPressed(TECLAS[p][4]) && recarga[p] <= 0f) {
                disparar(p);
                recarga[p] = TIEMPO_RECARGA;
            }
        }
        actualizarBalas(delta);
    }

    private void moverTanque(int p, float delta) {
        float viejoX = posX[p], viejoY = posY[p];
        if (Gdx.input.isKeyPressed(TECLAS[p][0])) { posY[p] += VELOCIDAD_TANQUE * delta; dirX[p] = 0; dirY[p] = 1; angulo[p] = 90f; }
        else if (Gdx.input.isKeyPressed(TECLAS[p][1])) { posY[p] -= VELOCIDAD_TANQUE * delta; dirX[p] = 0; dirY[p] = -1; angulo[p] = 270f; }
        else if (Gdx.input.isKeyPressed(TECLAS[p][2])) { posX[p] -= VELOCIDAD_TANQUE * delta; dirX[p] = -1; dirY[p] = 0; angulo[p] = 180f; }
        else if (Gdx.input.isKeyPressed(TECLAS[p][3])) { posX[p] += VELOCIDAD_TANQUE * delta; dirX[p] = 1; dirY[p] = 0; angulo[p] = 0f; }

        posX[p] = MathUtils.clamp(posX[p], MARGEN_ARENA, ANCHO_VIRTUAL - MARGEN_ARENA - TANQUE_TAMANO);
        posY[p] = MathUtils.clamp(posY[p], MARGEN_ARENA, ALTO_VIRTUAL - MARGEN_ARENA - TANQUE_TAMANO);
        rectJugador[p].set(posX[p], posY[p], TANQUE_TAMANO, TANQUE_TAMANO);

        int otro = 1 - p;
        if (chocaConMuro(rectJugador[p]) || rectJugador[p].overlaps(rectJugador[otro])) {
            posX[p] = viejoX; posY[p] = viejoY;
            rectJugador[p].set(posX[p], posY[p], TANQUE_TAMANO, TANQUE_TAMANO);
        }
    }

    private void disparar(int p) {
        float centro = (TANQUE_TAMANO - BALA_TAMANO) / 2f;
        float bx = posX[p] + centro + dirX[p] * (TANQUE_TAMANO / 2f + 4f);
        float by = posY[p] + centro + dirY[p] * (TANQUE_TAMANO / 2f + 4f);
        balas.add(new Bala(bx, by, dirX[p] * VELOCIDAD_BALA, dirY[p] * VELOCIDAD_BALA, p));
    }

    private void actualizarBalas(float delta) {
        for (int i = balas.size() - 1; i >= 0; i--) {
            Bala b = balas.get(i);
            b.x += b.vx * delta;
            b.y += b.vy * delta;
            b.rect.setPosition(b.x, b.y);

            if (b.x < MARGEN_ARENA || b.x > ANCHO_VIRTUAL - MARGEN_ARENA - BALA_TAMANO ||
                b.y < MARGEN_ARENA || b.y > ALTO_VIRTUAL - MARGEN_ARENA - BALA_TAMANO ||
                chocaConMuro(b.rect)) {
                balas.remove(i);
                continue;
            }

            int rival = 1 - b.dueno;
            if (b.rect.overlaps(rectJugador[rival])) {
                balas.remove(i);
                vida[rival]--;
                if (vida[rival] <= 0) terminarRonda();
            }
        }
    }

    private boolean chocaConMuro(Rectangle r) {
        for (Rectangle m : muros) {
            if (r.overlaps(m)) return true;
        }
        return false;
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(fondoTex, 0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        for (Rectangle m : muros) {
            batch.draw(cajaTex, m.x, m.y, m.width, m.height);
        }

        for (Bala b : balas) {
            batch.draw(texLaser[b.dueno], b.x, b.y, BALA_TAMANO, BALA_TAMANO);
        }

        for (int p = 0; p < 2; p++) {
            spritesTanque[p].setPosition(posX[p], posY[p]);
            spritesTanque[p].setRotation(angulo[p]);
            spritesTanque[p].draw(batch);
        }
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (vida[0] == vida[1]) return "¡EMPATE EN EL DUELO DE TANQUES!";
        int ganador = (vida[0] > vida[1]) ? 0 : 1;
        int pts = PUNTOS_VICTORIA;
        if (vida[1 - ganador] <= 0) pts = (vida[ganador] == VIDA_MAXIMA) ? PUNTOS_KO_PERFECTO : PUNTOS_KO;
        if (pts == PUNTOS_KO_PERFECTO) explicacion = "Destruyó al rival sin perder ninguna vida: 3 puntos.";
        else if (pts == PUNTOS_KO) explicacion = "Destruyó al rival, pero perdió vidas: 2 puntos.";
        else explicacion = "Nadie fue destruido: terminó con más vidas (" + vida[ganador] + " contra " + vida[1 - ganador] + "): 1 punto.";
        return sumarPuntos(ganador + 1, pts);
    }

    @Override
    protected void reiniciarRonda() {
        reiniciarDuelo();
    }

    @Override protected int getVidaMaxima() { return VIDA_MAXIMA; }
    @Override protected int getVida(int jugador) { return vida[jugador]; }

    @Override
    protected String getTextoP1() {
        return "P1 (Victorias: " + victoriasP1 + ")";
    }

    @Override
    protected String getTextoP2() {
        return "P2 (Victorias: " + victoriasP2 + ")";
    }

    @Override
    protected String getResumen() {
        return "HP Final: P1 [" + vida[0] + "]  vs  P2 [" + vida[1] + "]";
    }

    @Override
    public void dispose() {
        super.dispose();
        fondoTex.dispose();
        cajaTex.dispose();
        for (int i = 0; i < 2; i++) {
            texLaser[i].dispose();
            texTanque[i].dispose();
        }
    }
}
