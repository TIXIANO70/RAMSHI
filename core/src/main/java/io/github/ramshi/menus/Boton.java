package io.github.ramshi.menus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import io.github.ramshi.Main;

public class Boton {

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
