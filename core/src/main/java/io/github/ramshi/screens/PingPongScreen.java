package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

public class PingPongScreen extends BaseMinigameScreen {

    // =======================================================
    // CONSTANTES Y CONFIGURACIÓN
    // =======================================================
    private static final float ROUND_DURATION = 10f; // 10 segundos estándar RAMSHI
    private static final float PALETA_ANCHO = 15f;
    private static final float PALETA_ALTO = 80f;
    private static final float RADIO_BOMBA = 15f;
    private static final float VELOCIDAD_PALETA = 410f; // px/segundo
    private static final float VELOCIDAD_BOMBA_BASE_X = 480f;
    private static final float VELOCIDAD_BOMBA_BASE_Y = 420f;

    // Posiciones horizontales de las paletas
    private static final float P1_X = 30f;
    private static final float P2_X = VIRTUAL_WIDTH - 45f;

    // Colores originales del compañero
    private static final Color COLOR_BG = new Color(30 / 255f, 30 / 255f, 45 / 255f, 1f);
    private static final Color COLOR_LINEA = new Color(60 / 255f, 60 / 255f, 80 / 255f, 1f);
    private static final Color COLOR_P1 = new Color(255 / 255f, 75 / 255f, 120 / 255f, 1f);
    private static final Color COLOR_P2 = new Color(0f, 204 / 255f, 255 / 255f, 1f);
    private static final Color COLOR_BOMBA = new Color(255 / 255f, 140 / 255f, 0f, 1f);

    // Estado del juego
    private int p1Score = 0;
    private int p2Score = 0;
    private float p1Y = (VIRTUAL_HEIGHT - PALETA_ALTO) / 2f;
    private float p2Y = (VIRTUAL_HEIGHT - PALETA_ALTO) / 2f;

    private float bombaX;
    private float bombaY;
    private float bombaVelX;
    private float bombaVelY;

    public PingPongScreen(Game game) {
        super(game);
        reiniciarPelota(1);
    }

    private void reiniciarPelota(int quienSaca) {
        bombaX = VIRTUAL_WIDTH / 2f;
        bombaY = VIRTUAL_HEIGHT / 2f;
        bombaVelX = (quienSaca == 1 ? 1 : -1) * VELOCIDAD_BOMBA_BASE_X;
        bombaVelY = (MathUtils.randomBoolean() ? 1 : -1) * VELOCIDAD_BOMBA_BASE_Y;
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        // Controles Jugador 1: W y S
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            p1Y += VELOCIDAD_PALETA * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            p1Y -= VELOCIDAD_PALETA * delta;
        }
        p1Y = MathUtils.clamp(p1Y, 0f, VIRTUAL_HEIGHT - PALETA_ALTO);

