package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ShipsScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;        // Duración del minijuego (segundos)
    private static final int MAX_POINTS = 3;                // Puntos máximos del ganador (el mínimo es 1)
    private static final float PLAYER_SIZE = 48f;           // Tamaño del barco jugador (px)
    private static final float PLAYER_SPEED = 520f;         // Velocidad de movimiento lateral (px/s)
    private static final float BALL_MIN_SIZE = 16f;         // Tamaño mínimo de bala de cañón (px)
    private static final float BALL_MAX_SIZE = 32f;         // Tamaño máximo de bala de cañón (px)
    private static final float FALL_SPEED_START = 260f;     // Velocidad de caída al inicio (px/s)
    private static final float FALL_SPEED_END = 900f;       // Velocidad de caída al final (px/s)
    private static final float DIFFICULTY_CURVE = 1.3f;     // 1 = sube parejo; mayor = más tranquilo al inicio y más brusco al final
    private static final float SPAWN_INTERVAL_START = 0.5f; // Segundos entre balas al inicio
    private static final float SPAWN_INTERVAL_END = 0.25f;  // Segundos entre balas al final
    private static final float FIRST_SPAWN_DELAY = 0.5f;    // Tiempo hasta la primera bala (s)
    private static final float HITBOX_MARGIN = 4f;          // Margen de perdón en colisiones (px)
    private static final float PLAYER_Y = 40f;              // Altura de los barcos sobre el agua
    private static final float BALL_SPAWN_Y = 400f;         // Altura desde donde salen las balas
    private static final float ARENA_MARGIN = 20f;          // Margen lateral de la zona de cada jugador
    private static final Color HIT_COLOR = new Color(1f, 0.35f, 0.35f, 1f); // Tinte del barco golpeado
    private static final int[][] KEYS = {                   // Controles {izquierda, derecha}
        {Input.Keys.A, Input.Keys.D},
        {Input.Keys.LEFT, Input.Keys.RIGHT}
    };

    private static final float HALF_WIDTH = VIRTUAL_WIDTH / 2f;
    private static final float ARENA_WIDTH = HALF_WIDTH - 2 * ARENA_MARGIN;

    private static class SpawnEvent {
        final float time, xNorm, size;
        SpawnEvent(float time, float xNorm, float size) { this.time = time; this.xNorm = xNorm; this.size = size; }
    }

    private static class Cannonball {
        float x, y;
        final float size;
        Cannonball(float x, float y, float size) { this.x = x; this.y = y; this.size = size; }
    }

    private final Texture background = loadTexture("ships/ships_bg.png", true);
    private final Texture ball = loadTexture("ships/ships_cannon.png", false);
    private final Texture[] playerShips = {
        loadTexture("ships/ships_p1.png", false), loadTexture("ships/ships_p2.png", false)
    };

    private final List<SpawnEvent> events = new ArrayList<>();
    private final List<List<Cannonball>> balls = new ArrayList<>();
    private final float[] playerX = new float[2];
    private final boolean[] alive = new boolean[2];
    private final Rectangle playerBox = new Rectangle();
    private final Rectangle ballBox = new Rectangle();
    private int nextEvent;
    private float elapsed;
    private float endTime;
    private int winner;

    public ShipsScreen(Game game) {
        super(game);
        balls.add(new ArrayList<Cannonball>());
        balls.add(new ArrayList<Cannonball>());
        onRoundReset();
    }

    private Texture loadTexture(String path, boolean smooth) {
        Texture t = new Texture(path);
        Texture.TextureFilter f = smooth ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
        t.setFilter(f, f);
        return t;
    }

    @Override protected float getRoundDuration() { return ROUND_DURATION; }

    @Override
    protected void updateGame(float delta) {
        elapsed += delta;
        spawnBalls();

        for (int p = 0; p < 2; p++) {
            movePlayer(p, delta);
            updateBalls(p, delta);
        }

        if (!alive[0] || !alive[1]) {
            winner = (!alive[0] && !alive[1]) ? 0 : (alive[0] ? 1 : 2);
            endTime = elapsed;
            finishRound();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        batch.draw(background, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        for (int p = 0; p < 2; p++) {
            for (Cannonball b : balls.get(p)) {
                batch.draw(ball, b.x, b.y, b.size, b.size);
            }
            batch.setColor(alive[p] ? Color.WHITE : HIT_COLOR);
            batch.draw(playerShips[p], playerX[p], PLAYER_Y, PLAYER_SIZE, PLAYER_SIZE);
            batch.setColor(Color.WHITE);
        }

        font.getData().setScale(1.0f);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "P1: A / D", 24f, 24f);
        font.draw(batch, "P2: FLECHAS", 590f, 24f);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 1f);
        shapeRenderer.line(HALF_WIDTH, 16f, HALF_WIDTH, 432f);
        shapeRenderer.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (winner == 0) return "¡EMPATE EN LA RONDA!";

        int points = winnerPoints();
        if (winner == 1) p1GlobalWins += points;
        else p2GlobalWins += points;
        return "¡JUGADOR " + winner + " GANA!  +" + points + (points == 1 ? " pt" : " pts");
    }

    @Override
    protected void onRoundReset() {
        elapsed = 0f;
        endTime = 0f;
        nextEvent = 0;
        winner = 0;
        for (int p = 0; p < 2; p++) {
            alive[p] = true;
            playerX[p] = arenaLeft(p) + (ARENA_WIDTH - PLAYER_SIZE) / 2f;
            balls.get(p).clear();
        }
        generateSchedule();
    }

    @Override protected String getP1StatusText() { return statusText(0, p1GlobalWins); }
    @Override protected String getP2StatusText() { return statusText(1, p2GlobalWins); }

    @Override
    protected String getScoreSummary() {
        return "P1 (" + (alive[0] ? "sobrevivió" : "hundido")
            + ") vs P2 (" + (alive[1] ? "sobrevivió" : "hundido") + ")";
    }

    @Override
    public void dispose() {
        super.dispose();
        background.dispose();
        ball.dispose();
        for (Texture t : playerShips) t.dispose();
    }

    private void generateSchedule() {
        events.clear();
        float t = FIRST_SPAWN_DELAY;
        while (t < ROUND_DURATION) {
            events.add(new SpawnEvent(t, MathUtils.random(), MathUtils.random(BALL_MIN_SIZE, BALL_MAX_SIZE)));
            t += MathUtils.lerp(SPAWN_INTERVAL_START, SPAWN_INTERVAL_END, t / ROUND_DURATION);
        }
    }

    private void spawnBalls() {
        while (nextEvent < events.size() && events.get(nextEvent).time <= elapsed) {
            SpawnEvent e = events.get(nextEvent++);
            for (int p = 0; p < 2; p++) {
                float x = arenaLeft(p) + e.xNorm * (ARENA_WIDTH - e.size);
                balls.get(p).add(new Cannonball(x, BALL_SPAWN_Y, e.size));
            }
        }
    }

    private void movePlayer(int p, float delta) {
        if (!alive[p]) return;
        if (Gdx.input.isKeyPressed(KEYS[p][0])) playerX[p] -= PLAYER_SPEED * delta;
        if (Gdx.input.isKeyPressed(KEYS[p][1])) playerX[p] += PLAYER_SPEED * delta;
        float left = arenaLeft(p);
        playerX[p] = MathUtils.clamp(playerX[p], left, left + ARENA_WIDTH - PLAYER_SIZE);
    }

    private void updateBalls(int p, float delta) {
        List<Cannonball> list = balls.get(p);
        float fall = fallSpeed() * delta;
        for (int i = list.size() - 1; i >= 0; i--) {
            Cannonball b = list.get(i);
            b.y -= fall;
            if (alive[p] && hits(p, b)) alive[p] = false;
            if (b.y + b.size < PLAYER_Y) list.remove(i);
        }
    }

    private boolean hits(int p, Cannonball b) {
        playerBox.set(playerX[p] + HITBOX_MARGIN, PLAYER_Y + HITBOX_MARGIN,
            PLAYER_SIZE - 2 * HITBOX_MARGIN, PLAYER_SIZE - 2 * HITBOX_MARGIN);
        ballBox.set(b.x + HITBOX_MARGIN, b.y + HITBOX_MARGIN,
            b.size - 2 * HITBOX_MARGIN, b.size - 2 * HITBOX_MARGIN);
        return playerBox.overlaps(ballBox);
    }

    private float fallSpeed() {
        float progress = MathUtils.clamp(elapsed / ROUND_DURATION, 0f, 1f);
        return MathUtils.lerp(FALL_SPEED_START, FALL_SPEED_END, (float) Math.pow(progress, DIFFICULTY_CURVE));
    }

    private int winnerPoints() {
        int points = MAX_POINTS - (int) (endTime / ROUND_DURATION * MAX_POINTS);
        return MathUtils.clamp(points, 1, MAX_POINTS);
    }

    private String statusText(int p, int wins) {
        return "P" + (p + 1) + ": " + (alive[p] ? "A FLOTE" : "HUNDIDO") + " (Wins: " + wins + ")";
    }

    private float arenaLeft(int p) {
        return p * HALF_WIDTH + ARENA_MARGIN;
    }
}
