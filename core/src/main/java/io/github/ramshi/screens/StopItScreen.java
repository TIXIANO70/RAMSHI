package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.Main;

public class StopItScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int MAX_INTENTOS = 12;
    private static final float VELOCIDAD_INICIAL = 0.9f;
    private static final float AUMENTO_VELOCIDAD = 0.15f;
    private static final float MEDIA_ZONA_INICIAL = 0.09f;
    private static final float MEDIA_ZONA_MIN = 0.04f;
    private static final float ACHIQUE_ZONA = 0.006f;
    private static final float PAUSA_RESULTADO = 0.5f;
    private static final int PUNTOS_PERFECTO = 3;
    private static final int PUNTOS_BIEN = 2;
    private static final int PUNTOS_CERCA = 1;
    private static final float PROPORCION_PERFECTO = 0.3f;
    private static final float PROPORCION_CERCA = 2f;
    private static final int DIFERENCIA_PARA_3 = 6;
    private static final int DIFERENCIA_PARA_2 = 3;
    private static final int[] TECLAS = {Input.Keys.W, Input.Keys.UP};
    private static final float[] BARRA_X = {40f, 440f};
    private static final float BARRA_ANCHO = 320f;
    private static final float BARRA_Y = 220f;
    private static final float BARRA_ALTO = 36f;

    private final float[] centroZona = new float[MAX_INTENTOS];

    private final float[] pos = new float[2];
    private final int[] dir = {1, 1};
    private final boolean[] frenado = new boolean[2];
    private final float[] tiempoPausa = new float[2];
    private final int[] intento = new int[2];
    private final int[] puntaje = new int[2];
    private final String[] ultimoTexto = {"", ""};
    private final Color[] ultimoColor = {Color.WHITE, Color.WHITE};

    private final Texture fondo = cargarTextura("stopit/stopit_bg.png");

    public StopItScreen(Game juego) {
        super(juego);
        reiniciarEstado();
    }

    private Texture cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        for (int p = 0; p < 2; p++) {
            if (frenado[p]) {
                tiempoPausa[p] -= delta;
                if (tiempoPausa[p] <= 0f) {
                    frenado[p] = false;
                    intento[p] = Math.min(intento[p] + 1, MAX_INTENTOS - 1);
                    pos[p] = 0f;
                    dir[p] = 1;
                }
                continue;
            }

            pos[p] += dir[p] * velocidad(p) * delta;
            if (pos[p] >= 1f) { pos[p] = 1f; dir[p] = -1; }
            if (pos[p] <= 0f) { pos[p] = 0f; dir[p] = 1; }

            if (Gdx.input.isKeyJustPressed(TECLAS[p])) {
                evaluar(p);
            }
        }
    }

    private void evaluar(int p) {
        float mitad = mediaZona(p);
        float d = Math.abs(pos[p] - centroZona[intento[p]]);
        int puntos;
        if (d <= mitad * PROPORCION_PERFECTO) {
            puntos = PUNTOS_PERFECTO;
            ultimoTexto[p] = "¡PERFECTO! +" + puntos;
            ultimoColor[p] = new Color(1f, 0.85f, 0.2f, 1f);
        } else if (d <= mitad) {
            puntos = PUNTOS_BIEN;
            ultimoTexto[p] = "¡BIEN! +" + puntos;
            ultimoColor[p] = new Color(0.3f, 0.9f, 0.3f, 1f);
        } else if (d <= mitad * PROPORCION_CERCA) {
            puntos = PUNTOS_CERCA;
            ultimoTexto[p] = "CERCA +" + puntos;
            ultimoColor[p] = new Color(1f, 0.6f, 0.2f, 1f);
        } else {
            puntos = 0;
            ultimoTexto[p] = "FALLASTE";
            ultimoColor[p] = new Color(1f, 0.3f, 0.3f, 1f);
        }
        puntaje[p] += puntos;
        frenado[p] = true;
        tiempoPausa[p] = PAUSA_RESULTADO;
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(fondo, 0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        batch.end();

        formas.begin(ShapeType.Filled);
        for (int p = 0; p < 2; p++) {
            float x = BARRA_X[p];
            formas.setColor(0.18f, 0.18f, 0.2f, 1f);
            formas.rect(x, BARRA_Y, BARRA_ANCHO, BARRA_ALTO);

            float mitad = mediaZona(p);
            float c = centroZona[intento[p]];
            dibujarZona(x, c, mitad * PROPORCION_CERCA, 0.55f, 0.35f, 0.12f);
            dibujarZona(x, c, mitad, 0.2f, 0.7f, 0.25f);
            dibujarZona(x, c, mitad * PROPORCION_PERFECTO, 1f, 0.85f, 0.2f);

            if (frenado[p]) formas.setColor(ultimoColor[p]);
            else formas.setColor(Color.WHITE);
            formas.rect(x + pos[p] * BARRA_ANCHO - 3f, BARRA_Y - 8f, 6f, BARRA_ALTO + 16f);
        }
        formas.setColor(0.35f, 0.35f, 0.35f, 1f);
        formas.rect(ANCHO_VIRTUAL / 2f - 1f, 16f, 2f, 416f);
        formas.end();

        batch.begin();
        usarFuente(Main.fuenteMediana);
        for (int p = 0; p < 2; p++) {
            if (frenado[p]) {
                fuente.setColor(ultimoColor[p]);
                dibujarCentradoEn(batch, ultimoTexto[p], BARRA_X[p], BARRA_ANCHO, 320f);
            }
        }
        usarFuente(Main.fuenteChica);
        fuente.setColor(0.6f, 0.6f, 0.65f, 1f);
        fuente.draw(batch, "P1: [W] frenar", 24f, 24f);
        dibujarDerecha("P2: [FLECHA ARRIBA] frenar", 24f);
        batch.end();
    }

    private void dibujarZona(float barraX, float centro, float mitad, float r, float g, float b) {
        float izquierda = Math.max(0f, centro - mitad);
        float derecha = Math.min(1f, centro + mitad);
        formas.setColor(r, g, b, 1f);
        formas.rect(barraX + izquierda * BARRA_ANCHO, BARRA_Y, (derecha - izquierda) * BARRA_ANCHO, BARRA_ALTO);
    }

    private void dibujarCentradoEn(SpriteBatch batch, String texto, float x, float w, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, x + (w - layout.width) / 2f, y);
    }

    @Override
    protected String determinarGanador() {
        int diferencia = Math.abs(puntaje[0] - puntaje[1]);
        if (diferencia == 0) return "¡EMPATE!";
        int pts = 1;
        if (diferencia >= DIFERENCIA_PARA_3) pts = 3;
        else if (diferencia >= DIFERENCIA_PARA_2) pts = 2;
        return sumarPuntos(puntaje[0] > puntaje[1] ? 1 : 2, pts);
    }

    @Override
    protected void reiniciarRonda() {
        reiniciarEstado();
    }

    @Override
    protected String getTextoP1() {
        return "P1: " + puntaje[0] + " pts (Victorias: " + victoriasP1 + ")";
    }

    @Override
    protected String getTextoP2() {
        return "P2: " + puntaje[1] + " pts (Victorias: " + victoriasP2 + ")";
    }

    @Override
    protected String getResumen() {
        return "Puntos: P1 (" + puntaje[0] + ") vs P2 (" + puntaje[1] + ")";
    }

    private void reiniciarEstado() {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            centroZona[i] = MathUtils.random(0.2f, 0.8f);
        }
        for (int p = 0; p < 2; p++) {
            pos[p] = 0f;
            dir[p] = 1;
            frenado[p] = false;
            tiempoPausa[p] = 0f;
            intento[p] = 0;
            puntaje[p] = 0;
            ultimoTexto[p] = "";
        }
    }

    private float velocidad(int p) {
        return VELOCIDAD_INICIAL + AUMENTO_VELOCIDAD * intento[p];
    }

    private float mediaZona(int p) {
        return Math.max(MEDIA_ZONA_MIN, MEDIA_ZONA_INICIAL - ACHIQUE_ZONA * intento[p]);
    }

    @Override
    public void dispose() {
        super.dispose();
        fondo.dispose();
    }
}