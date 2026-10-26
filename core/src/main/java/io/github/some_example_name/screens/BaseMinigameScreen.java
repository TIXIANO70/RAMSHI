package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;

public abstract class BaseMinigameScreen implements Screen{
    //variables y constantes
    public static final float VIRTUAL_WIDTH = 800f;
    public static final float VIRTUAL_HEIGHT = 480f;

    //constructor
    protected BaseMinigameScreen(Game game){
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.viewport.apply();
        this.camera.position.set(VIRTUAL_WIDTH/2f, VIRTUAL_HEIGHT/2f, 0);
        this.timeRemaining = getRoundDuration();
    };
    // variables y constantes del constructor
    protected final Game game;
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

    //metodos de la clase
    public void finishRound(){
        roundOver = true;
        roundWinnerMessage = determineRoundWinner();
    }
    public void startNewRound(){
        timeRemaining = getRoundDuration();
        roundOver = false;
        onRoundReset();
    }
    public void drawHUD(){
        batch.begin();

        font.getData().setScale(1.2f);
        font.setColor(0.3f, 0.9f, 0.3f, 1f);                 // verde P1
        font.draw(batch, getP1StatusText(), 24f, 462f);

        int seconds = (int) Math.ceil(timeRemaining);
        if (timeRemaining <= 5f && !roundOver) {
            font.setColor(1f, 0.3f, 0.3f, 1f);               // rojo últimos 5s
        } else {
            font.setColor(1f, 0.9f, 0.2f, 1f);               // amarillo
        }
        font.getData().setScale(1.4f);
        font.draw(batch, "TIEMPO: " + seconds + "s", 340f, 462f);

        font.getData().setScale(1.2f);
        font.setColor(0.3f, 0.7f, 1f, 1f);                   // celeste P2
        font.draw(batch, getP2StatusText(), 590f, 462f);

        batch.end();
    }
    public void drawRoundOverOverlay(){
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
        String scoreSummary = getScoreSummary();        com.badlogic.gdx.graphics.g2d.GlyphLayout scoreLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, scoreSummary);
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
    //herencias screen
    @Override
    public void render(float delta){
        // Permitir regresar al menú principal con la tecla ESC en cualquier momento
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        if (!roundOver) {
            timeRemaining -= delta;
            if (timeRemaining <= 0f) {
                timeRemaining = 0f;
                finishRound();
            } else {
                updateGame(delta);
            }
        } else {
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
        drawGame(batch);


        // 4. Dibujar HUD superior con puntajes y tiempo
        drawHUD();

        // 5. Si la ronda finalizó, dibujar el banner con el resultado
        if (roundOver) {
            drawRoundOverOverlay();
        }
    }
    @Override
    public void dispose(){
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
    @Override
    public void show(){}
    @Override
    public void resize(int width, int height){viewport.update(width, height, true);}
    @Override
    public void pause(){}
    @Override
    public void resume(){}
    @Override
    public void hide(){}
    //Funciones a heredar
    protected abstract float getRoundDuration();
    protected abstract void updateGame(float delta);
    protected abstract void drawGame(SpriteBatch batch);
    protected abstract String determineRoundWinner();
    protected abstract void onRoundReset();
    protected abstract String getP1StatusText();   // "P1: 3 pts (Wins: 1)"
    protected abstract String getP2StatusText();   // "P2: 5 pts (Wins: 0)"
    protected abstract String getScoreSummary();   // "Puntaje de ronda: P1 (3) vs P2 (5)"
}
