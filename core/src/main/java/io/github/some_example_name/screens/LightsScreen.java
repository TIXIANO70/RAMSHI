package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_example_name.entities.Direction;

public class LightsScreen extends BaseMinigameScreen {

    private enum Phase { COUNTDOWN, SHOWING, INPUT }

    // --- Configuración ---
    private static final int SEQUENCE_LENGTH = 4;
    private static final float COUNTDOWN_TIME = 1f;
    private static final float LIGHT_ON_TIME = 0.5f;
    private static final float LIGHT_GAP_TIME = 0.2f;
    private static final float FLASH_TIME = 0.15f;

    // --- Geometría (coordenadas virtuales 800x480) ---
    private static final float HALF_WIDTH = VIRTUAL_WIDTH / 2f;
    private static final float CENTER_Y = 225f;
    private static final float PAD_SIZE = 90f;
    private static final float PAD_GAP = 10f;
    private static final float BOX_SIZE = 24f;
    private static final float BOX_GAP = 8f;

    // Orden de los pads: 0=arriba, 1=izquierda, 2=abajo, 3=derecha
    private static final Direction[] PADS = {
        Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT
    };
    private static final int[] P1_KEYS = {
        Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D
    };
    private static final int[] P2_KEYS = {
        Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT
    };

    private static final Color[] PAD_ON = {
        new Color(0.20f, 0.90f, 0.30f, 1f),
        new Color(0.95f, 0.25f, 0.25f, 1f),
        new Color(0.98f, 0.85f, 0.20f, 1f),
        new Color(0.25f, 0.50f, 1.00f, 1f)
    };
    private static final Color[] PAD_OFF = new Color[4];
    static {
        for (int i = 0; i < 4; i++) {
            Color c = PAD_ON[i];
            PAD_OFF[i] = new Color(c.r * 0.25f, c.g * 0.25f, c.b * 0.25f, 1f);
        }
    }

    // --- Estado de la ronda ---
    private final Direction[] sequence = new Direction[SEQUENCE_LENGTH];
    private final int[] progress = new int[2];      // aciertos de cada jugador
    private final int[] flashPad = new int[2];      // último pad presionado
    private final float[] flashTimer = new float[2];
    private Phase phase;
    private float stateTime;
    private int winner;                             // 0 = nadie/empate, 1 = P1, 2 = P2

    public LightsScreen(Game game) {
        super(game);
        resetState();
    }

    // ===================== Hooks de BaseMinigameScreen =====================

    @Override
    protected float getRoundDuration() {
        return 30f;
    }

