package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.entities.Snake;
import io.github.some_example_name.entities.SnakeArena;
import io.github.some_example_name.utils.InputHandler;


public class SnakeScreen implements Screen {

    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    public static final int ARENA_GRID_WIDTH = 22;
    public static final int ARENA_GRID_HEIGHT = 26;

    private static final float MOVE_INTERVAL = 0.12f;
    private float moveTimer = 0f;

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final InputHandler inputHandler;

    private final Texture tile1;
    private final Texture tile2;

    private final SnakeArena arenaP1;
    private final SnakeArena arenaP2;

    private static final float ROUND_DURATION = 30f;
    private float timeRemaining = ROUND_DURATION;
    private boolean roundOver = false;
    private String roundWinnerMessage = "";

    private int p1GlobalWins;
    private int p2GlobalWins;


    public SnakeScreen(Game game) {
        this(game, 0, 0);
    }

    public SnakeScreen(Game game, int p1Wins, int p2Wins) {
        this.game = game;
        this.p1GlobalWins = p1Wins;
        this.p2GlobalWins = p2Wins;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.inputHandler = new InputHandler();

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        this.tile1 = new Texture("snake/tiles/Tile1.png");
        this.tile2 = new Texture("snake/tiles/Tile2.png");

        this.arenaP1 = new SnakeArena(1, 24f, 16f, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);

        this.arenaP2 = new SnakeArena(2, 424f, 16f, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
    }

    @Override
    public void show() {
        com.badlogic.gdx.Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        if (!roundOver) {
            timeRemaining -= delta;
            if (timeRemaining <= 0f) {
                timeRemaining = 0f;
                finishRound();
            } else {
                io.github.some_example_name.entities.Direction p1Dir = inputHandler.getP1Direction(arenaP1.getCurrentDirection());
                if (p1Dir != null) {
                    arenaP1.setNextDirection(p1Dir);
                }

                io.github.some_example_name.entities.Direction p2Dir = inputHandler.getP2Direction(arenaP2.getCurrentDirection());
                if (p2Dir != null) {
                    arenaP2.setNextDirection(p2Dir);
                }

                moveTimer += delta;
                if (moveTimer >= MOVE_INTERVAL) {
                    moveTimer -= MOVE_INTERVAL;
                    arenaP1.tick();
                    arenaP2.tick();
                }
            }
        } else {
            if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)
                || com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                startNewRound();
            }
        }



        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        arenaP1.render(batch);
        arenaP2.render(batch);
        batch.end();

        drawBordersAndDivider();

        drawHUD();

        if (roundOver) {
            drawRoundOverOverlay();
        }
    }


    private void finishRound() {
        roundOver = true;
        int s1 = arenaP1.getScore();
        int s2 = arenaP2.getScore();

        if (s1 > s2) {
            p1GlobalWins++;
            roundWinnerMessage = "¡JUGADOR 1 GANA LA RONDA!";
        } else if (s2 > s1) {
            p2GlobalWins++;
            roundWinnerMessage = "¡JUGADOR 2 GANA LA RONDA!";
        } else {
            roundWinnerMessage = "¡EMPATE EN LA RONDA!";
        }
    }


    private void startNewRound() {
        timeRemaining = ROUND_DURATION;
        roundOver = false;
        roundWinnerMessage = "";
        inputHandler.reset();
        arenaP1.resetForNewRound();
        arenaP2.resetForNewRound();
    }



    private void drawBordersAndDivider() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);

        float arenaWidthPx = ARENA_GRID_WIDTH * Snake.TILE_SIZE;
        float arenaHeightPx = ARENA_GRID_HEIGHT * Snake.TILE_SIZE;
        shapeRenderer.rect(23f, 15f, arenaWidthPx + 2f, arenaHeightPx + 2f);

        shapeRenderer.rect(423f, 15f, arenaWidthPx + 2f, arenaHeightPx + 2f);

        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(VIRTUAL_WIDTH / 2f, 16f, VIRTUAL_WIDTH / 2f, 432f);

        shapeRenderer.end();
    }


    private void drawHUD() {
        batch.begin();

        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        font.getData().setScale(1.2f);
        font.draw(batch, "P1: " + arenaP1.getScore() + " pts (Wins: " + p1GlobalWins + ")", 24f, 462f);

        int seconds = (int) Math.ceil(timeRemaining);
        if (timeRemaining <= 5f && !roundOver) {
            font.setColor(1f, 0.3f, 0.3f, 1f); // Alerta roja en los últimos 5s
        } else {
            font.setColor(1f, 0.9f, 0.2f, 1f); // Amarillo normal
        }
        font.getData().setScale(1.4f);
        font.draw(batch, "TIEMPO: " + seconds + "s", 340f, 462f);

        font.setColor(0.3f, 0.7f, 1f, 1f);
        font.getData().setScale(1.2f);
        font.draw(batch, "P2: " + arenaP2.getScore() + " pts (Wins: " + p2GlobalWins + ")", 590f, 462f);

        batch.end();
    }


    private void drawRoundOverOverlay() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.92f);
        shapeRenderer.rect(160f, 130f, 480f, 220f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.84f, 0f, 1f);
        shapeRenderer.rect(160f, 130f, 480f, 220f);
        shapeRenderer.end();

        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, roundWinnerMessage);
        font.draw(batch, titleLayout, (VIRTUAL_WIDTH - titleLayout.width) / 2f, 320f);

        font.setColor(0.85f, 0.85f, 0.85f, 1f);
        font.getData().setScale(1.1f);
        String scoreSummary = "Puntaje de ronda: P1 (" + arenaP1.getScore() + ") vs P2 (" + arenaP2.getScore() + ")";
        com.badlogic.gdx.graphics.g2d.GlyphLayout scoreLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, scoreSummary);
        font.draw(batch, scoreLayout, (VIRTUAL_WIDTH - scoreLayout.width) / 2f, 275f);

        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(1.2f);
        String globalWinsSummary = "VICTORIAS TOTALES:  P1 [" + p1GlobalWins + "]  -  P2 [" + p2GlobalWins + "]";
        com.badlogic.gdx.graphics.g2d.GlyphLayout winsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, globalWinsSummary);
        font.draw(batch, winsLayout, (VIRTUAL_WIDTH - winsLayout.width) / 2f, 235f);

        font.setColor(0.7f, 1f, 0.7f, 1f);
        font.getData().setScale(1.0f);
        String instruction = "[ ESPACIO ] Siguiente Ronda       [ ESC ] Menu Principal";
        com.badlogic.gdx.graphics.g2d.GlyphLayout instrLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, instruction);
        font.draw(batch, instrLayout, (VIRTUAL_WIDTH - instrLayout.width) / 2f, 175f);

        batch.end();
    }



    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (com.badlogic.gdx.Gdx.input.getInputProcessor() == inputHandler) {
            com.badlogic.gdx.Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (com.badlogic.gdx.Gdx.input.getInputProcessor() == inputHandler) {
            com.badlogic.gdx.Gdx.input.setInputProcessor(null);
        }
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        tile1.dispose();
        tile2.dispose();
        arenaP1.dispose();
        arenaP2.dispose();
    }

}

