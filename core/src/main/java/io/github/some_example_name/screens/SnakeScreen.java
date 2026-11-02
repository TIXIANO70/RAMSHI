package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.entities.Direction;
import io.github.some_example_name.entities.Snake;
import io.github.some_example_name.entities.SnakeArena;
import io.github.some_example_name.utils.InputHandler;

/**
 * Pantalla del minijuego Snake en modo local 1v1 (Split-Screen).
 * Hereda de BaseMinigameScreen toda la infraestructura común de la sesión (HUD, timer,
 * victorias globales y ciclo de vida), implementando exclusivamente las mecánicas de Snake:
 * simulación por ticks, renderizado de las dos arenas y captura de entradas (WASD y Flechas).
 */
public class SnakeScreen extends BaseMinigameScreen {

    // Dimensiones optimizadas de cada arena en casillas de 16px (17 x 20 = 272px x 320px)
    // Representa un ~40% menos de pantalla respecto al original para mayor dinamismo
    public static final int ARENA_GRID_WIDTH = 17;
    public static final int ARENA_GRID_HEIGHT = 20;

    // Intervalo de tiempo por paso de simulación (tick)
    private static final float MOVE_INTERVAL = 0.12f;
    private float moveTimer = 0f;

    private final InputHandler inputHandler;

    // Texturas compartidas del tablero de baldosas
    private final Texture tile1;
    private final Texture tile2;

    // Arenas independientes para el modo Split-Screen
    private final SnakeArena arenaP1;
    private final SnakeArena arenaP2;

    /**
     * Constructor por defecto que inicia la sesión de Snake con victorias en cero.
     *
     * @param game Instancia principal del juego.
     */
    public SnakeScreen(Game game) {
        this(game, 0, 0);
    }

    /**
     * Constructor parametrizado que conserva las victorias globales acumuladas.
     *
     * @param game    Instancia principal del juego.
     * @param p1Wins  Victorias acumuladas del Jugador 1.
     * @param p2Wins  Victorias acumuladas del Jugador 2.
     */
    public SnakeScreen(Game game, int p1Wins, int p2Wins) {
        super(game, p1Wins, p2Wins);

        this.inputHandler = new InputHandler();

        // Carga de texturas compartidas para el fondo
        this.tile1 = new Texture("snake/tiles/Tile1.png");
        this.tile2 = new Texture("snake/tiles/Tile2.png");

        // Arena P1: Centrada en la mitad izquierda (X: 64, Y: 56)
        this.arenaP1 = new SnakeArena(1, 64f, 56f, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);

        // Arena P2: Centrada en la mitad derecha (X: 464, Y: 56)
        this.arenaP2 = new SnakeArena(2, 464f, 56f, ARENA_GRID_WIDTH, ARENA_GRID_HEIGHT, tile1, tile2);
    }

    /**
     * Registra el InputHandler personalizado para captura de WASD (P1) y Flechas (P2).
     */
    @Override
    protected void registerInputProcessor() {
        Gdx.input.setInputProcessor(inputHandler);
    }

    /**
     * Actualiza la simulación de Snake: procesa entradas pendientes y ejecuta ticks de movimiento.
     *
     * @param delta Tiempo transcurrido desde el último cuadro en segundos.
     */
    @Override
    protected void updateMinigame(float delta) {
        // 1. Capturar entradas de ambos jugadores
        Direction p1Dir = inputHandler.getP1Direction(arenaP1.getCurrentDirection());
        if (p1Dir != null) {
            arenaP1.setNextDirection(p1Dir);
        }

        Direction p2Dir = inputHandler.getP2Direction(arenaP2.getCurrentDirection());
        if (p2Dir != null) {
            arenaP2.setNextDirection(p2Dir);
        }

        // 2. Acumular tiempo para el tick de simulación (0.12s)
        moveTimer += delta;
        if (moveTimer >= MOVE_INTERVAL) {
            moveTimer -= MOVE_INTERVAL;
            arenaP1.tick();
            arenaP2.tick();
        }
    }

    /**
     * Renderiza las baldosas, serpientes, comida y los marcos divisores de ambas arenas.
     *
     * @param batch         Instancia de SpriteBatch compartida configurada con la cámara.
     * @param shapeRenderer Instancia de ShapeRenderer compartida configurada con la cámara.
     */
    @Override
    protected void renderMinigame(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // 1. Dibujar arenas (baldosas, comida y serpientes)
        batch.begin();
        arenaP1.render(batch);
        arenaP2.render(batch);
        batch.end();

        // 2. Dibujar marcos delimitadores y línea divisoria central
        drawBordersAndDivider(shapeRenderer);
    }

    /**
     * Dibuja los marcos de las arenas y la línea divisoria central adaptados a las nuevas dimensiones.
     *
     * @param shapeRenderer Instancia para dibujo de primitivas geométricas.
     */
    private void drawBordersAndDivider(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.25f, 0.25f, 0.25f, 1f);

        float arenaWidthPx = ARENA_GRID_WIDTH * Snake.TILE_SIZE;
        float arenaHeightPx = ARENA_GRID_HEIGHT * Snake.TILE_SIZE;

        // Borde arena P1 (64f, 56f)
        shapeRenderer.rect(63f, 55f, arenaWidthPx + 2f, arenaHeightPx + 2f);

        // Borde arena P2 (464f, 56f)
        shapeRenderer.rect(463f, 55f, arenaWidthPx + 2f, arenaHeightPx + 2f);

        // Línea central divisoria
        shapeRenderer.setColor(0.35f, 0.35f, 0.35f, 1f);
        shapeRenderer.line(VIRTUAL_WIDTH / 2f, 40f, VIRTUAL_WIDTH / 2f, 410f);

        shapeRenderer.end();
    }

    /**
     * Obtiene el puntaje acumulado por la serpiente del Jugador 1 en la ronda.
     */
    @Override
    protected int getP1Score() {
        return arenaP1.getScore();
    }

    /**
     * Obtiene el puntaje acumulado por la serpiente del Jugador 2 en la ronda.
     */
    @Override
    protected int getP2Score() {
        return arenaP2.getScore();
    }

    /**
     * Reinicia las arenas y el buffer de entradas para dar inicio a una nueva ronda de 30 segundos.
     */
    @Override
    protected void resetMinigameRound() {
        moveTimer = 0f;
        inputHandler.reset();
        arenaP1.resetForNewRound();
        arenaP2.resetForNewRound();
    }

    /**
     * Libera las texturas y recursos específicos de Snake.
     */
    @Override
    protected void disposeMinigame() {
        tile1.dispose();
        tile2.dispose();
        arenaP1.dispose();
        arenaP2.dispose();
    }
}