    @Override
    protected void updateGame(float delta) {
        stateTime += delta;
        for (int p = 0; p < 2; p++) {
            if (flashTimer[p] > 0f) flashTimer[p] -= delta;
        }

        switch (phase) {
            case COUNTDOWN:
                if (stateTime >= COUNTDOWN_TIME) {
                    phase = Phase.SHOWING;
                    stateTime = 0f;
                }
                break;
            case SHOWING:
                if (stateTime >= SEQUENCE_LENGTH * (LIGHT_ON_TIME + LIGHT_GAP_TIME)) {
                    phase = Phase.INPUT;
                    stateTime = 0f;
                }
                break;
            case INPUT:
                boolean p1Done = handleInput(0, P1_KEYS);
                boolean p2Done = handleInput(1, P2_KEYS);
                if (p1Done && p2Done) {
                    winner = 0;          // terminaron en el mismo frame: empate
                    finishRound();
                } else if (p1Done) {
                    winner = 1;
                    finishRound();
                } else if (p2Done) {
                    winner = 2;
                    finishRound();
                }
                break;
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        Direction lit = getLitDirection();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                boolean on = (lit == PADS[i]) || (flashTimer[p] > 0f && flashPad[p] == i);
                shapeRenderer.setColor(on ? PAD_ON[i] : PAD_OFF[i]);
                shapeRenderer.rect(padX(p, i), padY(i), PAD_SIZE, PAD_SIZE);
            }
            drawProgressBoxes(p);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(HALF_WIDTH, 16f, HALF_WIDTH, 432f);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);
        if (!roundOver) {
            drawCentered(batch, getPhaseMessage(), 420f);
        }
        font.getData().setScale(1.0f);
        font.setColor(0.6f, 0.6f, 0.65f, 1f);
        font.draw(batch, "P1: W A S D", 24f, 24f);
        font.draw(batch, "P2: FLECHAS", 590f, 24f);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (winner == 1) {
            p1GlobalWins++;
            return "¡JUGADOR 1 GANA LA RONDA!";
        } else if (winner == 2) {
            p2GlobalWins++;
            return "¡JUGADOR 2 GANA LA RONDA!";
        }
        return "¡EMPATE EN LA RONDA!";
    }

    @Override
    protected void onRoundReset() {
        resetState();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + progress[0] + "/" + SEQUENCE_LENGTH + " (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + progress[1] + "/" + SEQUENCE_LENGTH + " (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Progreso: P1 (" + progress[0] + "/" + SEQUENCE_LENGTH
            + ") vs P2 (" + progress[1] + "/" + SEQUENCE_LENGTH + ")";
    }

    @Override
    public void dispose() {
        super.dispose();
        // Cuando agreguemos Texture, se liberan acá.
    }

    // ===================== Lógica propia =====================

    private void resetState() {
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            sequence[i] = PADS[MathUtils.random(0, 3)];
        }
        progress[0] = 0;
        progress[1] = 0;
        flashTimer[0] = 0f;
        flashTimer[1] = 0f;
        winner = 0;
        phase = Phase.COUNTDOWN;
        stateTime = 0f;
    }

    /** Procesa las teclas de un jugador. Devuelve true si completó la secuencia. */
    private boolean handleInput(int player, int[] keys) {
        for (int i = 0; i < 4; i++) {
            if (Gdx.input.isKeyJustPressed(keys[i])) {
                flashPad[player] = i;
                flashTimer[player] = FLASH_TIME;

                if (PADS[i] == sequence[progress[player]]) {
                    progress[player]++;
                    if (progress[player] == SEQUENCE_LENGTH) {
                        return true;
                    }
                } else {
                    progress[player] = 0; // error: vuelve a empezar
                }
            }
        }
        return false;
    }

    /** Qué dirección está encendida ahora mismo en la fase SHOWING (o null). */
    private Direction getLitDirection() {
        if (phase != Phase.SHOWING) return null;
        float slotLength = LIGHT_ON_TIME + LIGHT_GAP_TIME;
        int slot = (int) (stateTime / slotLength);
        float inSlot = stateTime - slot * slotLength;
        if (slot < SEQUENCE_LENGTH && inSlot < LIGHT_ON_TIME) {
            return sequence[slot];
        }
        return null;
    }

    private String getPhaseMessage() {
        switch (phase) {
            case COUNTDOWN: return "PREPARATE...";
            case SHOWING:   return "MIRA LA SECUENCIA";
            default:        return "¡REPETILA!";
        }
    }

    private void drawProgressBoxes(int player) {
        float total = SEQUENCE_LENGTH * BOX_SIZE + (SEQUENCE_LENGTH - 1) * BOX_GAP;
        float startX = player * HALF_WIDTH + (HALF_WIDTH - total) / 2f;
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            if (i < progress[player]) {
                shapeRenderer.setColor(0.3f, 0.9f, 0.3f, 1f);
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            }
            shapeRenderer.rect(startX + i * (BOX_SIZE + BOX_GAP), 50f, BOX_SIZE, BOX_SIZE);
        }
    }

    private float padX(int player, int pad) {
        float cx = player * HALF_WIDTH + HALF_WIDTH / 2f;
        switch (pad) {
            case 1:  return cx - PAD_SIZE * 1.5f - PAD_GAP;
            case 3:  return cx + PAD_SIZE * 0.5f + PAD_GAP;
            default: return cx - PAD_SIZE / 2f;
        }
    }

    private float padY(int pad) {
        switch (pad) {
            case 0:  return CENTER_Y + PAD_SIZE * 0.5f + PAD_GAP;
            case 2:  return CENTER_Y - PAD_SIZE * 1.5f - PAD_GAP;
            default: return CENTER_Y - PAD_SIZE / 2f;
        }
    }

    private void drawCentered(SpriteBatch batch, String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2f, y);
    }
}