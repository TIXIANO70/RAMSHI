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
import io.github.ramshi.menus.MainMenuScreen;
import io.github.ramshi.session.GameSession;

public class ResultsScreen implements Screen {

    private static final float ANCHO_VIRTUAL = 800f;
    private static final float ALTO_VIRTUAL = 480f;

    private static final float BLOQUEO_INICIAL = 1.5f;
    private static final float ALTO_FILA = 20f;
    private static final float FILAS_INICIO_Y = 345f;
    private static final float COL_NUM = 60f, COL_NOMBRE = 100f, COL_P1 = 470f, COL_P2 = 550f, COL_GANADOR = 630f;

    private final Game juego;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer formas = new ShapeRenderer();
    private float tiempoEntrada = BaseMinigameScreen.DURACION_FUNDIDO;
    private BitmapFont fuente = Main.fuenteChica;
    private final OrthographicCamera camara = new OrthographicCamera();
    private final Viewport vista = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
    private final GameSession sesion;
    private final String textoGanador;
    private final String textoMotivo;
    private final int juegosP1, juegosP2;
    private float tiempoBloqueo = BLOQUEO_INICIAL;

    public ResultsScreen(Game juego, GameSession sesion) {
        this.juego = juego;
        this.sesion = sesion;
        vista.apply();
        camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);

        int p1 = sesion.totalP1(), p2 = sesion.totalP2();
        int g1 = 0, g2 = 0;
        for (GameSession.Resultado r : sesion.getResultados()) {
            if (r.puntosP1 > r.puntosP2) g1++;
            else if (r.puntosP2 > r.puntosP1) g2++;
        }
        juegosP1 = g1;
        juegosP2 = g2;

        if (p1 != p2) {
            int w = p1 > p2 ? 1 : 2;
            textoGanador = "¡GANA EL JUGADOR " + w + "!";
            textoMotivo = "Por " + Math.abs(p1 - p2) + " puntos de diferencia (" + p1 + " a " + p2 + ")";
        } else if (g1 != g2) {
            int w = g1 > g2 ? 1 : 2;
            textoGanador = "¡GANA EL JUGADOR " + w + "!";
            textoMotivo = "Empate en puntos (" + p1 + "), desempata por juegos ganados (" + g1 + " a " + g2 + ")";
        } else {
            textoGanador = "¡EMPATE!";
            textoMotivo = "Mismos puntos (" + p1 + ") y mismos juegos ganados (" + g1 + ")";
        }
    }

    @Override
    public void render(float delta) {
        tiempoBloqueo -= delta;
        if (tiempoBloqueo <= 0f && (Gdx.input.justTouched()
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            juego.setScreen(new MainMenuScreen(juego));
            dispose();
            return;
        }

        ScreenUtils.clear(0.06f, 0.06f, 0.08f, 1f);
        camara.update();
        batch.setProjectionMatrix(camara.combined);
        batch.begin();

        fuente.setColor(1f, 0.85f, 0.2f, 1f);
        usarFuente(Main.fuenteCartel);
        dibujarCentrado(textoGanador, 465f);
        fuente.setColor(Color.WHITE);
        usarFuente(Main.fuenteChica);
        dibujarCentrado(textoMotivo, 422f);
        dibujarCentrado("Juegos ganados:  P1 " + juegosP1 + " - " + juegosP2 + " P2", 400f);

        usarFuente(Main.fuenteChica);
        fuente.setColor(Color.LIGHT_GRAY);
        float y = 372f;
        fuente.draw(batch, "#", COL_NUM, y);
        fuente.draw(batch, "MINIJUEGO", COL_NOMBRE, y);
        fuente.draw(batch, "P1", COL_P1, y);
        fuente.draw(batch, "P2", COL_P2, y);
        fuente.draw(batch, "GANO", COL_GANADOR, y);

        int i = 0;
        for (GameSession.Resultado r : sesion.getResultados()) {
            float ry = FILAS_INICIO_Y - i * ALTO_FILA;
            fuente.setColor(Color.WHITE);
            fuente.draw(batch, String.valueOf(i + 1), COL_NUM, ry);
            fuente.draw(batch, r.tipo.getNombre(), COL_NOMBRE, ry);
            fuente.setColor(0.3f, 0.9f, 0.3f, 1f);
            fuente.draw(batch, "+" + r.puntosP1, COL_P1, ry);
            fuente.setColor(0.3f, 0.7f, 1f, 1f);
            fuente.draw(batch, "+" + r.puntosP2, COL_P2, ry);
            fuente.setColor(Color.WHITE);
            fuente.draw(batch, r.puntosP1 > r.puntosP2 ? "P1" : (r.puntosP2 > r.puntosP1 ? "P2" : "-"), COL_GANADOR, ry);
            i++;
        }

        fuente.setColor(1f, 0.85f, 0.2f, 1f);
        fuente.draw(batch, "TOTAL", COL_NOMBRE, 45f);
        fuente.draw(batch, String.valueOf(sesion.totalP1()), COL_P1, 45f);
        fuente.draw(batch, String.valueOf(sesion.totalP2()), COL_P2, 45f);

        if (tiempoBloqueo <= 0f) {
            fuente.setColor(0.7f, 1f, 0.7f, 1f);
            dibujarCentrado("Click o ESPACIO para volver al menu", 20f);
        }
        batch.end();

        tiempoEntrada -= delta;
        formas.setProjectionMatrix(camara.combined);
        BaseMinigameScreen.dibujarFundido(formas, tiempoEntrada / BaseMinigameScreen.DURACION_FUNDIDO);
    }

    private void dibujarCentrado(String texto, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, (ANCHO_VIRTUAL - layout.width) / 2f, y);
    }

    @Override public void resize(int width, int height) { vista.update(width, height, true); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose();
        formas.dispose();
    }

    private void usarFuente(BitmapFont nueva) {
        nueva.setColor(fuente.getColor());
        fuente = nueva;
    }
}