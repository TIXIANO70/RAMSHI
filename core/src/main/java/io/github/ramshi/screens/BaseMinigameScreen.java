package io.github.ramshi.screens;

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
import io.github.ramshi.Main;
import io.github.ramshi.session.GameSession;
import io.github.ramshi.session.MinigameType;

public abstract class BaseMinigameScreen implements Screen {
    public static final float ANCHO_VIRTUAL = 800f;
    public static final float ALTO_VIRTUAL = 480f;

    private static final float SEGUNDOS_INTRO = 1.2f;
    private static final float SEGUNDOS_RESULTADO = 2.5f;
    private static final float MARGEN_HUD = 16f;
    private static final float ALTURA_HUD = 462f;

    protected final Game juego;
    protected final GameSession sesion;
    protected SpriteBatch batch;
    protected BitmapFont fuente;
    protected ShapeRenderer formas;
    protected OrthographicCamera camara;
    protected Viewport vista;
    protected float tiempoRestante;
    protected boolean rondaTerminada;
    protected int victoriasP1;
    protected int victoriasP2;
    protected String mensajeGanador;

    private float tiempoIntro = SEGUNDOS_INTRO;
    private float tiempoResultado = 0f;
    private int puntosGanadosP1 = 0;
    private int puntosGanadosP2 = 0;

