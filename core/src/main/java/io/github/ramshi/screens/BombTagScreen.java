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

public class BombTagScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final int WIN_POINTS = 2;
    private static final float PLAYER_SPEED = 210f;
    private static final float DASH_SPEED = 480f;
    private static final float DASH_DURATION = 0.28f;
    private static final float DASH_COOLDOWN = 3.0f;
    private static final float BOMB_HOLDER_BOOST = 1.08f;
    private static final float TRANSFER_IMMUNITY = 0.85f;
    private static final float START_IMMUNITY = 1.0f;
    private static final float DETONATION_TIME = 0.05f;
    private static final float PLAYER_RADIUS = 18f;
    private static final float PLAYER_SIZE = 40f;
    private static final float START_X = 120f;
    private static final float BOMB_SIZE = 36f;
    private static final float MARGIN = 50f;
    private static final int[][] KEYS = {
        { Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE },
        { Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.ENTER }
    };
    private static final String[] HUD_TEXT = { "P1: WASD | [ESPACIO] ", "P2: FLECHAS | [ENTER] " };
    private static final float[] HUD_X = { 55f, VIRTUAL_WIDTH - 290f };
    private static final Color[] HUD_COLORS = { new Color(1f, 0.3f, 0.6f, 1f), new Color(0f, 0.85f, 1f, 1f) };

    private final Rectangle[] pillars = {
        new Rectangle(200f, 150f, 60f, 60f), new Rectangle(200f, 270f, 60f, 60f),
        new Rectangle(540f, 150f, 60f, 60f), new Rectangle(540f, 270f, 60f, 60f),
        new Rectangle(370f, 210f, 60f, 60f)
    };

    private static class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;
    }

    private static class Runner {
        int index;
        int[] keys;
        Texture sprite;
        final Vector2 pos = new Vector2(), dir = new Vector2();
        float dashTimer, dashCooldown;
    }

    private final List<Texture> textures = new ArrayList<>();
    private final Texture bgTex = loadTexture("bombtag/arena_bg.png", true);
    private final Texture bombTex = loadTexture("bombtag/bomb.png", false);
    private final Texture pillarTex = loadTexture("bombtag/pillar.png", false);
    private final Runner[] runners = { new Runner(), new Runner() };
    private final List<Particle> particles = new ArrayList<>();
    private Runner bombHolder;
    private float transferCooldown, bombPulse;

    public BombTagScreen(Game game) {
        super(game);
        for (int i = 0; i < 2; i++) {
            runners[i].index = i;
            runners[i].keys = KEYS[i];
            runners[i].sprite = loadTexture("bombtag/player_p" + (i + 1) + ".png", true);
        }
        onRoundReset();
    }

    private Texture loadTexture(String path, boolean smooth) {
        Texture t = new Texture(path);
        if (smooth) t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        textures.add(t);
        return t;
    }

    @Override
    protected void onRoundReset() {
        for (Runner r : runners) {
            r.pos.set(r.index == 0 ? START_X : VIRTUAL_WIDTH - START_X, VIRTUAL_HEIGHT / 2f);
            r.dashTimer = r.dashCooldown = 0f;
        }
        transferCooldown = START_IMMUNITY;
        particles.clear();
        bombHolder = runners[MathUtils.random(1)];
    }

    @Override protected float getRoundDuration() { return ROUND_DURATION; }

    @Override
    protected void updateGame(float delta) {
        bombPulse += delta * 6f;
        transferCooldown -= delta;
        for (Runner r : runners) updateRunner(r, delta);
        handleContact();

        if (MathUtils.randomBoolean(0.35f)) {
            addParticle(bombHolder.pos.x + MathUtils.random(-8f, 8f), bombHolder.pos.y + 18f,
                MathUtils.random(-30f, 30f), MathUtils.random(30f, 80f), 0.4f, Color.ORANGE);
        }
        if (timeRemaining <= DETONATION_TIME) {
            burst(bombHolder.pos.x, bombHolder.pos.y, Color.RED, 60, 300f, 1.2f);
            finishRound();
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            if (p.life <= 0f) particles.remove(i);
        }
    }

    private void updateRunner(Runner r, float delta) {
        r.dashTimer -= delta;
        r.dashCooldown -= delta;

        r.dir.set(0f, 0f);
        if (Gdx.input.isKeyPressed(r.keys[0])) r.dir.y += 1f;
        if (Gdx.input.isKeyPressed(r.keys[1])) r.dir.y -= 1f;
        if (Gdx.input.isKeyPressed(r.keys[2])) r.dir.x -= 1f;
        if (Gdx.input.isKeyPressed(r.keys[3])) r.dir.x += 1f;
        r.dir.nor();

        if (Gdx.input.isKeyJustPressed(r.keys[4]) && r.dashCooldown <= 0f && !r.dir.isZero()) {
            r.dashTimer = DASH_DURATION;
            r.dashCooldown = DASH_COOLDOWN;
        }

        float speed = (r.dashTimer > 0f) ? DASH_SPEED : PLAYER_SPEED;
        if (r == bombHolder) speed *= BOMB_HOLDER_BOOST;

        float nextX = r.pos.x + r.dir.x * speed * delta;
        if (fits(nextX, VIRTUAL_WIDTH) && !hitsPillar(nextX, r.pos.y)) r.pos.x = nextX;
        float nextY = r.pos.y + r.dir.y * speed * delta;
        if (fits(nextY, VIRTUAL_HEIGHT) && !hitsPillar(r.pos.x, nextY)) r.pos.y = nextY;
    }

    private void handleContact() {
        Vector2 p1 = runners[0].pos, p2 = runners[1].pos;
        float overlap = PLAYER_RADIUS * 2f - p1.dst(p2);
        if (overlap <= 0f) return;

        Vector2 push = new Vector2(p2).sub(p1).nor();
        if (push.isZero()) push.set(1f, 0f);
        p1.mulAdd(push, -overlap / 2f);
        p2.mulAdd(push, overlap / 2f);

        if (transferCooldown <= 0f) {
            bombHolder = (bombHolder == runners[0]) ? runners[1] : runners[0];
            transferCooldown = TRANSFER_IMMUNITY;
            burst((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f, Color.YELLOW, 18, 140f, 0.45f);
        }
    }

    private boolean fits(float value, float size) {
        return value - PLAYER_RADIUS >= MARGIN && value + PLAYER_RADIUS <= size - MARGIN;
    }

    private boolean hitsPillar(float x, float y) {
        for (Rectangle r : pillars) {
            float dx = x - MathUtils.clamp(x, r.x, r.x + r.width);
            float dy = y - MathUtils.clamp(y, r.y, r.y + r.height);
            if (dx * dx + dy * dy < PLAYER_RADIUS * PLAYER_RADIUS) return true;
        }
        return false;
    }

    private void addParticle(float x, float y, float vx, float vy, float life, Color color) {
        Particle p = new Particle();
        p.x = x; p.y = y; p.vx = vx; p.vy = vy;
        p.life = p.maxLife = life;
        p.color = color;
        particles.add(p);
    }

    private void burst(float x, float y, Color color, int count, float maxSpeed, float maxLife) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(360f), speed = MathUtils.random(maxSpeed);
            addParticle(x, y, MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.2f, maxLife), color);
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        batch.draw(bgTex, 0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        for (Rectangle r : pillars) batch.draw(pillarTex, r.x, r.y, r.width, r.height);

        font.getData().setScale(1.1f);
        for (Runner r : runners) {
            batch.draw(r.sprite, r.pos.x - PLAYER_SIZE / 2f, r.pos.y - PLAYER_SIZE / 2f, PLAYER_SIZE, PLAYER_SIZE);
            String dash = (r.dashCooldown <= 0f) ? "DASH LISTO" : String.format("DASH: %.1fs", r.dashCooldown);
            font.setColor(HUD_COLORS[r.index]);
            font.draw(batch, HUD_TEXT[r.index] + dash, HUD_X[r.index], 32f);
        }

        Vector2 bomb = bombHolder.pos;
        batch.draw(bombTex, bomb.x - BOMB_SIZE / 2f, bomb.y + 14f + MathUtils.sin(bombPulse) * 3f, BOMB_SIZE, BOMB_SIZE);
        font.getData().setScale(1.2f);
        font.setColor(HUD_COLORS[bombHolder.index]);
        font.draw(batch, "¡P" + (bombHolder.index + 1) + " TIENE LA BOMBA! ¡PASALA!", VIRTUAL_WIDTH / 2f - 135f, 465f);
        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        for (Particle p : particles) {
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, p.life / p.maxLife);
            shapeRenderer.circle(p.x, p.y, 3.2f);
        }
        shapeRenderer.end();
    }

    @Override
    protected String determineRoundWinner() {
        int loser = bombHolder.index + 1, winner = 3 - loser;
        if (winner == 1) p1GlobalWins += WIN_POINTS;
        else p2GlobalWins += WIN_POINTS;
        return "¡P" + loser + " EXPLOTO! ¡JUGADOR " + winner + " GANA (" + WIN_POINTS + " PUNTOS)!";
    }

    @Override protected String getP1StatusText() { return "P1 (Wins: " + p1GlobalWins + ")"; }
    @Override protected String getP2StatusText() { return "P2 (Wins: " + p2GlobalWins + ")"; }
    @Override protected String getScoreSummary() { return "P" + (bombHolder.index + 1) + " detono con la bomba"; }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : textures) t.dispose();
    }
}
