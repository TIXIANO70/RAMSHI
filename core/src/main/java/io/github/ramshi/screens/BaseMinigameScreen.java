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
import io.github.some_example_name.session.GameSession;
import io.github.some_example_name.session.MinigameType;

public abstract class BaseMinigameScreen implements Screen {
    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    // ===== AJUSTES DEL FLUJO (comunes a todos los minijuegos) =====
    private static final float INTRO_SECONDS = 1.2f;   // cartel "preparate" antes de cada juego
    private static final float RESULT_SECONDS = 2.5f;  // cuánto se muestra el ganador antes de seguir

    protected final Game game;
    protected final GameSession session;
    protected SpriteBatch batch;
    protected BitmapFont font;
    protected ShapeRenderer shapeRenderer;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected float timeRemaining;
    protected boolean roundOver;
    protected int p1GlobalWins;
    protected int p2GlobalWins;
    protected String roundWinnerMessage;

    private float introTimer = INTRO_SECONDS;
    private float resultTimer = 0f;
    private int lastP1Gained = 0;
    private int lastP2Gained = 0;

    protected BaseMinigameScreen(Game game) {
        this.game = game;
        this.session = GameSession.current();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        this.timeRemaining = getRoundDuration();
    }

    /** Cierra la ronda. Es seguro llamarlo más de una vez: solo cuenta la primera. */
    public void finishRound() {
        if (roundOver) return;
        int p1Before = p1GlobalWins;
        int p2Before = p2GlobalWins;
        roundOver = true;
        roundWinnerMessage = determineRoundWinner();
        lastP1Gained = p1GlobalWins - p1Before;   // puntos que sumó cada uno en ESTE juego
        lastP2Gained = p2GlobalWins - p2Before;
        resultTimer = 0f;
    }

    public void startNewRound() {
        timeRemaining = getRoundDuration();
        roundOver = false;
        resultTimer = 0f;
        onRoundReset();
    }

    /** Cambia de pantalla y libera la actual. */
    protected void goTo(Screen next) {
        game.setScreen(next);
        dispose();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goTo(new MainMenuScreen(game));
            return;
        }

        if (introTimer > 0f) {
            introTimer -= delta;
        } else if (!roundOver) {
            timeRemaining -= delta;
            if (timeRemaining <= 0f) {
                timeRemaining = 0f;
                finishRound();
            } else {
                updateGame(delta);
            }
        } else {
            resultTimer += delta;
            boolean skip = !session.isCampaign()
                && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER));
            if (resultTimer >= RESULT_SECONDS || skip) {
                if (session.isCampaign()) {
                    session.recordResult(lastP1Gained, lastP2Gained);
                    goTo(session.nextScreen(game));
                    return;
                }
                startNewRound();
            }
        }

        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawGame(batch);
        drawHUD();

        if (introTimer > 0f) drawIntroOverlay();
        else if (roundOver) drawRoundOverOverlay();
    }

    public void drawHUD() {
        batch.begin();

        if (session.isCampaign()) {
            font.setColor(Color.WHITE);
            font.getData().setScale(0.9f);
            drawCentered("Juego " + (session.getIndex() + 1) + "/" + session.getTotalGames()
                + "    Total:  P1 " + session.totalP1() + " - " + session.totalP2() + " P2", 478f);
        }

        font.getData().setScale(1.2f);
        font.setColor(0.3f, 0.9f, 0.3f, 1f);
        font.draw(batch, getP1StatusText(), 24f, 462f);

        int seconds = (int) Math.ceil(timeRemaining);
        if (timeRemaining <= 5f && !roundOver) font.setColor(1f, 0.3f, 0.3f, 1f);
        else font.setColor(1f, 0.9f, 0.2f, 1f);
        font.getData().setScale(1.4f);
        font.draw(batch, "TIEMPO: " + seconds + "s", 340f, 462f);

        font.getData().setScale(1.2f);
        font.setColor(0.3f, 0.7f, 1f, 1f);
        font.draw(batch, getP2StatusText(), 590f, 462f);

        batch.end();
    }

    private void drawIntroOverlay() {
        drawBox(160f, 170f, 480f, 140f);
        MinigameType type = session.currentType();
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        drawCentered(type != null ? type.getDisplayName() : "", 275f);
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(1.3f);
        drawCentered("¡PREPARATE!", 215f);
        font.getData().setScale(1.2f);
        batch.end();
    }

    public void drawRoundOverOverlay() {
        drawBox(160f, 130f, 480f, 220f);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        drawCentered(roundWinnerMessage, 320f);

        font.setColor(0.85f, 0.85f, 0.85f, 1f);
        font.getData().setScale(1.1f);
        drawCentered(getScoreSummary(), 275f);

        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.getData().setScale(1.2f);
        if (session.isCampaign()) {
            drawCentered("PARTIDA:  P1 [" + (session.totalP1() + lastP1Gained) + "]  -  P2 ["
                + (session.totalP2() + lastP2Gained) + "]", 235f);
        } else {
            drawCentered("VICTORIAS TOTALES:  P1 [" + p1GlobalWins + "]  -  P2 [" + p2GlobalWins + "]", 235f);
        }

        font.setColor(0.7f, 1f, 0.7f, 1f);
        font.getData().setScale(1.0f);
        drawCentered(session.isCampaign() ? "Siguiente juego en un instante..."
            : "[ ESPACIO ] Siguiente Ronda       [ ESC ] Menu Principal", 175f);

        font.getData().setScale(1.2f);
        batch.end();
    }

    private void drawBox(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.92f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.84f, 0f, 1f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
    }

    private void drawCentered(String text, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2f, y);
    }

    @Override public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    protected abstract float getRoundDuration();
    protected abstract void updateGame(float delta);
    protected abstract void drawGame(SpriteBatch batch);
    protected abstract String determineRoundWinner();
    protected abstract void onRoundReset();
    protected abstract String getP1StatusText();
    protected abstract String getP2StatusText();
    protected abstract String getScoreSummary();
}