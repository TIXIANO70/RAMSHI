package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ShipsScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int PUNTOS_MAX = 3;
    private static final float TAMANO_BARCO = 48f;
    private static final float VELOCIDAD_BARCO = 520f;
    private static final float TAMANO_BALA_MIN = 16f;
    private static final float TAMANO_BALA_MAX = 32f;
    private static final float VELOCIDAD_CAIDA_INICIO = 260f;
    private static final float VELOCIDAD_CAIDA_FIN = 1150f;
    private static final float CURVA_DIFICULTAD = 2.2f;
    private static final float INTERVALO_BALAS_INICIO = 0.5f;
    private static final float INTERVALO_BALAS_FIN = 0.15f;
    private static final float PRIMERA_BALA = 0.5f;
    private static final float MARGEN_GOLPE = 4f;
    private static final float BARCO_Y = 40f;
    private static final float BALAS_Y = 400f;
    private static final float MARGEN_ZONA = 20f;
    private static final Color COLOR_GOLPE = new Color(1f, 0.35f, 0.35f, 1f);
    private static final int[][] TECLAS = {
        {Input.Keys.A, Input.Keys.D},
        {Input.Keys.LEFT, Input.Keys.RIGHT}
    };
    private static final float MITAD_ANCHO = ANCHO_VIRTUAL / 2f;
    private static final float ANCHO_ZONA = MITAD_ANCHO - 2 * MARGEN_ZONA;

    private static class EventoBala {
        final float tiempo, xNorm, tamano;
        EventoBala(float tiempo, float xNorm, float tamano) { this.tiempo = tiempo; this.xNorm = xNorm; this.tamano = tamano; }
    }

    private static class BalaCanon {
        float x, y;
        final float tamano;
        BalaCanon(float x, float y, float tamano) { this.x = x; this.y = y; this.tamano = tamano; }
    }

    private final Texture fondo = cargarTextura("ships/ships_bg.png", true);
    private final Texture bala = cargarTextura("ships/ships_cannon.png", false);
    private final Texture[] barcos = {
        cargarTextura("ships/ships_p1.png", false), cargarTextura("ships/ships_p2.png", false)
    };

    private final List<EventoBala> eventos = new ArrayList<>();
    private final List<List<BalaCanon>> balas = new ArrayList<>();
    private final float[] barcoX = new float[2];
    private final boolean[] vivo = new boolean[2];
    private final Rectangle cajaBarco = new Rectangle();
    private final Rectangle cajaBala = new Rectangle();
    private int siguienteEvento;
    private float transcurrido;
    private float tiempoFinal;
    private int ganador;

    public ShipsScreen(Game juego) {
        super(juego);
        balas.add(new ArrayList<BalaCanon>());
        balas.add(new ArrayList<BalaCanon>());
        reiniciarRonda();
    }

    private Texture cargarTextura(String ruta, boolean suave) {
        Texture t = new Texture(ruta);
        Texture.TextureFilter f = suave ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
        t.setFilter(f, f);
        return t;
    }

    @Override protected String getConsigna() { return "¡ESQUIVÁ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.dos("mover", Controles.A, Controles.D, Controles.IZQUIERDA, Controles.DERECHA)
        };
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        transcurrido += delta;
        lanzarBalas();

        for (int p = 0; p < 2; p++) {
            moverBarco(p, delta);
            actualizarBalas(p, delta);
        }

        if (!vivo[0] || !vivo[1]) {
            ganador = (!vivo[0] && !vivo[1]) ? 0 : (vivo[0] ? 1 : 2);
            tiempoFinal = transcurrido;
            terminarRonda();
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(fondo, 0, 0, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        for (int p = 0; p < 2; p++) {
            for (BalaCanon b : balas.get(p)) {
                batch.draw(bala, b.x, b.y, b.tamano, b.tamano);
            }
            batch.setColor(vivo[p] ? Color.WHITE : COLOR_GOLPE);
            batch.draw(barcos[p], barcoX[p], BARCO_Y, TAMANO_BARCO, TAMANO_BARCO);
            batch.setColor(Color.WHITE);
        }
        batch.end();

        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(0.9f, 0.9f, 0.9f, 1f);
        formas.line(MITAD_ANCHO, 16f, MITAD_ANCHO, 432f);
        formas.end();
    }

    @Override
    protected String determinarGanador() {
        if (ganador == 0) return "¡EMPATE EN LA RONDA!";

        explicacion = "El rival se hundió a los " + String.format("%.1f s", tiempoFinal) + " " + reglaTiempo();
        return sumarPuntos(ganador, puntosGanador());
    }

    @Override
    protected void reiniciarRonda() {
        transcurrido = 0f;
        tiempoFinal = 0f;
        siguienteEvento = 0;
        ganador = 0;
        for (int p = 0; p < 2; p++) {
            vivo[p] = true;
            barcoX[p] = zonaIzquierda(p) + (ANCHO_ZONA - TAMANO_BARCO) / 2f;
            balas.get(p).clear();
        }
        generarPatron();
    }

    @Override protected String getTextoP1() { return textoEstado(0, victoriasP1); }
    @Override protected String getTextoP2() { return textoEstado(1, victoriasP2); }

    @Override
    protected String getResumen() {
        return "P1 (" + (vivo[0] ? "sobrevivió" : "hundido")
            + ") vs P2 (" + (vivo[1] ? "sobrevivió" : "hundido") + ")";
    }

    @Override
    public void dispose() {
        super.dispose();
        fondo.dispose();
        bala.dispose();
        for (Texture t : barcos) t.dispose();
    }

    private void generarPatron() {
        eventos.clear();
        float t = PRIMERA_BALA;
        while (t < DURACION_RONDA) {
            eventos.add(new EventoBala(t, MathUtils.random(), MathUtils.random(TAMANO_BALA_MIN, TAMANO_BALA_MAX)));
            t += MathUtils.lerp(INTERVALO_BALAS_INICIO, INTERVALO_BALAS_FIN, t / DURACION_RONDA);
        }
    }

    private void lanzarBalas() {
        while (siguienteEvento < eventos.size() && eventos.get(siguienteEvento).tiempo <= transcurrido) {
            EventoBala e = eventos.get(siguienteEvento++);
            for (int p = 0; p < 2; p++) {
                float x = zonaIzquierda(p) + e.xNorm * (ANCHO_ZONA - e.tamano);
                balas.get(p).add(new BalaCanon(x, BALAS_Y, e.tamano));
            }
        }
    }

    private void moverBarco(int p, float delta) {
        if (!vivo[p]) return;
        if (Gdx.input.isKeyPressed(TECLAS[p][0])) barcoX[p] -= VELOCIDAD_BARCO * delta;
        if (Gdx.input.isKeyPressed(TECLAS[p][1])) barcoX[p] += VELOCIDAD_BARCO * delta;
        float izquierda = zonaIzquierda(p);
        barcoX[p] = MathUtils.clamp(barcoX[p], izquierda, izquierda + ANCHO_ZONA - TAMANO_BARCO);
    }

    private void actualizarBalas(int p, float delta) {
        List<BalaCanon> lista = balas.get(p);
        float caida = velocidadCaida() * delta;
        for (int i = lista.size() - 1; i >= 0; i--) {
            BalaCanon b = lista.get(i);
            b.y -= caida;
            if (vivo[p] && golpea(p, b)) vivo[p] = false;
            if (b.y + b.tamano < BARCO_Y) lista.remove(i);
        }
    }

    private boolean golpea(int p, BalaCanon b) {
        cajaBarco.set(barcoX[p] + MARGEN_GOLPE, BARCO_Y + MARGEN_GOLPE,
            TAMANO_BARCO - 2 * MARGEN_GOLPE, TAMANO_BARCO - 2 * MARGEN_GOLPE);
        cajaBala.set(b.x + MARGEN_GOLPE, b.y + MARGEN_GOLPE,
            b.tamano - 2 * MARGEN_GOLPE, b.tamano - 2 * MARGEN_GOLPE);
        return cajaBarco.overlaps(cajaBala);
    }

    private float velocidadCaida() {
        float progreso = MathUtils.clamp(transcurrido / DURACION_RONDA, 0f, 1f);
        return MathUtils.lerp(VELOCIDAD_CAIDA_INICIO, VELOCIDAD_CAIDA_FIN, (float) StrictMath.pow(progreso, CURVA_DIFICULTAD));
    }

    private int puntosGanador() {
        int puntos = PUNTOS_MAX - (int) (tiempoFinal / DURACION_RONDA * PUNTOS_MAX);
        return MathUtils.clamp(puntos, 1, PUNTOS_MAX);
    }

    private String textoEstado(int p, int victorias) {
        return "P" + (p + 1) + ": " + (vivo[p] ? "A FLOTE" : "HUNDIDO") + " (Victorias: " + victorias + ")";
    }

    private float zonaIzquierda(int p) {
        return p * MITAD_ANCHO + MARGEN_ZONA;
    }
}
