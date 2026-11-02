package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Clase base abstracta para todas las pantallas de minijuegos 1v1 en RAMSHI.
 * Implementa el patrón Template Method centralizando la resolución panorámica (800x480),
 * los recursos gráficos compartidos, el temporizador de ronda, las victorias globales,
 * el HUD superior y los controles de navegación universal.
 */
public abstract class BaseMinigameScreen implements Screen {

    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    // Duración estándar de cada ronda en segundos
    protected static final float DEFAULT_ROUND_DURATION = 30f;
    protected float roundDuration = DEFAULT_ROUND_DURATION;
    protected float timeRemaining = DEFAULT_ROUND_DURATION;
    protected boolean roundOver = false;
    protected String roundWinnerMessage = "";

    // Contador global de victorias entre rondas
    protected int p1GlobalWins;
    protected int p2GlobalWins;

    protected final Game game;
    protected final SpriteBatch batch;
    protected final ShapeRenderer shapeRenderer;
    protected final BitmapFont font;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;

    /**
     * Constructor base con victorias inicializadas en cero.
     *
     * @param game Instancia principal del juego.
     */
    public BaseMinigameScreen(Game game) {
        this(game, 0, 0);
    }

    /**
     * Constructor base parametrizado con victorias globales acumuladas.
     *
     * @param game    Instancia principal del juego.
     * @param p1Wins  Victorias acumuladas del Jugador 1.
     * @param p2Wins  Victorias acumuladas del Jugador 2.
     */
    public BaseMinigameScreen(Game game, int p1Wins, int p2Wins) {
        this.game = game;
        this.p1GlobalWins = p1Wins;
        this.p2GlobalWins = p2Wins;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();

        // Configuración de la cámara y el viewport panorámico estándar (800x480)
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {
        registerInputProcessor();
    }

    /**
     * Gancho opcional para que la subclase registre su InputProcessor específico.
     */
    protected void registerInputProcessor() {
        // Implementación por defecto vacía; sobrescribir si el minijuego utiliza InputAdapter
    }

    /**
     * Método plantilla principal del Game Loop.
     * Gestiona el temporizador, delega la actualización y renderizado al minijuego,
     * y dibuja los overlays y el HUD estándar.
     */
    @Override
    public void render(float delta) {
        // Permitir regresar al menú principal con la tecla ESC en cualquier momento
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // 1. Control del temporizador y flujo de la ronda
        if (!roundOver) {
            timeRemaining -= delta;
            if (timeRemaining <= 0f) {
                timeRemaining = 0f;
                finishRound();
            } else {
                // Actualizar la lógica y mecánicas propias del minijuego
                updateMinigame(delta);
            }
        } else {
            // Si la ronda terminó, esperar confirmación para la siguiente
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                startNewRound();
            }
        }

        // 2. Limpieza de pantalla con tono oscuro estándar
        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // 3. Dibujar elementos específicos del minijuego
        renderMinigame(batch, shapeRenderer);

        // 4. Dibujar HUD superior con puntajes y tiempo
        drawHUD();

        // 5. Si la ronda finalizó, dibujar el banner con el resultado
        if (roundOver) {
            drawRoundOverOverlay();
        }
    }

    /**
     * Evalúa los puntajes finales de la ronda usando los contratos getP1Score() y getP2Score(),
     * define el mensaje de victoria e incrementa el contador global correspondiente.
     */
    protected void finishRound() {
        roundOver = true;
        int s1 = getP1Score();
        int s2 = getP2Score();

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
     * Reinicia las variables de tiempo y estado de la ronda, delegando en resetMinigameRound().
     */
    protected void startNewRound() {
        timeRemaining = roundDuration;
        roundOver = false;
        roundWinnerMessage = "";
        resetMinigameRound();
    }

    /**
     * Dibuja las etiquetas de los jugadores, marcadores y victorias globales junto al cronómetro en el HUD.
     */
    protected void drawHUD() {
        batch.begin();

        // Marcador P1 (Verde) con Victorias
        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        font.getData().setScale(1.2f);
        font.draw(batch, "P1: " + getP1Score() + " pts (Wins: " + p1GlobalWins + ")", 24f, 462f);

        // Cronómetro de ronda central
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
        font.draw(batch, "P2: " + getP2Score() + " pts (Wins: " + p2GlobalWins + ")", 590f, 462f);

        batch.end();
    }

    /**
     * Dibuja el cartel central al expirar el tiempo de la ronda con el ganador,
     * puntajes obtenidos e instrucciones de navegación.
     */
    protected void drawRoundOverOverlay() {
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
        GlyphLayout titleLayout = new GlyphLayout(font, roundWinnerMessage);
        font.draw(batch, titleLayout, (VIRTUAL_WIDTH - titleLayout.width) / 2f, 320f);

        // 2. Puntaje obtenido en la ronda
        font.setColor(0.85f, 0.85f, 0.85f, 1f);
        font.getData().setScale(1.1f);
        String scoreSummary = "Puntaje de ronda: P1 (" + getP1Score() + ") vs P2 (" + getP2Score() + ")";
        GlyphLayout scoreLayout = new GlyphLayout(font, scoreSummary);
        font.draw(batch, scoreLayout, (VIRTUAL_WIDTH - scoreLayout.width) / 2f, 275f);

        // 3. Marcador global de victorias acumuladas
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(1.2f);
        String globalWinsSummary = "VICTORIAS TOTALES:  P1 [" + p1GlobalWins + "]  -  P2 [" + p2GlobalWins + "]";
        GlyphLayout winsLayout = new GlyphLayout(font, globalWinsSummary);
        font.draw(batch, winsLayout, (VIRTUAL_WIDTH - winsLayout.width) / 2f, 235f);

        // 4. Instrucciones de interacción
        font.setColor(0.7f, 1f, 0.7f, 1f);
        font.getData().setScale(1.0f);
        String instruction = "[ ESPACIO ] Siguiente Ronda       [ ESC ] Menu Principal";
        GlyphLayout instrLayout = new GlyphLayout(font, instruction);
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
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (Gdx.input.getInputProcessor() != null) {
            Gdx.input.setInputProcessor(null);
        }
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        disposeMinigame();
    }

    // =========================================================================
    // MÉTODOS ABSTRACTOS / CONTRATOS PARA SUBCLASES
    // =========================================================================

    /**
     * Actualiza la lógica, físicas y movimiento específicos del minijuego.
     *
     * @param delta Tiempo transcurrido desde el último cuadro en segundos.
     */
    protected abstract void updateMinigame(float delta);

    /**
     * Dibuja las entidades, arenas y elementos gráficos propios del minijuego.
     *
     * @param batch Instancia compartida de SpriteBatch configurada con la cámara.
     * @param shapeRenderer Instancia compartida de ShapeRenderer configurada con la cámara.
     */
    protected abstract void renderMinigame(SpriteBatch batch, ShapeRenderer shapeRenderer);

    /**
     * Obtiene el puntaje acumulado por el Jugador 1 en la ronda activa.
     *
     * @return Puntaje de P1.
     */
    protected abstract int getP1Score();

    /**
     * Obtiene el puntaje acumulado por el Jugador 2 en la ronda activa.
     *
     * @return Puntaje de P2.
     */
    protected abstract int getP2Score();

    /**
     * Reinicia las entidades, arenas o variables de juego para dar inicio a una nueva ronda.
     */
    protected abstract void resetMinigameRound();

    /**
     * Libera las texturas y recursos GPU específicos que pertenecen a la subclase.
     */
    protected abstract void disposeMinigame();
}
