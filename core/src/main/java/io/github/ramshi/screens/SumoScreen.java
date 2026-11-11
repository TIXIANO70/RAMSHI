package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.Main;

public class SumoScreen extends BaseMinigameScreen {

    private static final float DURACION_RONDA = 10f;
    private static final float RADIO = 22f;
    private static final float TAMANO_SPRITE = 48f;
    private static final float IMPULSO_TAP = 16f;
    private static final float VELOCIDAD_MANTENER = 240f;
    private static final float FUERZA_SALTO = 400f;
    private static final float GRAVEDAD = 950f;
    private static final float VELOCIDAD_CAIDA = 450f;
    private static final float DISTANCIA_INICIO = 110f;
    private static final float RETROCESO_CHOQUE = 38f;
    private static final float EMPUJE = 14f;
    private static final float ARRASTRE = 4f;
    private static final float ONDA_RADIO_INICIAL = 8f;
    private static final float ONDA_CRECIMIENTO = 300f;
    private static final float ONDA_DESVANECER = 3.0f;
    private static final int PUNTOS_CENTRO = 1;
    private static final float MARGEN_CENTRO = 5f;
    private static final int[][] TECLAS = {
        {Input.Keys.A, Input.Keys.D, Input.Keys.W},
        {Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP}
    };
    private static final float PLAT_ANCHO_INICIAL = 440f;
    private static final float PLAT_ANCHO_FINAL = 260f;
    private static final float PLAT_ALTO = 22f;
    private static final float PLAT_Y = 190f;
    private static final float PLAT_CENTRO_X = ANCHO_VIRTUAL / 2f;
    private static final float BASE_Y = PLAT_Y + PLAT_ALTO + RADIO;
    private static final Color COLOR_FONDO = new Color(30 / 255f, 30 / 255f, 45 / 255f, 1f);
    private static final Color COLOR_PLATAFORMA = new Color(0f, 204 / 255f, 255 / 255f, 1f);
    private static final Color COLOR_P1 = new Color(255 / 255f, 75 / 255f, 120 / 255f, 1f);
    private static final Color COLOR_P2 = new Color(0f, 255 / 255f, 180 / 255f, 1f);

    private final Texture sumoP1Frente = new Texture("sumo/sumo_p1_front.png");
    private final Texture sumoP1Lado  = new Texture("sumo/sumo_p1_side.png");
    private final Texture sumoP2Frente = new Texture("sumo/sumo_p2_front.png");
    private final Texture sumoP2Lado  = new Texture("sumo/sumo_p2_side.png");

    private float p1X;
    private float p1Y;
    private float p1VelY = 0f;

    private float p2X;
    private float p2Y;
    private float p2VelY = 0f;

    private float ultimoMovP1 = 0f;
    private float ultimoMovP2 = 0f;
    private boolean p1Cayo;
    private boolean p2Cayo;

    private boolean ondaActiva = false;
    private float ondaX;
    private float ondaY;
    private float ondaRadio;
    private float ondaAlfa;

    private float platAncho = PLAT_ANCHO_INICIAL;
    private float platX = PLAT_CENTRO_X - PLAT_ANCHO_INICIAL / 2f;
    private int rondasGanadasP1 = 0;
    private int rondasGanadasP2 = 0;
    private String motivoFinal = "";

    public SumoScreen(Game juego) {
        super(juego);
        reiniciarLuchadores();
    }

    private void reiniciarLuchadores() {
        platAncho = PLAT_ANCHO_INICIAL;
        platX = PLAT_CENTRO_X - platAncho / 2f;
        p1X = platX + DISTANCIA_INICIO;
        p1Y = BASE_Y;
        p1VelY = 0f;

        p2X = platX + platAncho - DISTANCIA_INICIO;
        p2Y = BASE_Y;
        p2VelY = 0f;

        ultimoMovP1 = 0f;
        ultimoMovP2 = 0f;
        p1Cayo = false;
        p2Cayo = false;
        ondaActiva = false;
        motivoFinal = "";
    }

