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
import java.util.ArrayList;
import java.util.List;

public class TankDuelScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final float TANQUE_TAMANO = 44f;
    private static final float VELOCIDAD_TANQUE = 210f;
    private static final float BALA_TAMANO = 14f;
    private static final float VELOCIDAD_BALA = 480f;
    private static final float TIEMPO_RECARGA = 0.40f;
    private static final int VIDA_MAXIMA = 3;
    private static final float MARGEN_ARENA = 40f;
    private static final int PUNTOS_VICTORIA = 1;
    private static final int PUNTOS_KO = 2;
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

    private final Texture bgTex = cargarTextura("tanks/arena_bg.png");
    private final Texture crateTex = cargarTextura("tanks/crate.png");
    private final Texture[] laserTex = {
        cargarTextura("tanks/laser_p1.png"), cargarTextura("tanks/laser_p2.png")
    };
    private final Texture[] tankTex = {
        cargarTextura("tanks/tank_p1.png"), cargarTextura("tanks/tank_p2.png")
    };
    private final Sprite[] tankSprites = new Sprite[2];

    private final float[] posX = new float[2];
    private final float[] posY = new float[2];
    private final float[] angle = new float[2];
    private final int[] dirX = new int[2];
    private final int[] dirY = new int[2];
    private final int[] hp = new int[2];
    private final float[] cooldown = new float[2];
    private final Rectangle[] playerRect = { new Rectangle(), new Rectangle() };

    public TankDuelScreen(Game game) {
        super(game);
        for (int i = 0; i < 2; i++) {
            tankSprites[i] = new Sprite(tankTex[i]);
            tankSprites[i].setSize(TANQUE_TAMANO, TANQUE_TAMANO);
            tankSprites[i].setOriginCenter();
        }
        reiniciarDuelo();
    }

    private Texture cargarTextura(String path) {
        Texture t = new Texture(path);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    private void reiniciarDuelo() {
        posX[0] = INICIO_X; posY[0] = (VIRTUAL_HEIGHT - TANQUE_TAMANO) / 2f;
        dirX[0] = 1; dirY[0] = 0; angle[0] = 0f; hp[0] = VIDA_MAXIMA; cooldown[0] = 0f;
        playerRect[0].set(posX[0], posY[0], TANQUE_TAMANO, TANQUE_TAMANO);

        posX[1] = VIRTUAL_WIDTH - INICIO_X - TANQUE_TAMANO; posY[1] = posY[0];
        dirX[1] = -1; dirY[1] = 0; angle[1] = 180f; hp[1] = VIDA_MAXIMA; cooldown[1] = 0f;
        playerRect[1].set(posX[1], posY[1], TANQUE_TAMANO, TANQUE_TAMANO);

        balas.clear();
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        for (int p = 0; p < 2; p++) {
            if (cooldown[p] > 0f) cooldown[p] -= delta;
            moverTanque(p, delta);
            if (Gdx.input.isKeyJustPressed(TECLAS[p][4]) && cooldown[p] <= 0f) {
                disparar(p);
                cooldown[p] = TIEMPO_RECARGA;
            }
        }
        actualizarBalas(delta);
    }

    private void moverTanque(int p, float delta) {
        float oldX = posX[p], oldY = posY[p];
        if (Gdx.input.isKeyPressed(TECLAS[p][0])) { posY[p] += VELOCIDAD_TANQUE * delta; dirX[p] = 0; dirY[p] = 1; angle[p] = 90f; }
        else if (Gdx.input.isKeyPressed(TECLAS[p][1])) { posY[p] -= VELOCIDAD_TANQUE * delta; dirX[p] = 0; dirY[p] = -1; angle[p] = 270f; }
        else if (Gdx.input.isKeyPressed(TECLAS[p][2])) { posX[p] -= VELOCIDAD_TANQUE * delta; dirX[p] = -1; dirY[p] = 0; angle[p] = 180f; }
        else if (Gdx.input.isKeyPressed(TECLAS[p][3])) { posX[p] += VELOCIDAD_TANQUE * delta; dirX[p] = 1; dirY[p] = 0; angle[p] = 0f; }

        posX[p] = MathUtils.clamp(posX[p], MARGEN_ARENA, VIRTUAL_WIDTH - MARGEN_ARENA - TANQUE_TAMANO);
        posY[p] = MathUtils.clamp(posY[p], MARGEN_ARENA, VIRTUAL_HEIGHT - MARGEN_ARENA - TANQUE_TAMANO);
        playerRect[p].set(posX[p], posY[p], TANQUE_TAMANO, TANQUE_TAMANO);

        int otro = 1 - p;
        if (chocaConMuro(playerRect[p]) || playerRect[p].overlaps(playerRect[otro])) {
            posX[p] = oldX; posY[p] = oldY;
            playerRect[p].set(posX[p], posY[p], TANQUE_TAMANO, TANQUE_TAMANO);
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

            if (b.x < MARGEN_ARENA || b.x > VIRTUAL_WIDTH - MARGEN_ARENA - BALA_TAMANO ||
                b.y < MARGEN_ARENA || b.y > VIRTUAL_HEIGHT - MARGEN_ARENA - BALA_TAMANO ||
                chocaConMuro(b.rect)) {
                balas.remove(i);
                continue;
            }

            int rival = 1 - b.dueno;
            if (b.rect.overlaps(playerRect[rival])) {
                balas.remove(i);
                hp[rival]--;
                if (hp[rival] <= 0) finishRound();
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
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        batch.draw(bgTex, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        for (Rectangle m : muros) {
            batch.draw(crateTex, m.x, m.y, m.width, m.height);
        }

        for (Bala b : balas) {
            batch.draw(laserTex[b.dueno], b.x, b.y, BALA_TAMANO, BALA_TAMANO);
        }

        for (int p = 0; p < 2; p++) {
            tankSprites[p].setPosition(posX[p], posY[p]);
            tankSprites[p].setRotation(angle[p]);
            tankSprites[p].draw(batch);
        }

        font.getData().setScale(1.1f);
        font.setColor(1f, 0.35f, 0.45f, 1f);
        font.draw(batch, "P1 HP: " + hp[0] + "/" + VIDA_MAXIMA + "  [WASD / ESPACIO]", 50f, 30f);

        font.setColor(0.2f, 0.75f, 1f, 1f);
        font.draw(batch, "P2 HP: " + hp[1] + "/" + VIDA_MAXIMA + "  [FLECHAS / ENTER]", VIRTUAL_WIDTH - 360f, 30f);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (hp[0] == hp[1]) return "¡EMPATE EN EL DUELO DE TANQUES!";
        int ganador = (hp[0] > hp[1]) ? 0 : 1;
        int pts = (hp[1 - ganador] <= 0) ? PUNTOS_KO : PUNTOS_VICTORIA;
        if (ganador == 0) p1GlobalWins += pts;
        else p2GlobalWins += pts;
        return "¡JUGADOR " + (ganador + 1) + " GANA (" + pts + (pts == 1 ? " PUNTO" : " PUNTOS") + ")!";
    }

    @Override
    protected void onRoundReset() {
        reiniciarDuelo();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: HP " + hp[0] + " (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: HP " + hp[1] + " (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "HP Final: P1 [" + hp[0] + "]  vs  P2 [" + hp[1] + "]";
    }

    @Override
    public void dispose() {
        super.dispose();
        bgTex.dispose();
        crateTex.dispose();
        for (int i = 0; i < 2; i++) {
            laserTex[i].dispose();
            tankTex[i].dispose();
        }
    }
}
