package io.github.ramshi.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.ramshi.Main;

import java.util.HashMap;
import java.util.Map;

public class Controles {

    public static final int[][] W = {{86}};
    public static final int[][] A = {{120}};
    public static final int[][] S = {{121}};
    public static final int[][] D = {{122}};
    public static final int[][] ARRIBA = {{166}};
    public static final int[][] IZQUIERDA = {{169}};
    public static final int[][] ABAJO = {{168}};
    public static final int[][] DERECHA = {{167}};
    public static final int[][] ESPACIO = {{235, 236, 237}};
    public static final int[][] ENTER = {{100, 101}, {134, 135}};

    private static final String RUTA_TILE = "ui/kenney/white/tile_%04d.png";
    private static final int TAMANO_TILE = 16;
    private static final int ESCALA_GRANDE_MAX = 4;
    private static final float ANCHO_MAX_GRANDE = 340f;
    private static final float ALTO_MAX_CHICA = 32f;
    private static final float SEPARACION_TECLAS = 0.25f;
    private static final float SEPARACION_CONTROLES = 2f;
    private static final float SEPARACION_LEYENDA = 28f;
    private static final float SEPARACION_TEXTO = 6f;
    private static final float TAMANO_ICONO_GRANDE = 64f;
    private static final float TAMANO_ICONO_CHICO = 32f;
    private static final Color COLOR_TEXTO = new Color(1f, 1f, 1f, 1f);

    public static class Control {
        final String accion;
        final boolean cruz;
        final int[][][][] teclas;
        String icono;

        Control(String accion, boolean cruz, int[][][] p1, int[][][] p2) {
            this.accion = accion;
            this.cruz = cruz;
            this.teclas = new int[][][][] {p1, p2};
        }

        public Control conIcono(String ruta) {
            icono = ruta;
            return this;
        }
    }

    public static Control una(String accion, int[][] p1, int[][] p2) {
        return new Control(accion, false, new int[][][] {p1}, new int[][][] {p2});
    }

    public static Control dos(String accion, int[][] p1a, int[][] p1b, int[][] p2a, int[][] p2b) {
        return new Control(accion, false, new int[][][] {p1a, p1b}, new int[][][] {p2a, p2b});
    }

    public static Control cruz(String accion) {
        return new Control(accion, true, new int[][][] {W, A, S, D}, new int[][][] {ARRIBA, IZQUIERDA, ABAJO, DERECHA});
    }

    private static final Map<String, Texture> texturas = new HashMap<>();

    private static Texture textura(String ruta) {
        Texture t = texturas.get(ruta);
        if (t == null) {
            t = new Texture(ruta);
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texturas.put(ruta, t);
        }
        return t;
    }

    private static Texture textura(int numero) {
        return textura(String.format(RUTA_TILE, numero));
    }

    public static void liberar() {
        for (Texture t : texturas.values()) t.dispose();
        texturas.clear();
    }

    public static void dibujarTecla(SpriteBatch batch, int[][] tecla, float x, float y, float escala) {
        float lado = TAMANO_TILE * escala;
        for (int fila = 0; fila < tecla.length; fila++) {
            for (int col = 0; col < tecla[fila].length; col++) {
                float ty = y + (tecla.length - 1 - fila) * lado;
                batch.draw(textura(tecla[fila][col]), x + col * lado, ty, lado, lado);
            }
        }
    }

    private static float ancho(Control c, int jugador) {
        if (c.cruz) return 3f;
        float total = 0f;
        for (int[][] t : c.teclas[jugador]) total += t[0].length;
        return total + SEPARACION_TECLAS * (c.teclas[jugador].length - 1);
    }

    private static float alto(Control c, int jugador) {
        if (c.cruz) return 2f;
        float max = 0f;
        for (int[][] t : c.teclas[jugador]) max = Math.max(max, t.length);
        return max;
    }