    private float leerMovimiento(int p, float delta) {
        float mov = 0f;
        if (Gdx.input.isKeyJustPressed(TECLAS[p][0])) mov -= IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(TECLAS[p][0])) mov -= VELOCIDAD_MANTENER * delta;
        if (Gdx.input.isKeyJustPressed(TECLAS[p][1])) mov += IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(TECLAS[p][1])) mov += VELOCIDAD_MANTENER * delta;
        return mov;
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        if (p1Cayo || p2Cayo) {
            if (p1Cayo) p1Y -= VELOCIDAD_CAIDA * delta;
            if (p2Cayo) p2Y -= VELOCIDAD_CAIDA * delta;
            return;
        }

        platAncho = MathUtils.lerp(PLAT_ANCHO_INICIAL, PLAT_ANCHO_FINAL, 1f - tiempoRestante / DURACION_RONDA);
        platX = PLAT_CENTRO_X - platAncho / 2f;

        float movP1 = leerMovimiento(0, delta);
        if (Gdx.input.isKeyJustPressed(TECLAS[0][2]) && Math.abs(p1Y - BASE_Y) < 1.5f) {
            p1VelY = FUERZA_SALTO;
        }

        float movP2 = leerMovimiento(1, delta);
        if (Gdx.input.isKeyJustPressed(TECLAS[1][2]) && Math.abs(p2Y - BASE_Y) < 1.5f) {
            p2VelY = FUERZA_SALTO;
        }

        p1X += movP1;
        p2X += movP2;

        ultimoMovP1 = movP1;
        ultimoMovP2 = movP2;

        boolean p1SobrePlataforma = (p1X >= platX - RADIO && p1X <= platX + platAncho + RADIO);
        p1VelY -= GRAVEDAD * delta;
        p1Y += p1VelY * delta;
        if (p1SobrePlataforma && p1Y <= BASE_Y) {
            p1Y = BASE_Y;
            p1VelY = 0f;
        }

        boolean p2SobrePlataforma = (p2X >= platX - RADIO && p2X <= platX + platAncho + RADIO);
        p2VelY -= GRAVEDAD * delta;
        p2Y += p2VelY * delta;
        if (p2SobrePlataforma && p2Y <= BASE_Y) {
            p2Y = BASE_Y;
            p2VelY = 0f;
        }

        float distanciaX = Math.abs(p2X - p1X);
        float distanciaY = Math.abs(p2Y - p1Y);

        if (distanciaX < RADIO * 2f && distanciaY < RADIO * 1.5f) {
            float solapamiento = (RADIO * 2f) - distanciaX;

            p1X -= solapamiento / 2f;
            p2X += solapamiento / 2f;

            boolean p1AtacandoHaciaP2 = (p1X < p2X && movP1 > 0f) || (p1X > p2X && movP1 < 0f);
            boolean p2AtacandoHaciaP1 = (p2X > p1X && movP2 < 0f) || (p2X < p1X && movP2 > 0f);

            if (p1AtacandoHaciaP2 && p2AtacandoHaciaP1) {
                ondaActiva = true;
                ondaX = (p1X + p2X) / 2f;
                ondaY = (p1Y + p2Y) / 2f;
                ondaRadio = ONDA_RADIO_INICIAL;
                ondaAlfa = 1f;

                if (p1X < p2X) {
                    p1X -= RETROCESO_CHOQUE;
                    p2X += RETROCESO_CHOQUE;
                } else {
                    p1X += RETROCESO_CHOQUE;
                    p2X -= RETROCESO_CHOQUE;
                }
            } else if (p1AtacandoHaciaP2) {
                if (p1X < p2X) {
                    p2X += EMPUJE;
                    p1X += ARRASTRE;
                } else {
                    p2X -= EMPUJE;
                    p1X -= ARRASTRE;
                }
            } else if (p2AtacandoHaciaP1) {
                if (p2X > p1X) {
                    p1X -= EMPUJE;
                    p2X -= ARRASTRE;
                } else {
                    p1X += EMPUJE;
                    p2X += ARRASTRE;
                }
            }
        }

        if (ondaActiva) {
            ondaRadio += ONDA_CRECIMIENTO * delta;
            ondaAlfa -= ONDA_DESVANECER * delta;
            if (ondaAlfa <= 0f) {
                ondaActiva = false;
            }
        }

        boolean p1Fuera = (p1X < platX - RADIO || p1X > platX + platAncho + RADIO);
        boolean p2Fuera = (p2X < platX - RADIO || p2X > platX + platAncho + RADIO);

        if (p1Fuera && p2Fuera) {
            p1Cayo = true;
            p2Cayo = true;
            motivoFinal = "¡AMBOS CAYERON AL MISMO TIEMPO!";
            terminarRonda();
        } else if (p1Fuera) {
            p1Cayo = true;
            rondasGanadasP2++;
            motivoFinal = "¡RING OUT! JUGADOR 2 EMPUJA AL RIVAL";
            terminarRonda();
        } else if (p2Fuera) {
            p2Cayo = true;
            rondasGanadasP1++;
            motivoFinal = "¡RING OUT! JUGADOR 1 EMPUJA AL RIVAL";
            terminarRonda();
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        formas.begin(ShapeType.Filled);

        formas.setColor(COLOR_FONDO);
        formas.rect(0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);

        formas.setColor(COLOR_PLATAFORMA);
        formas.rect(platX, PLAT_Y, platAncho, PLAT_ALTO);

        formas.setColor(1f, 0.3f, 0.3f, 0.8f);
        formas.rect(platX, PLAT_Y, 20f, PLAT_ALTO);
        formas.rect(platX + platAncho - 20f, PLAT_Y, 20f, PLAT_ALTO);

        float escalaSombra1 = MathUtils.clamp(1f - (p1Y - BASE_Y) / 180f, 0.35f, 1f);
        float escalaSombra2 = MathUtils.clamp(1f - (p2Y - BASE_Y) / 180f, 0.35f, 1f);

        formas.setColor(0.08f, 0.1f, 0.14f, 0.6f);
        formas.ellipse(p1X - (RADIO * 0.9f * escalaSombra1), PLAT_Y + PLAT_ALTO - 4f, RADIO * 1.8f * escalaSombra1, 8f * escalaSombra1);
        formas.ellipse(p2X - (RADIO * 0.9f * escalaSombra2), PLAT_Y + PLAT_ALTO - 4f, RADIO * 1.8f * escalaSombra2, 8f * escalaSombra2);

        formas.end();

        batch.begin();

        Texture texP1 = (Math.abs(ultimoMovP1) > 0.01f) ? sumoP1Lado : sumoP1Frente;
        boolean espejoP1 = (ultimoMovP1 < -0.01f);
        float dibujoP1X = p1X - TAMANO_SPRITE / 2f;
        float dibujoP1Y = p1Y - TAMANO_SPRITE / 2f;
        batch.draw(texP1, dibujoP1X, dibujoP1Y, TAMANO_SPRITE, TAMANO_SPRITE, 0, 0, 48, 48, espejoP1, false);

        Texture texP2 = (Math.abs(ultimoMovP2) > 0.01f) ? sumoP2Lado : sumoP2Frente;
        boolean espejoP2 = (ultimoMovP2 <= 0.01f);
        float dibujoP2X = p2X - TAMANO_SPRITE / 2f;
        float dibujoP2Y = p2Y - TAMANO_SPRITE / 2f;
        batch.draw(texP2, dibujoP2X, dibujoP2Y, TAMANO_SPRITE, TAMANO_SPRITE, 0, 0, 48, 48, espejoP2, false);

        fuente.setColor(COLOR_P1);
        usarFuente(Main.fuenteChica);
        fuente.draw(batch, "P1: [A/D] Empuja  [W] Salta", 45f, 70f);

        fuente.setColor(COLOR_P2);
        dibujarDerecha("P2: [FLECHAS] Empuja  [ARRIBA] Salta", 70f);

        batch.end();

        if (ondaActiva && ondaAlfa > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            formas.begin(ShapeType.Line);
            Gdx.gl.glLineWidth(3f);
            formas.setColor(1f, 0.9f, 0.3f, MathUtils.clamp(ondaAlfa, 0f, 1f));
            formas.circle(ondaX, ondaY, ondaRadio);

            formas.setColor(1f, 1f, 1f, MathUtils.clamp(ondaAlfa * 0.7f, 0f, 1f));
            formas.circle(ondaX, ondaY, ondaRadio * 0.65f);
            formas.end();

            Gdx.gl.glLineWidth(1f);
        }
    }

    @Override
    protected String determinarGanador() {
        if (p1Cayo && p2Cayo) {
            return "¡EMPATE POR DOBLE CAÍDA!";
        }

        if (p2Cayo) return sumarPuntos(1, puntosPorTiempo());
        if (p1Cayo) return sumarPuntos(2, puntosPorTiempo());

        float d1 = Math.abs(p1X - PLAT_CENTRO_X);
        float d2 = Math.abs(p2X - PLAT_CENTRO_X);

        if (d1 < d2 - MARGEN_CENTRO) return sumarPuntos(1, PUNTOS_CENTRO);
        if (d2 < d1 - MARGEN_CENTRO) return sumarPuntos(2, PUNTOS_CENTRO);
        return "¡EMPATE POR TIEMPO!";
    }

    @Override
    protected void reiniciarRonda() {
        reiniciarLuchadores();
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
        return motivoFinal.isEmpty() ? "Lucha cerrada sobre la plataforma" : motivoFinal;
    }

    @Override
    public void dispose() {
        super.dispose();
        sumoP1Frente.dispose();
        sumoP1Lado.dispose();
        sumoP2Frente.dispose();
        sumoP2Lado.dispose();
    }
}
