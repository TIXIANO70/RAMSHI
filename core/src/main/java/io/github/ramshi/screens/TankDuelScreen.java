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
 * ====================================================================
 * MINIJUEGO 1v1: DUELO DE TANQUES NEÓN (TankDuelScreen)
 * ====================================================================
 * Dos tanques combaten en una arena cerrada con obstáculos y paredes reflectantes.
 * - Jugador 1: W/S (avanzar/retroceder), A/D (girar), ESPACIO (disparar), F (escudo).
 * - Jugador 2: Flechas (moverse/girar), ENTER (disparar), M (escudo).
 */
public class TankDuelScreen extends BaseMinigameScreen {

    // ====================================================================
    // 1. AJUSTES MODIFICABLES PARA EL EXAMEN (CAMBIAR AQUÍ EN VIVO)
    // ====================================================================
    // >> DURACIÓN DE LA RONDA: cambiar a 15f o 30f si piden otra duración
    private static final float ROUND_DURATION = 20f;

    // >> VELOCIDAD DE LOS TANQUES: píxeles por segundo (aumentar para más rápido, ej: 260f)
    private static final float TANK_SPEED = 185f;

    // >> VELOCIDAD DE GIRO: grados por segundo que rota el tanque (ej: 300f para giro instantáneo)
    private static final float TANK_TURN_SPEED = 200f;

    // >> VELOCIDAD DEL PROYECTIL LÁSER: píxeles por segundo (ej: 600f para disparo súper veloz)
    private static final float BULLET_SPEED = 460f;

    // >> MÁXIMO DE REBOTES DE CADA BALA EN PAREDES (ej: 4 para rebotes caóticos)
    private static final int MAX_BOUNCES = 2;

    // >> CADENCIA DE DISPARO: segundos de espera entre disparos (ej: 0.2f para disparar rápido)
    private static final float FIRE_COOLDOWN = 0.45f;

    // >> PUNTOS DE VIDA (HP) DE CADA TANQUE: ej: 1 para muerte súbita o 5 para partida larga
    private static final int MAX_HP = 3;

    // >> DURACIÓN DEL ESCUDO ACTIVO (en segundos)
    private static final float SHIELD_DURATION = 2.5f;

    // >> TIEMPO DE RECARGA DEL ESCUDO (en segundos)
    private static final float SHIELD_COOLDOWN = 6.0f;

    // >> TAMAÑO DE COLISIÓN Y SPRITES
    private static final float TANK_RADIUS = 22f;          // Radio de colisión circular del tanque
    private static final float TANK_SPRITE_SIZE = 48f;     // Ancho y alto del sprite del tanque

    // Límites de la arena (800x480)
    private static final float ARENA_MIN_X = 40f;
    private static final float ARENA_MAX_X = VIRTUAL_WIDTH - 40f;
    private static final float ARENA_MIN_Y = 40f;
    private static final float ARENA_MAX_Y = VIRTUAL_HEIGHT - 40f;

    // ====================================================================
    // 2. TEXTURAS Y ELEMENTOS GRÁFICOS
    // ====================================================================
    private final Texture bgTex;
    private final Texture tankP1Tex;
    private final Texture tankP2Tex;
    private final Texture laserP1Tex;
    private final Texture laserP2Tex;
    private final Texture crateTex;
    private final Texture shieldTex;
    private final Texture powerUpTex;

    // 4 barricadas defensivas en la arena
    private final Rectangle[] obstaculos = new Rectangle[] {
        new Rectangle(230f, 130f, 50f, 85f),
        new Rectangle(230f, 265f, 50f, 85f),
        new Rectangle(520f, 130f, 50f, 85f),
        new Rectangle(520f, 265f, 50f, 85f)
    };

