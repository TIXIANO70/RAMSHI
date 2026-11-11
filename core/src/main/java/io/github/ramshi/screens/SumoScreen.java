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

public class SumoScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final float RADIO = 22f;
    private static final float SPRITE_SIZE = 48f;
    private static final float IMPULSO_TAP = 16f;
    private static final float VELOCIDAD_HOLD = 240f;
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
    private static final int PUNTOS_KO = 2;
    private static final int PUNTOS_CENTRO = 1;
    private static final float MARGEN_CENTRO = 5f;
    private static final int[][] KEYS = {
        {Input.Keys.A, Input.Keys.D, Input.Keys.W},
        {Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP}
    };
    private static final float PLAT_ANCHO = 440f;
    private static final float PLAT_ALTO = 22f;
    private static final float PLAT_X = (VIRTUAL_WIDTH - PLAT_ANCHO) / 2f;
    private static final float PLAT_Y = 190f;
    private static final float PLAT_CENTER_X = PLAT_X + PLAT_ANCHO / 2f;
    private static final float BASE_Y = PLAT_Y + PLAT_ALTO + RADIO;
    private static final Color COLOR_BG = new Color(30 / 255f, 30 / 255f, 45 / 255f, 1f);
    private static final Color COLOR_PLATAFORMA = new Color(0f, 204 / 255f, 255 / 255f, 1f);
    private static final Color COLOR_P1 = new Color(255 / 255f, 75 / 255f, 120 / 255f, 1f);
    private static final Color COLOR_P2 = new Color(0f, 255 / 255f, 180 / 255f, 1f);

    private final Texture sumoP1Front = new Texture("sumo/sumo_p1_front.png");
    private final Texture sumoP1Side  = new Texture("sumo/sumo_p1_side.png");
    private final Texture sumoP2Front = new Texture("sumo/sumo_p2_front.png");
    private final Texture sumoP2Side  = new Texture("sumo/sumo_p2_side.png");

    private float p1X;
    private float p1Y;
    private float p1VelY = 0f;

    private float p2X;
    private float p2Y;
    private float p2VelY = 0f;

    private float lastMoveP1 = 0f;
    private float lastMoveP2 = 0f;
    private boolean p1Cayo;
    private boolean p2Cayo;

    private boolean shockwaveActive = false;
    private float shockwaveX;
    private float shockwaveY;
    private float shockwaveRadius;
    private float shockwaveAlpha;

    private int p1RoundsWon = 0;
    private int p2RoundsWon = 0;
    private String outcomeReason = "";

    public SumoScreen(Game game) {
        super(game);
        resetFighters();
    }

    private void resetFighters() {
        p1X = PLAT_X + DISTANCIA_INICIO;
        p1Y = BASE_Y;
        p1VelY = 0f;

        p2X = PLAT_X + PLAT_ANCHO - DISTANCIA_INICIO;
        p2Y = BASE_Y;
        p2VelY = 0f;

        lastMoveP1 = 0f;
        lastMoveP2 = 0f;
        p1Cayo = false;
        p2Cayo = false;
        shockwaveActive = false;
        outcomeReason = "";
    }

    private float leerMovimiento(int p, float delta) {
        float move = 0f;
        if (Gdx.input.isKeyJustPressed(KEYS[p][0])) move -= IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(KEYS[p][0])) move -= VELOCIDAD_HOLD * delta;
        if (Gdx.input.isKeyJustPressed(KEYS[p][1])) move += IMPULSO_TAP;
        else if (Gdx.input.isKeyPressed(KEYS[p][1])) move += VELOCIDAD_HOLD * delta;
        return move;
    }

    @Override
    protected float getRoundDuration() {
        return ROUND_DURATION;
    }

    @Override
    protected void updateGame(float delta) {
        if (p1Cayo || p2Cayo) {
            if (p1Cayo) p1Y -= VELOCIDAD_CAIDA * delta;
            if (p2Cayo) p2Y -= VELOCIDAD_CAIDA * delta;
            return;
        }

        float moveP1 = leerMovimiento(0, delta);
        if (Gdx.input.isKeyJustPressed(KEYS[0][2]) && Math.abs(p1Y - BASE_Y) < 1.5f) {
            p1VelY = FUERZA_SALTO;
        }

        float moveP2 = leerMovimiento(1, delta);
        if (Gdx.input.isKeyJustPressed(KEYS[1][2]) && Math.abs(p2Y - BASE_Y) < 1.5f) {
            p2VelY = FUERZA_SALTO;
        }

        p1X += moveP1;
        p2X += moveP2;

        lastMoveP1 = moveP1;
        lastMoveP2 = moveP2;

        boolean p1SobrePlataforma = (p1X >= PLAT_X - RADIO && p1X <= PLAT_X + PLAT_ANCHO + RADIO);
        p1VelY -= GRAVEDAD * delta;
        p1Y += p1VelY * delta;
        if (p1SobrePlataforma && p1Y <= BASE_Y) {
            p1Y = BASE_Y;
            p1VelY = 0f;
        }

        boolean p2SobrePlataforma = (p2X >= PLAT_X - RADIO && p2X <= PLAT_X + PLAT_ANCHO + RADIO);
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

            boolean p1AtacandoHaciaP2 = (p1X < p2X && moveP1 > 0f) || (p1X > p2X && moveP1 < 0f);
            boolean p2AtacandoHaciaP1 = (p2X > p1X && moveP2 < 0f) || (p2X < p1X && moveP2 > 0f);

            if (p1AtacandoHaciaP2 && p2AtacandoHaciaP1) {
                shockwaveActive = true;
                shockwaveX = (p1X + p2X) / 2f;
                shockwaveY = (p1Y + p2Y) / 2f;
                shockwaveRadius = ONDA_RADIO_INICIAL;
                shockwaveAlpha = 1f;

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

        if (shockwaveActive) {
            shockwaveRadius += ONDA_CRECIMIENTO * delta;
            shockwaveAlpha -= ONDA_DESVANECER * delta;
            if (shockwaveAlpha <= 0f) {
                shockwaveActive = false;
            }
        }

        boolean p1Fuera = (p1X < PLAT_X - RADIO || p1X > PLAT_X + PLAT_ANCHO + RADIO);
        boolean p2Fuera = (p2X < PLAT_X - RADIO || p2X > PLAT_X + PLAT_ANCHO + RADIO);

        if (p1Fuera && p2Fuera) {
            p1Cayo = true;
            p2Cayo = true;
            outcomeReason = "¡AMBOS CAYERON AL MISMO TIEMPO!";
            finishRound();
        } else if (p1Fuera) {
            p1Cayo = true;
            p2RoundsWon++;
            outcomeReason = "¡RING OUT! JUGADOR 2 EMPUJA AL RIVAL";
            finishRound();
        } else if (p2Fuera) {
            p2Cayo = true;
            p1RoundsWon++;
            outcomeReason = "¡RING OUT! JUGADOR 1 EMPUJA AL RIVAL";
            finishRound();
        }
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(COLOR_BG);
        shapeRenderer.rect(0f, 0f, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        shapeRenderer.setColor(COLOR_PLATAFORMA);
        shapeRenderer.rect(PLAT_X, PLAT_Y, PLAT_ANCHO, PLAT_ALTO);

        shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.8f);
        shapeRenderer.rect(PLAT_X, PLAT_Y, 20f, PLAT_ALTO);
        shapeRenderer.rect(PLAT_X + PLAT_ANCHO - 20f, PLAT_Y, 20f, PLAT_ALTO);

        float shadowScale1 = MathUtils.clamp(1f - (p1Y - BASE_Y) / 180f, 0.35f, 1f);
        float shadowScale2 = MathUtils.clamp(1f - (p2Y - BASE_Y) / 180f, 0.35f, 1f);

        shapeRenderer.setColor(0.08f, 0.1f, 0.14f, 0.6f);
        shapeRenderer.ellipse(p1X - (RADIO * 0.9f * shadowScale1), PLAT_Y + PLAT_ALTO - 4f, RADIO * 1.8f * shadowScale1, 8f * shadowScale1);
        shapeRenderer.ellipse(p2X - (RADIO * 0.9f * shadowScale2), PLAT_Y + PLAT_ALTO - 4f, RADIO * 1.8f * shadowScale2, 8f * shadowScale2);

        shapeRenderer.end();

        batch.begin();

        Texture p1Tex = (Math.abs(lastMoveP1) > 0.01f) ? sumoP1Side : sumoP1Front;
        boolean p1FlipX = (lastMoveP1 < -0.01f);
        float p1DrawX = p1X - SPRITE_SIZE / 2f;
        float p1DrawY = p1Y - SPRITE_SIZE / 2f;
        batch.draw(p1Tex, p1DrawX, p1DrawY, SPRITE_SIZE, SPRITE_SIZE, 0, 0, 48, 48, p1FlipX, false);

        Texture p2Tex = (Math.abs(lastMoveP2) > 0.01f) ? sumoP2Side : sumoP2Front;
        boolean p2FlipX = (lastMoveP2 <= 0.01f);
        float p2DrawX = p2X - SPRITE_SIZE / 2f;
        float p2DrawY = p2Y - SPRITE_SIZE / 2f;
        batch.draw(p2Tex, p2DrawX, p2DrawY, SPRITE_SIZE, SPRITE_SIZE, 0, 0, 48, 48, p2FlipX, false);

        font.setColor(COLOR_P1);
        font.getData().setScale(1.1f);
        font.draw(batch, "P1: [A/D] Empuja  [W] Salta", 45f, 70f);

        font.setColor(COLOR_P2);
        font.draw(batch, "P2: [FLECHAS] Empuja  [ARRIBA] Salta", VIRTUAL_WIDTH - 295f, 70f);

        batch.end();

        if (shockwaveActive && shockwaveAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeType.Line);
            Gdx.gl.glLineWidth(3f);
            shapeRenderer.setColor(1f, 0.9f, 0.3f, MathUtils.clamp(shockwaveAlpha, 0f, 1f));
            shapeRenderer.circle(shockwaveX, shockwaveY, shockwaveRadius);

            shapeRenderer.setColor(1f, 1f, 1f, MathUtils.clamp(shockwaveAlpha * 0.7f, 0f, 1f));
            shapeRenderer.circle(shockwaveX, shockwaveY, shockwaveRadius * 0.65f);
            shapeRenderer.end();

            Gdx.gl.glLineWidth(1f);
        }
    }

    @Override
    protected String determineRoundWinner() {
        if (p1Cayo && p2Cayo) {
            return "¡EMPATE POR DOBLE CAÍDA!";
        }

        if (p2Cayo) {
            p1GlobalWins += PUNTOS_KO;
            return "¡JUGADOR 1 GANA (" + PUNTOS_KO + " PUNTOS POR KO)!";
        } else if (p1Cayo) {
            p2GlobalWins += PUNTOS_KO;
            return "¡JUGADOR 2 GANA (" + PUNTOS_KO + " PUNTOS POR KO)!";
        }

        float d1 = Math.abs(p1X - PLAT_CENTER_X);
        float d2 = Math.abs(p2X - PLAT_CENTER_X);

        if (d1 < d2 - MARGEN_CENTRO) {
            p1GlobalWins += PUNTOS_CENTRO;
            return "¡JUGADOR 1 DOMINA EL CENTRO (" + PUNTOS_CENTRO + " PUNTO)!";
        } else if (d2 < d1 - MARGEN_CENTRO) {
            p2GlobalWins += PUNTOS_CENTRO;
            return "¡JUGADOR 2 DOMINA EL CENTRO (" + PUNTOS_CENTRO + " PUNTO)!";
        }
        return "¡EMPATE POR TIEMPO!";
    }

    @Override
    protected void onRoundReset() {
        resetFighters();
    }

    @Override
    protected String getP1StatusText() {
        return "P1 (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2 (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return outcomeReason.isEmpty() ? "Lucha cerrada sobre la plataforma" : outcomeReason;
    }

    @Override
    public void dispose() {
        super.dispose();
        sumoP1Front.dispose();
        sumoP1Side.dispose();
        sumoP2Front.dispose();
        sumoP2Side.dispose();
    }
}
