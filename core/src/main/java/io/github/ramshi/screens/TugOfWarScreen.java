package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

public class TugOfWarScreen extends BaseMinigameScreen {

    // ---- Duración (static final = constante de compilación, ver nota abajo) ----
    private static final float ROUND_DURATION = 10f;

    // ---- Controles (cambialos acá si querés otras teclas) ----
    private static final int P1_KEY = Input.Keys.A;
    private static final int P2_KEY = Input.Keys.LEFT;

    // ---- Mecánica ----
    private static final float PULL_STEP = 5f;     // píxeles que mueve cada pulsación
    private static final float MAX_OFFSET = 150f;  // desplazamiento máximo = línea de victoria
    private static final float SMOOTHING = 12f;    // qué tan rápido "alcanza" el dibujo a la lógica

    // ---- Layout (el mundo virtual es 800x480) ----
    private static final float CENTER_X = VIRTUAL_WIDTH / 2f;
    private static final float ROPE_Y = 235f;
    private static final float ROPE_H = 10f;
    private static final float ROPE_HALF = 140f;
    private static final float PLAYER_W = 64f;
    private static final float PLAYER_H = 90f;

    // Negativo = soga hacia P1 (izquierda), positivo = hacia P2 (derecha)
    private float ropeTarget = 0f;  // posición LÓGICA (decide el ganador)
    private float ropeShown = 0f;   // posición DIBUJADA (suavizada con delta)

    private int p1Pulls = 0;
    private int p2Pulls = 0;

    public TugOfWarScreen(Game game) {
        super(game);
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        if (Gdx.input.isKeyJustPressed(P1_KEY)) {
            ropeTarget -= PULL_STEP;
            p1Pulls++;
        }
        if (Gdx.input.isKeyJustPressed(P2_KEY)) {
            ropeTarget += PULL_STEP;
            p2Pulls++;
        }
        ropeTarget = MathUtils.clamp(ropeTarget, -MAX_OFFSET, MAX_OFFSET);

        // Suavizado independiente del framerate
        ropeShown = MathUtils.lerp(ropeShown, ropeTarget, Math.min(1f, SMOOTHING * delta));

        // Victoria por KO: alguien llevó la soga hasta su línea
        if (Math.abs(ropeTarget) >= MAX_OFFSET) {
            ropeShown = ropeTarget;
            finishRound();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        float ropeCenter = CENTER_X + ropeShown;

        shapeRenderer.begin(ShapeType.Filled);

        // Suelo
        shapeRenderer.setColor(0.22f, 0.40f, 0.18f, 1f);
        shapeRenderer.rect(0f, 150f, VIRTUAL_WIDTH, 180f);

        // Línea central
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rect(CENTER_X - 1f, 150f, 2f, 180f);

        // Líneas de victoria (la bandera de la soga tiene que tocarlas)
        shapeRenderer.setColor(0.3f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(CENTER_X - MAX_OFFSET - 2f, 150f, 4f, 180f);
        shapeRenderer.setColor(0.3f, 0.7f, 1f, 1f);
        shapeRenderer.rect(CENTER_X + MAX_OFFSET - 2f, 150f, 4f, 180f);

        // Soga
        shapeRenderer.setColor(0.55f, 0.38f, 0.18f, 1f);
        shapeRenderer.rect(ropeCenter - ROPE_HALF, ROPE_Y, ROPE_HALF * 2f, ROPE_H);

        // Bandera del centro de la soga
        shapeRenderer.setColor(0.9f, 0.15f, 0.15f, 1f);
        shapeRenderer.rect(ropeCenter - 4f, ROPE_Y - 10f, 8f, ROPE_H + 20f);

        // Jugadores agarrados de cada punta
        shapeRenderer.setColor(0.3f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(ropeCenter - ROPE_HALF - PLAYER_W, ROPE_Y - 40f, PLAYER_W, PLAYER_H);
        shapeRenderer.setColor(0.3f, 0.7f, 1f, 1f);
        shapeRenderer.rect(ropeCenter + ROPE_HALF, ROPE_Y - 40f, PLAYER_W, PLAYER_H);

        shapeRenderer.end();

        // Texto de ayuda
        batch.begin();
        font.getData().setScale(1.1f);
        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        font.draw(batch, "P1: machaca [A]", 60f, 100f);
        font.setColor(0.3f, 0.7f, 1f, 1f);
        font.draw(batch, "P2: machaca [FLECHA IZQ]", 480f, 100f);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (ropeTarget < -0.01f) {
            p1GlobalWins++;
            return "¡JUGADOR 1 GANA LA RONDA!";
        } else if (ropeTarget > 0.01f) {
            p2GlobalWins++;
            return "¡JUGADOR 2 GANA LA RONDA!";
        } else {
            return "¡EMPATE EN LA RONDA!";
        }
    }

    @Override
    protected void onRoundReset() {
        ropeTarget = 0f;
        ropeShown = 0f;
        p1Pulls = 0;
        p2Pulls = 0;
    }

    @Override
    protected String getP1StatusText() {
        return "P1 [A] (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2 [IZQ] (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Tirones: P1 (" + p1Pulls + ") vs P2 (" + p2Pulls + ")";
    }

    @Override
    public void dispose() {
        super.dispose(); // libera batch, shapeRenderer y font de la base
        // Cuando agreguemos Texture, se liberan acá (ver sección de sprites)
    }
}