package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.ramshi.Main;
import io.github.ramshi.screens.BaseMinigameScreen;
import io.github.ramshi.menus.Botonera.Boton;

public abstract class MenuScreen implements Screen {

    public static final float ANCHO = 800f;
    public static final float ALTO = 480f;

    protected static final Color COLOR_TITULO = new Color(1f, 0.85f, 0.2f, 1f);
    protected static final Color COLOR_SUBTITULO = new Color(0.4f, 0.8f, 1f, 1f);
    protected static final Color COLOR_TEXTO = new Color(0.9f, 0.9f, 0.92f, 1f);
    protected static final Color COLOR_NOTA = new Color(0.6f, 0.6f, 0.65f, 1f);
    private static final Color COLOR_FONDO = new Color(0.06f, 0.06f, 0.08f, 1f);
    private static final float TITULO_Y = 445f;
    private static final float VOLVER_X = 20f;
    private static final float VOLVER_Y = 20f;
    private static final float VOLVER_ANCHO = 150f;
    private static final float VOLVER_ALTO = 44f;

    protected final Game juego;
    protected final SpriteBatch batch = new SpriteBatch();
    protected final ShapeRenderer formas = new ShapeRenderer();
    protected final OrthographicCamera camara = new OrthographicCamera();
    protected final Viewport vista = new FitViewport(ANCHO, ALTO, camara);
    protected final Botonera botonera = new Botonera(vista);
    protected float mouseX;
    protected float mouseY;
    private final Vector3 mouse = new Vector3();
    protected float fundidoEntrada;
    private Boton volver;
    private boolean cerrada;

    protected MenuScreen(Game juego) {
        this.juego = juego;
        vista.apply();
        camara.position.set(ANCHO / 2f, ALTO / 2f, 0f);
    }

    protected Boton agregarVolver(Runnable accion) {
        volver = botonera.agregar(new Boton(VOLVER_X, VOLVER_Y, VOLVER_ANCHO, VOLVER_ALTO, "VOLVER", Boton.ROJO, accion))
            .conFuente(Main.fuenteMediana);
        return volver;
    }

    protected void abrir(Screen siguiente) {
        cerrada = true;
        juego.setScreen(siguiente);
        dispose();
    }

    protected void actualizar() {}

    protected abstract void dibujar();

    @Override
    public void render(float delta) {
        ScreenUtils.clear(COLOR_FONDO);
        camara.update();
        batch.setProjectionMatrix(camara.combined);
        formas.setProjectionMatrix(camara.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        vista.unproject(mouse);
        mouseX = mouse.x;
        mouseY = mouse.y;
        botonera.actualizar();

        actualizar();
        if (cerrada) return;

        dibujar();
        botonera.dibujar(formas, batch);
        if (fundidoEntrada > 0f) {
            fundidoEntrada -= delta;
            BaseMinigameScreen.dibujarFundido(formas, fundidoEntrada / BaseMinigameScreen.DURACION_FUNDIDO);
        }

        Boton elegido = botonera.elegido();
        if (volver != null && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) volver.ejecutar();
        else if (elegido != null) elegido.ejecutar();
    }

    protected void dibujarTitulo(String texto) {
        batch.begin();
        escribir(Main.fuenteCartel, COLOR_TITULO, texto, ANCHO / 2f, TITULO_Y);
        batch.end();
    }

    protected void escribir(BitmapFont fuente, Color color, String texto, float centroX, float y) {
        fuente.setColor(color);
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, centroX - layout.width / 2f, y);
    }

    protected float escribirParrafo(BitmapFont fuente, Color color, String texto, float x, float y, float ancho) {
        GlyphLayout layout = new GlyphLayout(fuente, texto, color, ancho, Align.left, true);
        fuente.draw(batch, layout, x, y);
        return layout.height;
    }

    @Override public void resize(int width, int height) { vista.update(width, height, true); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        formas.dispose();
    }
}
