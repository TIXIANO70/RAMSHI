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
 * MINIJUEGO 1v1: LA PAPA CALIENTE EXPLOSIVA (BombTagScreen)
 * ====================================================================
 * Persecución frenética en arena cerrada con pilares y aceleradores de velocidad.
 * Uno inicia con la bomba nuclear en la cabeza (con mecha corta).
 * Al tocar al rival se la pasa. Quien tenga la bomba a los 0 segundos EXPLOTA.
 * - Jugador 1: WASD (moverse), ESPACIO (Turbo Dash).
 * - Jugador 2: Flechas (moverse), ENTER (Turbo Dash).
 */
public class BombTagScreen extends BaseMinigameScreen {

    // ====================================================================
    // 1. AJUSTES MODIFICABLES PARA EL EXAMEN (CAMBIAR AQUÍ EN VIVO)
    // ====================================================================
    // >> DURACIÓN DE LA RONDA: segundos de cuenta regresiva (cambiar a 15f o 30f si piden otra duración)
    private static final float ROUND_DURATION = 20f;

    // >> VELOCIDAD NORMAL DE LOS CORREDORES: píxeles por segundo (ej: 300f para más rápido)
    private static final float PLAYER_SPEED = 210f;

    // >> VELOCIDAD EXTREMA DURANTE EL DASH: píxeles por segundo (ej: 700f para un turbo brutal)
    private static final float DASH_SPEED = 480f;

    // >> DURACIÓN DEL IMPULSO DE DASH (en segundos): ej: 0.4f para un dash más prolongado
    private static final float DASH_DURATION = 0.28f;

    // >> TIEMPO DE RECARGA DEL DASH (en segundos): ej: 1.5f para poder dashear muy seguido
    private static final float DASH_COOLDOWN = 3.0f;

    // >> VELOCIDAD AL PISAR LAS BANDAS ACELERADORAS (Boost Pads): píxeles por segundo
    private static final float BOOST_PAD_SPEED = 340f;

    // >> TIEMPO DE INMUNIDAD TRAS PASAR LA BOMBA (en segundos): evita devolverla en el mismo milisegundo
    private static final float TRANSFER_IMMUNITY = 0.85f;

    // >> BONIFICACIÓN DE VELOCIDAD PARA EL PORTADOR DE LA BOMBA (+8% para balancear la persecución)
    private static final float BOMB_HOLDER_BOOST = 1.08f;

    // >> TAMAÑOS DE COLISIÓN Y SPRITES
    private static final float PLAYER_RADIUS = 18f;        // Radio de colisión circular del jugador
    private static final float PLAYER_SPRITE_SIZE = 40f;   // Ancho y alto del sprite del jugador (40x40)

    // Límites de la arena (800x480)
    private static final float ARENA_MIN_X = 50f;
    private static final float ARENA_MAX_X = VIRTUAL_WIDTH - 50f;
    private static final float ARENA_MIN_Y = 50f;
    private static final float ARENA_MAX_Y = VIRTUAL_HEIGHT - 50f;

    // ====================================================================
    // 2. TEXTURAS Y ELEMENTOS GRÁFICOS
    // ====================================================================
    private final Texture bgTex;
    private final Texture player1Tex;
    private final Texture player2Tex;
    private final Texture bombTex;
    private final Texture pillarTex;
    private final Texture boostPadTex;

    // 5 pilares defensivos de titanio
    private final Rectangle[] obstaculos = new Rectangle[] {
        new Rectangle(200f, 150f, 60f, 60f),
        new Rectangle(200f, 270f, 60f, 60f),
        new Rectangle(540f, 150f, 60f, 60f),
        new Rectangle(540f, 270f, 60f, 60f),
        new Rectangle(370f, 210f, 60f, 60f) // Pilar central
    };

    // 2 bandas aceleradoras de velocidad (Boost Pads)
    private final Rectangle[] boostPads = new Rectangle[] {
        new Rectangle(350f, 90f, 100f, 30f),
        new Rectangle(350f, 360f, 100f, 30f)
    };

