package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.Main;

import java.util.Arrays;

public class LightsScreen extends BaseMinigameScreen {

    private static final float DURACION_TOTAL = 10f;
    private static final int[] LARGOS_SECUENCIA = {4, 5, 6};
    private static final float TIEMPO_MUESTRA = 1f;
    private static final float PROPORCION_ENCENDIDA = 0.7f;
    private static final float TIEMPO_CUENTA = 0.4f;
    private static final float TIEMPO_RESULTADO = 0.4f;
    private static final float TIEMPO_DESTELLO = 0.15f;
    private static final boolean REINICIAR_AL_FALLAR = true;
    private static final float TAMANO_TECLA = 78f;
    private static final float SEPARACION_TECLA = 8f;
    private static final float FILA_ABAJO_Y = 150f;
    private static final int[][] TECLAS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };
    private static final Color[] COLORES_TECLA = {
        new Color(0.20f, 0.90f, 0.30f, 1f), new Color(0.95f, 0.25f, 0.25f, 1f),
        new Color(0.98f, 0.85f, 0.20f, 1f), new Color(0.25f, 0.50f, 1.00f, 1f)
    };

    private enum Fase { CUENTA, MOSTRANDO, INGRESO, RESULTADO }

    private static final int TOTAL_RONDAS = LARGOS_SECUENCIA.length;
    private static final float MITAD_ANCHO = ANCHO_VIRTUAL / 2f;
    private static final float FILA_ARRIBA_Y = FILA_ABAJO_Y + TAMANO_TECLA + SEPARACION_TECLA;
    private static final int[][] TILES_TECLA = {
        {86, 120, 121, 122},
        {166, 169, 168, 167}
    };
    private static final int DESPLAZAMIENTO_RELLENA = 272;
    private static final int RECORTE_BORDE = 1;
    private static final int RECORTE_TAMANO = 13;

    private final TextureRegion[][] teclaApagada = new TextureRegion[2][4];
    private final TextureRegion[][] teclaEncendida = new TextureRegion[2][4];

    private final int[] secuencia = new int[Arrays.stream(LARGOS_SECUENCIA).max().getAsInt()];
    private final int[] progreso = new int[2];
    private final int[] teclaDestello = new int[2];
    private final float[] tiempoDestello = new float[2];
    private final int[] rondasGanadas = new int[2];
    private int rondaActual;
    private int ganadorSubronda;
    private Fase fase;
    private float tiempoFase;

    public LightsScreen(Game juego) {
        super(juego);
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                teclaApagada[p][i] = cargarTextura(rutaTile(TILES_TECLA[p][i]));
                teclaEncendida[p][i] = cargarTextura(rutaTile(TILES_TECLA[p][i] + DESPLAZAMIENTO_RELLENA));
            }
        }
        reiniciarRonda();
    }

    private String rutaTile(int numero) {
        return String.format("ui/kenney/white/tile_%04d.png", numero);
    }

    private TextureRegion cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return new TextureRegion(t, RECORTE_BORDE, RECORTE_BORDE, RECORTE_TAMANO, RECORTE_TAMANO);
    }

    @Override protected float getDuracionRonda() { return DURACION_TOTAL; }

    @Override
    protected void actualizarJuego(float delta) {
        tiempoFase += delta;
        for (int p = 0; p < 2; p++) tiempoDestello[p] -= delta;

        if (fase == Fase.CUENTA && tiempoFase >= TIEMPO_CUENTA) {
            cambiarFase(Fase.MOSTRANDO);
        } else if (fase == Fase.MOSTRANDO && tiempoFase >= TIEMPO_MUESTRA) {
            cambiarFase(Fase.INGRESO);
        } else if (fase == Fase.INGRESO) {
            boolean listoP1 = leerTeclas(0);
            boolean listoP2 = leerTeclas(1);
            if (listoP1 || listoP2) {
                ganadorSubronda = (listoP1 && listoP2) ? 0 : (listoP1 ? 1 : 2);
                if (ganadorSubronda != 0) rondasGanadas[ganadorSubronda - 1]++;
                cambiarFase(Fase.RESULTADO);
            }
        } else if (fase == Fase.RESULTADO && tiempoFase >= TIEMPO_RESULTADO) {
            rondaActual++;
            if (rondaActual >= TOTAL_RONDAS) terminarRonda();
            else iniciarSubronda();
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        int encendida = teclaActual();

        formas.begin(ShapeRenderer.ShapeType.Filled);
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                Color c = COLORES_TECLA[i];
                formas.setColor(c.r * 0.25f, c.g * 0.25f, c.b * 0.25f, 1f);
                formas.rect(teclaX(p, i), teclaY(i), TAMANO_TECLA, TAMANO_TECLA);
            }
            dibujarProgreso(p);
        }
        formas.end();

        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(0.35f, 0.35f, 0.35f, 1f);
        formas.line(MITAD_ANCHO, 16f, MITAD_ANCHO, 432f);
        formas.end();

        batch.begin();
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                boolean prendida = (encendida == i) || (tiempoDestello[p] > 0f && teclaDestello[p] == i);
                batch.setColor(prendida ? COLORES_TECLA[i] : Color.WHITE);
                batch.draw(prendida ? teclaEncendida[p][i] : teclaApagada[p][i], teclaX(p, i), teclaY(i), TAMANO_TECLA, TAMANO_TECLA);
            }
        }
        batch.setColor(Color.WHITE);

        usarFuente(Main.fuenteMediana);
        fuente.setColor(Color.WHITE);
        if (!rondaTerminada) {
            GlyphLayout layout = new GlyphLayout(fuente, mensajeFase());
            fuente.draw(batch, layout, (ANCHO_VIRTUAL - layout.width) / 2f, 420f);
        }
        usarFuente(Main.fuenteChica);
        fuente.setColor(0.6f, 0.6f, 0.65f, 1f);
        fuente.draw(batch, "P1: W A S D", 24f, 24f);
        dibujarDerecha("P2: FLECHAS", 24f);
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        int p1 = rondasGanadas[0], p2 = rondasGanadas[1];
        if (p1 == p2) return "¡EMPATE! (" + p1 + "-" + p2 + ")";
        int ganador = (p1 > p2) ? 1 : 2, pts = Math.max(p1, p2);
        if (ganador == 1) victoriasP1 += pts;
        else victoriasP2 += pts;
        return "¡JUGADOR " + ganador + " GANA! (" + pts + "-" + Math.min(p1, p2) + ")  +" + pts + " pts";
    }

    @Override
    protected void reiniciarRonda() {
        rondasGanadas[0] = rondasGanadas[1] = 0;
        rondaActual = 0;
        iniciarSubronda();
    }

    @Override protected String getTextoP1() { return "P1: " + rondasGanadas[0] + " rondas (Victorias: " + victoriasP1 + ")"; }
    @Override protected String getTextoP2() { return "P2: " + rondasGanadas[1] + " rondas (Victorias: " + victoriasP2 + ")"; }
    @Override protected String getResumen() { return "Rondas ganadas: P1 (" + rondasGanadas[0] + ") vs P2 (" + rondasGanadas[1] + ")"; }

    @Override
    public void dispose() {
        super.dispose();
        for (int p = 0; p < 2; p++) {
            for (int i = 0; i < 4; i++) {
                teclaApagada[p][i].getTexture().dispose();
                teclaEncendida[p][i].getTexture().dispose();
            }
        }
    }

    private void iniciarSubronda() {
        for (int i = 0; i < largoSecuencia(); i++) secuencia[i] = MathUtils.random(0, 3);
        progreso[0] = progreso[1] = 0;
        tiempoDestello[0] = tiempoDestello[1] = 0f;
        ganadorSubronda = 0;
        cambiarFase(Fase.CUENTA);
    }

    private int largoSecuencia() {
        return LARGOS_SECUENCIA[Math.min(rondaActual, TOTAL_RONDAS - 1)];
    }

    private void cambiarFase(Fase nuevaFase) {
        fase = nuevaFase;
        tiempoFase = 0f;
    }

    private boolean leerTeclas(int jugador) {
        for (int i = 0; i < 4; i++) {
            if (!Gdx.input.isKeyJustPressed(TECLAS[jugador][i])) continue;

            teclaDestello[jugador] = i;
            tiempoDestello[jugador] = TIEMPO_DESTELLO;

            if (i == secuencia[progreso[jugador]]) {
                progreso[jugador]++;
                if (progreso[jugador] == largoSecuencia()) return true;
            } else if (REINICIAR_AL_FALLAR) {
                progreso[jugador] = 0;
            }
        }
        return false;
    }

    private int teclaActual() {
        if (fase != Fase.MOSTRANDO) return -1;
        float espacio = TIEMPO_MUESTRA / largoSecuencia();
        int indice = (int) (tiempoFase / espacio);
        boolean prendida = indice < largoSecuencia() && tiempoFase - indice * espacio < espacio * PROPORCION_ENCENDIDA;
        return prendida ? secuencia[indice] : -1;
    }

    private String mensajeFase() {
        String ronda = "RONDA " + (rondaActual + 1) + "/" + TOTAL_RONDAS + " - ";
        switch (fase) {
            case CUENTA: return ronda + "PREPARATE...";
            case MOSTRANDO:   return ronda + "MIRA LA SECUENCIA";
            case INGRESO:     return ronda + "¡REPETILA!";
            default:        return (ganadorSubronda == 0) ? "EMPATE EN ESTA RONDA" : "PUNTO PARA EL JUGADOR " + ganadorSubronda;
        }
    }

    private void dibujarProgreso(int jugador) {
        int largo = largoSecuencia();
        float caja = 24f, separacion = 8f;
        float total = largo * caja + (largo - 1) * separacion;
        float inicioX = jugador * MITAD_ANCHO + (MITAD_ANCHO - total) / 2f;
        for (int i = 0; i < largo; i++) {
            if (i < progreso[jugador]) formas.setColor(0.3f, 0.9f, 0.3f, 1f);
            else formas.setColor(0.2f, 0.2f, 0.2f, 1f);
            formas.rect(inicioX + i * (caja + separacion), 50f, caja, caja);
        }
    }

    private float teclaX(int jugador, int tecla) {
        float cx = jugador * MITAD_ANCHO + MITAD_ANCHO / 2f;
        if (tecla == 1) return cx - TAMANO_TECLA * 1.5f - SEPARACION_TECLA;
        if (tecla == 3) return cx + TAMANO_TECLA * 0.5f + SEPARACION_TECLA;
        return cx - TAMANO_TECLA / 2f;
    }

    private float teclaY(int tecla) {
        return (tecla == 0) ? FILA_ARRIBA_Y : FILA_ABAJO_Y;
    }
}
