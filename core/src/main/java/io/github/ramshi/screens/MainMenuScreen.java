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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.ramshi.session.GameSession;
import io.github.ramshi.session.MinigameType;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen implements Screen {

    public static final float ANCHO_VIRTUAL = 800f;
    public static final float ALTO_VIRTUAL = 480f;

    private static final int MIN_RONDAS = 3;
    private static final int MAX_RONDAS = 15;
    private static final int RONDAS_INICIALES = 8;
    private static final float BOTON_ANCHO = 260f;
    private static final float BOTON_ALTO = 55f;

    private enum Pagina { PRINCIPAL, ENDLESS }

    private static class Boton {
        final Rectangle rect;
        final String etiqueta;
        final Runnable accion;
        Boton(float x, float y, float w, float h, String etiqueta, Runnable accion) {
            this.rect = new Rectangle(x, y, w, h);
            this.etiqueta = etiqueta;
            this.accion = accion;
        }
    }

    private final Game juego;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont fuente = new BitmapFont();
    private final ShapeRenderer formas = new ShapeRenderer();
    private final OrthographicCamera camara = new OrthographicCamera();
    private final Viewport vista = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
    private final Vector3 posMouse = new Vector3();
    private final List<Boton> botonesPrincipales = new ArrayList<>();
    private final List<Boton> botonesEndless = new ArrayList<>();
    private Pagina pagina = Pagina.PRINCIPAL;
    private int rondas = RONDAS_INICIALES;

    public MainMenuScreen(Game juego) {
        this.juego = juego;
        vista.apply();
        camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);

        float x = (ANCHO_VIRTUAL - BOTON_ANCHO) / 2f;
        botonesPrincipales.add(new Boton(x, 280f, BOTON_ANCHO, BOTON_ALTO, "JUGAR",
            () -> abrir(GameSession.iniciarJugar(rondas).primeraPantalla(juego))));
        botonesPrincipales.add(new Boton(270f, 232f, 55f, 40f, "-",
            () -> rondas = Math.max(MIN_RONDAS, rondas - 1)));
        botonesPrincipales.add(new Boton(475f, 232f, 55f, 40f, "+",
            () -> rondas = Math.min(MAX_RONDAS, rondas + 1)));
        botonesPrincipales.add(new Boton(x, 150f, BOTON_ANCHO, BOTON_ALTO, "ENDLESS", () -> pagina = Pagina.ENDLESS));
        botonesPrincipales.add(new Boton(x, 80f, BOTON_ANCHO, BOTON_ALTO, "SALIR", () -> Gdx.app.exit()));

        MinigameType[] tipos = MinigameType.values();
        for (int i = 0; i < tipos.length; i++) {
            final MinigameType tipo = tipos[i];
            float bx = (i % 2 == 0) ? 80f : 420f;
            float by = 320f - (i / 2) * 45f;
            botonesEndless.add(new Boton(bx, by, 300f, 38f, tipo.getNombre(),
                () -> abrir(GameSession.iniciarEndless(tipo).primeraPantalla(juego))));
        }
        botonesEndless.add(new Boton(270f, 20f, BOTON_ANCHO, 40f, "VOLVER", () -> pagina = Pagina.PRINCIPAL));
    }

    private void abrir(Screen siguiente) {
        juego.setScreen(siguiente);
        dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.06f, 0.08f, 1f);
        camara.update();
        batch.setProjectionMatrix(camara.combined);
        formas.setProjectionMatrix(camara.combined);

        if (pagina == Pagina.ENDLESS && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) pagina = Pagina.PRINCIPAL;

        List<Boton> botones = (pagina == Pagina.PRINCIPAL) ? botonesPrincipales : botonesEndless;
        posMouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        vista.unproject(posMouse);
        Boton resaltado = null;
        for (Boton b : botones) if (b.rect.contains(posMouse.x, posMouse.y)) resaltado = b;

        formas.begin(ShapeRenderer.ShapeType.Filled);
        for (Boton b : botones) {
            if (b == resaltado) formas.setColor(0.12f, 0.35f, 0.18f, 1f);
            else formas.setColor(0.12f, 0.16f, 0.14f, 1f);
            formas.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        formas.end();
        formas.begin(ShapeRenderer.ShapeType.Line);
        for (Boton b : botones) {
            if (b == resaltado) formas.setColor(0.3f, 0.9f, 0.4f, 1f);
            else formas.setColor(0.3f, 0.5f, 0.35f, 1f);
            formas.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        formas.end();

        batch.begin();
        fuente.setColor(1f, 0.85f, 0.2f, 1f);
        fuente.getData().setScale(3.6f);
        dibujarCentrado("RAMSHI", 440f);
        fuente.setColor(0.4f, 0.8f, 1f, 1f);
        fuente.getData().setScale(1.3f);
        dibujarCentrado(pagina == Pagina.PRINCIPAL ? "MICROJUEGOS 1v1 LOCAL" : "ELEGI UN MINIJUEGO", 365f);

        for (Boton b : botones) {
            fuente.getData().setScale(b.rect.height > 50f ? 1.6f : 1.2f);
            fuente.setColor(b == resaltado ? Color.WHITE : Color.LIGHT_GRAY);
            GlyphLayout layout = new GlyphLayout(fuente, b.etiqueta);
            fuente.draw(batch, layout, b.rect.x + (b.rect.width - layout.width) / 2f,
                b.rect.y + (b.rect.height + layout.height) / 2f);
        }
        if (pagina == Pagina.PRINCIPAL) {
            fuente.setColor(Color.WHITE);
            fuente.getData().setScale(1.3f);
            dibujarCentrado("RONDAS: " + rondas, 260f);

            fuente.setColor(0.6f, 0.6f, 0.65f, 1f);
            fuente.getData().setScale(1.0f);
            dibujarCentrado("P1: WASD  |  P2: FLECHAS  |  10 SEGUNDOS POR JUEGO", 35f);
        }
        batch.end();

        if (Gdx.input.justTouched() && resaltado != null) resaltado.accion.run();
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

    @Override
    public void dispose() {
        batch.dispose();
        fuente.dispose();
        formas.dispose();
    }
}