    // ====================================================================
    // 3. ESTRUCTURAS INTERNAS (Partículas de Humo y Explosión)
    // ====================================================================
    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;
        Particle(float x, float y, float vx, float vy, float life, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life; this.color = new Color(color);
        }
    }
    private final List<Particle> particles = new ArrayList<>();

    // Estado de los jugadores
    private final Vector2 p1Pos = new Vector2();
    private final Vector2 p2Pos = new Vector2();
    private float p1DashTimer = 0f;
    private float p1DashCooldown = 0f;
    private float p2DashTimer = 0f;
    private float p2DashCooldown = 0f;

    // Estado de la bomba (1 = P1 tiene la bomba, 2 = P2 tiene la bomba)
    private int bombHolder;
    private float transferCooldown = 0f;
    private float bombPulseAnim = 0f;
    private boolean exploded = false;

    // ====================================================================
    // 4. CONSTRUCTOR
    // ====================================================================
    public BombTagScreen(Game game) {
        super(game);

        // Carga de Texturas HD con filtrado bilineal (TextureFilter.Linear)
        bgTex = new Texture("bombtag/arena_bg.png");
        player1Tex = new Texture("bombtag/player_p1.png");
        player2Tex = new Texture("bombtag/player_p2.png");
        bombTex = new Texture("bombtag/bomb.png");
        pillarTex = new Texture("bombtag/pillar.png");
        boostPadTex = new Texture("bombtag/boost_pad.png");

        bgTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        player1Tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        player2Tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        resetArena();
    }

    private void resetArena() {
        p1Pos.set(120f, VIRTUAL_HEIGHT / 2f);
        p2Pos.set(VIRTUAL_WIDTH - 120f, VIRTUAL_HEIGHT / 2f);
        p1DashTimer = 0f;
        p1DashCooldown = 0f;
        p2DashTimer = 0f;
        p2DashCooldown = 0f;
        transferCooldown = 1.0f;
        exploded = false;
        particles.clear();

        // Elige aleatoriamente quién empieza con la bomba
        bombHolder = MathUtils.randomBoolean() ? 1 : 2;
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
        bombPulseAnim += delta * 6f;

        // >> 5.1 Descontar tiempos de recarga (Cooldowns)
        if (transferCooldown > 0f) transferCooldown -= delta;
        if (p1DashCooldown > 0f) p1DashCooldown -= delta;
        if (p2DashCooldown > 0f) p2DashCooldown -= delta;
        if (p1DashTimer > 0f) p1DashTimer -= delta;
        if (p2DashTimer > 0f) p2DashTimer -= delta;

        // Si ya explotó en el segundo final, solo actualizar partículas
        if (exploded) {
            updateParticles(delta);
            return;
        }

        // >> 5.2 CONTROLES JUGADOR 1 (WASD para moverse, ESPACIO para Dash)
        Vector2 p1Dir = new Vector2(0f, 0f);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) p1Dir.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) p1Dir.y -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) p1Dir.x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) p1Dir.x += 1f;
        p1Dir.nor(); // Normalizar para evitar velocidad diagonal extra

        // Activar Dash P1
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && p1DashCooldown <= 0f && p1Dir.len2() > 0.1f) {
            p1DashTimer = DASH_DURATION;
            p1DashCooldown = DASH_COOLDOWN;
            spawnDashTrail(p1Pos.x, p1Pos.y, Color.PINK);
        }

        // Determinar velocidad actual de P1
        float p1Speed = (p1DashTimer > 0f) ? DASH_SPEED : (isOnBoostPad(p1Pos) ? BOOST_PAD_SPEED : PLAYER_SPEED);
        if (bombHolder == 1) p1Speed *= BOMB_HOLDER_BOOST; // Portador corre más rápido

        movePlayerWithCollision(p1Pos, p1Dir.x * p1Speed * delta, p1Dir.y * p1Speed * delta);

        // >> 5.3 CONTROLES JUGADOR 2 (Flechas para moverse, ENTER para Dash)
        Vector2 p2Dir = new Vector2(0f, 0f);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) p2Dir.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) p2Dir.y -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) p2Dir.x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) p2Dir.x += 1f;
        p2Dir.nor();

        // Activar Dash P2
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && p2DashCooldown <= 0f && p2Dir.len2() > 0.1f) {
            p2DashTimer = DASH_DURATION;
            p2DashCooldown = DASH_COOLDOWN;
            spawnDashTrail(p2Pos.x, p2Pos.y, Color.CYAN);
        }

        float p2Speed = (p2DashTimer > 0f) ? DASH_SPEED : (isOnBoostPad(p2Pos) ? BOOST_PAD_SPEED : PLAYER_SPEED);
        if (bombHolder == 2) p2Speed *= BOMB_HOLDER_BOOST;

        movePlayerWithCollision(p2Pos, p2Dir.x * p2Speed * delta, p2Dir.y * p2Speed * delta);

        // >> 5.4 CHISPAS DE LA BOMBA EN MOVIMIENTO
        Vector2 bombPos = (bombHolder == 1) ? p1Pos : p2Pos;
        if (MathUtils.randomBoolean(0.35f)) {
            particles.add(new Particle(
                bombPos.x + MathUtils.random(-8f, 8f),
                bombPos.y + 18f,
                MathUtils.random(-30f, 30f),
                MathUtils.random(30f, 80f),
                0.4f,
                Color.ORANGE
            ));
        }

        // >> 5.5 COLISIÓN ENTRE JUGADORES Y TRANSFERENCIA DE LA BOMBA
        float dist = p1Pos.dst(p2Pos);
        if (dist < PLAYER_RADIUS * 2f) {
            // Separación elástica para evitar que se encimen
            float overlap = (PLAYER_RADIUS * 2f) - dist;
            Vector2 pushDir = new Vector2(p2Pos).sub(p1Pos).nor();
            if (pushDir.len2() == 0) pushDir.set(1f, 0f);
            p1Pos.sub(pushDir.x * overlap * 0.5f, pushDir.y * overlap * 0.5f);
            p2Pos.add(pushDir.x * overlap * 0.5f, pushDir.y * overlap * 0.5f);

            // Si expiró la inmunidad, ¡la bomba cambia de portador!
            if (transferCooldown <= 0f) {
                bombHolder = (bombHolder == 1) ? 2 : 1;
                transferCooldown = TRANSFER_IMMUNITY;
                spawnTagShockwave(p1Pos.x * 0.5f + p2Pos.x * 0.5f, p1Pos.y * 0.5f + p2Pos.y * 0.5f);
            }
        }

        // >> 5.6 DETONACIÓN NUCLEAR AL SEGUNDO CERO
        if (timeRemaining <= 0.05f && !exploded) {
            exploded = true;
            Vector2 victimPos = (bombHolder == 1) ? p1Pos : p2Pos;
            spawnExplosion(victimPos.x, victimPos.y);
            finishRound(); // Cierra la ronda y declara ganador al sobreviviente
        }

        updateParticles(delta);
    }

    // ====================================================================
    // 6. FUNCIONES AUXILIARES (Movimiento, Colisiones y Efectos)
    // ====================================================================
    private boolean isOnBoostPad(Vector2 pos) {
        for (Rectangle pad : boostPads) {
            if (pad.contains(pos.x, pos.y)) return true;
        }
        return false;
    }

    private void movePlayerWithCollision(Vector2 pos, float dx, float dy) {
        float nextX = pos.x + dx;
        float nextY = pos.y + dy;

        // Comprobar colisión X contra los 5 pilares
        boolean hitX = false;
        for (Rectangle r : obstaculos) {
            if (circleIntersectsRect(nextX, pos.y, PLAYER_RADIUS, r)) {
                hitX = true;
                break;
            }
        }
        if (!hitX && nextX - PLAYER_RADIUS >= ARENA_MIN_X && nextX + PLAYER_RADIUS <= ARENA_MAX_X) {
            pos.x = nextX;
        }

        // Comprobar colisión Y contra los 5 pilares
        boolean hitY = false;
        for (Rectangle r : obstaculos) {
            if (circleIntersectsRect(pos.x, nextY, PLAYER_RADIUS, r)) {
                hitY = true;
                break;
            }
        }
        if (!hitY && nextY - PLAYER_RADIUS >= ARENA_MIN_Y && nextY + PLAYER_RADIUS <= ARENA_MAX_Y) {
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

    private void spawnDashTrail(float x, float y, Color col) {
        for (int i = 0; i < 6; i++) {
            particles.add(new Particle(x + MathUtils.random(-6f, 6f), y + MathUtils.random(-6f, 6f), MathUtils.random(-20f, 20f), MathUtils.random(-20f, 20f), 0.25f, col));
        }
    }

    private void spawnTagShockwave(float x, float y) {
        for (int i = 0; i < 18; i++) {
            float angle = i * (360f / 18f);
            particles.add(new Particle(x, y, MathUtils.cosDeg(angle) * 140f, MathUtils.sinDeg(angle) * 140f, 0.45f, Color.YELLOW));
        }
    }

    private void spawnExplosion(float x, float y) {
        for (int i = 0; i < 60; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(60f, 300f);
            particles.add(new Particle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed, MathUtils.random(0.5f, 1.2f), MathUtils.randomBoolean() ? Color.ORANGE : Color.RED));
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

        // Aceleradores de velocidad
        for (Rectangle pad : boostPads) {
            batch.draw(boostPadTex, pad.x, pad.y, pad.width, pad.height);
        }

        // Pilares de titanio
        for (Rectangle r : obstaculos) {
            batch.draw(pillarTex, r.x, r.y, r.width, r.height);
        }

        // Corredores P1 y P2 (Sprites 40x40)
        batch.draw(player1Tex, p1Pos.x - 20f, p1Pos.y - 20f, PLAYER_SPRITE_SIZE, PLAYER_SPRITE_SIZE);
        batch.draw(player2Tex, p2Pos.x - 20f, p2Pos.y - 20f, PLAYER_SPRITE_SIZE, PLAYER_SPRITE_SIZE);

        // Bomba activa flotando con oscilación vertical suave
        Vector2 bombPos = (bombHolder == 1) ? p1Pos : p2Pos;
        float floatOffset = MathUtils.sin(bombPulseAnim) * 3f;
        batch.draw(bombTex, bombPos.x - 18f, bombPos.y + 14f + floatOffset, 36f, 36f);

        // HUD inferior con recargas de Dash
        font.getData().setScale(1.1f);
        font.setColor(1f, 0.3f, 0.6f, 1f);
        String p1DashStatus = (p1DashCooldown <= 0f) ? "[ESPACIO] DASH LISTO" : String.format("DASH: %.1fs", p1DashCooldown);
        font.draw(batch, "P1: WASD | " + p1DashStatus, 55f, 32f);

        font.setColor(0f, 0.85f, 1f, 1f);
        String p2DashStatus = (p2DashCooldown <= 0f) ? "[ENTER] DASH LISTO" : String.format("DASH: %.1fs", p2DashCooldown);
        font.draw(batch, "P2: FLECHAS | " + p2DashStatus, VIRTUAL_WIDTH - 290f, 32f);

        // Indicador central superior
        font.getData().setScale(1.2f);
        if (bombHolder == 1) {
            font.setColor(1f, 0.35f, 0.2f, 1f);
            font.draw(batch, "¡P1 TIENE LA BOMBA! ¡PASALA!", VIRTUAL_WIDTH / 2f - 135f, 465f);
        } else {
            font.setColor(0f, 0.85f, 1f, 1f);
            font.draw(batch, "¡P2 TIENE LA BOMBA! ¡PASALA!", VIRTUAL_WIDTH / 2f - 135f, 465f);
        }

        batch.end();

        // Renderizado del halo de peligro y partículas con ShapeRenderer
        shapeRenderer.begin(ShapeType.Filled);

        // Halo pulsante debajo del portador de la bomba
        float pulse = 6f + MathUtils.sin(bombPulseAnim) * 4f;
        shapeRenderer.setColor(1f, 0.2f, 0f, 0.3f);
        shapeRenderer.circle(bombPos.x, bombPos.y, PLAYER_RADIUS + 10f + pulse);

        // Chispas de la mecha y explosión
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
        if (bombHolder == 1) {
            int pts = 2;
            p2GlobalWins += pts;
            return "¡P1 EXPLOTO! ¡JUGADOR 2 GANA (" + pts + " PUNTOS)!";
        } else {
            int pts = 2;
            p1GlobalWins += pts;
            return "¡P2 EXPLOTO! ¡JUGADOR 1 GANA (" + pts + " PUNTOS)!";
        }
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
        return (bombHolder == 1) ? "P1 detono con la bomba" : "P2 detono con la bomba";
    }

    // ====================================================================
    // 9. LIBERACIÓN DE MEMORIA NATIVA (VRAM)
    // ====================================================================
    @Override
    public void dispose() {
        super.dispose();
        if (bgTex != null) bgTex.dispose();
        if (player1Tex != null) player1Tex.dispose();
        if (player2Tex != null) player2Tex.dispose();
        if (bombTex != null) bombTex.dispose();
        if (pillarTex != null) pillarTex.dispose();
        if (boostPadTex != null) boostPadTex.dispose();
    }
}
