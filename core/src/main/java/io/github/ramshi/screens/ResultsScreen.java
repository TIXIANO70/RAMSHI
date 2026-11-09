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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.session.GameSession;

/** Pantalla final del modo Jugar: ganador, motivo y puntos de cada juego. */
public class ResultsScreen implements Screen {

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 480f;

    // ===== AJUSTES =====
    private static final float INPUT_LOCK = 1.5f;   // segundos antes de aceptar teclas/click (evita saltarla sin querer)
    private static final float ROW_HEIGHT = 20f;
    private static final float ROWS_START_Y = 345f;
    private static final float COL_NUM = 60f, COL_NAME = 100f, COL_P1 = 470f, COL_P2 = 550f, COL_WIN = 630f;

    private final Game game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    private final GameSession session;
    private final String winnerText;
    private final String reasonText;
    private final int games1, games2;
    private float lockTimer = INPUT_LOCK;

    public ResultsScreen(Game game, GameSession session) {
        this.game = game;
        this.session = session;
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        int p1 = session.totalP1(), p2 = session.totalP2();
        int g1 = 0, g2 = 0;
        for (GameSession.Result r : session.getResults()) {
            if (r.p1Points > r.p2Points) g1++;
            else if (r.p2Points > r.p1Points) g2++;
        }
        games1 = g1;
        games2 = g2;

        if (p1 != p2) {
            int w = p1 > p2 ? 1 : 2;
            winnerText = "¡GANA EL JUGADOR " + w + "!";
            reasonText = "Por " + Math.abs(p1 - p2) + " puntos de diferencia (" + p1 + " a " + p2 + ")";
        } else if (g1 != g2) {
            int w = g1 > g2 ? 1 : 2;
            winnerText = "¡GANA EL JUGADOR " + w + "!";
            reasonText = "Empate en puntos (" + p1 + "), desempata por juegos ganados (" + g1 + " a " + g2 + ")";
        } else {
            winnerText = "¡EMPATE!";
            reasonText = "Mismos puntos (" + p1 + ") y mismos juegos ganados (" + g1 + ")";
        }
    }

    @Override
    public void render(float delta) {
        lockTimer -= delta;
        if (lockTimer <= 0f && (Gdx.input.justTouched()
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        ScreenUtils.clear(0.06f, 0.06f, 0.08f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(2.2f);
        drawCentered(winnerText, 465f);
        font.setColor(Color.WHITE);
        font.getData().setScale(1.1f);
        drawCentered(reasonText, 422f);
        drawCentered("Juegos ganados:  P1 " + games1 + " - " + games2 + " P2", 400f);

        font.getData().setScale(1.0f);
        font.setColor(Color.LIGHT_GRAY);
        float y = 372f;
        font.draw(batch, "#", COL_NUM, y);
        font.draw(batch, "MINIJUEGO", COL_NAME, y);
        font.draw(batch, "P1", COL_P1, y);
        font.draw(batch, "P2", COL_P2, y);
        font.draw(batch, "GANO", COL_WIN, y);

        int i = 0;
        for (GameSession.Result r : session.getResults()) {
            float ry = ROWS_START_Y - i * ROW_HEIGHT;
            font.setColor(Color.WHITE);
            font.draw(batch, String.valueOf(i + 1), COL_NUM, ry);
            font.draw(batch, r.type.getDisplayName(), COL_NAME, ry);
            font.setColor(0.3f, 0.9f, 0.3f, 1f);
            font.draw(batch, "+" + r.p1Points, COL_P1, ry);
            font.setColor(0.3f, 0.7f, 1f, 1f);
            font.draw(batch, "+" + r.p2Points, COL_P2, ry);
            font.setColor(Color.WHITE);
            font.draw(batch, r.p1Points > r.p2Points ? "P1" : (r.p2Points > r.p1Points ? "P2" : "-"), COL_WIN, ry);
            i++;
        }

        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "TOTAL", COL_NAME, 45f);
        font.draw(batch, String.valueOf(session.totalP1()), COL_P1, 45f);
        font.draw(batch, String.valueOf(session.totalP2()), COL_P2, 45f);

        if (lockTimer <= 0f) {
            font.setColor(0.7f, 1f, 0.7f, 1f);
            drawCentered("Click o ESPACIO para volver al menu", 20f);
        }
        batch.end();
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
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}