package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class QuickdrawScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int VIDA_MAXIMA = 3;
    private static final float PRIMER_INTERVALO = 1f;
    private static final float INTERVALO_BASE = 1f;
    private static final float AZAR_MIN = 0.1f;
    private static final float AZAR_MAX = 2f;
    private static final float TIEMPO_REACCION = 0.35f;
    private static final float P1_X = 0f;
    private static final float P2_X = 600f;
    private static final float ESCOPETA_Y = 180f;
    private static final float TAMANO_ESCOPETA = 128f;
    private static final float CORAZON_Y = 300f;
    private static final float TAMANO_CORAZON = 32f;
    private static final float SEPARACION_CORAZON = 48f;
    private static final int[][] TECLAS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };
    private static final String[] NOMBRES_TECLAS_P1 = {"W", "A", "S", "D"};
    private static final String[] NOMBRES_TECLAS_P2 = {"UP", "LEFT", "DOWN", "RIGHT"};

    private final Texture corazon = cargarTextura("quickdraw/heart.png");
    private final Texture corazonVacio = cargarTextura("quickdraw/empty_heart.png");
    private final Texture escopetaAzul = cargarTextura("quickdraw/blue_shotgun.png");
    private final Texture escopetaRoja = cargarTextura("quickdraw/red_shotgun.png");

    private int vidaP1 = VIDA_MAXIMA;
    private int vidaP2 = VIDA_MAXIMA;
    private float intervaloGatillo = PRIMER_INTERVALO;
    private float tiempoGatillo;
    private boolean puedeDisparar;
    private float tiempoReaccion;
    private boolean contarReaccion = false;
    private boolean yaDisparo;
    private int opcionDisparo;

    public QuickdrawScreen(Game juego) {
        super(juego);
    }

    private Texture cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    @Override protected float getDuracionRonda() { return DURACION_RONDA; }

    @Override
    protected void actualizarJuego(float delta) {
        for (int p = 0; p < 2; p++) {
            for (int k = 0; k < 4; k++) {
                if (Gdx.input.isKeyJustPressed(TECLAS[p][k])) disparar(p + 1, k + 1);
            }
        }
        tiempoGatillo += delta;
        if (contarReaccion) {
            tiempoReaccion += delta;
        }
        if (tiempoReaccion > TIEMPO_REACCION) {
            contarReaccion = false;
            tiempoReaccion = 0;
            yaDisparo = false;
        }
        if (intervaloGatillo < tiempoGatillo) {
            tiempoGatillo -= intervaloGatillo;
            if (!puedeDisparar) {
                intervaloGatillo = INTERVALO_BASE + MathUtils.random(AZAR_MIN, AZAR_MAX);
                habilitarGatillo();
            }
        }
    }

    private void quitarVida(int jugador) {
        if (jugador == 1 && !yaDisparo) {
            vidaP1--;
            yaDisparo = false;
        } else if (jugador == 2 && !yaDisparo) {
            vidaP2--;
            yaDisparo = false;
        }
    }

    private void disparar(int jugador, int tecla) {
        if (tecla == opcionDisparo && puedeDisparar) {
            puedeDisparar = false;
            quitarVida(jugador == 1 ? 2 : 1);
        } else {
            quitarVida(jugador);
        }
        if (!contarReaccion) {
            contarReaccion = true;
            yaDisparo = true;
        }
        if (vidaP1 == 0 || vidaP2 == 0) {
            terminarRonda();
        }
    }

    private void habilitarGatillo() {
        opcionDisparo = MathUtils.random(1, 4);
        puedeDisparar = true;
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(escopetaAzul, P1_X, ESCOPETA_Y, TAMANO_ESCOPETA, TAMANO_ESCOPETA);
        batch.draw(escopetaRoja, P2_X, ESCOPETA_Y, TAMANO_ESCOPETA, TAMANO_ESCOPETA);
        for (int i = 0; i < vidaP1; i++) {
            batch.draw(corazon, P1_X + i * SEPARACION_CORAZON, CORAZON_Y, TAMANO_CORAZON, TAMANO_CORAZON);
        }
        for (int i = 0; i < vidaP2; i++) {
            batch.draw(corazon, P2_X + i * SEPARACION_CORAZON, CORAZON_Y, TAMANO_CORAZON, TAMANO_CORAZON);
        }
        if (puedeDisparar) {
            fuente.draw(batch, NOMBRES_TECLAS_P1[opcionDisparo - 1], 400, 200);
            fuente.draw(batch, NOMBRES_TECLAS_P2[opcionDisparo - 1], 400, 250);
        }
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (vidaP1 == vidaP2) return "¡EMPATE!";
        return (vidaP1 > vidaP2) ? sumarPuntos(1, Math.max(1, vidaP1)) : sumarPuntos(2, Math.max(1, vidaP2));
    }

    @Override
    protected void reiniciarRonda() {
        vidaP1 = VIDA_MAXIMA;
        vidaP2 = VIDA_MAXIMA;
        intervaloGatillo = PRIMER_INTERVALO;
        tiempoGatillo = 0;
    }

    @Override protected String getTextoP1() { return "Vida: " + vidaP1; }
    @Override protected String getTextoP2() { return "Vida: " + vidaP2; }
    @Override protected String getResumen() { return "P1(" + vidaP1 + "hp) vs P2(" + vidaP2 + "hp)"; }

    @Override
    public void dispose() {
        super.dispose();
        escopetaRoja.dispose();
        escopetaAzul.dispose();
        corazon.dispose();
        corazonVacio.dispose();
    }
}
