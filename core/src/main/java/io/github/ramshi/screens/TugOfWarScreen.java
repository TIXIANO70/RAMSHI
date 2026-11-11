package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.Main;

public class TugOfWarScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int PUNTOS_SIN_KO = 1;
    private static final int[][] TECLAS = {
        {Input.Keys.A, Input.Keys.D},
        {Input.Keys.LEFT, Input.Keys.RIGHT}
    };
    private static final float PASO_TIRON = 5f;
    private static final float DESPLAZAMIENTO_MAX = 150f;
    private static final float SUAVIZADO = 12f;
    private static final float CENTRO_X = ANCHO_VIRTUAL / 2f;
    private static final float SOGA_Y = 235f;
    private static final float SOGA_ALTO = 10f;
    private static final float MITAD_SOGA = 140f;
    private static final float JUGADOR_ANCHO = 64f;
    private static final float JUGADOR_ALTO = 90f;

    private float sogaObjetivo = 0f;
    private float sogaMostrada = 0f;

    private final int[] tirones = new int[2];
    private final int[] ultimaTecla = {-1, -1};

    public TugOfWarScreen(Game juego) {
        super(juego);
    }

    @Override protected String getConsigna() { return "¡TIRÁ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.dos("alternar", Controles.A, Controles.D, Controles.IZQUIERDA, Controles.DERECHA)
        };
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        for (int p = 0; p < 2; p++) {
            for (int k = 0; k < 2; k++) {
                if (Gdx.input.isKeyJustPressed(TECLAS[p][k]) && k != ultimaTecla[p]) {
                    ultimaTecla[p] = k;
                    sogaObjetivo += (p == 0) ? -PASO_TIRON : PASO_TIRON;
                    tirones[p]++;
                }
            }
        }
        sogaObjetivo = MathUtils.clamp(sogaObjetivo, -DESPLAZAMIENTO_MAX, DESPLAZAMIENTO_MAX);

        sogaMostrada = MathUtils.lerp(sogaMostrada, sogaObjetivo, Math.min(1f, SUAVIZADO * delta));

        if (Math.abs(sogaObjetivo) >= DESPLAZAMIENTO_MAX) {
            sogaMostrada = sogaObjetivo;
            terminarRonda();
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        float centroSoga = CENTRO_X + sogaMostrada;

        formas.begin(ShapeType.Filled);

        formas.setColor(0.22f, 0.40f, 0.18f, 1f);
        formas.rect(0f, 150f, ANCHO_VIRTUAL, 180f);

        formas.setColor(1f, 1f, 1f, 1f);
        formas.rect(CENTRO_X - 1f, 150f, 2f, 180f);

        formas.setColor(0.3f, 0.9f, 0.3f, 1f);
        formas.rect(CENTRO_X - DESPLAZAMIENTO_MAX - 2f, 150f, 4f, 180f);
        formas.setColor(0.3f, 0.7f, 1f, 1f);
        formas.rect(CENTRO_X + DESPLAZAMIENTO_MAX - 2f, 150f, 4f, 180f);

        formas.setColor(0.55f, 0.38f, 0.18f, 1f);
        formas.rect(centroSoga - MITAD_SOGA, SOGA_Y, MITAD_SOGA * 2f, SOGA_ALTO);

        formas.setColor(0.9f, 0.15f, 0.15f, 1f);
        formas.rect(centroSoga - 4f, SOGA_Y - 10f, 8f, SOGA_ALTO + 20f);

        formas.setColor(0.3f, 0.9f, 0.3f, 1f);
        formas.rect(centroSoga - MITAD_SOGA - JUGADOR_ANCHO, SOGA_Y - 40f, JUGADOR_ANCHO, JUGADOR_ALTO);
        formas.setColor(0.3f, 0.7f, 1f, 1f);
        formas.rect(centroSoga + MITAD_SOGA, SOGA_Y - 40f, JUGADOR_ANCHO, JUGADOR_ALTO);

        formas.end();
    }

    @Override
    protected String determinarGanador() {
        if (Math.abs(sogaObjetivo) < 0.01f) return "¡EMPATE EN LA RONDA!";
        int ganador = (sogaObjetivo < 0f) ? 1 : 2;
        int pts = (Math.abs(sogaObjetivo) >= DESPLAZAMIENTO_MAX) ? puntosPorTiempo() : PUNTOS_SIN_KO;
        return sumarPuntos(ganador, pts);
    }

    @Override
    protected void reiniciarRonda() {
        sogaObjetivo = 0f;
        sogaMostrada = 0f;
        tirones[0] = tirones[1] = 0;
        ultimaTecla[0] = ultimaTecla[1] = -1;
    }

    @Override
    protected String getTextoP1() {
        return "P1 (Victorias: " + victoriasP1 + ")";
    }

    @Override
    protected String getTextoP2() {
        return "P2 (Victorias: " + victoriasP2 + ")";
    }

    @Override
    protected String getResumen() {
        return "Tirones: P1 (" + tirones[0] + ") vs P2 (" + tirones[1] + ")";
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}