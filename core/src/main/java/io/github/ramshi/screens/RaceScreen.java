package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.ramshi.Main;

import java.util.ArrayList;

public class RaceScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final float VELOCIDAD_INICIAL = 5f;
    private static final float ACELERACION = 5f;
    private static final float INTERVALO_VELOCIDAD = 1f;
    private static final float SEPARACION_INICIAL = 320f;
    private static final float SEPARACION_FINAL = 260f;
    private static final float VENTAJA_PARA_3 = 1.5f;
    private static final float VENTAJA_PARA_2 = 1.2f;
    private static final float VELOCIDAD_GENERAL = 32f;
    private static final int CARRILES = 3;
    private static final float ANCHO_CARRIL = 128f;
    private static final float ANCHO_PISTA = ANCHO_CARRIL * CARRILES;
    private static final float P1_X = 0f;
    private static final float P2_X = 432f;
    private static final float AUTO_Y = 80f;
    private static final float TAMANO_AUTO_ORIGINAL = 32f;
    private static final float MARGEN_GOLPE = 10f;
    private static final float PARTE_TRASERA_LIBRE = 0.35f;
    private static final int[][] TECLAS = {
        {Input.Keys.A, Input.Keys.D},
        {Input.Keys.LEFT, Input.Keys.RIGHT}
    };

    private static class Obstaculo {
        float x, y, w, h;
        Rectangle rect;

        Obstaculo(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.rect = new Rectangle(x, y, w, h);
        }
    }

    private final Texture pista = cargarTextura("race/pista.png");
    private final Texture auto1 = cargarTextura("race/car1.png");
    private final Texture auto2 = cargarTextura("race/car2.png");
    private final Texture obstaculoTex = cargarTextura("race/obstacle.png");

    private final float escalaX = ANCHO_PISTA / pista.getWidth();
    private final float tamanoAuto = TAMANO_AUTO_ORIGINAL * escalaX;
    private final ArrayList<Obstaculo> obstaculosP1 = new ArrayList<Obstaculo>();
    private final ArrayList<Obstaculo> obstaculosP2 = new ArrayList<Obstaculo>();
    private float distanciaP1 = 0;
    private float distanciaP2 = 0;
    private int carrilP1 = 1;
    private int carrilP2 = 1;
    private float velocidadP1 = VELOCIDAD_INICIAL;
    private float velocidadP2 = VELOCIDAD_INICIAL;
    private final float[] recorrido = new float[2];
    private float tiempoVelocidad = 0f;
    private float autoX1;
    private float autoX2;
    private Rectangle rectP1;
    private Rectangle rectP2;

    public RaceScreen(Game juego) {
        super(juego);
        reiniciarRonda();
    }

    private Texture cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    private Rectangle hitbox(float autoX) {
        float trasera = tamanoAuto * PARTE_TRASERA_LIBRE;
        return new Rectangle(autoX + MARGEN_GOLPE, AUTO_Y + trasera,
            tamanoAuto - 2 * MARGEN_GOLPE, tamanoAuto - trasera - MARGEN_GOLPE);
    }

    private float carrilX(float pistaX, int carril) {
        return pistaX + TAMANO_AUTO_ORIGINAL / 2f * escalaX + TAMANO_AUTO_ORIGINAL * carril * escalaX;
    }

    @Override protected String getConsigna() { return "¡ESQUIVÁ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.dos("carril", Controles.A, Controles.D, Controles.IZQUIERDA, Controles.DERECHA)
        };
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        if (Gdx.input.isKeyJustPressed(TECLAS[0][0])) carrilP1 = MathUtils.clamp(carrilP1 - 1, 0, CARRILES - 1);
        if (Gdx.input.isKeyJustPressed(TECLAS[0][1])) carrilP1 = MathUtils.clamp(carrilP1 + 1, 0, CARRILES - 1);
        if (Gdx.input.isKeyJustPressed(TECLAS[1][0])) carrilP2 = MathUtils.clamp(carrilP2 - 1, 0, CARRILES - 1);
        if (Gdx.input.isKeyJustPressed(TECLAS[1][1])) carrilP2 = MathUtils.clamp(carrilP2 + 1, 0, CARRILES - 1);
        autoX1 = carrilX(P1_X, carrilP1);
        autoX2 = carrilX(P2_X, carrilP2);

        tiempoVelocidad += delta;
        if (tiempoVelocidad >= INTERVALO_VELOCIDAD) {
            velocidadP1 += ACELERACION;
            velocidadP2 += ACELERACION;
            distanciaP1 += velocidadP1;
            distanciaP2 += velocidadP2;
            tiempoVelocidad -= INTERVALO_VELOCIDAD;
        }
        velocidadP1 = actualizarObstaculos(0, P1_X, obstaculosP1, rectP1, velocidadP1, delta);
        velocidadP2 = actualizarObstaculos(1, P2_X, obstaculosP2, rectP2, velocidadP2, delta);
    }

    private void crearObstaculo(float pistaX, ArrayList<Obstaculo> lista) {
        float x = carrilX(pistaX, MathUtils.random(0, CARRILES - 1));
        lista.add(new Obstaculo(x, ALTO_VIRTUAL, tamanoAuto, tamanoAuto));
    }

    private float actualizarObstaculos(int p, float pistaX, ArrayList<Obstaculo> lista, Rectangle auto, float velocidad, float delta) {
        float caida = velocidad * delta * VELOCIDAD_GENERAL;
        recorrido[p] += caida;
        float separacion = MathUtils.lerp(SEPARACION_INICIAL, SEPARACION_FINAL, 1f - tiempoRestante / DURACION_RONDA);
        if (recorrido[p] >= separacion) {
            recorrido[p] = 0f;
            crearObstaculo(pistaX, lista);
        }
        for (int i = lista.size() - 1; i >= 0; i--) {
            Obstaculo obs = lista.get(i);
            obs.y -= caida;
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);
            if (auto.overlaps(obs.rect)) {
                velocidad = 0f;
                lista.remove(i);
            } else if (obs.y + obs.h < 0) {
                lista.remove(i);
            }
        }
        return velocidad;
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        float altoPista = pista.getHeight();
        for (float y = 0; y < ALTO_VIRTUAL; y += altoPista) {
            batch.draw(pista, P1_X, y, ANCHO_PISTA, altoPista);
            batch.draw(pista, P2_X, y, ANCHO_PISTA, altoPista);
        }

        batch.draw(auto1, autoX1, AUTO_Y, tamanoAuto, tamanoAuto);
        rectP1 = hitbox(autoX1);
        batch.draw(auto2, autoX2, AUTO_Y, tamanoAuto, tamanoAuto);
        rectP2 = hitbox(autoX2);

        usarFuente(Main.fuenteChica);
        fuente.setColor(Color.WHITE);
        fuente.draw(batch, "Velocidad: " + (int) velocidadP1, P1_X + 8f, 62f);
        fuente.draw(batch, "Velocidad: " + (int) velocidadP2, P2_X + 8f, 62f);

        for (Obstaculo obs : obstaculosP1) batch.draw(obstaculoTex, obs.x, obs.y, obs.w, obs.h);
        for (Obstaculo obs : obstaculosP2) batch.draw(obstaculoTex, obs.x, obs.y, obs.w, obs.h);
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (distanciaP1 == distanciaP2) return "¡EMPATE EN LA RONDA!";
        float mayor = Math.max(distanciaP1, distanciaP2), menor = Math.min(distanciaP1, distanciaP2);
        int pts = 1;
        if (mayor >= menor * VENTAJA_PARA_3) pts = 3;
        else if (mayor >= menor * VENTAJA_PARA_2) pts = 2;
        String ventaja = (menor > 0) ? "+" + Math.round((mayor / menor - 1f) * 100) + "%" : "el rival no avanzó";
        explicacion = "Recorrió " + (int) mayor + " m contra " + (int) menor + " m (" + ventaja + "). +50% = 3 pts, +20% = 2, si no 1.";
        return sumarPuntos(distanciaP1 > distanciaP2 ? 1 : 2, pts);
    }

    @Override
    protected void reiniciarRonda() {
        distanciaP1 = 0;
        distanciaP2 = 0;
        obstaculosP1.clear();
        obstaculosP2.clear();
        carrilP1 = 1;
        carrilP2 = 1;
        autoX1 = carrilX(P1_X, carrilP1);
        autoX2 = carrilX(P2_X, carrilP2);
        velocidadP1 = VELOCIDAD_INICIAL;
        velocidadP2 = VELOCIDAD_INICIAL;
        recorrido[0] = recorrido[1] = 0f;
        tiempoVelocidad = 0f;
    }

    @Override protected String getTextoP1() { return "P1: " + (int) distanciaP1 + " m (Victorias: " + victoriasP1 + ")"; }
    @Override protected String getTextoP2() { return "P2: " + (int) distanciaP2 + " m (Victorias: " + victoriasP2 + ")"; }
    @Override protected String getResumen() { return "Distancia: P1 (" + (int) distanciaP1 + "m) vs P2 (" + (int) distanciaP2 + "m)"; }

    @Override
    public void dispose() {
        super.dispose();
        pista.dispose();
        auto1.dispose();
        auto2.dispose();
        obstaculoTex.dispose();
    }
}
