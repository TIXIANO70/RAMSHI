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

/**
 * Pantalla principal del minijuego Snake en modo local 1v1 (Split-Screen).
 * Gestiona la resolución panorámica, dos arenas independientes para P1 y P2,
 * la captura de entradas por teclado y el renderizado del HUD y divisores.
 */
public class SnakeScreen implements Screen {

    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    // Dimensiones de cada arena en casillas de 16px (22 x 26 baldosas = 352px x 416px)
    public static final int ARENA_GRID_WIDTH = 22;
    public static final int ARENA_GRID_HEIGHT = 26;

    // Intervalo de tiempo por paso de simulación
    private static final float MOVE_INTERVAL = 0.12f;
    private float moveTimer = 0f;

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final InputHandler inputHandler;

    // Texturas compartidas del tablero
    private final Texture tile1;
    private final Texture tile2;

    // Arenas independientes para el modo Split-Screen
    private final SnakeArena arenaP1;
    private final SnakeArena arenaP2;

    /**
     * Constructor de SnakeScreen en modo 1v1 Split-Screen.
     *
     * @param game Instancia principal del juego.
     */
    public SnakeScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.inputHandler = new InputHandler();

        // Configuración de la cámara y el viewport panorámico (800x480)
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        // Carga de texturas compartidas para el fondo
        this.tile1 = new Texture("snake/tiles/Tile1.png");
        this.tile2 = new Texture("snake/tiles/Tile2.png");

        // Arena P1: Izquierda (X: 24, Y: 16)
        this.arenaP1 = new SnakeArena(1, 24f, 16f, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);

        // Arena P2: Derecha (X: 424, Y: 16)
        this.arenaP2 = new SnakeArena(2, 424f, 16f, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // 1. Capturar entradas de ambos jugadores de forma desacoplada
        arenaP1.setNextDirection(inputHandler.getP1Direction(arenaP1.getSnake().getHead().getDirection()));
        arenaP2.setNextDirection(inputHandler.getP2Direction(arenaP2.getSnake().getHead().getDirection()));

        // 2. Acumular tiempo para el tick de simulación
        moveTimer += delta;
        if (moveTimer >= MOVE_INTERVAL) {
            moveTimer -= MOVE_INTERVAL;
            arenaP1.tick();
            arenaP2.tick();
        }

        // Limpieza de pantalla con tono oscuro
        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 3. Dibujar arenas (baldosas, comida y serpiente de cada jugador)
        batch.begin();
        arenaP1.render(batch);
        arenaP2.render(batch);
        batch.end();

        // 4. Dibujar líneas divisorias y bordes estéticos
        drawBordersAndDivider();

        // 5. Dibujar HUD superior con puntajes
        drawHUD();
    }

    /**
     * Dibuja los marcos delimitadores de cada arena y la línea divisoria central.
     */
    private void drawBordersAndDivider() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);

        // Borde arena P1
        float arenaWidthPx = ARENA_GRID_WIDTH * Snake.TILE_SIZE;
        float arenaHeightPx = ARENA_GRID_HEIGHT * Snake.TILE_SIZE;
        shapeRenderer.rect(23f, 15f, arenaWidthPx + 2f, arenaHeightPx + 2f);

        // Borde arena P2
        shapeRenderer.rect(423f, 15f, arenaWidthPx + 2f, arenaHeightPx + 2f);

        // Línea central divisoria
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(VIRTUAL_WIDTH / 2f, 16f, VIRTUAL_WIDTH / 2f, 432f);

        shapeRenderer.end();
    }

    /**
     * Dibuja las etiquetas de los jugadores y sus marcadores actuales en el área superior.
     */
    private void drawHUD() {
        batch.begin();

        // Marcador P1 (Verde)
        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        font.getData().setScale(1.4f);
        font.draw(batch, "P1 (WASD): " + arenaP1.getScore(), 24f, 462f);

        // Indicador central 1v1
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.draw(batch, "SPLIT 1v1", 360f, 462f);

        // Marcador P2 (Azul/Celeste)
        font.setColor(0.3f, 0.7f, 1f, 1f);
        font.getData().setScale(1.4f);
        font.draw(batch, "P2 (FLECHAS): " + arenaP2.getScore(), 570f, 462f);

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
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        tile1.dispose();
        tile2.dispose();
        arenaP1.dispose();
        arenaP2.dispose();
    }
}

