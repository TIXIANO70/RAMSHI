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

    // Duración de la ronda en segundos y tiempo restante
    private static final float ROUND_DURATION = 30f;
    private float timeRemaining = ROUND_DURATION;
    private boolean roundOver = false;
    private String roundWinnerMessage = "";

    // Contador global de victorias entre rondas
    private int p1GlobalWins;
    private int p2GlobalWins;

    /**
     * Constructor por defecto de SnakeScreen que inicia el contador global en cero.
     *
     * @param game Instancia principal del juego.
     */
    public SnakeScreen(Game game) {
        this(game, 0, 0);
    }

    /**
     * Constructor parametrizado con victorias globales acumuladas.
     *
     * @param game        Instancia principal del juego.
     * @param p1Wins      Victorias globales acumuladas del Jugador 1.
     * @param p2Wins      Victorias globales acumuladas del Jugador 2.
     */
    public SnakeScreen(Game game, int p1Wins, int p2Wins) {
        this.game = game;
        this.p1GlobalWins = p1Wins;
        this.p2GlobalWins = p2Wins;
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
        // Permitir regresar al menú principal con la tecla ESC en cualquier momento
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // 1. Control del temporizador de la ronda
        if (!roundOver) {
            timeRemaining -= delta;
            if (timeRemaining <= 0f) {
                timeRemaining = 0f;
                finishRound();
            } else {
                // Capturar entradas de ambos jugadores
                arenaP1.setNextDirection(inputHandler.getP1Direction(arenaP1.getSnake().getHead().getDirection()));
                arenaP2.setNextDirection(inputHandler.getP2Direction(arenaP2.getSnake().getHead().getDirection()));

                // Acumular tiempo para el tick de simulación
                moveTimer += delta;
                if (moveTimer >= MOVE_INTERVAL) {
                    moveTimer -= MOVE_INTERVAL;
                    arenaP1.tick();
                    arenaP2.tick();
                }
            }
        } else {
            // Si la ronda terminó, esperar confirmación para la siguiente
            if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)
                || com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                startNewRound();
            }
        }


        // Limpieza de pantalla con tono oscuro
        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 2. Dibujar arenas (baldosas, comida y serpientes)
        batch.begin();
        arenaP1.render(batch);
        arenaP2.render(batch);
        batch.end();

        // 3. Dibujar líneas divisorias y bordes estéticos
        drawBordersAndDivider();

        // 4. Dibujar HUD superior con puntajes y tiempo
        drawHUD();

        // 5. Si la ronda finalizó, dibujar el banner con el resultado
        if (roundOver) {
            drawRoundOverOverlay();
        }
    }

    /**
     * Evalúa los puntajes finales de la ronda y define el mensaje de victoria, sumando al contador global.
     */
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

    /**
     * Reinicia las variables de tiempo y arenas para dar inicio a una nueva ronda de 30 segundos.
     */
    private void startNewRound() {
        timeRemaining = ROUND_DURATION;
        roundOver = false;
        roundWinnerMessage = "";
        arenaP1.resetForNewRound();
        arenaP2.resetForNewRound();
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
     * Dibuja las etiquetas de los jugadores, marcadores y victorias globales junto al cronómetro en el HUD.
     */
    private void drawHUD() {
        batch.begin();

        // Marcador P1 (Verde) con Victorias
        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        font.getData().setScale(1.2f);
        font.draw(batch, "P1: " + arenaP1.getScore() + " pts (Wins: " + p1GlobalWins + ")", 24f, 462f);

        // Cronómetro de 30s central
        int seconds = (int) Math.ceil(timeRemaining);
        if (timeRemaining <= 5f && !roundOver) {
            font.setColor(1f, 0.3f, 0.3f, 1f); // Alerta roja en los últimos 5s
        } else {
            font.setColor(1f, 0.9f, 0.2f, 1f); // Amarillo normal
        }
        font.getData().setScale(1.4f);
        font.draw(batch, "TIEMPO: " + seconds + "s", 340f, 462f);

        // Marcador P2 (Azul/Celeste) con Victorias
        font.setColor(0.3f, 0.7f, 1f, 1f);
        font.getData().setScale(1.2f);
        font.draw(batch, "P2: " + arenaP2.getScore() + " pts (Wins: " + p2GlobalWins + ")", 590f, 462f);

        batch.end();
    }

    /**
     * Dibuja el cartel central al expirar los 30 segundos con el ganador, puntajes y victorias globales.
     */
    private void drawRoundOverOverlay() {
        // Marco oscuro de fondo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.92f);
        shapeRenderer.rect(160f, 130f, 480f, 220f);
        shapeRenderer.end();

        // Borde dorado
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.84f, 0f, 1f);
        shapeRenderer.rect(160f, 130f, 480f, 220f);
        shapeRenderer.end();

        // Textos del resultado
        batch.begin();

        // 1. Título del ganador de la ronda
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, roundWinnerMessage);
        font.draw(batch, titleLayout, (VIRTUAL_WIDTH - titleLayout.width) / 2f, 320f);

        // 2. Puntaje obtenido en la ronda
        font.setColor(0.85f, 0.85f, 0.85f, 1f);
        font.getData().setScale(1.1f);
        String scoreSummary = "Puntaje de ronda: P1 (" + arenaP1.getScore() + ") vs P2 (" + arenaP2.getScore() + ")";
        com.badlogic.gdx.graphics.g2d.GlyphLayout scoreLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, scoreSummary);
        font.draw(batch, scoreLayout, (VIRTUAL_WIDTH - scoreLayout.width) / 2f, 275f);

        // 3. Marcador global de victorias acumuladas
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(1.2f);
        String globalWinsSummary = "VICTORIAS TOTALES:  P1 [" + p1GlobalWins + "]  -  P2 [" + p2GlobalWins + "]";
        com.badlogic.gdx.graphics.g2d.GlyphLayout winsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, globalWinsSummary);
        font.draw(batch, winsLayout, (VIRTUAL_WIDTH - winsLayout.width) / 2f, 235f);

        // 4. Instrucciones de interacción
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

