package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * MINIJUEGO 1v1: LA PAPA CALIENTE (BombTagScreen)
 * Persecución en una arena cerrada con pilares y aceleradores de velocidad.
 * Uno empieza con la bomba; al tocar al rival se la pasa.
 * Quien tenga la bomba cuando se acaba el tiempo EXPLOTA y pierde.
 * - Jugador 1: WASD (moverse), ESPACIO (dash).
 * - Jugador 2: Flechas (moverse), ENTER (dash).
 */
public class BombTagScreen extends BaseMinigameScreen {

    // ===== AJUSTES =====
    private static final float ROUND_DURATION = 10f;       // duración de la ronda (s)
    private static final int WIN_POINTS = 2;               // puntos para el que sobrevive

    private static final float PLAYER_SPEED = 210f;        // velocidad normal (px/s)
    private static final float DASH_SPEED = 480f;          // velocidad durante el dash (px/s)
    private static final float DASH_DURATION = 0.28f;      // duración del dash (s)
    private static final float DASH_COOLDOWN = 3.0f;       // recarga del dash (s)
    private static final float BOOST_PAD_SPEED = 340f;     // velocidad sobre un acelerador (px/s)
    private static final float BOMB_HOLDER_BOOST = 1.08f;  // multiplicador de velocidad del portador

    private static final float TRANSFER_IMMUNITY = 0.85f;  // inmunidad tras pasar la bomba (s)
    private static final float START_IMMUNITY = 1.0f;      // inmunidad al empezar la ronda (s)
    private static final float DETONATION_TIME = 0.05f;    // tiempo restante en que explota la bomba (s)

    private static final float PLAYER_RADIUS = 18f;        // radio de colisión del jugador
    private static final float PLAYER_SPRITE_SIZE = 40f;   // tamaño del sprite del jugador
    private static final float PLAYER_START_X = 120f;      // distancia al borde donde arranca cada uno
    private static final float BOMB_SIZE = 36f;            // tamaño del sprite de la bomba
    private static final float BOMB_OFFSET_Y = 14f;        // altura de la bomba sobre el jugador

    // Límites de la arena (800x480)
    private static final float ARENA_MIN_X = 50f;
    private static final float ARENA_MAX_X = VIRTUAL_WIDTH - 50f;
    private static final float ARENA_MIN_Y = 50f;
    private static final float ARENA_MAX_Y = VIRTUAL_HEIGHT - 50f;

    // Controles por jugador: {arriba, abajo, izquierda, derecha, dash}
    private static final int[][] KEYS = {
        { Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE },
        { Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.ENTER }
    };
    private static final String[] CONTROLS_TEXT = { "P1: WASD | ", "P2: FLECHAS | " };
    private static final String[] DASH_READY_TEXT = { "[ESPACIO] DASH LISTO", "[ENTER] DASH LISTO" };
    private static final float[] HUD_X = { 55f, VIRTUAL_WIDTH - 290f };

    // Colores por jugador: {P1, P2}
    private static final Color[] TRAIL_COLORS = { Color.PINK, Color.CYAN };
    private static final Color[] HUD_COLORS = { new Color(1f, 0.3f, 0.6f, 1f), new Color(0f, 0.85f, 1f, 1f) };
    private static final Color[] HOLDER_COLORS = { new Color(1f, 0.35f, 0.2f, 1f), new Color(0f, 0.85f, 1f, 1f) };

    // Pilares (obstáculos) y aceleradores
    private final Rectangle[] obstaculos = new Rectangle[] {
        new Rectangle(200f, 150f, 60f, 60f),
        new Rectangle(200f, 270f, 60f, 60f),
        new Rectangle(540f, 150f, 60f, 60f),
        new Rectangle(540f, 270f, 60f, 60f),
        new Rectangle(370f, 210f, 60f, 60f) // pilar central
    };
    private final Rectangle[] boostPads = new Rectangle[] {
        new Rectangle(350f, 90f, 100f, 30f),
        new Rectangle(350f, 360f, 100f, 30f)
    };

