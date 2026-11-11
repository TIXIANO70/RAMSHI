package io.github.ramshi.screens;

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
    private static final float TOTAL_DURATION = 10f;   // Duración TOTAL del minijuego (todas las rondas), en segundos
    private static final int[] SEQUENCE_LENGTHS = {4, 4, 5}; // Luces por ronda (la cantidad de elementos = cantidad de rondas)
    private static final float SHOW_TIME = 1f;         // Tiempo en que se muestra CADA secuencia (igual en todas las rondas)
    private static final float LIGHT_ON_RATIO = 0.7f;  // Fracción de cada luz que está encendida (el resto es pausa)
    private static final float COUNTDOWN_TIME = 0.4f;  // Pausa antes de mostrar cada secuencia (s)
    private static final float RESULT_TIME = 0.4f;     // Pausa al terminar cada ronda (s)
    private static final float FLASH_TIME = 0.15f;     // Destello al presionar una tecla (s)
    private static final boolean RESET_ON_ERROR = true; // true: al fallar se reinicia el progreso

    // Tamaño y posición de las teclas en pantalla (px, mundo de 800x480)
    private static final float PAD_SIZE = 80f;
    private static final float PAD_GAP = 8f;
    private static final float ROW_BOTTOM_Y = 150f;

    // Controles: orden 0=arriba, 1=izquierda, 2=abajo, 3=derecha
    private static final int[][] KEYS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},           // Jugador 1
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

    private enum Phase { COUNTDOWN, SHOWING, INPUT, RESULT }

    private static final int TOTAL_ROUNDS = SEQUENCE_LENGTHS.length;
    private static final float HALF_WIDTH = VIRTUAL_WIDTH / 2f;
    private static final float ROW_TOP_Y = ROW_BOTTOM_Y + PAD_SIZE + PAD_GAP;
    private static final String[][] KEY_NAMES = {
        {"w", "a", "s", "d"},
        {"up", "left", "down", "right"}
    };

    // Texturas [jugador][tecla]
    private final Texture[][] keyOff = new Texture[2][4];
    private final Texture[][] keyOn = new Texture[2][4];

    // Estado
    private final int[] sequence = new int[maxLength()];   // índices de tecla (0-3)
    private final int[] progress = new int[2];
    private final int[] flashPad = new int[2];
    private final float[] flashTimer = new float[2];
    private final int[] roundWins = new int[2];             // sub-rondas ganadas por cada jugador
    private int currentRound;                               // 0, 1, 2...
    private int subWinner;                                  // ganador de la sub-ronda: 0 = nadie, 1 = P1, 2 = P2
    private Phase phase;
    private float stateTime;

    public LightsScreen(Game game) {
        super(game);
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                keyOff[p][i] = loadTexture("lights/key_" + KEY_NAMES[p][i] + "_off.png");
                keyOn[p][i] = loadTexture("lights/key_" + KEY_NAMES[p][i] + "_on.png");
            }
        }
        resetAll();
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(path);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    private static int maxLength() {
        int max = 0;
        for (int n : SEQUENCE_LENGTHS) max = Math.max(max, n);
        return max;
    }

    // ===================== Hooks de BaseMinigameScreen =====================

    @Override
    protected float getRoundDuration() {
        return TOTAL_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        stateTime += delta;
        for (int p = 0; p < 2; p++) {
            flashTimer[p] -= delta;
        }

        if (phase == Phase.COUNTDOWN && stateTime >= COUNTDOWN_TIME) {
            changePhase(Phase.SHOWING);
        } else if (phase == Phase.SHOWING && stateTime >= SHOW_TIME) {
            changePhase(Phase.INPUT);
        } else if (phase == Phase.INPUT) {
            boolean p1Done = handleInput(0);
            boolean p2Done = handleInput(1);
            if (p1Done || p2Done) {
                subWinner = (p1Done && p2Done) ? 0 : (p1Done ? 1 : 2);
                if (subWinner != 0) roundWins[subWinner - 1]++;
                changePhase(Phase.RESULT);
            }
        } else if (phase == Phase.RESULT && stateTime >= RESULT_TIME) {
            currentRound++;
            if (currentRound >= TOTAL_ROUNDS) {
                finishRound();          // terminaron todas las rondas
            } else {
                startSubRound();
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

    /** Se llama una sola vez al terminar todo: el ganador suma tantos puntos como rondas ganó. */
    @Override
    protected String determineRoundWinner() {
        int p1 = roundWins[0];
        int p2 = roundWins[1];
        if (p1 > p2) {
            p1GlobalWins += p1;
            return "¡JUGADOR 1 GANA! (" + p1 + "-" + p2 + ")  +" + p1 + " pts";
        } else if (p2 > p1) {
            p2GlobalWins += p2;
            return "¡JUGADOR 2 GANA! (" + p2 + "-" + p1 + ")  +" + p2 + " pts";
        }
        return "¡EMPATE! (" + p1 + "-" + p2 + ")";
    }

    @Override
    protected void onRoundReset() {
        resetAll();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + roundWins[0] + " rondas (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + roundWins[1] + " rondas (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Rondas ganadas: P1 (" + roundWins[0] + ") vs P2 (" + roundWins[1] + ")";
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

    /** Reinicia todo el minijuego (se usa al empezar y al reiniciar con ESPACIO). */
    private void resetAll() {
        roundWins[0] = roundWins[1] = 0;
        currentRound = 0;
        startSubRound();
    }

    /** Prepara una sub-ronda: nueva secuencia, progreso en cero y cuenta previa. */
    private void startSubRound() {
        for (int i = 0; i < sequenceLength(); i++) {
            sequence[i] = MathUtils.random(0, 3);
        }
        progress[0] = progress[1] = 0;
        flashTimer[0] = flashTimer[1] = 0f;
        subWinner = 0;
        changePhase(Phase.COUNTDOWN);
    }

    private int sequenceLength() {
        return SEQUENCE_LENGTHS[Math.min(currentRound, TOTAL_ROUNDS - 1)];
    }

    private void changePhase(Phase newPhase) {
        phase = newPhase;
        stateTime = 0f;
    }

    /** Procesa las teclas de un jugador. Devuelve true si completó la secuencia. */
    private boolean handleInput(int player) {
        for (int i = 0; i < 4; i++) {
            if (!Gdx.input.isKeyJustPressed(KEYS[player][i])) continue;

            flashPad[player] = i;
            flashTimer[player] = FLASH_TIME;

            if (i == sequence[progress[player]]) {
                progress[player]++;
                if (progress[player] == sequenceLength()) return true;
            } else if (RESET_ON_ERROR) {
                progress[player] = 0;
            }
        }
        return false;
    }

    /** Tecla encendida ahora mismo durante SHOWING (-1 si ninguna). */
    private int getLitPad() {
        if (phase != Phase.SHOWING) return -1;
        float slot = SHOW_TIME / sequenceLength();      // cada luz ocupa una parte igual del tiempo total
        int index = (int) (stateTime / slot);
        float inSlot = stateTime - index * slot;
        if (index < sequenceLength() && inSlot < slot * LIGHT_ON_RATIO) {
            return sequence[index];
        }
        return -1;
    }

    private String getPhaseMessage() {
        String round = "RONDA " + (currentRound + 1) + "/" + TOTAL_ROUNDS + " - ";
        switch (phase) {
            case COUNTDOWN: return round + "PREPARATE...";
            case SHOWING:   return round + "MIRA LA SECUENCIA";
            case INPUT:     return round + "¡REPETILA!";
            default:
                if (subWinner == 0) return "EMPATE EN ESTA RONDA";
                return "PUNTO PARA EL JUGADOR " + subWinner;
        }
    }

    private void drawProgressBoxes(int player) {
        int length = sequenceLength();
        float box = 24f, gap = 8f;
        float total = length * box + (length - 1) * gap;
        float startX = player * HALF_WIDTH + (HALF_WIDTH - total) / 2f;
        for (int i = 0; i < length; i++) {
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
