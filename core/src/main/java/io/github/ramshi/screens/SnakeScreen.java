package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class SnakeScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final float MOVE_INTERVAL = 0.098f;
    private static final int GRID = 14;
    private static final float TILE_SIZE = 16f;
    private static final float[] ARENA_X = {88f, 488f};
    private static final float ARENA_Y = 108f;
    private static final int START_LENGTH = 3;
    private static final int[] START_X = {4, GRID - 5};
    private static final int[] START_DIR = {3, 1};
    private static final int[][] KEYS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };

    private static final int[] DX = {0, -1, 0, 1};
    private static final int[] DY = {1, 0, -1, 0};
    private static final String[] DIR_NAMES = {"Up", "Left", "Down", "Right"};
    private static final int[] BODY_MASKS = {5, 10, 6, 3, 12, 9};
    private static final String[] BODY_SUFFIXES = {"", "_1", "_2", "_3", "_4", "_5"};

    private final List<Texture> textures = new ArrayList<>();
    private final Texture tile1 = load("snake/tiles/Tile1.png");
    private final Texture tile2 = load("snake/tiles/Tile2.png");
    private final Texture egg = load("snake/egg.png");
    private final Texture[][] heads = new Texture[2][4];
    private final Texture[][] tails = new Texture[2][4];
    private final Texture[][] bodies = new Texture[2][16];

    private final List<List<int[]>> snakes = new ArrayList<>();
    private final int[] dir = new int[2];
    private final int[] nextDir = new int[2];
    private final int[] grow = new int[2];
    private final int[] score = new int[2];
    private final int[] foodX = new int[2];
    private final int[] foodY = new int[2];
    private float moveTimer;

    public SnakeScreen(Game game) {
        super(game);
        for (int p = 0; p < 2; p++) {
            String n = "P" + (p + 1);
            for (int d = 0; d < 4; d++) {
                heads[p][d] = load("snake/head/Cabeza" + n + "_" + DIR_NAMES[d] + ".png");
                tails[p][d] = load("snake/tail/Cola" + n + "_" + DIR_NAMES[d] + ".png");
            }
            for (int i = 0; i < BODY_MASKS.length; i++) {
                bodies[p][BODY_MASKS[i]] = load("snake/Body/Body" + n + BODY_SUFFIXES[i] + ".png");
            }
            snakes.add(new ArrayList<int[]>());
            respawn(p);
        }
    }

    private Texture load(String path) {
        Texture t = new Texture(path);
        textures.add(t);
        return t;
    }

    @Override protected float getRoundDuration() { return ROUND_DURATION; }

    @Override
    protected void updateGame(float delta) {
        for (int p = 0; p < 2; p++) {
            for (int d = 0; d < 4; d++) {
                if (Gdx.input.isKeyJustPressed(KEYS[p][d]) && d != (dir[p] + 2) % 4) nextDir[p] = d;
            }
        }
        moveTimer += delta;
        if (moveTimer >= MOVE_INTERVAL) {
            moveTimer -= MOVE_INTERVAL;
            step(0);
            step(1);
        }
    }

    private void step(int p) {
        List<int[]> s = snakes.get(p);
        dir[p] = nextDir[p];
        int[] head = s.get(s.size() - 1);
        int x = head[0] + DX[dir[p]];
        int y = head[1] + DY[dir[p]];
        s.add(new int[]{x, y});
        if (grow[p] > 0) grow[p]--;
        else s.remove(0);

        if (x == foodX[p] && y == foodY[p]) {
            grow[p]++;
            score[p]++;
            placeFood(p);
        }
        boolean outside = x < 0 || y < 0 || x >= GRID || y >= GRID;
        if (outside || hitsItself(s)) respawn(p);
    }

    private boolean hitsItself(List<int[]> s) {
        int[] head = s.get(s.size() - 1);
        for (int i = 0; i < s.size() - 1; i++) {
            if (s.get(i)[0] == head[0] && s.get(i)[1] == head[1]) return true;
        }
        return false;
    }

    private void respawn(int p) {
        List<int[]> s = snakes.get(p);
        s.clear();
        dir[p] = nextDir[p] = START_DIR[p];
        for (int i = START_LENGTH - 1; i >= 0; i--) {
            s.add(new int[]{START_X[p] - DX[dir[p]] * i, GRID / 2 - DY[dir[p]] * i});
        }
        grow[p] = 0;
        score[p] = 0;
        placeFood(p);
    }

    private void placeFood(int p) {
        int attempts = 0;
        do {
            foodX[p] = MathUtils.random(GRID - 1);
            foodY[p] = MathUtils.random(GRID - 1);
        } while (occupies(p, foodX[p], foodY[p]) && ++attempts < 100);
    }

    private boolean occupies(int p, int x, int y) {
        for (int[] c : snakes.get(p)) {
            if (c[0] == x && c[1] == y) return true;
        }
        return false;
    }

    private int dirTo(int[] from, int[] to) {
        for (int d = 0; d < 4; d++) {
            if (from[0] + DX[d] == to[0] && from[1] + DY[d] == to[1]) return d;
        }
        return 0;
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        for (int p = 0; p < 2; p++) {
            for (int x = 0; x < GRID; x++) {
                for (int y = 0; y < GRID; y++) drawCell(batch, (x + y) % 2 == 0 ? tile1 : tile2, p, x, y);
            }
            drawCell(batch, egg, p, foodX[p], foodY[p]);

            List<int[]> s = snakes.get(p);
            int last = s.size() - 1;
            for (int i = 0; i <= last; i++) {
                int[] c = s.get(i);
                Texture t;
                if (i == last) t = heads[p][dir[p]];
                else if (i == 0) t = tails[p][dirTo(c, s.get(1))];
                else t = bodies[p][(1 << dirTo(c, s.get(i - 1))) | (1 << dirTo(c, s.get(i + 1)))];
                if (t != null) drawCell(batch, t, p, c[0], c[1]);
            }
        }
        batch.end();

        float size = GRID * TILE_SIZE;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
        for (float x : ARENA_X) shapeRenderer.rect(x - 1f, ARENA_Y - 1f, size + 2f, size + 2f);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(VIRTUAL_WIDTH / 2f, ARENA_Y, VIRTUAL_WIDTH / 2f, ARENA_Y + size);
        shapeRenderer.end();
    }

    private void drawCell(SpriteBatch batch, Texture t, int p, int x, int y) {
        batch.draw(t, ARENA_X[p] + x * TILE_SIZE, ARENA_Y + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    @Override
    protected String determineRoundWinner() {
        if (score[0] == score[1]) return "¡EMPATE EN LA RONDA!";
        int winner = (score[0] > score[1]) ? 0 : 1;
        int pts = (Math.abs(score[0] - score[1]) >= 2 || score[winner] >= 3) ? 2 : 1;
        if (winner == 0) p1GlobalWins += pts;
        else p2GlobalWins += pts;
        return "¡JUGADOR " + (winner + 1) + " GANA (" + pts + " " + (pts == 1 ? "PUNTO" : "PUNTOS") + ")!";
    }

    @Override
    protected void onRoundReset() {
        moveTimer = 0f;
        respawn(0);
        respawn(1);
    }

    @Override protected String getP1StatusText() { return "P1: " + score[0] + " pts (Wins: " + p1GlobalWins + ")"; }
    @Override protected String getP2StatusText() { return "P2: " + score[1] + " pts (Wins: " + p2GlobalWins + ")"; }
    @Override protected String getScoreSummary() { return "Puntaje de ronda: P1 (" + score[0] + ") vs P2 (" + score[1] + ")"; }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : textures) t.dispose();
    }
}
