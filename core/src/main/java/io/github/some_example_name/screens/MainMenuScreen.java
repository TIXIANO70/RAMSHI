package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class MainMenuScreen implements Screen {

    private final Game game;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private Rectangle jugarButton;
    private Rectangle salirButton;

    public MainMenuScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();

        // Tamaño de los botones
        jugarButton = new Rectangle(300, 250, 200, 60);
        salirButton = new Rectangle(300, 150, 200, 60);
    }

    @Override
    public void render(float delta) {

        // Fondo
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Posición del mouse
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // -------------------------
        // DIBUJAR BOTONES
        // -------------------------

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);

        shapeRenderer.rect(
            jugarButton.x,
            jugarButton.y,
            jugarButton.width,
            jugarButton.height
        );

        shapeRenderer.rect(
            salirButton.x,
            salirButton.y,
            salirButton.width,
            salirButton.height
        );

        shapeRenderer.end();

        // -------------------------
        // DIBUJAR TEXTO
        // -------------------------

        batch.begin();

        font.getData().setScale(3);
        drawCenteredText("SNAKE", 400);

        font.getData().setScale(2);
        drawCenteredText("JUGAR", jugarButton.y + 40);
        drawCenteredText("SALIR", salirButton.y + 40);

        batch.end();

        // -------------------------
        // DETECTAR CLICK
        // -------------------------

        if (Gdx.input.justTouched()) {

            if (jugarButton.contains(mouseX, mouseY)) {
                game.setScreen(new SnakeScreen(game));
            }

            if (salirButton.contains(mouseX, mouseY)) {
                Gdx.app.exit();
            }
        }
    }

    private void drawCenteredText(String text, float y) {

        GlyphLayout layout = new GlyphLayout(font, text);

        float x = (Gdx.graphics.getWidth() - layout.width) / 2;

        font.draw(batch, layout, x, y);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
