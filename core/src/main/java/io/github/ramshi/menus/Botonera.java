package io.github.ramshi.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.ramshi.Main;

import java.util.ArrayList;
import java.util.List;

public class Botonera {

    private static final float BRILLO_NORMAL = 0.6f;
    private static final float BRILLO_BORDE = 0.45f;
    private static final float PESO_LATERAL = 2f;
    private static final Color COLOR_INACTIVO = new Color(0.2f, 0.2f, 0.23f, 1f);
    private static final Color TEXTO_RESALTADO = Color.WHITE;
    private static final Color TEXTO_NORMAL = new Color(0.85f, 0.85f, 0.88f, 1f);
    private static final Color TEXTO_INACTIVO = new Color(0.45f, 0.45f, 0.5f, 1f);

    public static class Boton {
        public static final Color VERDE = new Color(0.2f, 0.62f, 0.3f, 1f);
        public static final Color ROJO = new Color(0.75f, 0.2f, 0.2f, 1f);
        public static final Color NEUTRO = new Color(0.22f, 0.35f, 0.55f, 1f);

        public final Rectangle rect;
        public final Color color;
        public String texto;
        public BitmapFont fuente = Main.fuenteGrande;
        public boolean activo = true;
        private final Runnable accion;

        public Boton(float x, float y, float ancho, float alto, String texto, Color color, Runnable accion) {
            this.rect = new Rectangle(x, y, ancho, alto);
            this.texto = texto;
            this.color = color;
            this.accion = accion;
        }

        public Boton conFuente(BitmapFont fuente) {
            this.fuente = fuente;
            return this;
        }

        public void ejecutar() {
            if (activo && accion != null) accion.run();
        }
    }

    public boolean navegarHorizontal = true;

    private final List<Boton> botones = new ArrayList<>();
    private final Viewport vista;
    private final Vector3 mouse = new Vector3();
    private final Color tono = new Color();
    private float ultimoMouseX = -1f;
    private float ultimoMouseY = -1f;
    private Boton bajoMouse;
    private Boton enfocado;

    public Botonera(Viewport vista) {
        this.vista = vista;
    }

    public Boton agregar(Boton boton) {
        botones.add(boton);
        return boton;
    }

    public void actualizar() {
        mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        vista.unproject(mouse);
        bajoMouse = null;
        for (Boton b : botones) {
            if (b.activo && b.rect.contains(mouse.x, mouse.y)) bajoMouse = b;
        }
        boolean mouseSeMovio = mouse.x != ultimoMouseX || mouse.y != ultimoMouseY;
        ultimoMouseX = mouse.x;
        ultimoMouseY = mouse.y;
        if (mouseSeMovio && bajoMouse != null) enfocado = bajoMouse;
        if (enfocado == null || !enfocado.activo) enfocado = primerActivo();
        if (enfocado == null) return;

        if (apretada(Input.Keys.W, Input.Keys.UP)) mover(0f, 1f);
        if (apretada(Input.Keys.S, Input.Keys.DOWN)) mover(0f, -1f);
        if (navegarHorizontal && apretada(Input.Keys.A, Input.Keys.LEFT)) mover(-1f, 0f);
        if (navegarHorizontal && apretada(Input.Keys.D, Input.Keys.RIGHT)) mover(1f, 0f);
    }

    public void enfocar(Boton boton) {
        enfocado = boton;
    }

    public Boton elegido() {
        if (Gdx.input.justTouched()) return bajoMouse;
        if (apretada(Input.Keys.ENTER, Input.Keys.SPACE)) return enfocado;
        return null;
    }

    private boolean apretada(int tecla1, int tecla2) {
        return Gdx.input.isKeyJustPressed(tecla1) || Gdx.input.isKeyJustPressed(tecla2);
    }

    private Boton primerActivo() {
        for (Boton b : botones) if (b.activo) return b;
        return null;
    }

    private void mover(float dx, float dy) {
        float x = enfocado.rect.x + enfocado.rect.width / 2f;
        float y = enfocado.rect.y + enfocado.rect.height / 2f;
        Boton mejor = null;
        float mejorPuntaje = Float.MAX_VALUE;
        for (Boton b : botones) {
            if (b == enfocado || !b.activo) continue;
            float vx = b.rect.x + b.rect.width / 2f - x;
            float vy = b.rect.y + b.rect.height / 2f - y;
            float avance = vx * dx + vy * dy;
            if (avance <= 0f) continue;
            float puntaje = avance + Math.abs(vx * dy - vy * dx) * PESO_LATERAL;
            if (puntaje < mejorPuntaje) {
                mejorPuntaje = puntaje;
                mejor = b;
            }
        }
        if (mejor != null) enfocado = mejor;
    }

    public void dibujar(ShapeRenderer formas, SpriteBatch batch) {
        formas.begin(ShapeRenderer.ShapeType.Filled);
        for (Boton b : botones) {
            if (!b.activo) formas.setColor(COLOR_INACTIVO);
            else if (b == enfocado) formas.setColor(b.color);
            else formas.setColor(tono.set(b.color).mul(BRILLO_NORMAL, BRILLO_NORMAL, BRILLO_NORMAL, 1f));
            formas.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        formas.end();

        formas.begin(ShapeRenderer.ShapeType.Line);
        for (Boton b : botones) {
            if (!b.activo) formas.setColor(TEXTO_INACTIVO);
            else if (b == enfocado) formas.setColor(Color.WHITE);
            else formas.setColor(tono.set(b.color).lerp(Color.WHITE, BRILLO_BORDE));
            formas.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        formas.end();

        batch.begin();
        for (Boton b : botones) {
            b.fuente.setColor(!b.activo ? TEXTO_INACTIVO : (b == enfocado ? TEXTO_RESALTADO : TEXTO_NORMAL));
            GlyphLayout layout = new GlyphLayout(b.fuente, b.texto);
            b.fuente.draw(batch, layout, b.rect.x + (b.rect.width - layout.width) / 2f,
                b.rect.y + (b.rect.height + layout.height) / 2f);
        }
        batch.end();
    }
}
