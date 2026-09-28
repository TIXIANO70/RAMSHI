package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Menú principal de RAMSHI.
 * Presenta el título del proyecto, la información del modo 1v1 y los botones interactivos
 * adaptados al viewport panorámico de 800x480 con efectos visuales de selección.
 */
public class MainMenuScreen implements Screen {

    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    private final Game game;

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Rectangle jugarButton;
    private final Rectangle salirButton;
    private final Vector3 mousePos = new Vector3();

    /**
     * Constructor del menú principal.
     * Configura el viewport, fuentes, renderers y la disposición de los botones centrados.
     *
     * @param game Instancia principal del juego.
     */
    public MainMenuScreen(Game game) {
        this.game = game;

        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();

        // Configuración de cámara y viewport panorámico
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        // Botones centrados horizontalmente en 800px (ancho 260px)
        float buttonWidth = 260f;
        float buttonHeight = 55f;
        float buttonX = (VIRTUAL_WIDTH - buttonWidth) / 2f; // 270f

        this.jugarButton = new Rectangle(buttonX, 210f, buttonWidth, buttonHeight);
        this.salirButton = new Rectangle(buttonX, 130f, buttonWidth, buttonHeight);
    }

    @Override
    public void render(float delta) {
        // Limpieza de pantalla con tono oscuro elegante
        ScreenUtils.clear(0.06f, 0.06f, 0.08f, 1f);

        // Actualizar cámara y desproyectar posición del puntero a coordenadas virtuales
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        boolean hoverJugar = jugarButton.contains(mousePos.x, mousePos.y);
        boolean hoverSalir = salirButton.contains(mousePos.x, mousePos.y);

        // -------------------------
        // 1. DIBUJAR FONDOS DE BOTONES
        // -------------------------
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Botón Jugar (fondo resaltado si tiene hover)
        if (hoverJugar) {
            shapeRenderer.setColor(0.12f, 0.35f, 0.18f, 1f);
        } else {
            shapeRenderer.setColor(0.12f, 0.16f, 0.14f, 1f);
        }
        shapeRenderer.rect(jugarButton.x, jugarButton.y, jugarButton.width, jugarButton.height);

        // Botón Salir (fondo resaltado si tiene hover)
        if (hoverSalir) {
            shapeRenderer.setColor(0.38f, 0.12f, 0.12f, 1f);
        } else {
            shapeRenderer.setColor(0.18f, 0.12f, 0.12f, 1f);
        }
        shapeRenderer.rect(salirButton.x, salirButton.y, salirButton.width, salirButton.height);

        shapeRenderer.end();

        // -------------------------
        // 2. DIBUJAR BORDES DE BOTONES
        // -------------------------
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (hoverJugar) {
            shapeRenderer.setColor(0.3f, 0.9f, 0.4f, 1f);
        } else {
            shapeRenderer.setColor(0.3f, 0.5f, 0.35f, 1f);
        }
        shapeRenderer.rect(jugarButton.x, jugarButton.y, jugarButton.width, jugarButton.height);

        if (hoverSalir) {
            shapeRenderer.setColor(0.9f, 0.3f, 0.3f, 1f);
        } else {
            shapeRenderer.setColor(0.5f, 0.3f, 0.3f, 1f);
        }
        shapeRenderer.rect(salirButton.x, salirButton.y, salirButton.width, salirButton.height);

        shapeRenderer.end();

        // -------------------------
        // 3. DIBUJAR TEXTO Y TÍTULOS
        // -------------------------
        batch.begin();

        // Título del juego RAMSHI
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(3.6f);
        drawCenteredText("RAMSHI", 415f);

        // Subtítulo descriptivo
        font.setColor(0.4f, 0.8f, 1f, 1f);
        font.getData().setScale(1.3f);
        drawCenteredText("SNAKE 1v1 LOCAL SPLIT-SCREEN", 355f);

        // Texto en botones
        font.getData().setScale(1.6f);
        font.setColor(hoverJugar ? Color.WHITE : Color.LIGHT_GRAY);
        drawCenteredText("JUGAR 1v1", jugarButton.y + 38f);

        font.setColor(hoverSalir ? Color.WHITE : Color.LIGHT_GRAY);
        drawCenteredText("SALIR", salirButton.y + 38f);

        // Leyenda de controles al pie
        font.setColor(0.6f, 0.6f, 0.65f, 1f);
        font.getData().setScale(1.0f);
        drawCenteredText("P1: WASD  |  P2: FLECHAS  |  RONDAS DE 30 SEGUNDOS", 55f);

        batch.end();

        // -------------------------
        // 4. GESTIÓN DE CLICKS
        // -------------------------
        if (Gdx.input.justTouched()) {
            if (hoverJugar) {
                game.setScreen(new SnakeScreen(game));
                dispose();
            } else if (hoverSalir) {
                Gdx.app.exit();
            }
        }
    }

    /**
     * Dibuja un texto centrado horizontalmente en la pantalla a la altura Y especificada.
     */
    private void drawCenteredText(String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float x = (VIRTUAL_WIDTH - layout.width) / 2f;
        font.draw(batch, layout, x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}

