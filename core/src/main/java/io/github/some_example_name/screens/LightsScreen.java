package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class LightsScreen extends BaseMinigameScreen {

    // =====================================================================
    //  VALORES AJUSTABLES (todo lo que se pueda pedir cambiar está acá)
    // =====================================================================
    private static final float ROUND_DURATION = 30f;   // Duración de la ronda (segundos)
    private static final int   SEQUENCE_LENGTH = 4;    // Cantidad de luces de la secuencia
    private static final float COUNTDOWN_TIME = 1f;    // Cuenta previa antes de mostrar (s)
    private static final float LIGHT_SPEED = 1f;       // Velocidad de las luces: 2 = el doble de rápido
    private static final float LIGHT_ON_TIME = 0.5f;   // Cuánto dura encendida cada luz (s)
    private static final float LIGHT_GAP_TIME = 0.2f;  // Pausa entre luces (s)
    private static final float FLASH_TIME = 0.15f;     // Destello al presionar una tecla (s)
    private static final boolean RESET_ON_ERROR = true; // true: al fallar se reinicia el progreso

    // Tamaño y posición de las teclas en pantalla (px, mundo de 800x480)
    private static final float PAD_SIZE = 80f;
    private static final float PAD_GAP = 8f;
    private static final float ROW_BOTTOM_Y = 150f;

    // Controles: orden 0=arriba, 1=izquierda, 2=abajo, 3=derecha
    private static final int[][] KEYS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},          // Jugador 1
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT} // Jugador 2
    };

    // Colores de encendido: verde, rojo, amarillo, azul
    private static final Color[] PAD_COLORS = {
        new Color(0.20f, 0.90f, 0.30f, 1f),
        new Color(0.95f, 0.25f, 0.25f, 1f),
        new Color(0.98f, 0.85f, 0.20f, 1f),
        new Color(0.25f, 0.50f, 1.00f, 1f)
    };
    // =====================================================================

    private enum Phase { COUNTDOWN, SHOWING, INPUT }

    private static final float HALF_WIDTH = VIRTUAL_WIDTH / 2f;
    private static final float ROW_TOP_Y = ROW_BOTTOM_Y + PAD_SIZE + PAD_GAP;
    private static final String[][] KEY_NAMES = {
        {"w", "a", "s", "d"},
        {"up", "left", "down", "right"}
    };

    // Texturas [jugador][tecla]
    private final Texture[][] keyOff = new Texture[2][4];
    private final Texture[][] keyOn = new Texture[2][4];

    // Estado de la ronda
    private final int[] sequence = new int[SEQUENCE_LENGTH];  // índices de tecla (0-3)
    private final int[] progress = new int[2];
    private final int[] flashPad = new int[2];
    private final float[] flashTimer = new float[2];
    private Phase phase;
    private float stateTime;
    private int winner;                                       // 0 = empate, 1 = P1, 2 = P2

    public LightsScreen(Game game) {
        super(game);
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                keyOff[p][i] = loadTexture("lights/key_" + KEY_NAMES[p][i] + "_off.png");
                keyOn[p][i] = loadTexture("lights/key_" + KEY_NAMES[p][i] + "_on.png");
            }
        }
        resetState();
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(path);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    // ===================== Hooks de BaseMinigameScreen =====================

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        stateTime += delta;
        for (int p = 0; p < 2; p++) {
            flashTimer[p] -= delta;
        }

        if (phase == Phase.COUNTDOWN && stateTime >= COUNTDOWN_TIME) {
            changePhase(Phase.SHOWING);
        } else if (phase == Phase.SHOWING && stateTime >= SEQUENCE_LENGTH * slotLength()) {
            changePhase(Phase.INPUT);
        } else if (phase == Phase.INPUT) {
            boolean p1Done = handleInput(0);
            boolean p2Done = handleInput(1);
            if (p1Done || p2Done) {
                winner = (p1Done && p2Done) ? 0 : (p1Done ? 1 : 2);
                finishRound();
            }
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        int lit = getLitPad();

        // Fondos tenues, cajitas de progreso y línea divisoria
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                Color c = PAD_COLORS[i];
                shapeRenderer.setColor(c.r * 0.25f, c.g * 0.25f, c.b * 0.25f, 1f);
                shapeRenderer.rect(padX(p, i), padY(i), PAD_SIZE, PAD_SIZE);
            }
            drawProgressBoxes(p);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(HALF_WIDTH, 16f, HALF_WIDTH, 432f);
        shapeRenderer.end();

        // Teclas (imagen de contorno apagada / imagen blanca teñida encendida)
        batch.begin();
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                boolean on = (lit == i) || (flashTimer[p] > 0f && flashPad[p] == i);
                batch.setColor(on ? PAD_COLORS[i] : Color.WHITE);
                batch.draw(on ? keyOn[p][i] : keyOff[p][i], padX(p, i), padY(i), PAD_SIZE, PAD_SIZE);
            }
        }
        batch.setColor(Color.WHITE); // restaurar siempre

        // Textos
        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);
        if (!roundOver) {
            GlyphLayout layout = new GlyphLayout(font, getPhaseMessage());
            font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2f, 420f);
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
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                if (keyOff[p][i] != null) keyOff[p][i].dispose();
                if (keyOn[p][i] != null) keyOn[p][i].dispose();
            }
        }
    }

    // ===================== Lógica propia =====================

    private void resetState() {
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            sequence[i] = MathUtils.random(0, 3);
        }
        progress[0] = progress[1] = 0;
        flashTimer[0] = flashTimer[1] = 0f;
        winner = 0;
        changePhase(Phase.COUNTDOWN);
    }

    private void changePhase(Phase newPhase) {
        phase = newPhase;
        stateTime = 0f;
    }

    /** Duración de cada luz + su pausa, ajustada por LIGHT_SPEED. */
    private float slotLength() {
        return (LIGHT_ON_TIME + LIGHT_GAP_TIME) / LIGHT_SPEED;
    }

    /** Procesa las teclas de un jugador. Devuelve true si completó la secuencia. */
    private boolean handleInput(int player) {
        for (int i = 0; i < 4; i++) {
            if (!Gdx.input.isKeyJustPressed(KEYS[player][i])) continue;

            flashPad[player] = i;
            flashTimer[player] = FLASH_TIME;

            if (i == sequence[progress[player]]) {
                progress[player]++;
                if (progress[player] == SEQUENCE_LENGTH) return true;
            } else if (RESET_ON_ERROR) {
                progress[player] = 0;
            }
        }
        return false;
    }

    /** Tecla encendida ahora mismo durante SHOWING (-1 si ninguna). */
    private int getLitPad() {
        if (phase != Phase.SHOWING) return -1;
        int slot = (int) (stateTime / slotLength());
        float inSlot = stateTime - slot * slotLength();
        if (slot < SEQUENCE_LENGTH && inSlot < LIGHT_ON_TIME / LIGHT_SPEED) {
            return sequence[slot];
        }
        return -1;
    }

    private String getPhaseMessage() {
        switch (phase) {
            case COUNTDOWN: return "PREPARATE...";
            case SHOWING:   return "MIRA LA SECUENCIA";
            default:        return "¡REPETILA!";
        }
    }

    private void drawProgressBoxes(int player) {
        float box = 24f, gap = 8f;
        float total = SEQUENCE_LENGTH * box + (SEQUENCE_LENGTH - 1) * gap;
        float startX = player * HALF_WIDTH + (HALF_WIDTH - total) / 2f;
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            if (i < progress[player]) shapeRenderer.setColor(0.3f, 0.9f, 0.3f, 1f);
            else shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(startX + i * (box + gap), 50f, box, box);
        }
    }

    // Distribución tipo teclado: arriba centrada; izquierda, abajo y derecha en una fila
    private float padX(int player, int pad) {
        float cx = player * HALF_WIDTH + HALF_WIDTH / 2f;
        if (pad == 1) return cx - PAD_SIZE * 1.5f - PAD_GAP;
        if (pad == 3) return cx + PAD_SIZE * 0.5f + PAD_GAP;
        return cx - PAD_SIZE / 2f;
    }

    private float padY(int pad) {
        return (pad == 0) ? ROW_TOP_Y : ROW_BOTTOM_Y;
    }
}