    // ====================================================================
    // 3. ESTRUCTURAS INTERNAS (Balas y Partículas)
    // ====================================================================
    private static class LaserBullet {
        float x, y, vx, vy;
        int owner; // 1 = P1, 2 = P2
        int bouncesLeft = MAX_BOUNCES;
        float life = 3.5f;
        LaserBullet(float x, float y, float vx, float vy, int owner) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.owner = owner;
        }
    }
    private final List<LaserBullet> bullets = new ArrayList<>();

    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;
        Particle(float x, float y, float vx, float vy, float life, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life; this.color = new Color(color);
        }
    }
    private final List<Particle> particles = new ArrayList<>();

    // Estado Jugador 1
    private final Vector2 p1Pos = new Vector2();
    private float p1Angle = 0f;
    private int p1Hp = MAX_HP;
    private float p1FireCd = 0f;
    private float p1ShieldTimer = 0f;
    private float p1ShieldCd = 0f;
    private boolean p1TripleShot = false;

    // Estado Jugador 2
    private final Vector2 p2Pos = new Vector2();
    private float p2Angle = 180f;
    private int p2Hp = MAX_HP;
    private float p2FireCd = 0f;
    private float p2ShieldTimer = 0f;
    private float p2ShieldCd = 0f;
    private boolean p2TripleShot = false;

    // Power-Up central
    private boolean powerUpActive = false;
    private float powerUpTimer = 3.0f;
    private int powerUpType = 0; // 0: Triple Disparo, 1: +1 Vida, 2: Escudo Inmediato
    private float powerUpPulse = 0f;

    // ====================================================================
    // 4. CONSTRUCTOR
    // ====================================================================
    public TankDuelScreen(Game game) {
        super(game);

        // Carga de Texturas HD con filtrado bilineal (TextureFilter.Linear)
        bgTex = new Texture("tanks/arena_bg.png");
        tankP1Tex = new Texture("tanks/tank_p1.png");
        tankP2Tex = new Texture("tanks/tank_p2.png");
        laserP1Tex = new Texture("tanks/laser_p1.png");
        laserP2Tex = new Texture("tanks/laser_p2.png");
        crateTex = new Texture("tanks/crate.png");
        shieldTex = new Texture("tanks/shield_bubble.png");
        powerUpTex = new Texture("tanks/powerup.png");

        bgTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tankP1Tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        tankP2Tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        resetDuel();
    }

    private void resetDuel() {
        p1Pos.set(110f, VIRTUAL_HEIGHT / 2f);
        p1Angle = 0f;
        p1Hp = MAX_HP;
        p1FireCd = 0f;
        p1ShieldTimer = 0f;
        p1ShieldCd = 0f;
        p1TripleShot = false;

        p2Pos.set(VIRTUAL_WIDTH - 110f, VIRTUAL_HEIGHT / 2f);
        p2Angle = 180f;
        p2Hp = MAX_HP;
        p2FireCd = 0f;
        p2ShieldTimer = 0f;
        p2ShieldCd = 0f;
        p2TripleShot = false;

        bullets.clear();
        particles.clear();
        powerUpActive = false;
        powerUpTimer = 3.0f;
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    // ====================================================================
    // 5. ACTUALIZACIÓN DEL JUEGO POR FRAME (Game Loop)
    // ====================================================================
    @Override
    protected void updateGame(float delta) {
        powerUpPulse += delta * 6f;

        // >> 5.1 Descontar tiempos de recarga (Cooldowns)
        if (p1FireCd > 0f) p1FireCd -= delta;
        if (p2FireCd > 0f) p2FireCd -= delta;
        if (p1ShieldCd > 0f) p1ShieldCd -= delta;
        if (p2ShieldCd > 0f) p2ShieldCd -= delta;
        if (p1ShieldTimer > 0f) p1ShieldTimer -= delta;
        if (p2ShieldTimer > 0f) p2ShieldTimer -= delta;

        // >> 5.2 CONTROLES JUGADOR 1 (A/D rotan, W/S mueven, ESPACIO dispara, F escudo)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) p1Angle += TANK_TURN_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) p1Angle -= TANK_TURN_SPEED * delta;

        float p1Move = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) p1Move += TANK_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) p1Move -= TANK_SPEED * 0.7f * delta;

        if (p1Move != 0f) {
            // Trigonometría: descomponer velocidad en X e Y según el ángulo actual
            float dx = MathUtils.cosDeg(p1Angle) * p1Move;
            float dy = MathUtils.sinDeg(p1Angle) * p1Move;
            moveTankWithCollision(p1Pos, dx, dy);
        }

        // Disparo P1
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && p1FireCd <= 0f) {
            p1FireCd = FIRE_COOLDOWN;
            fireLaser(p1Pos.x, p1Pos.y, p1Angle, 1, p1TripleShot);
            p1TripleShot = false;
        }

        // Habilidad Escudo P1
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && p1ShieldCd <= 0f) {
            p1ShieldTimer = SHIELD_DURATION;
            p1ShieldCd = SHIELD_COOLDOWN;
            spawnSparks(p1Pos.x, p1Pos.y, Color.PINK, 15);
        }

        // >> 5.3 CONTROLES JUGADOR 2 (Flechas mueven/rotan, ENTER dispara, M escudo)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) p2Angle += TANK_TURN_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) p2Angle -= TANK_TURN_SPEED * delta;

        float p2Move = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) p2Move += TANK_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) p2Move -= TANK_SPEED * 0.7f * delta;

        if (p2Move != 0f) {
            float dx = MathUtils.cosDeg(p2Angle) * p2Move;
            float dy = MathUtils.sinDeg(p2Angle) * p2Move;
            moveTankWithCollision(p2Pos, dx, dy);
        }

        // Disparo P2
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && p2FireCd <= 0f) {
            p2FireCd = FIRE_COOLDOWN;
            fireLaser(p2Pos.x, p2Pos.y, p2Angle, 2, p2TripleShot);
            p2TripleShot = false;
        }

        // Habilidad Escudo P2
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) && p2ShieldCd <= 0f) {
            p2ShieldTimer = SHIELD_DURATION;
            p2ShieldCd = SHIELD_COOLDOWN;
            spawnSparks(p2Pos.x, p2Pos.y, Color.CYAN, 15);
        }

        // >> 5.4 POWER-UP CENTRAL (Aparece cada 6s)
        if (!powerUpActive) {
            powerUpTimer -= delta;
            if (powerUpTimer <= 0f) {
                powerUpActive = true;
                powerUpType = MathUtils.random(0, 2);
            }
        } else {
            float pUpX = VIRTUAL_WIDTH / 2f;
            float pUpY = VIRTUAL_HEIGHT / 2f;
            if (p1Pos.dst(pUpX, pUpY) < TANK_RADIUS + 16f) {
                applyPowerUp(1);
                powerUpActive = false;
                powerUpTimer = 6.0f;
                spawnSparks(pUpX, pUpY, Color.GREEN, 25);
            } else if (p2Pos.dst(pUpX, pUpY) < TANK_RADIUS + 16f) {
                applyPowerUp(2);
                powerUpActive = false;
                powerUpTimer = 6.0f;
                spawnSparks(pUpX, pUpY, Color.GREEN, 25);
            }
        }

        // >> 5.5 MOVER PROYECTILES LÁSER Y DETECTAR REBOTES / IMPACTOS
        for (int i = bullets.size() - 1; i >= 0; i--) {
            LaserBullet b = bullets.get(i);
            b.life -= delta;
            b.x += b.vx * delta;
            b.y += b.vy * delta;

            // Rastro de luz
            if (MathUtils.randomBoolean(0.35f)) {
                particles.add(new Particle(b.x, b.y, 0f, 0f, 0.2f, (b.owner == 1) ? Color.PINK : Color.CYAN));
            }

            // Rebote en paredes exteriores (invierte velocidad vx o vy)
            boolean bounced = false;
            if (b.x <= ARENA_MIN_X || b.x >= ARENA_MAX_X) {
                b.vx = -b.vx;
                bounced = true;
            }
            if (b.y <= ARENA_MIN_Y || b.y >= ARENA_MAX_Y) {
                b.vy = -b.vy;
                bounced = true;
            }

            // Rebote en obstáculos centrales
            for (Rectangle r : obstaculos) {
                if (r.contains(b.x, b.y)) {
                    float overlapL = Math.abs(b.x - r.x);
                    float overlapR = Math.abs(b.x - (r.x + r.width));
                    float overlapB = Math.abs(b.y - r.y);
                    float overlapT = Math.abs(b.y - (r.y + r.height));
                    float minOverlap = Math.min(Math.min(overlapL, overlapR), Math.min(overlapB, overlapT));
                    if (minOverlap == overlapL || minOverlap == overlapR) b.vx = -b.vx;
                    else b.vy = -b.vy;
                    bounced = true;
                    break;
                }
            }

            if (bounced) {
                b.bouncesLeft--;
                spawnSparks(b.x, b.y, (b.owner == 1) ? Color.PINK : Color.CYAN, 8);
                if (b.bouncesLeft < 0) {
                    bullets.remove(i);
                    continue;
                }
            }

            // Impacto en Tanque 1 (si no es el dueño de la bala)
            if (b.owner != 1 && p1Pos.dst(b.x, b.y) < TANK_RADIUS + 4f) {
                bullets.remove(i);
                if (p1ShieldTimer > 0f) {
                    spawnSparks(b.x, b.y, Color.YELLOW, 18); // El escudo bloquea el impacto
                } else {
                    p1Hp--;
                    spawnSparks(p1Pos.x, p1Pos.y, Color.RED, 25);
                    if (p1Hp <= 0) {
                        spawnExplosion(p1Pos.x, p1Pos.y, Color.RED);
                        finishRound(); // Victoria inmediata por KO
                    }
                }
                continue;
            }

            // Impacto en Tanque 2
            if (b.owner != 2 && p2Pos.dst(b.x, b.y) < TANK_RADIUS + 4f) {
                bullets.remove(i);
                if (p2ShieldTimer > 0f) {
                    spawnSparks(b.x, b.y, Color.YELLOW, 18);
                } else {
                    p2Hp--;
                    spawnSparks(p2Pos.x, p2Pos.y, Color.RED, 25);
                    if (p2Hp <= 0) {
                        spawnExplosion(p2Pos.x, p2Pos.y, Color.RED);
                        finishRound();
                    }
                }
                continue;
            }

            if (b.life <= 0f) bullets.remove(i);
        }

        updateParticles(delta);
    }

    // ====================================================================
    // 6. FUNCIONES AUXILIARES (Disparos, Físicas y Efectos)
    // ====================================================================
    private void fireLaser(float x, float y, float angle, int owner, boolean triple) {
        float muzzleX = x + MathUtils.cosDeg(angle) * (TANK_RADIUS + 6f);
        float muzzleY = y + MathUtils.sinDeg(angle) * (TANK_RADIUS + 6f);

        float vx = MathUtils.cosDeg(angle) * BULLET_SPEED;
        float vy = MathUtils.sinDeg(angle) * BULLET_SPEED;
        bullets.add(new LaserBullet(muzzleX, muzzleY, vx, vy, owner));

        // Disparo Triple en abanico (centro, +18° y -18°)
        if (triple) {
            float vx1 = MathUtils.cosDeg(angle + 18f) * BULLET_SPEED;
            float vy1 = MathUtils.sinDeg(angle + 18f) * BULLET_SPEED;
            bullets.add(new LaserBullet(muzzleX, muzzleY, vx1, vy1, owner));

            float vx2 = MathUtils.cosDeg(angle - 18f) * BULLET_SPEED;
            float vy2 = MathUtils.sinDeg(angle - 18f) * BULLET_SPEED;
            bullets.add(new LaserBullet(muzzleX, muzzleY, vx2, vy2, owner));
        }
        spawnSparks(muzzleX, muzzleY, (owner == 1) ? Color.PINK : Color.CYAN, 8);
    }

    private void applyPowerUp(int player) {
        if (player == 1) {
            if (powerUpType == 0) p1TripleShot = true;
            else if (powerUpType == 1) p1Hp = Math.min(MAX_HP, p1Hp + 1);
            else p1ShieldTimer = SHIELD_DURATION + 1.0f;
        } else {
            if (powerUpType == 0) p2TripleShot = true;
            else if (powerUpType == 1) p2Hp = Math.min(MAX_HP, p2Hp + 1);
            else p2ShieldTimer = SHIELD_DURATION + 1.0f;
        }
    }

    private void moveTankWithCollision(Vector2 pos, float dx, float dy) {
        float nextX = pos.x + dx;
        float nextY = pos.y + dy;

        // Comprobar obstáculos en X
        boolean hitX = false;
        for (Rectangle r : obstaculos) {
            if (circleIntersectsRect(nextX, pos.y, TANK_RADIUS, r)) {
                hitX = true;
                break;
            }
        }
        if (!hitX && nextX - TANK_RADIUS >= ARENA_MIN_X && nextX + TANK_RADIUS <= ARENA_MAX_X) {
            pos.x = nextX;
        }

        // Comprobar obstáculos en Y
        boolean hitY = false;
        for (Rectangle r : obstaculos) {
            if (circleIntersectsRect(pos.x, nextY, TANK_RADIUS, r)) {
                hitY = true;
                break;
            }
        }
        if (!hitY && nextY - TANK_RADIUS >= ARENA_MIN_Y && nextY + TANK_RADIUS <= ARENA_MAX_Y) {
            pos.y = nextY;
        }
    }

    private boolean circleIntersectsRect(float cx, float cy, float radius, Rectangle r) {
        float closestX = MathUtils.clamp(cx, r.x, r.x + r.width);
        float closestY = MathUtils.clamp(cy, r.y, r.y + r.height);
        float distX = cx - closestX;
        float distY = cy - closestY;
        return (distX * distX + distY * distY) < (radius * radius);
    }

    private void spawnSparks(float x, float y, Color col, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(40f, 180f);
            particles.add(new Particle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(0.2f, 0.45f), col));
        }
    }

    private void spawnExplosion(float x, float y, Color col) {
        for (int i = 0; i < 50; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(60f, 260f);
            particles.add(new Particle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(0.5f, 1.1f), col));
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

    // ====================================================================
    // 7. RENDERIZADO VISUAL
    // ====================================================================
    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();

        // Fondo de la arena
        batch.draw(bgTex, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Barricadas defensivas
        for (Rectangle r : obstaculos) {
            batch.draw(crateTex, r.x, r.y, r.width, r.height);
        }

        // Power-Up Central (escalado seno y rotación continua)
        if (powerUpActive) {
            float pUpX = VIRTUAL_WIDTH / 2f;
            float pUpY = VIRTUAL_HEIGHT / 2f;
            float scale = 1f + MathUtils.sin(powerUpPulse) * 0.15f;
            batch.draw(powerUpTex, pUpX - 16f, pUpY - 16f, 16f, 16f, 32f, 32f, scale, scale, powerUpPulse * 15f, 0, 0, 32, 32, false, false);
        }

        // Proyectiles láser orientados según su trayectoria angular
        for (LaserBullet b : bullets) {
            Texture lTex = (b.owner == 1) ? laserP1Tex : laserP2Tex;
            float bAngle = MathUtils.atan2(b.vy, b.vx) * MathUtils.radDeg;
            batch.draw(lTex, b.x - 12f, b.y - 5f, 12f, 5f, 24f, 10f, 1f, 1f, bAngle, 0, 0, 24, 10, false, false);
        }

        // Tanques: batch.draw con rotación en el centro (originX: 24f, originY: 24f)
        batch.draw(tankP1Tex, p1Pos.x - 24f, p1Pos.y - 24f, 24f, 24f, TANK_SPRITE_SIZE, TANK_SPRITE_SIZE, 1f, 1f, p1Angle, 0, 0, 48, 48, false, false);
        batch.draw(tankP2Tex, p2Pos.x - 24f, p2Pos.y - 24f, 24f, 24f, TANK_SPRITE_SIZE, TANK_SPRITE_SIZE, 1f, 1f, p2Angle, 0, 0, 48, 48, false, false);

        // Escudos activos
        if (p1ShieldTimer > 0f) batch.draw(shieldTex, p1Pos.x - 32f, p1Pos.y - 32f, 64f, 64f);
        if (p2ShieldTimer > 0f) batch.draw(shieldTex, p2Pos.x - 32f, p2Pos.y - 32f, 64f, 64f);

        // HUD con vidas y habilidades
        font.getData().setScale(1.1f);
        font.setColor(1f, 0.3f, 0.6f, 1f);
        String p1ShieldStatus = (p1ShieldCd <= 0f) ? "[F] ESCUDO LISTO" : String.format("ESCUDO: %.1fs", p1ShieldCd);
        String p1TripleText = p1TripleShot ? " | [TRIPLE ACTIVADO]" : "";
        font.draw(batch, "P1 HP: " + p1Hp + "/" + MAX_HP + "  [WASD/ESPACIO]  " + p1ShieldStatus + p1TripleText, 45f, 32f);

        font.setColor(0f, 0.85f, 1f, 1f);
        String p2ShieldStatus = (p2ShieldCd <= 0f) ? "[M] ESCUDO LISTO" : String.format("ESCUDO: %.1fs", p2ShieldCd);
        String p2TripleText = p2TripleShot ? " | [TRIPLE ACTIVADO]" : "";
        font.draw(batch, "P2 HP: " + p2Hp + "/" + MAX_HP + "  [FLECHAS/ENTER]  " + p2ShieldStatus + p2TripleText, VIRTUAL_WIDTH - 425f, 32f);

        batch.end();

        // Renderizado de partículas de fuego y chispas
        shapeRenderer.begin(ShapeType.Filled);
        for (Particle p : particles) {
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, p.life / p.maxLife);
            shapeRenderer.circle(p.x, p.y, 3.2f);
        }
        shapeRenderer.end();
    }

    // ====================================================================
    // 8. RESOLUCIÓN DE LA RONDA
    // ====================================================================
    @Override
    protected String determineRoundWinner() {
        if (p1Hp > p2Hp) {
            int pts = (p2Hp <= 0) ? 2 : 1;
            p1GlobalWins += pts;
            return "¡JUGADOR 1 GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
        } else if (p2Hp > p1Hp) {
            int pts = (p1Hp <= 0) ? 2 : 1;
            p2GlobalWins += pts;
            return "¡JUGADOR 2 GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
        } else {
            return "¡EMPATE EN EL DUELO DE TANQUES!";
        }
    }

    @Override
    protected void onRoundReset() {
        resetDuel();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: HP " + p1Hp + " (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: HP " + p2Hp + " (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "HP Final: P1 [" + p1Hp + "]  vs  P2 [" + p2Hp + "]";
    }

    // ====================================================================
    // 9. LIBERACIÓN DE MEMORIA NATIVA (VRAM)
    // ====================================================================
    @Override
    public void dispose() {
        super.dispose();
        if (bgTex != null) bgTex.dispose();
        if (tankP1Tex != null) tankP1Tex.dispose();
        if (tankP2Tex != null) tankP2Tex.dispose();
        if (laserP1Tex != null) laserP1Tex.dispose();
        if (laserP2Tex != null) laserP2Tex.dispose();
        if (crateTex != null) crateTex.dispose();
        if (shieldTex != null) shieldTex.dispose();
        if (powerUpTex != null) powerUpTex.dispose();
    }
}
