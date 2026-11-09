package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.ramshi.entities.Direction;
import io.github.ramshi.entities.Snake;
import io.github.ramshi.entities.SnakeArena;
import io.github.ramshi.utils.InputHandler;

public class SnakeScreen extends BaseMinigameScreen {

    // ===== AJUSTES =====
    private static final float ROUND_DURATION = 10f;   // duración del minijuego (s)
    private static final float MOVE_INTERVAL = 0.12f;  // segundos por paso de la serpiente
    private static final int ARENA_GRID_WIDTH = 22;    // casillas de ancho de cada arena
    private static final int ARENA_GRID_HEIGHT = 26;   // casillas de alto de cada arena
    private static final float ARENA_P1_X = 24f;       // posición de la arena de P1 (px)
    private static final float ARENA_P2_X = 424f;      // posición de la arena de P2 (px)
    private static final float ARENA_Y = 16f;

    private final InputHandler inputHandler = new InputHandler();
    private final Texture tile1 = new Texture("snake/tiles/Tile1.png");
    private final Texture tile2 = new Texture("snake/tiles/Tile2.png");
    private final SnakeArena arenaP1;
    private final SnakeArena arenaP2;
    private float moveTimer = 0f;

    public SnakeScreen(Game game) {
        super(game);
        arenaP1 = new SnakeArena(1, ARENA_P1_X, ARENA_Y, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
        arenaP2 = new SnakeArena(2, ARENA_P2_X, ARENA_Y, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputHandler) Gdx.input.setInputProcessor(null);
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        Direction d1 = inputHandler.getP1Direction(arenaP1.getCurrentDirection());
        if (d1 != null) arenaP1.setNextDirection(d1);
        Direction d2 = inputHandler.getP2Direction(arenaP2.getCurrentDirection());
        if (d2 != null) arenaP2.setNextDirection(d2);

        moveTimer += delta;
        if (moveTimer >= MOVE_INTERVAL) {
            moveTimer -= MOVE_INTERVAL;
            arenaP1.tick();
            arenaP2.tick();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        arenaP1.render(batch);
        arenaP2.render(batch);
        batch.end();

        float w = ARENA_GRID_WIDTH * Snake.TILE_SIZE;
        float h = ARENA_GRID_HEIGHT * Snake.TILE_SIZE;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);
        shapeRenderer.rect(ARENA_P1_X - 1f, ARENA_Y - 1f, w + 2f, h + 2f);
        shapeRenderer.rect(ARENA_P2_X - 1f, ARENA_Y - 1f, w + 2f, h + 2f);
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(VIRTUAL_WIDTH / 2f, 16f, VIRTUAL_WIDTH / 2f, 432f);
        shapeRenderer.end();
    }

    @Override
    protected String determineRoundWinner() {
        int s1 = arenaP1.getScore();
        int s2 = arenaP2.getScore();
        if (s1 > s2) { p1GlobalWins++; return "¡JUGADOR 1 GANA LA RONDA!"; }
        if (s2 > s1) { p2GlobalWins++; return "¡JUGADOR 2 GANA LA RONDA!"; }
        return "¡EMPATE EN LA RONDA!";
    }

    @Override
    protected void onRoundReset() {
        moveTimer = 0f;
        inputHandler.reset();
        arenaP1.resetForNewRound();
        arenaP2.resetForNewRound();
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + arenaP1.getScore() + " pts (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + arenaP2.getScore() + " pts (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Puntaje de ronda: P1 (" + arenaP1.getScore() + ") vs P2 (" + arenaP2.getScore() + ")";
    }

    @Override
    public void dispose() {
        hide();
        super.dispose();
        tile1.dispose();
        tile2.dispose();
        arenaP1.dispose();
        arenaP2.dispose();
    }
}