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
 * MINIJUEGO 1v1: DUELO DE TANQUES (TankDuelScreen)
 * Dos tanques combaten en una arena con obstáculos y paredes donde rebotan los láseres.
 * - Jugador 1: W/S (avanzar/retroceder), A/D (girar), ESPACIO (disparar), F (escudo).
 * - Jugador 2: Flechas (moverse/girar), ENTER (disparar), M (escudo).
 */
public class TankDuelScreen extends BaseMinigameScreen {

    // ===== AJUSTES =====
    private static final float ROUND_DURATION = 10f;     // duración de la ronda (s)
    private static final int WIN_POINTS = 1;             // puntos por ganar con más vida
    private static final int KO_POINTS = 2;              // puntos por destruir al rival

    private static final float TANK_SPEED = 185f;        // velocidad hacia adelante (px/s)
    private static final float REVERSE_FACTOR = 0.7f;    // marcha atrás = velocidad * factor
    private static final float TANK_TURN_SPEED = 200f;   // giro (grados/s)
    private static final int MAX_HP = 3;                 // vidas de cada tanque

    private static final float BULLET_SPEED = 460f;      // velocidad del láser (px/s)
    private static final float BULLET_LIFE = 3.5f;       // segundos que dura un láser
    private static final int MAX_BOUNCES = 2;            // rebotes de cada láser
    private static final float FIRE_COOLDOWN = 0.45f;    // espera entre disparos (s)
    private static final float TRIPLE_SPREAD = 18f;      // apertura del disparo triple (grados)

    private static final float SHIELD_DURATION = 2.5f;   // duración del escudo (s)
    private static final float SHIELD_COOLDOWN = 6.0f;   // recarga del escudo (s)
    private static final float SHIELD_SIZE = 64f;        // tamaño del sprite del escudo

    private static final float POWERUP_FIRST_DELAY = 3.0f; // primer power-up a los X s
    private static final float POWERUP_RESPAWN = 6.0f;     // reaparece X s después de tomarlo

    private static final float TANK_RADIUS = 22f;        // radio de colisión del tanque
    private static final float TANK_SPRITE_SIZE = 48f;   // tamaño del sprite del tanque
    private static final float TANK_START_X = 110f;      // distancia al borde donde arranca cada uno

    // Límites de la arena (800x480)
    private static final float ARENA_MIN_X = 40f;
    private static final float ARENA_MAX_X = VIRTUAL_WIDTH - 40f;
    private static final float ARENA_MIN_Y = 40f;
    private static final float ARENA_MAX_Y = VIRTUAL_HEIGHT - 40f;

    // Controles por jugador: {avanzar, retroceder, girar izq., girar der., disparar, escudo}
    private static final int[][] KEYS = {
        { Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE, Input.Keys.F },
        { Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.ENTER, Input.Keys.M }
    };
    private static final String[] CONTROLS_TEXT = { "  [WASD/ESPACIO]  ", "  [FLECHAS/ENTER]  " };
    private static final String[] SHIELD_READY_TEXT = { "[F] ESCUDO LISTO", "[M] ESCUDO LISTO" };
    private static final float[] HUD_X = { 45f, VIRTUAL_WIDTH - 425f };

    // Colores por jugador: {P1, P2}
    private static final Color[] FX_COLORS = { Color.PINK, Color.CYAN };
    private static final Color[] HUD_COLORS = { new Color(1f, 0.3f, 0.6f, 1f), new Color(0f, 0.85f, 1f, 1f) };

    // Barricadas
    private final Rectangle[] obstaculos = new Rectangle[] {
        new Rectangle(230f, 130f, 50f, 85f),
        new Rectangle(230f, 265f, 50f, 85f),
        new Rectangle(520f, 130f, 50f, 85f),
        new Rectangle(520f, 265f, 50f, 85f)
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

    private static class LaserBullet {
        float x, y, vx, vy;
        final Tank owner;
        int bouncesLeft = MAX_BOUNCES;
        float life = BULLET_LIFE;
        LaserBullet(float x, float y, float vx, float vy, Tank owner) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.owner = owner;
        }
    }

    /** Un tanque: posición, ángulo, vida, teclas, sprites y tiempos de recarga. */
    private static class Tank {
        final int index;              // 0 = P1, 1 = P2
        final int forward, back, turnLeft, turnRight, fire, shield;
        final Texture sprite, laser;
        final Vector2 pos = new Vector2();
        float angle;
        int hp;
        float fireCd, shieldTimer, shieldCd;
        boolean tripleShot;

