package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

public class StopItScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final int MAX_ATTEMPTS = 12;
    private static final float SPEED_START = 0.9f;
    private static final float SPEED_STEP = 0.15f;
    private static final float ZONE_HALF_START = 0.09f;
    private static final float ZONE_HALF_MIN = 0.04f;
    private static final float ZONE_SHRINK = 0.006f;
    private static final float RESULT_PAUSE = 0.5f;
    private static final int POINTS_PERFECT = 3;
    private static final int POINTS_GREEN = 2;
    private static final int POINTS_NEAR = 1;
    private static final float PERFECT_RATIO = 0.3f;
    private static final float NEAR_RATIO = 2f;
    private static final int[] KEYS = {Input.Keys.W, Input.Keys.UP};
    private static final float[] BAR_X = {40f, 440f};
    private static final float BAR_W = 320f;
    private static final float BAR_Y = 220f;
    private static final float BAR_H = 36f;

    private final float[] zoneCenter = new float[MAX_ATTEMPTS];

    private final float[] pos = new float[2];
    private final int[] dir = {1, 1};
    private final boolean[] stopped = new boolean[2];
    private final float[] pauseTimer = new float[2];
    private final int[] attempt = new int[2];
    private final int[] score = new int[2];
    private final String[] lastText = {"", ""};
    private final Color[] lastColor = {Color.WHITE, Color.WHITE};

    public StopItScreen(Game game) {
        super(game);
        resetState();
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        for (int p = 0; p < 2; p++) {
            if (stopped[p]) {
                pauseTimer[p] -= delta;
                if (pauseTimer[p] <= 0f) {
                    stopped[p] = false;
                    attempt[p] = Math.min(attempt[p] + 1, MAX_ATTEMPTS - 1);
                    pos[p] = 0f;
                    dir[p] = 1;
                }
                continue;
            }

            pos[p] += dir[p] * speed(p) * delta;
            if (pos[p] >= 1f) { pos[p] = 1f; dir[p] = -1; }
            if (pos[p] <= 0f) { pos[p] = 0f; dir[p] = 1; }

            if (Gdx.input.isKeyJustPressed(KEYS[p])) {
                evaluate(p);
            }
        }
    }

    private void evaluate(int p) {
        float half = zoneHalf(p);
        float d = Math.abs(pos[p] - zoneCenter[attempt[p]]);
        int points;
        if (d <= half * PERFECT_RATIO) {
            points = POINTS_PERFECT;
            lastText[p] = "¡PERFECTO! +" + points;
            lastColor[p] = new Color(1f, 0.85f, 0.2f, 1f);
        } else if (d <= half) {
            points = POINTS_GREEN;
            lastText[p] = "¡BIEN! +" + points;
            lastColor[p] = new Color(0.3f, 0.9f, 0.3f, 1f);
        } else if (d <= half * NEAR_RATIO) {
            points = POINTS_NEAR;
            lastText[p] = "CERCA +" + points;
            lastColor[p] = new Color(1f, 0.6f, 0.2f, 1f);
        } else {
            points = 0;
            lastText[p] = "FALLASTE";
            lastColor[p] = new Color(1f, 0.3f, 0.3f, 1f);
        }
        score[p] += points;
        stopped[p] = true;
        pauseTimer[p] = RESULT_PAUSE;
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        shapeRenderer.begin(ShapeType.Filled);
        for (int p = 0; p < 2; p++) {
            float x = BAR_X[p];
            shapeRenderer.setColor(0.18f, 0.18f, 0.2f, 1f);
            shapeRenderer.rect(x, BAR_Y, BAR_W, BAR_H);

            float half = zoneHalf(p);
            float c = zoneCenter[attempt[p]];
            drawZone(x, c, half * NEAR_RATIO, 0.55f, 0.35f, 0.12f);
            drawZone(x, c, half, 0.2f, 0.7f, 0.25f);
            drawZone(x, c, half * PERFECT_RATIO, 1f, 0.85f, 0.2f);

            if (stopped[p]) shapeRenderer.setColor(lastColor[p]);
            else shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x + pos[p] * BAR_W - 3f, BAR_Y - 8f, 6f, BAR_H + 16f);
        }
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.rect(VIRTUAL_WIDTH / 2f - 1f, 16f, 2f, 416f);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(1.3f);
        for (int p = 0; p < 2; p++) {
            if (stopped[p]) {
                font.setColor(lastColor[p]);
                drawCenteredIn(batch, lastText[p], BAR_X[p], BAR_W, 320f);
            }
        }
        font.getData().setScale(1.0f);
        font.setColor(0.6f, 0.6f, 0.65f, 1f);
        font.draw(batch, "P1: [W] frenar", 24f, 24f);
        font.draw(batch, "P2: [FLECHA ARRIBA] frenar", 520f, 24f);
        batch.end();
    }

    private void drawZone(float barX, float center, float half, float r, float g, float b) {
        float left = Math.max(0f, center - half);
        float right = Math.min(1f, center + half);
        shapeRenderer.setColor(r, g, b, 1f);
        shapeRenderer.rect(barX + left * BAR_W, BAR_Y, (right - left) * BAR_W, BAR_H);
    }

    private void drawCenteredIn(SpriteBatch batch, String text, float x, float w, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, layout, x + (w - layout.width) / 2f, y);
    }

    @Override
    protected String determineRoundWinner() {
        if (score[0] > score[1]) {
            p1GlobalWins++;
            return "¡JUGADOR 1 GANA!";
        } else if (score[1] > score[0]) {
            p2GlobalWins++;
            return "¡JUGADOR 2 GANA!";
        }
        return "¡EMPATE!";
    }

    @Override
    protected void onRoundReset() {
        resetState();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + score[0] + " pts (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + score[1] + " pts (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Puntos: P1 (" + score[0] + ") vs P2 (" + score[1] + ")";
    }

    private void resetState() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            zoneCenter[i] = MathUtils.random(0.2f, 0.8f);
        }
        for (int p = 0; p < 2; p++) {
            pos[p] = 0f;
            dir[p] = 1;
            stopped[p] = false;
            pauseTimer[p] = 0f;
            attempt[p] = 0;
            score[p] = 0;
            lastText[p] = "";
        }
    }

    private float speed(int p) {
        return SPEED_START + SPEED_STEP * attempt[p];
    }

    private float zoneHalf(int p) {
        return Math.max(ZONE_HALF_MIN, ZONE_HALF_START - ZONE_SHRINK * attempt[p]);
    }
}