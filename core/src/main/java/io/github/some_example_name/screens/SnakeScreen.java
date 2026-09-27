package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.entities.Snake;
import io.github.some_example_name.entities.Direction;
import io.github.some_example_name.utils.InputHandler;

/**
 * Pantalla principal del minijuego Snake.
 * Gestiona el viewport, el renderizado del tablero con baldosas verdes y las entidades del juego.
 */
public class SnakeScreen implements Screen {
    private final InputHandler inputHandler = new InputHandler();
    private Direction currentDirection = Direction.RIGHT;
    public static final float VIRTUAL_WIDTH = 640f;
    public static final float VIRTUAL_HEIGHT = 480f;
    public static final int GRID_WIDTH = (int) (VIRTUAL_WIDTH / Snake.TILE_SIZE);   // 40 columnas
    public static final int GRID_HEIGHT = (int) (VIRTUAL_HEIGHT / Snake.TILE_SIZE); // 30 filas

    private final Game game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    // Texturas para el tablero estilo ajedrez verde
    private final Texture tile1;
    private final Texture tile2;

    // Entidad de la serpiente
    private final Snake snake;

    /**
     * Constructor de SnakeScreen.
     *
     * @param game Instancia principal del juego para permitir transiciones entre pantallas.
     */
    public SnakeScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Configuración de la cámara y el viewport para mantener la relación de aspecto
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        // Carga de texturas del fondo (baldosas de pasto/verde)
        this.tile1 = new Texture("snake/tiles/Tile1.png");
        this.tile2 = new Texture("snake/tiles/Tile2.png");

        // Inicialización de la serpiente
        this.snake = new Snake();
    }

    @Override
    public void show() {
        // Inicializaciones al mostrarse la pantalla (por ahora no se requieren adicionales)
    }

    @Override
    public void render(float delta) {
        // Limpieza de pantalla con un tono verde oscuro base
        ScreenUtils.clear(0.12f, 0.35f, 0.12f, 1f);

        // Actualizar la matriz de proyección con la cámara del viewport
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // 1. Dibujar el fondo del tablero con baldosas alternadas
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Texture currentTile = ((x + y) % 2 == 0) ? tile1 : tile2;
                batch.draw(currentTile, x * Snake.TILE_SIZE, y * Snake.TILE_SIZE, Snake.TILE_SIZE, Snake.TILE_SIZE);
            }
        }

        // 2. Dibujar la serpiente estática sobre el tablero
        snake.render(batch);
        // Actualizar la dirección según las teclas presionadas
        currentDirection = inputHandler.getNewDirection(currentDirection);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Actualizar el viewport al cambiar el tamaño de la ventana
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
        // Liberar todos los recursos de texturas y batch
        batch.dispose();
        tile1.dispose();
        tile2.dispose();
        snake.dispose();
    }
}