        Tank(int index, Texture sprite, Texture laser) {
            this.index = index;
            this.sprite = sprite;
            this.laser = laser;
            int[] k = KEYS[index];
            forward = k[0]; back = k[1]; turnLeft = k[2]; turnRight = k[3]; fire = k[4]; shield = k[5];
        }

        void reset() {
            float x = (index == 0) ? TANK_START_X : VIRTUAL_WIDTH - TANK_START_X;
            pos.set(x, VIRTUAL_HEIGHT / 2f);
            angle = (index == 0) ? 0f : 180f;
            hp = MAX_HP;
            fireCd = 0f;
            shieldTimer = 0f;
            shieldCd = 0f;
            tripleShot = false;
        }

        void tickTimers(float delta) {
            if (fireCd > 0f) fireCd -= delta;
            if (shieldCd > 0f) shieldCd -= delta;
            if (shieldTimer > 0f) shieldTimer -= delta;
        }
    }

    // ===== ESTADO =====
    private final List<Texture> textures = new ArrayList<>();
    private final Texture bgTex;
    private final Texture crateTex;
    private final Texture shieldTex;
    private final Texture powerUpTex;

    private final Tank[] tanks;
    private final List<LaserBullet> bullets = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    // Power-up central
    private boolean powerUpActive = false;
    private float powerUpTimer = POWERUP_FIRST_DELAY;
    private int powerUpType = 0; // 0: disparo triple, 1: +1 vida, 2: escudo inmediato
    private float powerUpPulse = 0f;

    public TankDuelScreen(Game game) {
        super(game);
        bgTex = loadTexture("tanks/arena_bg.png", true);
        crateTex = loadTexture("tanks/crate.png", false);
        shieldTex = loadTexture("tanks/shield_bubble.png", false);
        powerUpTex = loadTexture("tanks/powerup.png", false);
        tanks = new Tank[] {
            new Tank(0, loadTexture("tanks/tank_p1.png", true), loadTexture("tanks/laser_p1.png", false)),
            new Tank(1, loadTexture("tanks/tank_p2.png", true), loadTexture("tanks/laser_p2.png", false))
        };
        resetDuel();
    }

    private Texture loadTexture(String path, boolean smooth) {
        Texture t = new Texture(path);
        if (smooth) t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        textures.add(t);
        return t;
    }