    // ===== CLASES INTERNAS =====
    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;
        Particle(float x, float y, float vx, float vy, float life, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life; this.color = new Color(color);
        }
    }

    /** Un jugador: posición, teclas, sprite y tiempos del dash. */
    private static class Runner {
        final int index;              // 0 = P1, 1 = P2
        final int up, down, left, right, dash;
        final Texture sprite;
        final Vector2 pos = new Vector2();
        final Vector2 dir = new Vector2();
        float dashTimer;
        float dashCooldown;

        Runner(int index, Texture sprite) {
            this.index = index;
            this.sprite = sprite;
            int[] k = KEYS[index];
            up = k[0]; down = k[1]; left = k[2]; right = k[3]; dash = k[4];
        }

        void reset() {
            float x = (index == 0) ? PLAYER_START_X : VIRTUAL_WIDTH - PLAYER_START_X;
            pos.set(x, VIRTUAL_HEIGHT / 2f);
            dashTimer = 0f;
            dashCooldown = 0f;
        }

        void tickTimers(float delta) {
            if (dashCooldown > 0f) dashCooldown -= delta;
            if (dashTimer > 0f) dashTimer -= delta;
        }

        /** Lee las teclas y deja en 'dir' la dirección normalizada. */
        void readDirection() {
            dir.set(0f, 0f);
            if (Gdx.input.isKeyPressed(up)) dir.y += 1f;
            if (Gdx.input.isKeyPressed(down)) dir.y -= 1f;
            if (Gdx.input.isKeyPressed(left)) dir.x -= 1f;
            if (Gdx.input.isKeyPressed(right)) dir.x += 1f;
            dir.nor(); // evita velocidad extra en diagonal
        }
    }

    // ===== ESTADO =====
    private final List<Texture> textures = new ArrayList<>();
    private final Texture bgTex;
    private final Texture bombTex;
    private final Texture pillarTex;
    private final Texture boostPadTex;

    private final Runner[] runners;
    private final List<Particle> particles = new ArrayList<>();
    private Runner bombHolder;
    private float transferCooldown = 0f;
    private float bombPulseAnim = 0f;

    public BombTagScreen(Game game) {
        super(game);
        bgTex = loadTexture("bombtag/arena_bg.png", true);
        bombTex = loadTexture("bombtag/bomb.png", false);
        pillarTex = loadTexture("bombtag/pillar.png", false);
        boostPadTex = loadTexture("bombtag/boost_pad.png", false);
        runners = new Runner[] {
            new Runner(0, loadTexture("bombtag/player_p1.png", true)),
            new Runner(1, loadTexture("bombtag/player_p2.png", true))
        };
        resetArena();
    }

    private Texture loadTexture(String path, boolean smooth) {
        Texture t = new Texture(path);
        if (smooth) t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        textures.add(t);
        return t;
    }

    private void resetArena() {
        for (Runner r : runners) r.reset();
        transferCooldown = START_IMMUNITY;
        particles.clear();
        bombHolder = MathUtils.randomBoolean() ? runners[0] : runners[1]; // quién empieza con la bomba
    }

    private Runner other(Runner r) {
        return (r == runners[0]) ? runners[1] : runners[0];
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    // ===== LÓGICA =====
    @Override
    protected void updateGame(float delta) {
        bombPulseAnim += delta * 6f;
        if (transferCooldown > 0f) transferCooldown -= delta;
        for (Runner r : runners) r.tickTimers(delta);

        for (Runner r : runners) updateRunner(r, delta);
        emitBombSparks();
        handleContact();

        // La bomba explota al llegar a cero
        if (timeRemaining <= DETONATION_TIME) {
            spawnExplosion(bombHolder.pos.x, bombHolder.pos.y);
            finishRound();
        }

        updateParticles(delta);
    }

    private void updateRunner(Runner r, float delta) {
        r.readDirection();

        if (Gdx.input.isKeyJustPressed(r.dash) && r.dashCooldown <= 0f && r.dir.len2() > 0.1f) {
            r.dashTimer = DASH_DURATION;
            r.dashCooldown = DASH_COOLDOWN;
            spawnDashTrail(r.pos.x, r.pos.y, TRAIL_COLORS[r.index]);
        }

        float speed = (r.dashTimer > 0f) ? DASH_SPEED : (isOnBoostPad(r.pos) ? BOOST_PAD_SPEED : PLAYER_SPEED);
        if (r == bombHolder) speed *= BOMB_HOLDER_BOOST;

        moveWithCollision(r.pos, r.dir.x * speed * delta, r.dir.y * speed * delta);
    }

    private void emitBombSparks() {
        if (MathUtils.randomBoolean(0.35f)) {
            Vector2 b = bombHolder.pos;
            addParticle(b.x + MathUtils.random(-8f, 8f), b.y + 18f,
                MathUtils.random(-30f, 30f), MathUtils.random(30f, 80f), 0.4f, Color.ORANGE);
        }
    }

    /** Si se tocan, los separa y (si no hay inmunidad) pasa la bomba. */
    private void handleContact() {
        Vector2 p1 = runners[0].pos;
        Vector2 p2 = runners[1].pos;
        float dist = p1.dst(p2);
        if (dist >= PLAYER_RADIUS * 2f) return;

        float overlap = (PLAYER_RADIUS * 2f) - dist;
        Vector2 pushDir = new Vector2(p2).sub(p1).nor();
        if (pushDir.len2() == 0) pushDir.set(1f, 0f);
        p1.sub(pushDir.x * overlap * 0.5f, pushDir.y * overlap * 0.5f);
        p2.add(pushDir.x * overlap * 0.5f, pushDir.y * overlap * 0.5f);

        if (transferCooldown <= 0f) {
            bombHolder = other(bombHolder);
            transferCooldown = TRANSFER_IMMUNITY;
            spawnTagShockwave(p1.x * 0.5f + p2.x * 0.5f, p1.y * 0.5f + p2.y * 0.5f);
        }
    }

    private boolean isOnBoostPad(Vector2 pos) {
        for (Rectangle pad : boostPads) {
            if (pad.contains(pos.x, pos.y)) return true;
        }
        return false;
    }

    /** Mueve por ejes separados: si un eje choca, el otro igual puede avanzar. */
    private void moveWithCollision(Vector2 pos, float dx, float dy) {
        float nextX = pos.x + dx;
        if (!hitsPillar(nextX, pos.y) && fitsInArena(nextX, ARENA_MIN_X, ARENA_MAX_X)) pos.x = nextX;

        float nextY = pos.y + dy;
        if (!hitsPillar(pos.x, nextY) && fitsInArena(nextY, ARENA_MIN_Y, ARENA_MAX_Y)) pos.y = nextY;
    }

    private boolean fitsInArena(float value, float min, float max) {
        return value - PLAYER_RADIUS >= min && value + PLAYER_RADIUS <= max;
    }

    private boolean hitsPillar(float x, float y) {
        for (Rectangle r : obstaculos) {
            // Distancia del centro del jugador al punto más cercano del pilar
            float distX = x - MathUtils.clamp(x, r.x, r.x + r.width);
            float distY = y - MathUtils.clamp(y, r.y, r.y + r.height);
            if (distX * distX + distY * distY < PLAYER_RADIUS * PLAYER_RADIUS) return true;
        }
        return false;
    }

    // ===== PARTÍCULAS =====
    private void addParticle(float x, float y, float vx, float vy, float life, Color color) {
        particles.add(new Particle(x, y, vx, vy, life, color));
    }

    private void spawnDashTrail(float x, float y, Color col) {
        for (int i = 0; i < 6; i++) {
            addParticle(x + MathUtils.random(-6f, 6f), y + MathUtils.random(-6f, 6f),
                MathUtils.random(-20f, 20f), MathUtils.random(-20f, 20f), 0.25f, col);
        }
    }

    private void spawnTagShockwave(float x, float y) {
        for (int i = 0; i < 18; i++) {
            float angle = i * (360f / 18f);
            addParticle(x, y, MathUtils.cosDeg(angle) * 140f, MathUtils.sinDeg(angle) * 140f, 0.45f, Color.YELLOW);
        }
    }

    private void spawnExplosion(float x, float y) {
        for (int i = 0; i < 60; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(60f, 300f);
            addParticle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.5f, 1.2f), MathUtils.randomBoolean() ? Color.ORANGE : Color.RED);
        }
    }

    private void updateParticles(float delta) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            if (p.life <= 0f) particles.remove(i);
        }
    }

    // ===== DIBUJO =====
    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();

        batch.draw(bgTex, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        for (Rectangle pad : boostPads) batch.draw(boostPadTex, pad.x, pad.y, pad.width, pad.height);
        for (Rectangle r : obstaculos) batch.draw(pillarTex, r.x, r.y, r.width, r.height);

        float half = PLAYER_SPRITE_SIZE / 2f;
        for (Runner r : runners) {
            batch.draw(r.sprite, r.pos.x - half, r.pos.y - half, PLAYER_SPRITE_SIZE, PLAYER_SPRITE_SIZE);
        }

        // Bomba flotando sobre el portador
        Vector2 bombPos = bombHolder.pos;
        float floatOffset = MathUtils.sin(bombPulseAnim) * 3f;
        batch.draw(bombTex, bombPos.x - BOMB_SIZE / 2f, bombPos.y + BOMB_OFFSET_Y + floatOffset, BOMB_SIZE, BOMB_SIZE);

        // HUD inferior: controles y recarga del dash
        font.getData().setScale(1.1f);
        for (Runner r : runners) {
            font.setColor(HUD_COLORS[r.index]);
            String dashStatus = (r.dashCooldown <= 0f) ? DASH_READY_TEXT[r.index] : String.format("DASH: %.1fs", r.dashCooldown);
            font.draw(batch, CONTROLS_TEXT[r.index] + dashStatus, HUD_X[r.index], 32f);
        }

        // Aviso superior de quién tiene la bomba
        font.getData().setScale(1.2f);
        font.setColor(HOLDER_COLORS[bombHolder.index]);
        font.draw(batch, "¡P" + (bombHolder.index + 1) + " TIENE LA BOMBA! ¡PASALA!", VIRTUAL_WIDTH / 2f - 135f, 465f);

        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        // Halo pulsante debajo del portador
        float pulse = 6f + MathUtils.sin(bombPulseAnim) * 4f;
        shapeRenderer.setColor(1f, 0.2f, 0f, 0.3f);
        shapeRenderer.circle(bombPos.x, bombPos.y, PLAYER_RADIUS + 10f + pulse);
        // Chispas y explosión
        for (Particle p : particles) {
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, p.life / p.maxLife);
            shapeRenderer.circle(p.x, p.y, 3.2f);
        }
        shapeRenderer.end();
    }

    // ===== RESULTADO =====
    @Override
    protected String determineRoundWinner() {
        Runner winner = other(bombHolder);
        if (winner.index == 0) p1GlobalWins += WIN_POINTS;
        else p2GlobalWins += WIN_POINTS;
        return "¡P" + (bombHolder.index + 1) + " EXPLOTO! ¡JUGADOR " + (winner.index + 1)
            + " GANA (" + WIN_POINTS + " PUNTOS)!";
    }

    @Override
    protected void onRoundReset() {
        resetArena();
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
        return "P" + (bombHolder.index + 1) + " detono con la bomba";
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : textures) t.dispose();
    }
}
