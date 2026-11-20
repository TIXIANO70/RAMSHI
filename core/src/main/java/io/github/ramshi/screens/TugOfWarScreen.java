package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.Sonidos;
import java.util.Arrays;

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

    private static final float SOGA_CENTRO_Y = 208f;
    private static final float SOGA_ALTO = 10f;
    private static final float SOGA_Y = SOGA_CENTRO_Y - SOGA_ALTO / 2f;
    private static final float MITAD_SOGA = 240f;
    private static final float ESCALA_TEXTURA_SOGA = 24f;

    private static final float LINEA_Y = 148f;
    private static final float LINEA_ALTO = 112f;
    private static final int SEGMENTOS_CENTRO = 8;
    private static final float POZO_MIN_X = 248f;
    private static final float POZO_MAX_X = 552f;

    private static final int CANTIDAD_HOMBRES = 4;
    private static final int CELDA_SPRITE = 24;
    private static final float TAMANO_HOMBRE = 48f;
    private static final float INICIO_EQUIPO = 150f;
    private static final float SEPARACION_HOMBRES = 25f;
    private static final float OFFSET_Y_ARRIBA = -9f;
    private static final float OFFSET_Y_ABAJO = -41f;
    private static final float VELOCIDAD_RECUPERACION = 5.5f;

    private static final int MAX_PARTICULAS = 24;

    private float sogaObjetivo = 0f;
    private float sogaMostrada = 0f;
    private float tiempoAnim = 0f;

    private final int[] tirones = new int[2];
    private final int[] ultimaTecla = {-1, -1};
    private final float[] impulsoTiron = new float[2];

    private final float[] partX = new float[MAX_PARTICULAS];
    private final float[] partY = new float[MAX_PARTICULAS];
    private final float[] partVx = new float[MAX_PARTICULAS];
    private final float[] partVy = new float[MAX_PARTICULAS];
    private final float[] partVida = new float[MAX_PARTICULAS];
    private final float[] partMaxVida = new float[MAX_PARTICULAS];
    private final float[] partTam = new float[MAX_PARTICULAS];
    private final boolean[] partAgua = new boolean[MAX_PARTICULAS];
    private int siguienteParticula = 0;

    private final Texture fondo = cargarTextura("tugofwar/tugofwar_bg.png");
    private final Texture texturaEquipoP1 = cargarTextura("tugofwar/team_p1.png");
    private final Texture texturaEquipoP2 = cargarTextura("tugofwar/team_p2.png");
    private final Texture texturaSoga = cargarTextura("tugofwar/rope.png");
    private final TextureRegion[][][] spritesEquipos = new TextureRegion[2][][];

    public TugOfWarScreen(Game juego) {
        super(juego);
        texturaSoga.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
        spritesEquipos[0] = TextureRegion.split(texturaEquipoP1, CELDA_SPRITE, CELDA_SPRITE);
        spritesEquipos[1] = TextureRegion.split(texturaEquipoP2, CELDA_SPRITE, CELDA_SPRITE);
    }

    @Override protected String getConsigna() { return "¡TIRÁ!"; }

    @Override
    protected Controles.Control[] getControles() {
        return new Controles.Control[] {
            Controles.dos("alternar", Controles.A, Controles.D, Controles.IZQUIERDA, Controles.DERECHA)
        };
    }

    private Texture cargarTextura(String ruta) {
        Texture t = new Texture(ruta);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    @Override
    protected float getDuracionRonda() {
        return DURACION_RONDA;
    }

    @Override
    protected void actualizarJuego(float delta) {
        tiempoAnim += delta;

        for (int p = 0; p < 2; p++) {
            impulsoTiron[p] = Math.max(0f, impulsoTiron[p] - VELOCIDAD_RECUPERACION * delta);
            for (int k = 0; k < 2; k++) {
                if (Gdx.input.isKeyJustPressed(TECLAS[p][k]) && k != ultimaTecla[p]) {
                    ultimaTecla[p] = k;
                    sogaObjetivo += (p == 0) ? -PASO_TIRON : PASO_TIRON;
                    tirones[p]++;
                    impulsoTiron[p] = 1f;
                    generarParticulasTiron(p);
                    Sonidos.sonar("tiron", 0.65f);
                }
            }
        }
        sogaObjetivo = MathUtils.clamp(sogaObjetivo, -DESPLAZAMIENTO_MAX, DESPLAZAMIENTO_MAX);
        sogaMostrada = MathUtils.lerp(sogaMostrada, sogaObjetivo, Math.min(1f, SUAVIZADO * delta));

        actualizarParticulas(delta);

        if (Math.abs(sogaObjetivo) >= DESPLAZAMIENTO_MAX) {
            sogaMostrada = sogaObjetivo;
            terminarRonda();
        }
    }

    private void generarParticulasTiron(int equipo) {
        float dir = (equipo == 0) ? -1f : 1f;
        int baseIdx = tirones[equipo] % CANTIDAD_HOMBRES;
        float centroSoga = CENTRO_X + sogaMostrada;

        for (int m = 0; m < 2; m++) {
            int hombre = (baseIdx + m * 2) % CANTIDAD_HOMBRES;
            float cx = centroSoga + dir * (INICIO_EQUIPO + hombre * SEPARACION_HOMBRES);
            float pieX = cx - dir * 8f;
            float pieY = SOGA_CENTRO_Y + ((hombre % 2 == 0) ? 13f : -13f);
            int semilla = tirones[equipo] * 7 + m * 13 + hombre * 5;

            int idx = siguienteParticula;
            siguienteParticula = (siguienteParticula + 1) % MAX_PARTICULAS;

            partX[idx] = pieX + ((semilla % 5) - 2);
            partY[idx] = pieY + (((semilla / 2) % 5) - 2);
            partVx[idx] = -dir * (24f + (semilla % 18));
            partVy[idx] = ((hombre % 2 == 0) ? 1f : -1f) * (10f + ((semilla / 3) % 14));
            partMaxVida[idx] = 0.24f + (semilla % 5) * 0.03f;
            partVida[idx] = partMaxVida[idx];
            partTam[idx] = 3f + (semilla % 3);
            partAgua[idx] = (cx >= POZO_MIN_X && cx <= POZO_MAX_X);
        }
    }

    private void actualizarParticulas(float delta) {
        float amortiguacion = Math.max(0f, 1f - 3.5f * delta);
        for (int i = 0; i < MAX_PARTICULAS; i++) {
            if (partVida[i] <= 0f) continue;
            partVida[i] -= delta;
            partX[i] += partVx[i] * delta;
            partY[i] += partVy[i] * delta;
            partVx[i] *= amortiguacion;
            partVy[i] *= amortiguacion;
        }
    }

    @Override
    protected void dibujarJuego(SpriteBatch batch) {
        batch.begin();
        batch.draw(fondo, 0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        batch.end();

        float centroSoga = CENTRO_X + sogaMostrada;
        float xInicioSoga = centroSoga - MITAD_SOGA;
        float xFinSoga = centroSoga + MITAD_SOGA;
        float largoSoga = MITAD_SOGA * 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        formas.begin(ShapeType.Filled);
        dibujarMarcasCampo();
        dibujarSombras(centroSoga, xInicioSoga, largoSoga);
        dibujarParticulas();
        formas.end();

        batch.begin();
        batch.draw(texturaSoga, xInicioSoga, SOGA_Y, largoSoga, SOGA_ALTO,
            0f, 1f, largoSoga / ESCALA_TEXTURA_SOGA, 0f);
        batch.end();

        formas.begin(ShapeType.Filled);
        dibujarNudosYBandana(centroSoga, xInicioSoga, xFinSoga);
        formas.end();

        batch.begin();
        dibujarEquipos(batch, centroSoga);
        batch.end();
    }

    private void dibujarMarcasCampo() {
        formas.setColor(1f, 1f, 1f, 0.82f);
        float paso = LINEA_ALTO / SEGMENTOS_CENTRO;
        for (int i = 0; i < SEGMENTOS_CENTRO; i++) {
            formas.rect(CENTRO_X - 1f, LINEA_Y + i * paso + 2f, 2f, paso - 6f);
        }

        formas.setColor(0.92f, 0.22f, 0.22f, 0.9f);
        formas.rect(CENTRO_X - DESPLAZAMIENTO_MAX - 2f, LINEA_Y, 4f, LINEA_ALTO);
        formas.setColor(0.25f, 0.65f, 1f, 0.9f);
        formas.rect(CENTRO_X + DESPLAZAMIENTO_MAX - 2f, LINEA_Y, 4f, LINEA_ALTO);
    }

    private void dibujarSombras(float centroSoga, float xInicioSoga, float largoSoga) {
        formas.setColor(0f, 0f, 0f, 0.28f);
        formas.rect(xInicioSoga, SOGA_Y - 3f, largoSoga, SOGA_ALTO - 2f);

        formas.setColor(0f, 0f, 0f, 0.30f);
        for (int p = 0; p < 2; p++) {
            float dir = (p == 0) ? -1f : 1f;
            for (int i = 0; i < CANTIDAD_HOMBRES; i++) {
                float cx = calcularCentroHombreX(centroSoga, p, i, dir);
                float sombraX = cx - dir * 1f;
                float sombraY = SOGA_CENTRO_Y + ((i % 2 == 0) ? 12f : -18f);
                formas.ellipse(sombraX - 15f, sombraY - 7f, 30f, 14f);
            }
        }
    }

    private void dibujarParticulas() {
        for (int i = 0; i < MAX_PARTICULAS; i++) {
            if (partVida[i] <= 0f) continue;
            float alfa = (partVida[i] / partMaxVida[i]) * 0.85f;
            if (partAgua[i]) {
                formas.setColor(0.58f, 0.82f, 0.98f, alfa);
            } else {
                formas.setColor(0.74f, 0.60f, 0.36f, alfa);
            }
            float tam = partTam[i];
            formas.rect(partX[i] - tam * 0.5f, partY[i] - tam * 0.5f, tam, tam);
        }
    }

    private void dibujarNudosYBandana(float centroSoga, float xInicioSoga, float xFinSoga) {
        formas.setColor(0.23f, 0.14f, 0.07f, 1f);
        formas.rect(xInicioSoga - 4f, SOGA_Y - 2f, 6f, SOGA_ALTO + 4f);
        formas.rect(xFinSoga - 2f, SOGA_Y - 2f, 6f, SOGA_ALTO + 4f);

        formas.setColor(0.47f, 0.30f, 0.14f, 1f);
        formas.rect(xInicioSoga - 3f, SOGA_Y - 1f, 4f, SOGA_ALTO + 2f);
        formas.rect(xFinSoga - 1f, SOGA_Y - 1f, 4f, SOGA_ALTO + 2f);

        float balanceo = MathUtils.sin(tiempoAnim * 8f) * 2.5f + (impulsoTiron[1] - impulsoTiron[0]) * 3f;

        formas.setColor(0.16f, 0.05f, 0.05f, 1f);
        formas.rect(centroSoga - 5f, SOGA_Y - 4f, 10f, SOGA_ALTO + 8f);
        formas.rect(centroSoga - 4f + balanceo * 0.5f, SOGA_Y - 16f, 8f, 13f);

        formas.setColor(0.92f, 0.16f, 0.16f, 1f);
        formas.rect(centroSoga - 4f, SOGA_Y - 3f, 8f, SOGA_ALTO + 6f);
        formas.rect(centroSoga - 3f + balanceo * 0.5f, SOGA_Y - 15f, 6f, 12f);

        formas.setColor(0.98f, 0.84f, 0.25f, 1f);
        formas.rect(centroSoga - 2f, SOGA_Y + 2f, 4f, SOGA_ALTO - 4f);
    }

    private void dibujarEquipos(SpriteBatch batch, float centroSoga) {
        for (int p = 0; p < 2; p++) {
            float dir = (p == 0) ? -1f : 1f;
            for (int i = 0; i < CANTIDAD_HOMBRES; i++) {
                boolean enTiron = impulsoTiron[p] > (0.22f + i * 0.03f);
                int fila = enTiron ? 1 : 0;
                float cx = calcularCentroHombreX(centroSoga, p, i, dir);
                float temblorY = MathUtils.sin(tiempoAnim * 18f + i * 1.9f + p * 3.1f)
                    * (0.4f + impulsoTiron[p] * 0.9f);
                float drawX = cx - TAMANO_HOMBRE / 2f;
                float drawY = SOGA_CENTRO_Y + ((i % 2 == 0) ? OFFSET_Y_ARRIBA : OFFSET_Y_ABAJO) + temblorY;

                batch.draw(spritesEquipos[p][fila][i], drawX, drawY, TAMANO_HOMBRE, TAMANO_HOMBRE);
            }
        }
    }

    private float calcularCentroHombreX(float centroSoga, int equipo, int indice, float dir) {
        float retroceso = impulsoTiron[equipo] * (3.5f + (indice % 2) * 1f);
        return centroSoga + dir * (INICIO_EQUIPO + indice * SEPARACION_HOMBRES + retroceso);
    }

    @Override
    protected String determinarGanador() {
        if (Math.abs(sogaObjetivo) < 0.01f) return "¡EMPATE EN LA RONDA!";
        int ganador = (sogaObjetivo < 0f) ? 1 : 2;
        boolean llego = Math.abs(sogaObjetivo) >= DESPLAZAMIENTO_MAX;
        int pts = llego ? puntosPorTiempo() : PUNTOS_SIN_KO;
        explicacion = llego
            ? "Llevó la soga hasta su línea a los " + segundosJugados() + " " + reglaTiempo()
            : "Nadie llegó a la línea: la soga quedó de su lado al terminar el tiempo, 1 punto.";
        return sumarPuntos(ganador, pts);
    }

    @Override
    protected void reiniciarRonda() {
        sogaObjetivo = 0f;
        sogaMostrada = 0f;
        tiempoAnim = 0f;
        tirones[0] = tirones[1] = 0;
        ultimaTecla[0] = ultimaTecla[1] = -1;
        impulsoTiron[0] = impulsoTiron[1] = 0f;
        siguienteParticula = 0;
        Arrays.fill(partVida, 0f);
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
        fondo.dispose();
        texturaEquipoP1.dispose();
        texturaEquipoP2.dispose();
        texturaSoga.dispose();
    }
}