    private void resetDuel() {
        for (Tank t : tanks) t.reset();
        bullets.clear();
        particles.clear();
        powerUpActive = false;
        powerUpTimer = POWERUP_FIRST_DELAY;
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    // ===== LÓGICA =====
    @Override
    protected void updateGame(float delta) {
        powerUpPulse += delta * 6f;
        for (Tank t : tanks) t.tickTimers(delta);
        for (Tank t : tanks) updateTank(t, delta);
        updatePowerUp(delta);
        updateBullets(delta);
        updateParticles(delta);
    }

    private void updateTank(Tank t, float delta) {
        if (Gdx.input.isKeyPressed(t.turnLeft)) t.angle += TANK_TURN_SPEED * delta;
        if (Gdx.input.isKeyPressed(t.turnRight)) t.angle -= TANK_TURN_SPEED * delta;

        float move = 0f;
        if (Gdx.input.isKeyPressed(t.forward)) move += TANK_SPEED * delta;
        if (Gdx.input.isKeyPressed(t.back)) move -= TANK_SPEED * REVERSE_FACTOR * delta;
        if (move != 0f) {
            // Descomponer el avance en X e Y según el ángulo del tanque
            moveWithCollision(t.pos, MathUtils.cosDeg(t.angle) * move, MathUtils.sinDeg(t.angle) * move);
        }

        if (Gdx.input.isKeyJustPressed(t.fire) && t.fireCd <= 0f) {
            t.fireCd = FIRE_COOLDOWN;
            fireLaser(t);
            t.tripleShot = false;
        }

        if (Gdx.input.isKeyJustPressed(t.shield) && t.shieldCd <= 0f) {
            t.shieldTimer = SHIELD_DURATION;
            t.shieldCd = SHIELD_COOLDOWN;
            spawnSparks(t.pos.x, t.pos.y, FX_COLORS[t.index], 15);
        }
    }

    private void updatePowerUp(float delta) {
        if (!powerUpActive) {
            powerUpTimer -= delta;
            if (powerUpTimer <= 0f) {
                powerUpActive = true;
                powerUpType = MathUtils.random(0, 2);
            }
            return;
        }
        float cx = VIRTUAL_WIDTH / 2f;
        float cy = VIRTUAL_HEIGHT / 2f;
        for (Tank t : tanks) {
            if (t.pos.dst(cx, cy) < TANK_RADIUS + 16f) {
                if (powerUpType == 0) t.tripleShot = true;
                else if (powerUpType == 1) t.hp = Math.min(MAX_HP, t.hp + 1);
                else t.shieldTimer = SHIELD_DURATION + 1.0f;
                powerUpActive = false;
                powerUpTimer = POWERUP_RESPAWN;
                spawnSparks(cx, cy, Color.GREEN, 25);
                return;
            }
        }
    }

    private void updateBullets(float delta) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            LaserBullet b = bullets.get(i);
            b.life -= delta;
            b.x += b.vx * delta;
            b.y += b.vy * delta;

            Color col = FX_COLORS[b.owner.index];
            if (MathUtils.randomBoolean(0.35f)) addParticle(b.x, b.y, 0f, 0f, 0.2f, col); // rastro

            if (bounce(b)) {
                b.bouncesLeft--;
                spawnSparks(b.x, b.y, col, 8);
                if (b.bouncesLeft < 0) {
                    bullets.remove(i);
                    continue;
                }
            }

            Tank hit = null;
            for (Tank t : tanks) {
                if (b.owner != t && t.pos.dst(b.x, b.y) < TANK_RADIUS + 4f) { hit = t; break; }
            }
            if (hit != null) {
                bullets.remove(i);
                damage(hit, b);
            } else if (b.life <= 0f) {
                bullets.remove(i);
            }
        }
    }

    /** Invierte la velocidad si el láser toca una pared o un obstáculo. Devuelve true si rebotó. */
    private boolean bounce(LaserBullet b) {
        boolean bounced = false;
        if (b.x <= ARENA_MIN_X || b.x >= ARENA_MAX_X) { b.vx = -b.vx; bounced = true; }
        if (b.y <= ARENA_MIN_Y || b.y >= ARENA_MAX_Y) { b.vy = -b.vy; bounced = true; }

        for (Rectangle r : obstaculos) {
            if (r.contains(b.x, b.y)) {
                // Rebota según el lado del obstáculo más cercano
                float overlapL = Math.abs(b.x - r.x);
                float overlapR = Math.abs(b.x - (r.x + r.width));
                float overlapB = Math.abs(b.y - r.y);
                float overlapT = Math.abs(b.y - (r.y + r.height));
                float minOverlap = Math.min(Math.min(overlapL, overlapR), Math.min(overlapB, overlapT));
                if (minOverlap == overlapL || minOverlap == overlapR) b.vx = -b.vx;
                else b.vy = -b.vy;
                return true;
            }
        }
        return bounced;
    }

    private void damage(Tank t, LaserBullet b) {
        if (t.shieldTimer > 0f) {
            spawnSparks(b.x, b.y, Color.YELLOW, 18); // el escudo bloquea el impacto
            return;
        }
        t.hp--;
        spawnSparks(t.pos.x, t.pos.y, Color.RED, 25);
        if (t.hp <= 0) {
            spawnExplosion(t.pos.x, t.pos.y, Color.RED);
            finishRound(); // victoria inmediata por KO
        }
    }

    private void fireLaser(Tank t) {
        float muzzleX = t.pos.x + MathUtils.cosDeg(t.angle) * (TANK_RADIUS + 6f);
        float muzzleY = t.pos.y + MathUtils.sinDeg(t.angle) * (TANK_RADIUS + 6f);

        addBullet(t, muzzleX, muzzleY, t.angle);
        if (t.tripleShot) { // abanico: centro, +apertura y -apertura
            addBullet(t, muzzleX, muzzleY, t.angle + TRIPLE_SPREAD);
            addBullet(t, muzzleX, muzzleY, t.angle - TRIPLE_SPREAD);
        }
        spawnSparks(muzzleX, muzzleY, FX_COLORS[t.index], 8);
    }

    private void addBullet(Tank owner, float x, float y, float angle) {
        bullets.add(new LaserBullet(x, y, MathUtils.cosDeg(angle) * BULLET_SPEED, MathUtils.sinDeg(angle) * BULLET_SPEED, owner));
    }

    /** Mueve por ejes separados: si un eje choca, el otro igual puede avanzar. */
    private void moveWithCollision(Vector2 pos, float dx, float dy) {
        float nextX = pos.x + dx;
        if (!hitsObstacle(nextX, pos.y) && fitsInArena(nextX, ARENA_MIN_X, ARENA_MAX_X)) pos.x = nextX;

        float nextY = pos.y + dy;
        if (!hitsObstacle(pos.x, nextY) && fitsInArena(nextY, ARENA_MIN_Y, ARENA_MAX_Y)) pos.y = nextY;
    }

    private boolean fitsInArena(float value, float min, float max) {
        return value - TANK_RADIUS >= min && value + TANK_RADIUS <= max;
    }

    private boolean hitsObstacle(float x, float y) {
        for (Rectangle r : obstaculos) {
            // Distancia del centro del tanque al punto más cercano del obstáculo
            float distX = x - MathUtils.clamp(x, r.x, r.x + r.width);
            float distY = y - MathUtils.clamp(y, r.y, r.y + r.height);
            if (distX * distX + distY * distY < TANK_RADIUS * TANK_RADIUS) return true;
        }
        return false;
    }

    // ===== PARTÍCULAS =====
    private void addParticle(float x, float y, float vx, float vy, float life, Color color) {
        particles.add(new Particle(x, y, vx, vy, life, color));
    }

    private void spawnSparks(float x, float y, Color col, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(40f, 180f);
            addParticle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(0.2f, 0.45f), col);
        }
    }

    private void spawnExplosion(float x, float y, Color col) {
        for (int i = 0; i < 50; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(60f, 260f);
            addParticle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(0.5f, 1.1f), col);
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
        for (Rectangle r : obstaculos) batch.draw(crateTex, r.x, r.y, r.width, r.height);

        // Power-up central (late y gira)
        if (powerUpActive) {
            float scale = 1f + MathUtils.sin(powerUpPulse) * 0.15f;
            batch.draw(powerUpTex, VIRTUAL_WIDTH / 2f - 16f, VIRTUAL_HEIGHT / 2f - 16f, 16f, 16f, 32f, 32f,
                scale, scale, powerUpPulse * 15f, 0, 0, 32, 32, false, false);
        }

        // Láseres orientados según su dirección
        for (LaserBullet b : bullets) {
            float bAngle = MathUtils.atan2(b.vy, b.vx) * MathUtils.radDeg;
            batch.draw(b.owner.laser, b.x - 12f, b.y - 5f, 12f, 5f, 24f, 10f, 1f, 1f, bAngle, 0, 0, 24, 10, false, false);
        }

        // Tanques rotados sobre su centro
        float half = TANK_SPRITE_SIZE / 2f;
        for (Tank t : tanks) {
            batch.draw(t.sprite, t.pos.x - half, t.pos.y - half, half, half, TANK_SPRITE_SIZE, TANK_SPRITE_SIZE,
                1f, 1f, t.angle, 0, 0, 48, 48, false, false);
        }
        for (Tank t : tanks) {
            if (t.shieldTimer > 0f) {
                batch.draw(shieldTex, t.pos.x - SHIELD_SIZE / 2f, t.pos.y - SHIELD_SIZE / 2f, SHIELD_SIZE, SHIELD_SIZE);
            }
        }

        // HUD inferior: vida, controles, escudo y disparo triple
        font.getData().setScale(1.1f);
        for (Tank t : tanks) {
            font.setColor(HUD_COLORS[t.index]);
            String shieldStatus = (t.shieldCd <= 0f) ? SHIELD_READY_TEXT[t.index] : String.format("ESCUDO: %.1fs", t.shieldCd);
            String tripleText = t.tripleShot ? " | [TRIPLE ACTIVADO]" : "";
            font.draw(batch, "P" + (t.index + 1) + " HP: " + t.hp + "/" + MAX_HP + CONTROLS_TEXT[t.index]
                + shieldStatus + tripleText, HUD_X[t.index], 32f);
        }

        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        for (Particle p : particles) {
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, p.life / p.maxLife);
            shapeRenderer.circle(p.x, p.y, 3.2f);
        }
        shapeRenderer.end();
    }

    // ===== RESULTADO =====
    @Override
    protected String determineRoundWinner() {
        Tank p1 = tanks[0], p2 = tanks[1];
        if (p1.hp == p2.hp) return "¡EMPATE EN EL DUELO DE TANQUES!";

        Tank winner = (p1.hp > p2.hp) ? p1 : p2;
        Tank loser = (winner == p1) ? p2 : p1;
        int pts = (loser.hp <= 0) ? KO_POINTS : WIN_POINTS;
        if (winner == p1) p1GlobalWins += pts;
        else p2GlobalWins += pts;
        return "¡JUGADOR " + (winner.index + 1) + " GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
    }

    @Override
    protected void onRoundReset() {
        resetDuel();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: HP " + tanks[0].hp + " (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: HP " + tanks[1].hp + " (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "HP Final: P1 [" + tanks[0].hp + "]  vs  P2 [" + tanks[1].hp + "]";
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : textures) t.dispose();
    }
}
