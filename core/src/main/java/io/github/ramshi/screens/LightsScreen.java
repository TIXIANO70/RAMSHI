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

import java.util.Arrays;

public class LightsScreen extends BaseMinigameScreen {

    private static final float TOTAL_DURATION = 10f;
    private static final int[] SEQUENCE_LENGTHS = {4, 4, 5};
    private static final float SHOW_TIME = 1f;
    private static final float LIGHT_ON_RATIO = 0.7f;
    private static final float COUNTDOWN_TIME = 0.4f;
    private static final float RESULT_TIME = 0.4f;
    private static final float FLASH_TIME = 0.15f;
    private static final boolean RESET_ON_ERROR = true;
    private static final float PAD_SIZE = 80f;
    private static final float PAD_GAP = 8f;
    private static final float ROW_BOTTOM_Y = 150f;
    private static final int[][] KEYS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };
    private static final Color[] PAD_COLORS = {
        new Color(0.20f, 0.90f, 0.30f, 1f), new Color(0.95f, 0.25f, 0.25f, 1f),
        new Color(0.98f, 0.85f, 0.20f, 1f), new Color(0.25f, 0.50f, 1.00f, 1f)
    };

    private enum Phase { COUNTDOWN, SHOWING, INPUT, RESULT }

    private static final int TOTAL_ROUNDS = SEQUENCE_LENGTHS.length;
    private static final float HALF_WIDTH = VIRTUAL_WIDTH / 2f;
    private static final float ROW_TOP_Y = ROW_BOTTOM_Y + PAD_SIZE + PAD_GAP;
    private static final String[][] KEY_NAMES = {
        {"w", "a", "s", "d"},
        {"up", "left", "down", "right"}
    };

    private final Texture[][] keyOff = new Texture[2][4];
    private final Texture[][] keyOn = new Texture[2][4];

    private final int[] sequence = new int[Arrays.stream(SEQUENCE_LENGTHS).max().getAsInt()];
    private final int[] progress = new int[2];
    private final int[] flashPad = new int[2];
    private final float[] flashTimer = new float[2];
    private final int[] roundWins = new int[2];
    private int currentRound;
    private int subWinner;
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
        onRoundReset();
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(path);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    @Override protected float getRoundDuration() { return TOTAL_DURATION; }

    @Override
    protected void updateGame(float delta) {
        stateTime += delta;
        for (int p = 0; p < 2; p++) flashTimer[p] -= delta;

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
            if (currentRound >= TOTAL_ROUNDS) finishRound();
            else startSubRound();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        int lit = getLitPad();

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

        batch.begin();
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                boolean on = (lit == i) || (flashTimer[p] > 0f && flashPad[p] == i);
                batch.setColor(on ? PAD_COLORS[i] : Color.WHITE);
                batch.draw(on ? keyOn[p][i] : keyOff[p][i], padX(p, i), padY(i), PAD_SIZE, PAD_SIZE);
            }
        }
        batch.setColor(Color.WHITE);

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
        int p1 = roundWins[0], p2 = roundWins[1];
        if (p1 == p2) return "¡EMPATE! (" + p1 + "-" + p2 + ")";
        int winner = (p1 > p2) ? 1 : 2, pts = Math.max(p1, p2);
        if (winner == 1) p1GlobalWins += pts;
        else p2GlobalWins += pts;
        return "¡JUGADOR " + winner + " GANA! (" + pts + "-" + Math.min(p1, p2) + ")  +" + pts + " pts";
    }

    @Override
    protected void onRoundReset() {
        roundWins[0] = roundWins[1] = 0;
        currentRound = 0;
        startSubRound();
    }

    @Override protected String getP1StatusText() { return "P1: " + roundWins[0] + " rondas (Wins: " + p1GlobalWins + ")"; }
    @Override protected String getP2StatusText() { return "P2: " + roundWins[1] + " rondas (Wins: " + p2GlobalWins + ")"; }
    @Override protected String getScoreSummary() { return "Rondas ganadas: P1 (" + roundWins[0] + ") vs P2 (" + roundWins[1] + ")"; }

    @Override
    public void dispose() {
        super.dispose();
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                keyOff[p][i].dispose();
                keyOn[p][i].dispose();
            }
        }
    }

    private void startSubRound() {
        for (int i = 0; i < sequenceLength(); i++) sequence[i] = MathUtils.random(0, 3);
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

    private int getLitPad() {
        if (phase != Phase.SHOWING) return -1;
        float slot = SHOW_TIME / sequenceLength();
        int index = (int) (stateTime / slot);
        boolean on = index < sequenceLength() && stateTime - index * slot < slot * LIGHT_ON_RATIO;
        return on ? sequence[index] : -1;
    }

    private String getPhaseMessage() {
        String round = "RONDA " + (currentRound + 1) + "/" + TOTAL_ROUNDS + " - ";
        switch (phase) {
            case COUNTDOWN: return round + "PREPARATE...";
            case SHOWING:   return round + "MIRA LA SECUENCIA";
            case INPUT:     return round + "¡REPETILA!";
            default:        return (subWinner == 0) ? "EMPATE EN ESTA RONDA" : "PUNTO PARA EL JUGADOR " + subWinner;
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
