package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

public class SumoScreen extends BaseMinigameScreen {

    // =======================================================
    // CONSTANTES Y CONFIGURACIÓN (Mundo 800x480)
    // =======================================================
    private static final float ROUND_DURATION = 10f; // 10s límite por microjuego
    private static final float RADIO = 22f;          // Radio de cada luchador
    private static final float IMPULSO_TAP = 16f;    // Píxeles al pulsar tecla
    private static final float VELOCIDAD_HOLD = 240f;// Píxeles/s al mantener pulsado

    // Plataforma central
    private static final float PLAT_ANCHO = 440f;
    private static final float PLAT_ALTO = 22f;
    private static final float PLAT_X = (VIRTUAL_WIDTH - PLAT_ANCHO) / 2f; // 180f
    private static final float PLAT_Y = 190f;
    private static final float PLAT_CENTER_X = PLAT_X + PLAT_ANCHO / 2f;

    // Colores fieles al compañero
    private static final Color COLOR_BG = new Color(30 / 255f, 30 / 255f, 45 / 255f, 1f);
    private static final Color COLOR_PLATAFORMA = new Color(0f, 204 / 255f, 255 / 255f, 1f);
    private static final Color COLOR_P1 = new Color(255 / 255f, 75 / 255f, 120 / 255f, 1f);
    private static final Color COLOR_P2 = new Color(0f, 255 / 255f, 180 / 255f, 1f);

    // Posición y estado de los luchadores
    private float p1X;
    private float p1Y;
    private float p2X;
    private float p2Y;
    private boolean p1Cayo;
    private boolean p2Cayo;

    private int p1RoundsWon = 0;
    private int p2RoundsWon = 0;
    private String outcomeReason = "";

    public SumoScreen(Game game) {
        super(game);
        resetFighters();
    }

    private void resetFighters() {
        p1X = PLAT_X + 110f;
        p1Y = PLAT_Y + PLAT_ALTO + RADIO;
        p2X = PLAT_X + PLAT_ANCHO - 110f;
        p2Y = PLAT_Y + PLAT_ALTO + RADIO;
        p1Cayo = false;
        p2Cayo = false;
        outcomeReason = "";
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        if (p1Cayo || p2Cayo) {
            // Animación de caída fuera de la plataforma
            if (p1Cayo) p1Y -= 450f * delta;
            if (p2Cayo) p2Y -= 450f * delta;
            return;
        }

        // Movimiento Jugador 1 (A: izquierda, D: derecha)
        float moveP1 = 0f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) moveP1 -= IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(Input.Keys.A)) moveP1 -= VELOCIDAD_HOLD * delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) moveP1 += IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) moveP1 += VELOCIDAD_HOLD * delta;

        // Movimiento Jugador 2 (Flecha Izquierda / Derecha)
        float moveP2 = 0f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) moveP2 -= IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveP2 -= VELOCIDAD_HOLD * delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) moveP2 += IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveP2 += VELOCIDAD_HOLD * delta;

        p1X += moveP1;
        p2X += moveP2;

        // Lógica de colisión y empuje mutuo del compañero
        float distancia = Math.abs(p2X - p1X);
        if (distancia < RADIO * 2f) {
            float solapamiento = (RADIO * 2f) - distancia;

            // Separación física
            p1X -= solapamiento / 2f;
            p2X += solapamiento / 2f;

            // Transferencia de empuje
            if (moveP1 > 0f) {
                p2X += 12f;
                p1X -= 5f;
            }
            if (moveP2 < 0f) {
                p1X -= 12f;
                p2X += 5f;
            }
        }

        // Control de caída de la plataforma (Ring-Out)
        boolean p1Fuera = (p1X < PLAT_X - RADIO || p1X > PLAT_X + PLAT_ANCHO + RADIO);
        boolean p2Fuera = (p2X < PLAT_X - RADIO || p2X > PLAT_X + PLAT_ANCHO + RADIO);

        if (p1Fuera && p2Fuera) {
            p1Cayo = true;
            p2Cayo = true;
            outcomeReason = "¡AMBOS CAYERON AL MISMO TIEMPO!";
            finishRound();
        } else if (p1Fuera) {
            p1Cayo = true;
            p2RoundsWon++;
            outcomeReason = "¡RING OUT! JUGADOR 2 EMPUJA AL RIVAL";
            finishRound();
        } else if (p2Fuera) {
            p2Cayo = true;
            p1RoundsWon++;
            outcomeReason = "¡RING OUT! JUGADOR 1 EMPUJA AL RIVAL";
            finishRound();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        shapeRenderer.begin(ShapeType.Filled);

        // Fondo
        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Plataforma central
        shapeRenderer.setColor(COLOR_PLATAFORMA);
        shapeRenderer.rect(PLAT_X, PLAT_Y, PLAT_ANCHO, PLAT_ALTO);

        // Bordes de advertencia en los extremos de la plataforma
        shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.8f);
        shapeRenderer.rect(PLAT_X, PLAT_Y, 20f, PLAT_ALTO);
        shapeRenderer.rect(PLAT_X + PLAT_ANCHO - 20f, PLAT_Y, 20f, PLAT_ALTO);

        // Jugador 1 (Rojo)
        shapeRenderer.setColor(COLOR_P1);
        shapeRenderer.circle(p1X, p1Y, RADIO);

        // Jugador 2 (Verde Menta)
        shapeRenderer.setColor(COLOR_P2);
        shapeRenderer.circle(p2X, p2Y, RADIO);

        shapeRenderer.end();

        // HUD con controles
        batch.begin();
        font.setColor(COLOR_P1);
        font.getData().setScale(1.2f);
        font.draw(batch, "P1: [A / D] Empuja", 60f, 70f);

        font.setColor(COLOR_P2);
        font.draw(batch, "P2: [FLECHAS] Empuja", VIRTUAL_WIDTH - 240f, 70f);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (p1Cayo && p2Cayo) {
            return "¡EMPATE POR DOBLE CAÍDA!";
        }

        if (p2Cayo) {
            // Ring-out directo otorga 2 puntos
            int pts = 2;
            p1GlobalWins += pts;
            return "¡JUGADOR 1 GANA (" + pts + " PUNTOS POR KO)!";
        } else if (p1Cayo) {
            int pts = 2;
            p2GlobalWins += pts;
            return "¡JUGADOR 2 GANA (" + pts + " PUNTOS POR KO)!";
        }

        // Si se acaba el tiempo sin caída: gana quien tenga mayor control del centro (1 punto)
        float d1 = Math.abs(p1X - PLAT_CENTER_X);
        float d2 = Math.abs(p2X - PLAT_CENTER_X);

        if (d1 < d2 - 5f) {
            int pts = 1;
            p1GlobalWins += pts;
            return "¡JUGADOR 1 DOMINA EL CENTRO (1 PUNTO)!";
        } else if (d2 < d1 - 5f) {
            int pts = 1;
            p2GlobalWins += pts;
            return "¡JUGADOR 2 DOMINA EL CENTRO (1 PUNTO)!";
        } else {
            return "¡EMPATE POR TIEMPO!";
        }
    }

    @Override
    protected void onRoundReset() {
        resetFighters();
    }

    @Override
    protected String getP1StatusText() {
        return "P1 (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2 (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return outcomeReason.isEmpty() ? "Lucha cerrada sobre la plataforma" : outcomeReason;
    }
}
