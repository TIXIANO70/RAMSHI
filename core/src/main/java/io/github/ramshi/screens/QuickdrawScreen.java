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
    private static final int[][] TECLAS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };
    private static final int[][][] TECLAS_SPRITE_P1 = {Controles.W, Controles.A, Controles.S, Controles.D};
    private static final int[][][] TECLAS_SPRITE_P2 = {Controles.ARRIBA, Controles.IZQUIERDA, Controles.ABAJO, Controles.DERECHA};
    private static final float ESCALA_TECLA = 4f;
    private static final float TECLA_Y = 210f;

    private final Texture fondo = cargarTextura("quickdraw/quickdraw_bg.png");
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

    @Override protected String getConsigna() { return "¡DISPARÁ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.cruz("la que salga")
        };
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
        batch.draw(fondo, 0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        batch.draw(escopetaAzul, P1_X, ESCOPETA_Y, TAMANO_ESCOPETA, TAMANO_ESCOPETA);
        batch.draw(escopetaRoja, P2_X, ESCOPETA_Y, TAMANO_ESCOPETA, TAMANO_ESCOPETA);
        if (puedeDisparar) {
            float lado = 16f * ESCALA_TECLA;
            Controles.dibujarTecla(batch, TECLAS_SPRITE_P1[opcionDisparo - 1], ANCHO_VIRTUAL / 2f - lado - 12f, TECLA_Y, ESCALA_TECLA);
            Controles.dibujarTecla(batch, TECLAS_SPRITE_P2[opcionDisparo - 1], ANCHO_VIRTUAL / 2f + 12f, TECLA_Y, ESCALA_TECLA);
        }
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (vidaP1 == vidaP2) return "¡EMPATE!";
        int vidaGanador = Math.max(1, Math.max(vidaP1, vidaP2));
        explicacion = "Terminó con " + vidaGanador + " de " + VIDA_MAXIMA + " vidas: 1 punto por cada vida que le quedó.";
        return sumarPuntos(vidaP1 > vidaP2 ? 1 : 2, vidaGanador);
    }

    @Override
    protected void reiniciarRonda() {
        vidaP1 = VIDA_MAXIMA;
        vidaP2 = VIDA_MAXIMA;
        intervaloGatillo = PRIMER_INTERVALO;
        tiempoGatillo = 0;
    }

    @Override protected int getVidaMaxima() { return VIDA_MAXIMA; }
    @Override protected int getVida(int jugador) { return jugador == 0 ? vidaP1 : vidaP2; }

    @Override protected String getTextoP1() { return "P1 (Victorias: " + victoriasP1 + ")"; }
    @Override protected String getTextoP2() { return "P2 (Victorias: " + victoriasP2 + ")"; }
    @Override protected String getResumen() { return "P1(" + vidaP1 + "hp) vs P2(" + vidaP2 + "hp)"; }

    @Override
    public void dispose() {
        super.dispose();
        fondo.dispose();
        escopetaRoja.dispose();
        escopetaAzul.dispose();
    }
}
