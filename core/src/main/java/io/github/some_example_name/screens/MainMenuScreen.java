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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.session.GameSession;
import io.github.some_example_name.session.MinigameType;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen implements Screen {

    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    // ===== AJUSTES =====
    private static final int MIN_ROUNDS = 3;       // mínimo de juegos en modo Jugar
    private static final int MAX_ROUNDS = 15;      // máximo (la pantalla de resultados entra hasta 15)
    private static final int DEFAULT_ROUNDS = 8;   // valor inicial del selector
    private static final float BTN_W = 260f;
    private static final float BTN_H = 55f;

    private enum Page { MAIN, ENDLESS }

    private static class Button {
        final Rectangle rect;
        final String label;
        final Runnable action;
        Button(float x, float y, float w, float h, String label, Runnable action) {
            this.rect = new Rectangle(x, y, w, h);
            this.label = label;
            this.action = action;
        }
    }

    private final Game game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    private final Vector3 mousePos = new Vector3();
    private final List<Button> mainButtons = new ArrayList<>();
    private final List<Button> endlessButtons = new ArrayList<>();
    private Page page = Page.MAIN;
    private int rounds = DEFAULT_ROUNDS;

    public MainMenuScreen(Game game) {
        this.game = game;
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        float x = (VIRTUAL_WIDTH - BTN_W) / 2f;
        mainButtons.add(new Button(x, 280f, BTN_W, BTN_H, "JUGAR",
            () -> open(GameSession.startCampaign(rounds).firstScreen(game))));
        mainButtons.add(new Button(270f, 232f, 55f, 40f, "-",
            () -> rounds = Math.max(MIN_ROUNDS, rounds - 1)));
        mainButtons.add(new Button(475f, 232f, 55f, 40f, "+",
            () -> rounds = Math.min(MAX_ROUNDS, rounds + 1)));
        mainButtons.add(new Button(x, 150f, BTN_W, BTN_H, "ENDLESS", () -> page = Page.ENDLESS));
        mainButtons.add(new Button(x, 80f, BTN_W, BTN_H, "SALIR", () -> Gdx.app.exit()));

        MinigameType[] types = MinigameType.values();
        for (int i = 0; i < types.length; i++) {
            final MinigameType type = types[i];
            float bx = (i % 2 == 0) ? 80f : 420f;
            float by = 340f - (i / 2) * 54f;
            endlessButtons.add(new Button(bx, by, 300f, 44f, type.getDisplayName(),
                () -> open(GameSession.startEndless(type).firstScreen(game))));
        }
        endlessButtons.add(new Button(270f, 10f, BTN_W, 40f, "VOLVER", () -> page = Page.MAIN));
    }

    private void open(Screen next) {
        game.setScreen(next);
        dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.06f, 0.08f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (page == Page.ENDLESS && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) page = Page.MAIN;

        List<Button> buttons = (page == Page.MAIN) ? mainButtons : endlessButtons;
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        Button hovered = null;
        for (Button b : buttons) if (b.rect.contains(mousePos.x, mousePos.y)) hovered = b;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Button b : buttons) {
            if (b == hovered) shapeRenderer.setColor(0.12f, 0.35f, 0.18f, 1f);
            else shapeRenderer.setColor(0.12f, 0.16f, 0.14f, 1f);
            shapeRenderer.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Button b : buttons) {
            if (b == hovered) shapeRenderer.setColor(0.3f, 0.9f, 0.4f, 1f);
            else shapeRenderer.setColor(0.3f, 0.5f, 0.35f, 1f);
            shapeRenderer.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        shapeRenderer.end();

        batch.begin();
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(3.6f);
        drawCentered("RAMSHI", 440f);
        font.setColor(0.4f, 0.8f, 1f, 1f);
        font.getData().setScale(1.3f);
        drawCentered(page == Page.MAIN ? "MICROJUEGOS 1v1 LOCAL" : "ELEGI UN MINIJUEGO", 365f);

        for (Button b : buttons) {
            font.getData().setScale(b.rect.height > 50f ? 1.6f : 1.2f);
            font.setColor(b == hovered ? Color.WHITE : Color.LIGHT_GRAY);
            GlyphLayout layout = new GlyphLayout(font, b.label);
            font.draw(batch, layout, b.rect.x + (b.rect.width - layout.width) / 2f,
                b.rect.y + (b.rect.height + layout.height) / 2f);
        }
        if (page == Page.MAIN) {
            font.setColor(Color.WHITE);
            font.getData().setScale(1.3f);
            drawCentered("RONDAS: " + rounds, 260f);
        }
        font.setColor(0.6f, 0.6f, 0.65f, 1f);
        font.getData().setScale(1.0f);
        drawCentered("P1: WASD  |  P2: FLECHAS  |  10 SEGUNDOS POR JUEGO", 35f);
        batch.end();

        if (Gdx.input.justTouched() && hovered != null) hovered.action.run();
    }

    private void drawCentered(String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2f, y);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}