    protected BaseMinigameScreen(Game juego) {
        this.juego = juego;
        this.sesion = GameSession.actual();
        this.batch = new SpriteBatch();
        this.fuente = Main.fuenteChica;
        this.formas = new ShapeRenderer();
        this.camara = new OrthographicCamera();
        this.vista = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
        this.vista.apply();
        this.camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);
        this.tiempoRestante = getDuracionRonda();
    }

    public void terminarRonda() {
        if (rondaTerminada) return;
        int antesP1 = victoriasP1;
        int antesP2 = victoriasP2;
        rondaTerminada = true;
        mensajeGanador = determinarGanador();
        puntosGanadosP1 = victoriasP1 - antesP1;
        puntosGanadosP2 = victoriasP2 - antesP2;
        tiempoResultado = 0f;
    }

    public void iniciarNuevaRonda() {
        tiempoRestante = getDuracionRonda();
        rondaTerminada = false;
        tiempoResultado = 0f;
        reiniciarRonda();
    }

    protected void irA(Screen siguiente) {
        juego.setScreen(siguiente);
        dispose();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            irA(new MainMenuScreen(juego));
            return;
        }

        if (tiempoIntro > 0f) {
            tiempoIntro -= delta;
        } else if (!rondaTerminada) {
            tiempoRestante -= delta;
            if (tiempoRestante <= 0f) {
                tiempoRestante = 0f;
                terminarRonda();
            } else {
                actualizarJuego(delta);
            }
        } else {
            tiempoResultado += delta;
            boolean saltar = !sesion.esModoJugar()
                && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER));
            if (tiempoResultado >= SEGUNDOS_RESULTADO || saltar) {
                if (sesion.esModoJugar()) {
                    sesion.registrarResultado(puntosGanadosP1, puntosGanadosP2);
                    irA(sesion.siguientePantalla(juego));
                    return;
                }
                iniciarNuevaRonda();
            }
        }

        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);
        camara.update();
        batch.setProjectionMatrix(camara.combined);
        formas.setProjectionMatrix(camara.combined);

        dibujarJuego(batch);
        dibujarHUD();

        if (tiempoIntro > 0f) dibujarIntro();
        else if (rondaTerminada) dibujarFinDeRonda();
    }

    public void dibujarHUD() {
        batch.begin();

        if (sesion.esModoJugar()) {
            fuente.setColor(Color.WHITE);
            usarFuente(Main.fuenteChica);
            dibujarCentrado("Juego " + (sesion.getIndice() + 1) + "/" + sesion.getTotalJuegos()
                + "    Total:  P1 " + sesion.totalP1() + " - " + sesion.totalP2() + " P2", 478f);
        }

        usarFuente(Main.fuenteMediana);
        fuente.setColor(0.3f, 0.9f, 0.3f, 1f);
        fuente.draw(batch, getTextoP1(), MARGEN_HUD, ALTURA_HUD);

        int segundos = (int) Math.ceil(tiempoRestante);
        if (tiempoRestante <= 5f && !rondaTerminada) fuente.setColor(1f, 0.3f, 0.3f, 1f);
        else fuente.setColor(1f, 0.9f, 0.2f, 1f);
        dibujarCentrado("TIEMPO: " + segundos + "s", ALTURA_HUD);

        fuente.setColor(0.3f, 0.7f, 1f, 1f);
        dibujarDerecha(getTextoP2(), ALTURA_HUD);

        batch.end();
    }

    private void dibujarIntro() {
        dibujarCaja(160f, 170f, 480f, 140f);
        MinigameType tipo = sesion.tipoActual();
        batch.begin();
        fuente.setColor(Color.WHITE);
        usarFuente(Main.fuenteCartel);
        dibujarCentrado(tipo != null ? tipo.getNombre() : "", 275f);
        fuente.setColor(1f, 0.85f, 0.2f, 1f);
        usarFuente(Main.fuenteMediana);
        dibujarCentrado("¡PREPARATE!", 215f);
        usarFuente(Main.fuenteMediana);
        batch.end();
    }

    public void dibujarFinDeRonda() {
        dibujarCaja(160f, 130f, 480f, 220f);
        batch.begin();

        fuente.setColor(Color.WHITE);
        usarFuente(Main.fuenteGrande);
        dibujarCentrado(mensajeGanador, 320f);

        fuente.setColor(0.85f, 0.85f, 0.85f, 1f);
        usarFuente(Main.fuenteChica);
        dibujarCentrado(getResumen(), 275f);

        fuente.setColor(1f, 0.85f, 0.2f, 1f);
        usarFuente(Main.fuenteMediana);
        if (sesion.esModoJugar()) {
            dibujarCentrado("PARTIDA:  P1 [" + (sesion.totalP1() + puntosGanadosP1) + "]  -  P2 ["
                + (sesion.totalP2() + puntosGanadosP2) + "]", 235f);
        } else {
            dibujarCentrado("VICTORIAS TOTALES:  P1 [" + victoriasP1 + "]  -  P2 [" + victoriasP2 + "]", 235f);
        }

        fuente.setColor(0.7f, 1f, 0.7f, 1f);
        usarFuente(Main.fuenteChica);
        dibujarCentrado(sesion.esModoJugar() ? "Siguiente juego en un instante..."
            : "[ ESPACIO ] Siguiente Ronda       [ ESC ] Menu Principal", 175f);

        usarFuente(Main.fuenteMediana);
        batch.end();
    }

    private void dibujarCaja(float x, float y, float w, float h) {
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.05f, 0.05f, 0.05f, 0.92f);
        formas.rect(x, y, w, h);
        formas.end();
        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(1f, 0.84f, 0f, 1f);
        formas.rect(x, y, w, h);
        formas.end();
    }

    private void dibujarCentrado(String texto, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, (ANCHO_VIRTUAL - layout.width) / 2f, y);
    }

    protected void dibujarDerecha(String texto, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, ANCHO_VIRTUAL - MARGEN_HUD - layout.width, y);
    }

    @Override public void dispose() {
        batch.dispose();
        formas.dispose();
    }
    @Override public void show() {}
    @Override public void resize(int width, int height) { vista.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    protected void usarFuente(BitmapFont nueva) {
        nueva.setColor(fuente.getColor());
        fuente = nueva;
    }

    protected String sumarPuntos(int jugador, int puntos) {
        if (jugador == 1) victoriasP1 += puntos;
        else victoriasP2 += puntos;
        return "¡JUGADOR " + jugador + " GANA (+" + puntos + (puntos == 1 ? " PUNTO" : " PUNTOS") + ")!";
    }

    protected int puntosPorTiempo() {
        float progreso = 1f - tiempoRestante / getDuracionRonda();
        if (progreso < 1f / 3f) return 3;
        if (progreso < 2f / 3f) return 2;
        return 1;
    }

    protected abstract float getDuracionRonda();
    protected abstract void actualizarJuego(float delta);
    protected abstract void dibujarJuego(SpriteBatch batch);
    protected abstract String determinarGanador();
    protected abstract void reiniciarRonda();
    protected abstract String getTextoP1();
    protected abstract String getTextoP2();
    protected abstract String getResumen();
}