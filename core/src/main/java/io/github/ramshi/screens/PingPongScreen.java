package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.Main;

public class PingPongScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int GOLES_PARA_GANAR = 2;
    private static final float PALETA_ANCHO = 15f;
    private static final float PALETA_ALTO = 80f;
    private static final float RADIO_BOMBA = 15f;
    private static final float VELOCIDAD_PALETA = 410f;
    private static final float VELOCIDAD_BOMBA_BASE_X = 480f;
    private static final float VELOCIDAD_BOMBA_BASE_Y = 420f;
    private static final float ACELERACION_REBOTE = 1.08f;
    private static final float P1_X = 30f;
    private static final float P2_X = ANCHO_VIRTUAL - 45f;
    private static final int[][] TECLAS = {
        {Input.Keys.W, Input.Keys.S},
        {Input.Keys.UP, Input.Keys.DOWN}
    };
    private static final Color COLOR_FONDO = new Color(30 / 255f, 30 / 255f, 45 / 255f, 1f);
    private static final Color COLOR_LINEA = new Color(60 / 255f, 60 / 255f, 80 / 255f, 1f);
    private static final Color COLOR_P1 = new Color(255 / 255f, 75 / 255f, 120 / 255f, 1f);
    private static final Color COLOR_P2 = new Color(0f, 204 / 255f, 255 / 255f, 1f);
    private static final Color COLOR_BOMBA = new Color(255 / 255f, 140 / 255f, 0f, 1f);

    private int puntajeP1 = 0;
    private int puntajeP2 = 0;
    private float p1Y = (ALTO_VIRTUAL - PALETA_ALTO) / 2f;
    private float p2Y = (ALTO_VIRTUAL - PALETA_ALTO) / 2f;
    private float bombaX;
    private float bombaY;
    private float bombaVelX;
    private float bombaVelY;

    public PingPongScreen(Game juego) {
        super(juego);
        reiniciarPelota(1);
    }

    private void reiniciarPelota(int quienSaca) {
        bombaX = ANCHO_VIRTUAL / 2f;
        bombaY = ALTO_VIRTUAL / 2f;
        bombaVelX = (quienSaca == 1 ? 1 : -1) * VELOCIDAD_BOMBA_BASE_X;
        bombaVelY = (MathUtils.randomBoolean() ? 1 : -1) * VELOCIDAD_BOMBA_BASE_Y;
    }

    private float moverPaleta(int p, float y, float delta) {
        if (Gdx.input.isKeyPressed(TECLAS[p][0])) y += VELOCIDAD_PALETA * delta;
        if (Gdx.input.isKeyPressed(TECLAS[p][1])) y -= VELOCIDAD_PALETA * delta;
        return MathUtils.clamp(y, 0f, ALTO_VIRTUAL - PALETA_ALTO);
    }

    @Override protected String getConsigna() { return "¡DEVOLVÉ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.dos("mover", Controles.W, Controles.S, Controles.ARRIBA, Controles.ABAJO)
        };
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        p1Y = moverPaleta(0, p1Y, delta);
        p2Y = moverPaleta(1, p2Y, delta);

        bombaX += bombaVelX * delta;
        bombaY += bombaVelY * delta;

        if (bombaY - RADIO_BOMBA <= 0f) {
            bombaY = RADIO_BOMBA;
            bombaVelY = Math.abs(bombaVelY);
        } else if (bombaY + RADIO_BOMBA >= ALTO_VIRTUAL) {
            bombaY = ALTO_VIRTUAL - RADIO_BOMBA;
            bombaVelY = -Math.abs(bombaVelY);
        }

        if (bombaX - RADIO_BOMBA <= P1_X + PALETA_ANCHO && bombaX + RADIO_BOMBA >= P1_X) {
            if (bombaY + RADIO_BOMBA >= p1Y && bombaY - RADIO_BOMBA <= p1Y + PALETA_ALTO && bombaVelX < 0) {
                bombaVelX = -bombaVelX * ACELERACION_REBOTE;
                bombaX = P1_X + PALETA_ANCHO + RADIO_BOMBA;
            }
        }

        if (bombaX + RADIO_BOMBA >= P2_X && bombaX - RADIO_BOMBA <= P2_X + PALETA_ANCHO) {
            if (bombaY + RADIO_BOMBA >= p2Y && bombaY - RADIO_BOMBA <= p2Y + PALETA_ALTO && bombaVelX > 0) {
                bombaVelX = -bombaVelX * ACELERACION_REBOTE;
                bombaX = P2_X - RADIO_BOMBA;
            }
        }

        if (bombaX + RADIO_BOMBA < 0f) {
            puntajeP2++;
            if (puntajeP2 >= GOLES_PARA_GANAR) terminarRonda();
            else reiniciarPelota(2);
        } else if (bombaX - RADIO_BOMBA > ANCHO_VIRTUAL) {
            puntajeP1++;
            if (puntajeP1 >= GOLES_PARA_GANAR) terminarRonda();
            else reiniciarPelota(1);
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        formas.begin(ShapeType.Filled);
        formas.setColor(COLOR_FONDO);
        formas.rect(0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        formas.setColor(COLOR_LINEA);
        for (float y = 0; y < ALTO_VIRTUAL; y += 24f) {
            formas.rect(ANCHO_VIRTUAL / 2f - 2f, y, 4f, 14f);
        }

        formas.setColor(COLOR_P1);
        formas.rect(P1_X, p1Y, PALETA_ANCHO, PALETA_ALTO);
        formas.setColor(COLOR_P2);
        formas.rect(P2_X, p2Y, PALETA_ANCHO, PALETA_ALTO);
        formas.setColor(COLOR_BOMBA);
        formas.circle(bombaX, bombaY, RADIO_BOMBA);
        formas.end();
    }

    @Override
    protected String determinarGanador() {
        if (puntajeP1 == puntajeP2) return "¡EMPATE EN LA RONDA!";
        int ganador = (puntajeP1 > puntajeP2) ? 1 : 2;
        int golesGanador = Math.max(puntajeP1, puntajeP2), golesPerdedor = Math.min(puntajeP1, puntajeP2);
        int pts = 1;
        if (golesGanador >= GOLES_PARA_GANAR) pts = (golesPerdedor == 0) ? 3 : 2;
        return sumarPuntos(ganador, pts);
    }

    @Override
    protected void reiniciarRonda() {
        puntajeP1 = 0;
        puntajeP2 = 0;
        p1Y = (ALTO_VIRTUAL - PALETA_ALTO) / 2f;
        p2Y = (ALTO_VIRTUAL - PALETA_ALTO) / 2f;
        reiniciarPelota(1);
    }

    @Override protected String getTextoP1() { return "P1: " + puntajeP1 + " (Victorias: " + victoriasP1 + ")"; }
    @Override protected String getTextoP2() { return "P2: " + puntajeP2 + " (Victorias: " + victoriasP2 + ")"; }
    @Override protected String getResumen() { return "Goles de Bomba: P1 [" + puntajeP1 + "]  -  P2 [" + puntajeP2 + "]"; }
}
