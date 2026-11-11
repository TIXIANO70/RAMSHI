package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RpsScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final int PIEDRA = 0;
    private static final int PAPEL  = 1;
    private static final int TIJERA = 2;
    private static final int DEFAULT_CHOICE = PAPEL;
    private static final int   COUNT_STEPS = 3;
    private static final float STEP_TIME   = 1.0f;
    private static final float REVEAL_TIME = 1.0f;
    private static final int HAND_POINTS       = 1;
    private static final int WIN_GLOBAL_POINTS = 1;
    private static final int TIE_GLOBAL_POINTS = 1;
    private static final int P1_KEY_PIEDRA = Input.Keys.A;
    private static final int P1_KEY_PAPEL  = Input.Keys.S;
    private static final int P1_KEY_TIJERA = Input.Keys.D;
    private static final int P2_KEY_PIEDRA = Input.Keys.LEFT;
    private static final int P2_KEY_PAPEL  = Input.Keys.DOWN;
    private static final int P2_KEY_TIJERA = Input.Keys.RIGHT;
    private static final float SPRITE_SIZE = 128f;
    private static final float P1_X = 140f;
    private static final float P2_X = 532f;
    private static final float HAND_Y = 180f;
    private static final String[] NAMES = {"PIEDRA", "PAPEL", "TIJERA"};
    private final Texture[] textures = new Texture[3];

    private int p1Score = 0;
    private int p2Score = 0;
    private int p1Choice = DEFAULT_CHOICE;
    private int p2Choice = DEFAULT_CHOICE;
    private float handTimer = 0f;
    private boolean revealed = false;
    private String handMessage = "";

    public RpsScreen(Game game) {
        super(game);
        textures[PIEDRA] = new Texture("rps/piedra.png");
        textures[PAPEL]  = new Texture("rps/papel.png");
        textures[TIJERA] = new Texture("rps/tijera.png");
        for (Texture t : textures) {
            t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        }
        resetHand();
    }

    private boolean wins(int a, int b) {
        return (a == PIEDRA && b == TIJERA)
            || (a == PAPEL  && b == PIEDRA)
            || (a == TIJERA && b == PAPEL);
    }

    private void resetHand() {
        p1Choice = DEFAULT_CHOICE;
        p2Choice = DEFAULT_CHOICE;
        handTimer = 0f;
        revealed = false;
        handMessage = "";
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        handTimer += delta;
        float countDuration = COUNT_STEPS * STEP_TIME;

        if (handTimer < countDuration) {
            if (Gdx.input.isKeyJustPressed(P1_KEY_PIEDRA)) p1Choice = PIEDRA;
            if (Gdx.input.isKeyJustPressed(P1_KEY_PAPEL))  p1Choice = PAPEL;
            if (Gdx.input.isKeyJustPressed(P1_KEY_TIJERA)) p1Choice = TIJERA;

            if (Gdx.input.isKeyJustPressed(P2_KEY_PIEDRA)) p2Choice = PIEDRA;
            if (Gdx.input.isKeyJustPressed(P2_KEY_PAPEL))  p2Choice = PAPEL;
            if (Gdx.input.isKeyJustPressed(P2_KEY_TIJERA)) p2Choice = TIJERA;
        } else if (!revealed) {
            revealed = true;
            if (wins(p1Choice, p2Choice)) {
                p1Score += HAND_POINTS;
                handMessage = "¡PUNTO P1!";
            } else if (wins(p2Choice, p1Choice)) {
                p2Score += HAND_POINTS;
                handMessage = "¡PUNTO P2!";
            } else {
                handMessage = "¡EMPATE!";
            }
        } else if (handTimer >= countDuration + REVEAL_TIME) {
            resetHand();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        int shownP1 = revealed ? p1Choice : DEFAULT_CHOICE;
        int shownP2 = revealed ? p2Choice : DEFAULT_CHOICE;

        batch.begin();
        batch.draw(textures[shownP1], P1_X, HAND_Y, SPRITE_SIZE, SPRITE_SIZE);
        batch.draw(textures[shownP2], P2_X, HAND_Y, SPRITE_SIZE, SPRITE_SIZE);

        font.setColor(Color.WHITE);
        font.getData().setScale(2.2f);
        if (!revealed) {
            int step = Math.min(COUNT_STEPS, (int) (handTimer / STEP_TIME) + 1);
            font.draw(batch, String.valueOf(step), 390f, 260f);
        } else {
            font.getData().setScale(1.4f);
            font.draw(batch, handMessage, 340f, 360f);
            font.draw(batch, NAMES[p1Choice], P1_X + 20f, 150f);
            font.draw(batch, NAMES[p2Choice], P2_X + 20f, 150f);
        }

        font.getData().setScale(1.0f);
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.draw(batch, "P1: [A] Piedra  [S] Papel  [D] Tijera", 40f, 60f);
        font.draw(batch, "P2: [IZQ] Piedra  [ABAJO] Papel  [DER] Tijera", 430f, 60f);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (p1Score > p2Score) {
            p1GlobalWins += WIN_GLOBAL_POINTS;
            return "GANA JUGADOR 1";
        } else if (p2Score > p1Score) {
            p2GlobalWins += WIN_GLOBAL_POINTS;
            return "GANA JUGADOR 2";
        } else {
            p1GlobalWins += TIE_GLOBAL_POINTS;
            p2GlobalWins += TIE_GLOBAL_POINTS;
            return "EMPATE (+1 A CADA UNO)";
        }
    }

    @Override
    protected void onRoundReset() {
        p1Score = 0;
        p2Score = 0;
        resetHand();
    }

    @Override
    protected String getP1StatusText() {
        return "Puntos P1: " + p1Score;
    }

    @Override
    protected String getP2StatusText() {
        return "Puntos P2: " + p2Score;
    }

    @Override
    protected String getScoreSummary() {
        return "P1 (" + p1Score + " pts) vs P2 (" + p2Score + " pts)";
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : textures) {
            if (t != null) t.dispose();
        }
    }
}