        // Controles Jugador 2: Flecha Arriba y Abajo
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            p2Y += VELOCIDAD_PALETA * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            p2Y -= VELOCIDAD_PALETA * delta;
        }
        p2Y = MathUtils.clamp(p2Y, 0f, VIRTUAL_HEIGHT - PALETA_ALTO);

        // Movimiento de la bomba
        bombaX += bombaVelX * delta;
        bombaY += bombaVelY * delta;

        // Rebote en bordes inferior y superior
        if (bombaY - RADIO_BOMBA <= 0f) {
            bombaY = RADIO_BOMBA;
            bombaVelY = Math.abs(bombaVelY);
        } else if (bombaY + RADIO_BOMBA >= VIRTUAL_HEIGHT) {
            bombaY = VIRTUAL_HEIGHT - RADIO_BOMBA;
            bombaVelY = -Math.abs(bombaVelY);
        }

        // Colisión con Paleta P1 (Izquierda)
        if (bombaX - RADIO_BOMBA <= P1_X + PALETA_ANCHO && bombaX + RADIO_BOMBA >= P1_X) {
            if (bombaY + RADIO_BOMBA >= p1Y && bombaY - RADIO_BOMBA <= p1Y + PALETA_ALTO && bombaVelX < 0) {
                bombaVelX = -bombaVelX * 1.05f; // Aceleración del 5% del compañero
                bombaX = P1_X + PALETA_ANCHO + RADIO_BOMBA;
            }
        }

        // Colisión con Paleta P2 (Derecha)
        if (bombaX + RADIO_BOMBA >= P2_X && bombaX - RADIO_BOMBA <= P2_X + PALETA_ANCHO) {
            if (bombaY + RADIO_BOMBA >= p2Y && bombaY - RADIO_BOMBA <= p2Y + PALETA_ALTO && bombaVelX > 0) {
                bombaVelX = -bombaVelX * 1.05f; // Aceleración del 5% del compañero
                bombaX = P2_X - RADIO_BOMBA;
            }
        }

        // Punto para P2 si la bomba pasa por la izquierda
        if (bombaX + RADIO_BOMBA < 0f) {
            p2Score++;
            if (p2Score >= 2) {
                finishRound();
            } else {
                reiniciarPelota(2);
            }
        }
        // Punto para P1 si la bomba pasa por la derecha
        else if (bombaX - RADIO_BOMBA > VIRTUAL_WIDTH) {
            p1Score++;
            if (p1Score >= 2) {
                finishRound();
            } else {
                reiniciarPelota(1);
            }
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        shapeRenderer.begin(ShapeType.Filled);

        // Fondo
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Línea central divisoria punteada
        shapeRenderer.setColor(COLOR_LINEA);
        for (float y = 0; y < VIRTUAL_HEIGHT; y += 24f) {
            shapeRenderer.rect(VIRTUAL_WIDTH / 2f - 2f, y, 4f, 14f);
        }

        // Paleta P1 (Roja)
        shapeRenderer.setColor(COLOR_P1);
        shapeRenderer.rect(P1_X, p1Y, PALETA_ANCHO, PALETA_ALTO);

        // Paleta P2 (Celeste)
        shapeRenderer.setColor(COLOR_P2);
        shapeRenderer.rect(P2_X, p2Y, PALETA_ANCHO, PALETA_ALTO);

        // Bomba (Naranja)
        shapeRenderer.setColor(COLOR_BOMBA);
        shapeRenderer.circle(bombaX, bombaY, RADIO_BOMBA);

        shapeRenderer.end();

        // HUD de puntuación y controles
        batch.begin();
        font.setColor(COLOR_P1);
        font.getData().setScale(1.2f);
        font.draw(batch, "P1: " + p1Score + "  [W/S]", 50f, 60f);

        font.setColor(COLOR_P2);
        font.draw(batch, "P2: " + p2Score + "  [FLECHAS]", VIRTUAL_WIDTH - 210f, 60f);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (p1Score > p2Score) {
            // Otorga 1 o 2 puntos según el desempeño (2 si sacó 2 goles o ventaja clara)
            int pts = (p1Score >= 2 || (p1Score - p2Score) >= 2) ? 2 : 1;
            p1GlobalWins += pts;
            return "¡JUGADOR 1 GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
        } else if (p2Score > p1Score) {
            int pts = (p2Score >= 2 || (p2Score - p1Score) >= 2) ? 2 : 1;
            p2GlobalWins += pts;
            return "¡JUGADOR 2 GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
        } else {
            return "¡EMPATE EN LA RONDA!";
        }
    }

    @Override
    protected void onRoundReset() {
        p1Score = 0;
        p2Score = 0;
        p1Y = (VIRTUAL_HEIGHT - PALETA_ALTO) / 2f;
        p2Y = (VIRTUAL_HEIGHT - PALETA_ALTO) / 2f;
        reiniciarPelota(1);
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + p1Score + " (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + p2Score + " (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Goles de Bomba: P1 [" + p1Score + "]  -  P2 [" + p2Score + "]";
    }
}
