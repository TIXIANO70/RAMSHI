package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.ramshi.Main;

import java.util.ArrayList;
import java.util.List;

public class BombTagScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final float PROPORCION_PARA_3 = 0.7f;
    private static final float PROPORCION_PARA_2 = 0.5f;
    private static final float VELOCIDAD_JUGADOR = 210f;
    private static final float VELOCIDAD_DASH = 480f;
    private static final float DURACION_DASH = 0.28f;
    private static final float RECARGA_DASH = 2.0f;
    private static final float BONUS_PORTADOR = 1.08f;
    private static final float INMUNIDAD_PASE = 0.85f;
    private static final float INMUNIDAD_INICIAL = 1.0f;
    private static final float TIEMPO_DETONACION = 0.05f;
    private static final float RADIO_JUGADOR = 18f;
    private static final float TAMANO_JUGADOR = 40f;
    private static final float INICIO_X = 120f;
    private static final float TAMANO_BOMBA = 36f;
    private static final float MARGEN = 50f;
    private static final int[][] TECLAS = {
        { Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.SPACE },
        { Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.ENTER }
    };
    private static final String[] TEXTO_HUD = { "P1: WASD | [ESPACIO] ", "P2: FLECHAS | [ENTER] " };
    private static final float HUD_X = 55f;
    private static final Color[] COLORES_HUD = { new Color(1f, 0.3f, 0.6f, 1f), new Color(0f, 0.85f, 1f, 1f) };

    private final Rectangle[] pilares = {
        new Rectangle(200f, 150f, 60f, 60f), new Rectangle(200f, 270f, 60f, 60f),
        new Rectangle(540f, 150f, 60f, 60f), new Rectangle(540f, 270f, 60f, 60f),
        new Rectangle(370f, 210f, 60f, 60f)
    };

    private static class Particula {
        float x, y, vx, vy, vida, vidaMax;
        Color color;
    }

    private static class Corredor {
        int indice;
        int[] teclas;
        Texture sprite;
        final Vector2 pos = new Vector2(), dir = new Vector2();
        float tiempoDash, recargaDash;
    }

    private final List<Texture> texturas = new ArrayList<>();
    private final Texture fondoTex = cargarTextura("bombtag/arena_bg.png", true);
    private final Texture bombaTex = cargarTextura("bombtag/bomb.png", false);
    private final Texture pilarTex = cargarTextura("bombtag/pillar.png", false);
    private final Corredor[] corredores = { new Corredor(), new Corredor() };
    private final List<Particula> particulas = new ArrayList<>();
    private final float[] tiempoConBomba = new float[2];
    private Corredor portador;
    private float inmunidad, pulsoBomba;

    public BombTagScreen(Game juego) {
        super(juego);
        for (int i = 0; i < 2; i++) {
            corredores[i].indice = i;
            corredores[i].teclas = TECLAS[i];
            corredores[i].sprite = cargarTextura("bombtag/player_p" + (i + 1) + ".png", true);
        }
        reiniciarRonda();
    }

    private Texture cargarTextura(String ruta, boolean suave) {
        Texture t = new Texture(ruta);
        if (suave) t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texturas.add(t);
        return t;
    }

    @Override
    protected void reiniciarRonda() {
        for (Corredor r : corredores) {
            r.pos.set(r.indice == 0 ? INICIO_X : ANCHO_VIRTUAL - INICIO_X, ALTO_VIRTUAL / 2f);
            r.tiempoDash = r.recargaDash = 0f;
            tiempoConBomba[r.indice] = 0f;
        }
        inmunidad = INMUNIDAD_INICIAL;
        particulas.clear();
        portador = corredores[MathUtils.random(1)];
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        pulsoBomba += delta * 6f;
        inmunidad -= delta;
        tiempoConBomba[portador.indice] += delta;
        for (Corredor r : corredores) actualizarCorredor(r, delta);
        manejarContacto();

        if (MathUtils.randomBoolean(0.35f)) {
            agregarParticula(portador.pos.x + MathUtils.random(-8f, 8f), portador.pos.y + 18f,
                MathUtils.random(-30f, 30f), MathUtils.random(30f, 80f), 0.4f, Color.ORANGE);
        }
        if (tiempoRestante <= TIEMPO_DETONACION) {
            chispas(portador.pos.x, portador.pos.y, Color.RED, 60, 300f, 1.2f);
            terminarRonda();
        }

        for (int i = particulas.size() - 1; i >= 0; i--) {
            Particula p = particulas.get(i);
            p.vida -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            if (p.vida <= 0f) particulas.remove(i);
        }
    }

    private void actualizarCorredor(Corredor r, float delta) {
        r.tiempoDash -= delta;
        r.recargaDash -= delta;

        r.dir.set(0f, 0f);
        if (Gdx.input.isKeyPressed(r.teclas[0])) r.dir.y += 1f;
        if (Gdx.input.isKeyPressed(r.teclas[1])) r.dir.y -= 1f;
        if (Gdx.input.isKeyPressed(r.teclas[2])) r.dir.x -= 1f;
        if (Gdx.input.isKeyPressed(r.teclas[3])) r.dir.x += 1f;
        r.dir.nor();

        if (Gdx.input.isKeyJustPressed(r.teclas[4]) && r.recargaDash <= 0f && !r.dir.isZero()) {
            r.tiempoDash = DURACION_DASH;
            r.recargaDash = RECARGA_DASH;
        }

        float velocidad = (r.tiempoDash > 0f) ? VELOCIDAD_DASH : VELOCIDAD_JUGADOR;
        if (r == portador) velocidad *= BONUS_PORTADOR;

        float sigX = r.pos.x + r.dir.x * velocidad * delta;
        if (cabe(sigX, ANCHO_VIRTUAL) && !chocaConPilar(sigX, r.pos.y)) r.pos.x = sigX;
        float sigY = r.pos.y + r.dir.y * velocidad * delta;
        if (cabe(sigY, ALTO_VIRTUAL) && !chocaConPilar(r.pos.x, sigY)) r.pos.y = sigY;
    }

    private void manejarContacto() {
        Vector2 p1 = corredores[0].pos, p2 = corredores[1].pos;
        float solapamiento = RADIO_JUGADOR * 2f - p1.dst(p2);
        if (solapamiento <= 0f) return;

        Vector2 empuje = new Vector2(p2).sub(p1).nor();
        if (empuje.isZero()) empuje.set(1f, 0f);
        p1.mulAdd(empuje, -solapamiento / 2f);
        p2.mulAdd(empuje, solapamiento / 2f);

        if (inmunidad <= 0f) {
            portador = (portador == corredores[0]) ? corredores[1] : corredores[0];
            inmunidad = INMUNIDAD_PASE;
            chispas((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f, Color.YELLOW, 18, 140f, 0.45f);
        }
    }

    private boolean cabe(float valor, float tamano) {
        return valor - RADIO_JUGADOR >= MARGEN && valor + RADIO_JUGADOR <= tamano - MARGEN;
    }

    private boolean chocaConPilar(float x, float y) {
        for (Rectangle r : pilares) {
            float dx = x - MathUtils.clamp(x, r.x, r.x + r.width);
            float dy = y - MathUtils.clamp(y, r.y, r.y + r.height);
            if (dx * dx + dy * dy < RADIO_JUGADOR * RADIO_JUGADOR) return true;
        }
        return false;
    }

    private void agregarParticula(float x, float y, float vx, float vy, float vida, Color color) {
        Particula p = new Particula();
        p.x = x; p.y = y; p.vx = vx; p.vy = vy;
        p.vida = p.vidaMax = vida;
        p.color = color;
        particulas.add(p);
    }

    private void chispas(float x, float y, Color color, int cantidad, float velocidadMax, float vidaMax) {
        for (int i = 0; i < cantidad; i++) {
            float angulo = MathUtils.random(360f), velocidad = MathUtils.random(velocidadMax);
            agregarParticula(x, y, MathUtils.cosDeg(angulo) * velocidad, MathUtils.sinDeg(angulo) * velocidad,
                MathUtils.random(0.2f, vidaMax), color);
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(fondoTex, 0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        for (Rectangle r : pilares) batch.draw(pilarTex, r.x, r.y, r.width, r.height);

        usarFuente(Main.fuenteChica);
        for (Corredor r : corredores) {
            batch.draw(r.sprite, r.pos.x - TAMANO_JUGADOR / 2f, r.pos.y - TAMANO_JUGADOR / 2f, TAMANO_JUGADOR, TAMANO_JUGADOR);
            String textoDash = (r.recargaDash <= 0f) ? "DASH LISTO" : String.format("DASH: %.1fs", r.recargaDash);
            fuente.setColor(COLORES_HUD[r.indice]);
            if (r.indice == 0) fuente.draw(batch, TEXTO_HUD[0] + textoDash, HUD_X, 32f);
        }

        Vector2 bomba = portador.pos;
        batch.draw(bombaTex, bomba.x - TAMANO_BOMBA / 2f, bomba.y + 14f + MathUtils.sin(pulsoBomba) * 3f, TAMANO_BOMBA, TAMANO_BOMBA);
        usarFuente(Main.fuenteMediana);
        fuente.setColor(COLORES_HUD[portador.indice]);
        fuente.draw(batch, "¡P" + (portador.indice + 1) + " TIENE LA BOMBA! ¡PASALA!", ANCHO_VIRTUAL / 2f - 135f, 465f);
        batch.end();

        formas.begin(ShapeType.Filled);
        for (Particula p : particulas) {
            formas.setColor(p.color.r, p.color.g, p.color.b, p.vida / p.vidaMax);
            formas.circle(p.x, p.y, 3.2f);
        }
        formas.end();
    }

    @Override
    protected String determinarGanador() {
        float proporcion = proporcionConBomba();
        int pts = 1;
        if (proporcion > PROPORCION_PARA_3) pts = 3;
        else if (proporcion > PROPORCION_PARA_2) pts = 2;
        return sumarPuntos(2 - portador.indice, pts);
    }

    private float proporcionConBomba() {
        return tiempoConBomba[portador.indice] / (tiempoConBomba[0] + tiempoConBomba[1]);
    }

    @Override protected String getTextoP1() { return "P1 (Victorias: " + victoriasP1 + ")"; }
    @Override protected String getTextoP2() { return "P2 (Victorias: " + victoriasP2 + ")"; }

    @Override
    protected String getResumen() {
        return "P" + (portador.indice + 1) + " detono: tuvo la bomba el " + Math.round(proporcionConBomba() * 100) + "% del tiempo";
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : texturas) t.dispose();
    }
}