    private static void dibujarControl(SpriteBatch batch, Control c, int jugador, float x, float y, float escala) {
        float lado = TAMANO_TILE * escala;
        int[][][] teclas = c.teclas[jugador];
        if (c.cruz) {
            dibujarTecla(batch, teclas[0], x + lado, y + lado, escala);
            dibujarTecla(batch, teclas[1], x, y, escala);
            dibujarTecla(batch, teclas[2], x + lado, y, escala);
            dibujarTecla(batch, teclas[3], x + 2 * lado, y, escala);
            return;
        }
        for (int[][] t : teclas) {
            dibujarTecla(batch, t, x, y, escala);
            x += (t[0].length + SEPARACION_TECLAS) * lado;
        }
    }

    public static void dibujarGrande(SpriteBatch batch, Control[] controles, int jugador, float centroX, float centroY) {
        float anchoTiles = SEPARACION_CONTROLES * (controles.length - 1);
        float altoTiles = 0f;
        for (Control c : controles) {
            anchoTiles += ancho(c, jugador);
            altoTiles = Math.max(altoTiles, alto(c, jugador));
        }
        float escala = Math.max(1, Math.min(ESCALA_GRANDE_MAX, (int) (ANCHO_MAX_GRANDE / (anchoTiles * TAMANO_TILE))));
        float lado = TAMANO_TILE * escala;
        float x = centroX - anchoTiles * lado / 2f;
        float base = centroY - altoTiles * lado / 2f;

        BitmapFont fuente = Main.fuenteMediana;
        fuente.setColor(COLOR_TEXTO);
        for (Control c : controles) {
            float anchoControl = ancho(c, jugador) * lado;
            dibujarControl(batch, c, jugador, x, base, escala);
            if (c.icono != null) {
                float iconoY = base - SEPARACION_TEXTO * 2 - TAMANO_ICONO_GRANDE;
                batch.draw(textura(c.icono), x + (anchoControl - TAMANO_ICONO_GRANDE) / 2f, iconoY, TAMANO_ICONO_GRANDE, TAMANO_ICONO_GRANDE);
            } else {
                GlyphLayout texto = new GlyphLayout(fuente, c.accion);
                fuente.draw(batch, texto, x + (anchoControl - texto.width) / 2f, base - SEPARACION_TEXTO * 2);
            }
            x += anchoControl + SEPARACION_CONTROLES * lado;
        }
    }

    public static void dibujarLeyenda(SpriteBatch batch, Control[] controles, int jugador, float centroX, float y) {
        BitmapFont fuente = Main.fuenteChica;
        fuente.setColor(COLOR_TEXTO);
        float[] escalas = new float[controles.length];
        GlyphLayout[] textos = new GlyphLayout[controles.length];
        float total = SEPARACION_LEYENDA * (controles.length - 1);
        for (int i = 0; i < controles.length; i++) {
            escalas[i] = Math.max(1, (int) (ALTO_MAX_CHICA / (alto(controles[i], jugador) * TAMANO_TILE)));
            textos[i] = new GlyphLayout(fuente, controles[i].accion);
            float anchoEtiqueta = (controles[i].icono != null) ? TAMANO_ICONO_CHICO : textos[i].width;
            total += ancho(controles[i], jugador) * TAMANO_TILE * escalas[i] + SEPARACION_TEXTO + anchoEtiqueta;
        }
        float x = centroX - total / 2f;
        for (int i = 0; i < controles.length; i++) {
            float anchoControl = ancho(controles[i], jugador) * TAMANO_TILE * escalas[i];
            dibujarControl(batch, controles[i], jugador, x, y, escalas[i]);
            x += anchoControl + SEPARACION_TEXTO;
            if (controles[i].icono != null) {
                batch.draw(textura(controles[i].icono), x, y, TAMANO_ICONO_CHICO, TAMANO_ICONO_CHICO);
                x += TAMANO_ICONO_CHICO + SEPARACION_LEYENDA;
            } else {
                fuente.draw(batch, textos[i], x, y + (ALTO_MAX_CHICA + textos[i].height) / 2f);
                x += textos[i].width + SEPARACION_LEYENDA;
            }
        }
    }
}
