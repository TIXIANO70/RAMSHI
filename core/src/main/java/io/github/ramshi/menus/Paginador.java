package io.github.ramshi.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import io.github.ramshi.Main;

public class Paginador {

    private static final float DISTANCIA_FLECHA = 70f;
    private static final float TAMANO_FLECHA = 14f;
    private static final float MARGEN_CLICK = 10f;
    private static final Color COLOR_FLECHA = new Color(1f, 0.85f, 0.2f, 1f);
    private static final Color COLOR_TEXTO = Color.WHITE;

    private final int total;
    private final float centroX;
    private final float y;
    private final Rectangle zonaIzquierda;
    private final Rectangle zonaDerecha;
    private int actual;

    public Paginador(int total, int inicial, float centroX, float y) {
        this.total = total;
        this.actual = ((inicial % total) + total) % total;
        this.centroX = centroX;
        this.y = y;
        float lado = TAMANO_FLECHA * 2f + MARGEN_CLICK * 2f;
        zonaIzquierda = new Rectangle(centroX - DISTANCIA_FLECHA - lado / 2f, y - lado / 2f, lado, lado);
        zonaDerecha = new Rectangle(centroX + DISTANCIA_FLECHA - lado / 2f, y - lado / 2f, lado, lado);
    }

    public int actual() { return actual; }

    public int vecino(int desplazamiento) {
        return ((actual + desplazamiento) % total + total) % total;
    }

    public void siguiente() { actual = vecino(1); }

    public void anterior() { actual = vecino(-1); }

    public void actualizar(float mouseX, float mouseY, boolean click) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) siguiente();
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) anterior();
        if (click && zonaDerecha.contains(mouseX, mouseY)) siguiente();
        if (click && zonaIzquierda.contains(mouseX, mouseY)) anterior();
    }

    public void dibujar(ShapeRenderer formas, SpriteBatch batch) {
        float izq = centroX - DISTANCIA_FLECHA;
        float der = centroX + DISTANCIA_FLECHA;
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(COLOR_FLECHA);
        formas.triangle(izq - TAMANO_FLECHA, y, izq + TAMANO_FLECHA, y + TAMANO_FLECHA, izq + TAMANO_FLECHA, y - TAMANO_FLECHA);
        formas.triangle(der + TAMANO_FLECHA, y, der - TAMANO_FLECHA, y + TAMANO_FLECHA, der - TAMANO_FLECHA, y - TAMANO_FLECHA);
        formas.end();

        batch.begin();
        Main.fuenteMediana.setColor(COLOR_TEXTO);
        GlyphLayout layout = new GlyphLayout(Main.fuenteMediana, (actual + 1) + "/" + total);
        Main.fuenteMediana.draw(batch, layout, centroX - layout.width / 2f, y + layout.height / 2f);
        batch.end();
    }
}
