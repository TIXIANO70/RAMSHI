package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ShipsScreen extends BaseMinigameScreen {

    // =====================================================================
    //  VALORES AJUSTABLES (todo lo que se pueda pedir cambiar está acá)
    // =====================================================================
    private static final float ROUND_DURATION = 10f;        // Duración del minijuego (segundos)
    private static final int MAX_POINTS = 3;                // Puntos máximos del ganador (el mínimo es 1)
    private static final float PLAYER_SIZE = 32f;           // Tamaño del jugador (px)
    private static final float PLAYER_SPEED = 520f;         // Velocidad de movimiento lateral (px/s)
    private static final float METEOR_MIN_SIZE = 20f;       // Tamaño mínimo de meteorito (px)
    private static final float METEOR_MAX_SIZE = 40f;       // Tamaño máximo de meteorito (px)
    private static final float FALL_SPEED_START = 260f;     // Velocidad de caída al inicio (px/s)
    private static final float FALL_SPEED_END = 900f;       // Velocidad de caída al final (px/s)
    private static final float DIFFICULTY_CURVE = 1.3f;     // 1 = sube parejo; mayor = más tranquilo al inicio y más brusco al final
    private static final float SPAWN_INTERVAL_START = 0.5f; // Segundos entre meteoritos al inicio
    private static final float SPAWN_INTERVAL_END = 0.25f;  // Segundos entre meteoritos al final
    private static final float FIRST_SPAWN_DELAY = 0.5f;    // Tiempo hasta el primer meteorito (s)
    private static final float HITBOX_MARGIN = 4f;          // Margen de perdón en colisiones (px)

    // Controles: {izquierda, derecha}
    private static final int[][] KEYS = {
        {Input.Keys.A, Input.Keys.D},          // Jugador 1
        {Input.Keys.LEFT, Input.Keys.RIGHT}    // Jugador 2
    };

    // Colores
    private static final Color[] PLAYER_COLORS = {
        new Color(0.3f, 0.9f, 0.3f, 1f),       // Jugador 1
        new Color(0.3f, 0.7f, 1f, 1f)          // Jugador 2
    };
    private static final Color HIT_COLOR = new Color(0.5f, 0.2f, 0.2f, 1f);     // Jugador golpeado
    private static final Color METEOR_COLOR = new Color(0.65f, 0.45f, 0.30f, 1f);
    private static final Color ARENA_COLOR = new Color(0.10f, 0.10f, 0.16f, 1f);

    // Zona de juego de cada jugador (mundo virtual 800x480)
    private static final float ARENA_MARGIN = 20f;
    private static final float ARENA_BOTTOM = 40f;
    private static final float ARENA_TOP = 430f;
    // =====================================================================

    private static final float HALF_WIDTH = VIRTUAL_WIDTH / 2f;
    private static final float ARENA_WIDTH = HALF_WIDTH - 2 * ARENA_MARGIN;

    /** Un meteorito programado: cuándo aparece, dónde (0..1 del ancho) y de qué tamaño. */
    private static class SpawnEvent {
        final float time, xNorm, size;
        SpawnEvent(float time, float xNorm, float size) {
            this.time = time;
            this.xNorm = xNorm;
            this.size = size;
        }
    }

    /** Un meteorito en pantalla. */
    private static class Meteor {
        float x, y;
        final float size;
        Meteor(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }

    // Estado
    private final List<SpawnEvent> events = new ArrayList<>();
    private final List<List<Meteor>> meteors = new ArrayList<>();
    private final float[] playerX = new float[2];
    private final boolean[] alive = new boolean[2];
    private final Rectangle playerBox = new Rectangle();   // reutilizados para colisiones
    private final Rectangle meteorBox = new Rectangle();
    private int nextEvent;
    private float elapsed;
    private float endTime;                                 // momento en que se decidió la ronda (s)
    private int winner;                                    // 0 = empate, 1 = P1, 2 = P2

    public ShipsScreen(Game game) {
        super(game);
        meteors.add(new ArrayList<Meteor>());
        meteors.add(new ArrayList<Meteor>());
        resetState();
    }

    // ===================== Hooks de BaseMinigameScreen =====================

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        elapsed += delta;
        spawnMeteors();

        for (int p = 0; p < 2; p++) {
            movePlayer(p, delta);
            updateMeteors(p, delta);
        }

        if (!alive[0] || !alive[1]) {
            winner = (!alive[0] && !alive[1]) ? 0 : (alive[0] ? 1 : 2);
            endTime = elapsed;
            finishRound();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int p = 0; p < 2; p++) drawArena(p);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(HALF_WIDTH, 16f, HALF_WIDTH, 432f);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(1.0f);
        font.setColor(0.6f, 0.6f, 0.65f, 1f);
        font.draw(batch, "P1: A / D", 24f, 24f);
        font.draw(batch, "P2: FLECHAS", 590f, 24f);
        batch.end();
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
        resetState();
    }

    @Override
    protected String getP1StatusText() {
        return statusText(0, p1GlobalWins);
    }

    @Override
    protected String getP2StatusText() {
        return statusText(1, p2GlobalWins);
    }

    @Override
    protected String getScoreSummary() {
        return "P1 (" + (alive[0] ? "sobrevivió" : "golpeado")
            + ") vs P2 (" + (alive[1] ? "sobrevivió" : "golpeado") + ")";
    }

    // ===================== Lógica =====================

    private void resetState() {
        elapsed = 0f;
        endTime = 0f;
        nextEvent = 0;
        winner = 0;
        for (int p = 0; p < 2; p++) {
            alive[p] = true;
            playerX[p] = arenaLeft(p) + (ARENA_WIDTH - PLAYER_SIZE) / 2f;
            meteors.get(p).clear();
        }
        generateSchedule();
    }

    /** Genera UNA lista de meteoritos que usan los dos jugadores (mismo patrón). */
    private void generateSchedule() {
        events.clear();
        float t = FIRST_SPAWN_DELAY;
        while (t < ROUND_DURATION) {
            events.add(new SpawnEvent(t, MathUtils.random(),
                MathUtils.random(METEOR_MIN_SIZE, METEOR_MAX_SIZE)));
            t += MathUtils.lerp(SPAWN_INTERVAL_START, SPAWN_INTERVAL_END, t / ROUND_DURATION);
        }
    }

    /** Crea, para ambos jugadores, los meteoritos cuyo momento ya llegó. */
    private void spawnMeteors() {
        while (nextEvent < events.size() && events.get(nextEvent).time <= elapsed) {
            SpawnEvent e = events.get(nextEvent++);
            for (int p = 0; p < 2; p++) {
                float x = arenaLeft(p) + e.xNorm * (ARENA_WIDTH - e.size);
                meteors.get(p).add(new Meteor(x, ARENA_TOP, e.size));
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

    private void updateMeteors(int p, float delta) {
        List<Meteor> list = meteors.get(p);
        float fall = fallSpeed() * delta;
        for (int i = list.size() - 1; i >= 0; i--) {
            Meteor m = list.get(i);
            m.y -= fall;
            if (alive[p] && hits(p, m)) alive[p] = false;
            if (m.y + m.size < ARENA_BOTTOM) list.remove(i);   // salió por abajo
        }
    }

    /** Colisión de cajas con un pequeño margen de perdón. */
    private boolean hits(int p, Meteor m) {
        playerBox.set(playerX[p] + HITBOX_MARGIN, ARENA_BOTTOM + HITBOX_MARGIN,
            PLAYER_SIZE - 2 * HITBOX_MARGIN, PLAYER_SIZE - 2 * HITBOX_MARGIN);
        meteorBox.set(m.x + HITBOX_MARGIN, m.y + HITBOX_MARGIN,
            m.size - 2 * HITBOX_MARGIN, m.size - 2 * HITBOX_MARGIN);
        return playerBox.overlaps(meteorBox);
    }

    private float fallSpeed() {
        float progress = MathUtils.clamp(elapsed / ROUND_DURATION, 0f, 1f);
        return MathUtils.lerp(FALL_SPEED_START, FALL_SPEED_END, (float) Math.pow(progress, DIFFICULTY_CURVE));
    }

    /** Cuanto antes cae el rival, más puntos (de MAX_POINTS a 1). */
    private int winnerPoints() {
        int points = MAX_POINTS - (int) (endTime / ROUND_DURATION * MAX_POINTS);
        return MathUtils.clamp(points, 1, MAX_POINTS);
    }

    private String statusText(int p, int wins) {
        return "P" + (p + 1) + ": " + (alive[p] ? "EN PIE" : "GOLPEADO") + " (Wins: " + wins + ")";
    }

    private float arenaLeft(int p) {
        return p * HALF_WIDTH + ARENA_MARGIN;
    }

    // ===================== Dibujo =====================

    /** Dibuja fondo, meteoritos y jugador de una arena (dentro de un begin/end Filled). */
    private void drawArena(int p) {
        shapeRenderer.setColor(ARENA_COLOR);
        shapeRenderer.rect(arenaLeft(p), ARENA_BOTTOM, ARENA_WIDTH, ARENA_TOP - ARENA_BOTTOM);

        shapeRenderer.setColor(METEOR_COLOR);
        for (Meteor m : meteors.get(p)) {
            shapeRenderer.circle(m.x + m.size / 2f, m.y + m.size / 2f, m.size / 2f);
        }

        shapeRenderer.setColor(alive[p] ? PLAYER_COLORS[p] : HIT_COLOR);
        shapeRenderer.rect(playerX[p], ARENA_BOTTOM, PLAYER_SIZE, PLAYER_SIZE);
    }
}