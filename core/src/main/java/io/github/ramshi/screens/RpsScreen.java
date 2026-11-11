package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.ramshi.Main;

public class RpsScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int PIEDRA = 0;
    private static final int PAPEL  = 1;
    private static final int TIJERA = 2;
    private static final int ELECCION_INICIAL = PAPEL;
    private static final int   PASOS_CUENTA = 3;
    private static final float TIEMPO_PASO   = 0.6f;
    private static final float TIEMPO_REVELAR = 0.8f;
    private static final int PUNTOS_MANO       = 1;
    private static final int PUNTOS_MAXIMOS = 3;
    private static final int TECLA_P1_PIEDRA = Input.Keys.A;
    private static final int TECLA_P1_PAPEL  = Input.Keys.S;
    private static final int TECLA_P1_TIJERA = Input.Keys.D;
    private static final int TECLA_P2_PIEDRA = Input.Keys.LEFT;
    private static final int TECLA_P2_PAPEL  = Input.Keys.DOWN;
    private static final int TECLA_P2_TIJERA = Input.Keys.RIGHT;
    private static final float TAMANO_SPRITE = 128f;
    private static final float P1_X = 140f;
    private static final float P2_X = 532f;
    private static final float MANO_Y = 180f;
    private static final String[] NOMBRES = {"PIEDRA", "PAPEL", "TIJERA"};
    private final Texture[] texturas = new Texture[3];

    private int puntajeP1 = 0;
    private int puntajeP2 = 0;
    private int eleccionP1 = ELECCION_INICIAL;
    private int eleccionP2 = ELECCION_INICIAL;
    private float tiempoMano = 0f;
    private boolean revelado = false;
    private String mensajeMano = "";

    public RpsScreen(Game juego) {
        super(juego);
        texturas[PIEDRA] = new Texture("rps/piedra.png");
        texturas[PAPEL]  = new Texture("rps/papel.png");
        texturas[TIJERA] = new Texture("rps/tijera.png");
        for (Texture t : texturas) {
            t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        }
        reiniciarMano();
    }

    private boolean gana(int a, int b) {
        return (a == PIEDRA && b == TIJERA)
            || (a == PAPEL  && b == PIEDRA)
            || (a == TIJERA && b == PAPEL);
    }

    private void reiniciarMano() {
        eleccionP1 = ELECCION_INICIAL;
        eleccionP2 = ELECCION_INICIAL;
        tiempoMano = 0f;
        revelado = false;
        mensajeMano = "";
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        tiempoMano += delta;
        float duracionCuenta = PASOS_CUENTA * TIEMPO_PASO;

        if (tiempoMano < duracionCuenta) {
            if (Gdx.input.isKeyJustPressed(TECLA_P1_PIEDRA)) eleccionP1 = PIEDRA;
            if (Gdx.input.isKeyJustPressed(TECLA_P1_PAPEL))  eleccionP1 = PAPEL;
            if (Gdx.input.isKeyJustPressed(TECLA_P1_TIJERA)) eleccionP1 = TIJERA;

            if (Gdx.input.isKeyJustPressed(TECLA_P2_PIEDRA)) eleccionP2 = PIEDRA;
            if (Gdx.input.isKeyJustPressed(TECLA_P2_PAPEL))  eleccionP2 = PAPEL;
            if (Gdx.input.isKeyJustPressed(TECLA_P2_TIJERA)) eleccionP2 = TIJERA;
        } else if (!revelado) {
            revelado = true;
            if (gana(eleccionP1, eleccionP2)) {
                puntajeP1 += PUNTOS_MANO;
                mensajeMano = "¡PUNTO P1!";
            } else if (gana(eleccionP2, eleccionP1)) {
                puntajeP2 += PUNTOS_MANO;
                mensajeMano = "¡PUNTO P2!";
            } else {
                mensajeMano = "¡EMPATE!";
            }
        } else if (tiempoMano >= duracionCuenta + TIEMPO_REVELAR) {
            reiniciarMano();
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        int mostradaP1 = revelado ? eleccionP1 : ELECCION_INICIAL;
        int mostradaP2 = revelado ? eleccionP2 : ELECCION_INICIAL;

        batch.begin();
        batch.draw(texturas[mostradaP1], P1_X, MANO_Y, TAMANO_SPRITE, TAMANO_SPRITE);
        batch.draw(texturas[mostradaP2], P2_X, MANO_Y, TAMANO_SPRITE, TAMANO_SPRITE);

        fuente.setColor(Color.WHITE);
        usarFuente(Main.fuenteCartel);
        if (!revelado) {
            int paso = Math.min(PASOS_CUENTA, (int) (tiempoMano / TIEMPO_PASO) + 1);
            fuente.draw(batch, String.valueOf(paso), 390f, 260f);
        } else {
            usarFuente(Main.fuenteMediana);
            fuente.draw(batch, mensajeMano, 340f, 360f);
            fuente.draw(batch, NOMBRES[eleccionP1], P1_X + 20f, 150f);
            fuente.draw(batch, NOMBRES[eleccionP2], P2_X + 20f, 150f);
        }

        usarFuente(Main.fuenteChica);
        fuente.setColor(0.7f, 0.7f, 0.7f, 1f);
        fuente.draw(batch, "P1: [A] Piedra  [S] Papel  [D] Tijera", 40f, 60f);
        fuente.draw(batch, "P2: [IZQ] Piedra  [ABAJO] Papel  [DER] Tijera", 430f, 60f);
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (puntajeP1 == puntajeP2) return "¡EMPATE!";
        int pts = Math.min(PUNTOS_MAXIMOS, Math.max(puntajeP1, puntajeP2));
        return sumarPuntos(puntajeP1 > puntajeP2 ? 1 : 2, pts);
    }

    @Override
    protected void reiniciarRonda() {
        puntajeP1 = 0;
        puntajeP2 = 0;
        reiniciarMano();
    }

    @Override
    protected String getTextoP1() {
        return "Puntos P1: " + puntajeP1;
    }

    @Override
    protected String getTextoP2() {
        return "Puntos P2: " + puntajeP2;
    }

    @Override
    protected String getResumen() {
        return "P1 (" + puntajeP1 + " pts) vs P2 (" + puntajeP2 + " pts)";
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : texturas) {
            if (t != null) t.dispose();
        }
    }
}
