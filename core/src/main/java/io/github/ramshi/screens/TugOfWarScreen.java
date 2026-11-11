package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;

public class TugOfWarScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final int TECLA_P1 = Input.Keys.A;
    private static final int TECLA_P2 = Input.Keys.LEFT;
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

    private int tironesP1 = 0;
    private int tironesP2 = 0;

    public TugOfWarScreen(Game juego) {
        super(juego);
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        if (Gdx.input.isKeyJustPressed(TECLA_P1)) {
            sogaObjetivo -= PASO_TIRON;
            tironesP1++;
        }
        if (Gdx.input.isKeyJustPressed(TECLA_P2)) {
            sogaObjetivo += PASO_TIRON;
            tironesP2++;
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

        batch.begin();
        fuente.getData().setScale(1.1f);
        fuente.setColor(0.3f, 0.9f, 0.3f, 1f);
        fuente.draw(batch, "P1: machaca [A]", 60f, 100f);
        fuente.setColor(0.3f, 0.7f, 1f, 1f);
        fuente.draw(batch, "P2: machaca [FLECHA IZQ]", 480f, 100f);
        batch.end();
    }

    @Override
    protected String determinarGanador() {
        if (sogaObjetivo < -0.01f) {
            victoriasP1++;
            return "¡JUGADOR 1 GANA LA RONDA!";
        } else if (sogaObjetivo > 0.01f) {
            victoriasP2++;
            return "¡JUGADOR 2 GANA LA RONDA!";
        } else {
            return "¡EMPATE EN LA RONDA!";
        }
    }

    @Override
    protected void reiniciarRonda() {
        sogaObjetivo = 0f;
        sogaMostrada = 0f;
        tironesP1 = 0;
        tironesP2 = 0;
    }

    @Override
    protected String getTextoP1() {
        return "P1 [A] (Victorias: " + victoriasP1 + ")";
    }

    @Override
    protected String getTextoP2() {
        return "P2 [IZQ] (Victorias: " + victoriasP2 + ")";
    }

    @Override
    protected String getResumen() {
        return "Tirones: P1 (" + tironesP1 + ") vs P2 (" + tironesP2 + ")";
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}