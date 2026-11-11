package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.ramshi.Main;
import io.github.ramshi.session.GameSession;
import io.github.ramshi.session.MinigameType;

public abstract class BaseMinigameScreen implements Screen {
    public static final float ANCHO_VIRTUAL = 800f;
    public static final float ALTO_VIRTUAL = 480f;

    private static final float SEGUNDOS_INTRO = 1.5f;
    private static final float TAMANO_CORAZON = 40f;
    private static final float SEPARACION_CORAZON = 8f;
    private static final float CORAZONES_Y = 398f;
    private static final float GANADOR_Y = 462f;
    private static final float MOTIVO_Y = 418f;
    private static final float EXPLICACION_Y = 392f;
    private static final float ANCHO_EXPLICACION = 700f;
    private static final float TABLA_Y = 336f;
    private static final float ALTO_FILA = 17f;
    private static final float TOTAL_Y = 58f;
    private static final float COL_NUMERO = 60f;
    private static final float COL_NOMBRE = 100f;
    private static final float COL_P1 = 470f;
    private static final float COL_P2 = 550f;
    private static final float COL_GANADOR = 630f;
    private static final float SEGUNDOS_RESULTADO = 2.5f;
    private static final float MARGEN_HUD = 16f;
    private static final float ALTURA_HUD = 462f;
    private static final float ALTO_LEYENDA = 40f;
    private static final float LEYENDA_Y = 4f;
    private static final float CONSIGNA_Y = 380f;
    private static final float CONTROLES_Y = 230f;
    private static final Color COLOR_CONSIGNA = new Color(1f, 0.85f, 0.2f, 1f);
    private static final Color COLOR_P1 = new Color(0.3f, 0.9f, 0.3f, 1f);
    private static final Color COLOR_P2 = new Color(0.3f, 0.7f, 1f, 1f);
    private static final float RELOJ_X = 400f;
    private static final float RELOJ_Y = 450f;
    private static final float RELOJ_RADIO = 24f;
    private static final float BORDE_RELOJ = 3f;
    private static final float SEPARACION_RELOJ = 6f;
    private static final float BARRA_Y = 470f;
    private static final float ALTO_BARRA = 8f;
    private static final float SESION_Y = 418f;
    private static final float SEGUNDOS_ALERTA = 3f;
    private static final Color COLOR_RELOJ = new Color(1f, 0.85f, 0.2f, 1f);
    private static final Color COLOR_RELOJ_ALERTA = new Color(1f, 0.3f, 0.3f, 1f);
    private static final Color COLOR_FONDO_RELOJ = new Color(0.1f, 0.1f, 0.12f, 0.9f);
    private static final Color COLOR_PISTA = new Color(0f, 0f, 0f, 0.45f);
    private static final Color COLOR_VERDE = new Color(0.25f, 0.85f, 0.3f, 1f);
    private static final Color COLOR_AMARILLO = new Color(1f, 0.85f, 0.2f, 1f);
    private static final Color COLOR_ROJO = new Color(0.95f, 0.25f, 0.2f, 1f);

    protected final Game juego;
    protected final GameSession sesion;
    protected SpriteBatch batch;
    protected BitmapFont fuente;
    protected ShapeRenderer formas;
    protected OrthographicCamera camara;
    protected Viewport vista;
    protected float tiempoRestante;
    protected boolean rondaTerminada;
    protected int victoriasP1;
    protected int victoriasP2;
    protected String mensajeGanador;
    protected String explicacion = "";

    private float tiempoIntro = SEGUNDOS_INTRO;
    private float tiempoResultado = 0f;
    private int puntosGanadosP1 = 0;
    private int puntosGanadosP2 = 0;
    private Texture corazon;
    private Texture corazonVacio;
    private final Color colorBarra = new Color();

    protected BaseMinigameScreen(Game juego) {
        this.juego = juego;
        this.sesion = GameSession.actual();
        this.batch = new SpriteBatch();
        this.fuente = Main.fuenteChica;
        this.formas = new ShapeRenderer();
        this.camara = new OrthographicCamera();
        this.vista = new FitViewport(ANCHO_VIRTUAL, ALTO_VIRTUAL, camara);
        this.vista.apply();
        this.camara.position.set(ANCHO_VIRTUAL / 2f, ALTO_VIRTUAL / 2f, 0);
        this.tiempoRestante = getDuracionRonda();
    }

    public void terminarRonda() {
        if (rondaTerminada) return;
        int antesP1 = victoriasP1;
        int antesP2 = victoriasP2;
        rondaTerminada = true;
        explicacion = "";
        mensajeGanador = determinarGanador();
        puntosGanadosP1 = victoriasP1 - antesP1;
        puntosGanadosP2 = victoriasP2 - antesP2;
        if (puntosGanadosP1 == 0 && puntosGanadosP2 == 0 && explicacion.isEmpty()) explicacion = "Empate: nadie suma puntos.";
        tiempoResultado = 0f;
    }

    public void iniciarNuevaRonda() {
        tiempoRestante = getDuracionRonda();
        rondaTerminada = false;
        tiempoResultado = 0f;
        reiniciarRonda();
    }

    protected void irA(Screen siguiente) {
        juego.setScreen(siguiente);
        dispose();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            irA(new MainMenuScreen(juego));
            return;
        }

        if (tiempoIntro > 0f) {
            tiempoIntro -= delta;
        } else if (!rondaTerminada) {
            tiempoRestante -= delta;
            if (tiempoRestante <= 0f) {
                tiempoRestante = 0f;
                terminarRonda();
            } else {
                actualizarJuego(delta);
            }
        } else {
            tiempoResultado += delta;
            boolean saltar = !sesion.esModoJugar()
                && (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER));
            if (tiempoResultado >= SEGUNDOS_RESULTADO || saltar) {
                if (sesion.esModoJugar()) {
                    sesion.registrarResultado(puntosGanadosP1, puntosGanadosP2);
                    irA(sesion.siguientePantalla(juego));
                    return;
                }
                iniciarNuevaRonda();
            }
        }

        ScreenUtils.clear(0.08f, 0.08f, 0.08f, 1f);
        camara.update();
        batch.setProjectionMatrix(camara.combined);
        formas.setProjectionMatrix(camara.combined);

        dibujarJuego(batch);
        dibujarHUD();

        if (tiempoIntro > 0f) dibujarIntro();
        else if (rondaTerminada) dibujarFinDeRonda();
    }

    public void dibujarHUD() {
        Controles.Control[] controles = getControles();
        if (controles.length > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            formas.begin(ShapeRenderer.ShapeType.Filled);
            formas.setColor(0f, 0f, 0f, 0.55f);
            formas.rect(0f, 0f, ANCHO_VIRTUAL, ALTO_LEYENDA);
            formas.end();
        }

        dibujarFondoReloj();

        batch.begin();
        for (int p = 0; p < 2 && controles.length > 0; p++) {
            Controles.dibujarLeyenda(batch, controles, p, centroJugador(p), LEYENDA_Y);
        }

        if (sesion.esModoJugar()) {
            fuente.setColor(Color.WHITE);
            usarFuente(Main.fuenteChica);
            dibujarCentrado("Juego " + (sesion.getIndice() + 1) + "/" + sesion.getTotalJuegos()
                + "    Total:  P1 " + sesion.totalP1() + " - " + sesion.totalP2() + " P2", SESION_Y);
        }

        usarFuente(Main.fuenteCartel);
        fuente.setColor(tiempoRestante <= SEGUNDOS_ALERTA && !rondaTerminada ? COLOR_RELOJ_ALERTA : COLOR_RELOJ);
        GlyphLayout numero = new GlyphLayout(fuente, String.valueOf((int) Math.ceil(tiempoRestante)));
        fuente.draw(batch, numero, RELOJ_X - numero.width / 2f, RELOJ_Y + numero.height / 2f);

        usarFuente(Main.fuenteMediana);
        fuente.setColor(COLOR_P1);
        fuente.draw(batch, getTextoP1(), MARGEN_HUD, ALTURA_HUD);

        fuente.setColor(COLOR_P2);
        dibujarDerecha(getTextoP2(), ALTURA_HUD);

        if (getVidaMaxima() > 0) dibujarCorazones();
        batch.end();
    }

    private void dibujarFondoReloj() {
        float fraccion = Math.max(0f, tiempoRestante / getDuracionRonda());
        float largoMax = RELOJ_X - RELOJ_RADIO - SEPARACION_RELOJ;
        float largo = largoMax * fraccion;
        float inicioDerecha = RELOJ_X + RELOJ_RADIO + SEPARACION_RELOJ;
        Color color = fraccion > 0.5f
            ? colorBarra.set(COLOR_AMARILLO).lerp(COLOR_VERDE, (fraccion - 0.5f) * 2f)
            : colorBarra.set(COLOR_ROJO).lerp(COLOR_AMARILLO, fraccion * 2f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(COLOR_PISTA);
        formas.rect(0f, BARRA_Y, largoMax, ALTO_BARRA);
        formas.rect(inicioDerecha, BARRA_Y, largoMax, ALTO_BARRA);
        formas.setColor(color);
        formas.rect(largoMax - largo, BARRA_Y, largo, ALTO_BARRA);
        formas.rect(inicioDerecha, BARRA_Y, largo, ALTO_BARRA);
        formas.circle(RELOJ_X, RELOJ_Y, RELOJ_RADIO + BORDE_RELOJ, 40);
        formas.setColor(COLOR_FONDO_RELOJ);
        formas.circle(RELOJ_X, RELOJ_Y, RELOJ_RADIO, 40);
        formas.end();
    }

    private void dibujarCorazones() {
        if (corazon == null) {
            corazon = new Texture("quickdraw/heart.png");
            corazonVacio = new Texture("quickdraw/empty_heart.png");
            corazon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            corazonVacio.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        int maximo = getVidaMaxima();
        float ancho = maximo * TAMANO_CORAZON + (maximo - 1) * SEPARACION_CORAZON;
        for (int p = 0; p < 2; p++) {
            float x = centroJugador(p) - ancho / 2f;
            for (int i = 0; i < maximo; i++) {
                Texture t = (i < getVida(p)) ? corazon : corazonVacio;
                batch.draw(t, x + i * (TAMANO_CORAZON + SEPARACION_CORAZON), CORAZONES_Y, TAMANO_CORAZON, TAMANO_CORAZON);
            }
        }
    }

    protected int getVidaMaxima() {
        return 0;
    }

    protected int getVida(int jugador) {
        return 0;
    }

    private void dibujarIntro() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.03f, 0.03f, 0.06f, 0.88f);
        formas.rect(0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        formas.setColor(1f, 0.84f, 0f, 1f);
        formas.rect(ANCHO_VIRTUAL / 2f - 1f, 40f, 2f, 360f);
        formas.end();

        MinigameType tipo = sesion.tipoActual();
        Controles.Control[] controles = getControles();
        batch.begin();
        usarFuente(Main.fuenteGrande);
        fuente.setColor(Color.WHITE);
        dibujarCentrado(tipo != null ? tipo.getNombre() : "", ALTURA_HUD);
        for (int p = 0; p < 2; p++) {
            usarFuente(Main.fuenteCartel);
            fuente.setColor(COLOR_CONSIGNA);
            dibujarCentradoEn(getConsigna(), centroJugador(p), CONSIGNA_Y);
            usarFuente(Main.fuenteMediana);
            fuente.setColor(p == 0 ? COLOR_P1 : COLOR_P2);
            dibujarCentradoEn("JUGADOR " + (p + 1), centroJugador(p), CONSIGNA_Y - 44f);
            if (controles.length > 0) Controles.dibujarGrande(batch, controles, p, centroJugador(p), CONTROLES_Y);
        }
        batch.end();
    }

    private float centroJugador(int p) {
        return ANCHO_VIRTUAL * (p == 0 ? 0.25f : 0.75f);
    }

    private void dibujarCentradoEn(String texto, float centroX, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, centroX - layout.width / 2f, y);
    }

    protected Controles.Control[] getControles() {
        return new Controles.Control[0];
    }

    protected String getConsigna() {
        return "¡PREPARATE!";
    }

    private void dibujarTablaPartida() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.03f, 0.03f, 0.06f, 0.93f);
        formas.rect(0f, 0f, ANCHO_VIRTUAL, ALTO_VIRTUAL);
        formas.setColor(0.12f, 0.12f, 0.18f, 1f);
        formas.rect(40f, EXPLICACION_Y - 42f, ANCHO_VIRTUAL - 80f, MOTIVO_Y - EXPLICACION_Y + 50f);
        formas.end();

        batch.begin();
        usarFuente(Main.fuenteCartel);
        fuente.setColor(COLOR_CONSIGNA);
        dibujarCentrado(mensajeGanador, GANADOR_Y);
        usarFuente(Main.fuenteMediana);
        fuente.setColor(Color.WHITE);
        dibujarCentrado(getResumen(), MOTIVO_Y);
        usarFuente(Main.fuenteChica);
        fuente.setColor(0.85f, 0.85f, 0.9f, 1f);
        dibujarExplicacion(EXPLICACION_Y);

        usarFuente(Main.fuenteChica);
        fuente.setColor(Color.LIGHT_GRAY);
        float y = TABLA_Y;
        fuente.draw(batch, "#", COL_NUMERO, y);
        fuente.draw(batch, "MINIJUEGO", COL_NOMBRE, y);
        fuente.draw(batch, "P1", COL_P1, y);
        fuente.draw(batch, "P2", COL_P2, y);
        fuente.draw(batch, "GANÓ", COL_GANADOR, y);

        int fila = 0;
        for (GameSession.Resultado r : sesion.getResultados()) {
            y -= ALTO_FILA;
            dibujarFila(++fila, r.tipo.getNombre(), r.puntosP1, r.puntosP2, y, Color.WHITE);
        }
        y -= ALTO_FILA;
        MinigameType actual = sesion.tipoActual();
        dibujarFila(++fila, actual != null ? actual.getNombre() : "", puntosGanadosP1, puntosGanadosP2, y, COLOR_CONSIGNA);

        usarFuente(Main.fuenteMediana);
        fuente.setColor(COLOR_CONSIGNA);
        fuente.draw(batch, "TOTAL", COL_NOMBRE, TOTAL_Y);
        fuente.setColor(COLOR_P1);
        fuente.draw(batch, String.valueOf(sesion.totalP1() + puntosGanadosP1), COL_P1, TOTAL_Y);
        fuente.setColor(COLOR_P2);
        fuente.draw(batch, String.valueOf(sesion.totalP2() + puntosGanadosP2), COL_P2, TOTAL_Y);

        usarFuente(Main.fuenteChica);
        fuente.setColor(0.7f, 1f, 0.7f, 1f);
        dibujarCentrado("Juego " + fila + " de " + sesion.getTotalJuegos() + "  -  siguiente en un instante...", 24f);
        batch.end();
    }

    private void dibujarExplicacion(float y) {
        GlyphLayout layout = new GlyphLayout(fuente, explicacion, fuente.getColor(), ANCHO_EXPLICACION, Align.center, true);
        fuente.draw(batch, layout, (ANCHO_VIRTUAL - ANCHO_EXPLICACION) / 2f, y);
    }

    private void dibujarFila(int numero, String nombre, int p1, int p2, float y, Color color) {
        fuente.setColor(color);
        fuente.draw(batch, String.valueOf(numero), COL_NUMERO, y);
        fuente.draw(batch, nombre, COL_NOMBRE, y);
        fuente.setColor(COLOR_P1);
        fuente.draw(batch, "+" + p1, COL_P1, y);
        fuente.setColor(COLOR_P2);
        fuente.draw(batch, "+" + p2, COL_P2, y);
        fuente.setColor(color);
        fuente.draw(batch, p1 > p2 ? "P1" : (p2 > p1 ? "P2" : "-"), COL_GANADOR, y);
    }

    public void dibujarFinDeRonda() {
        if (sesion.esModoJugar()) {
            dibujarTablaPartida();
            return;
        }
        dibujarCaja(100f, 110f, 600f, 260f);
        batch.begin();

        fuente.setColor(Color.WHITE);
        usarFuente(Main.fuenteGrande);
        dibujarCentrado(mensajeGanador, 350f);

        fuente.setColor(Color.WHITE);
        usarFuente(Main.fuenteMediana);
        dibujarCentrado(getResumen(), 310f);

        fuente.setColor(0.85f, 0.85f, 0.9f, 1f);
        usarFuente(Main.fuenteChica);
        GlyphLayout textoExplicacion = new GlyphLayout(fuente, explicacion, fuente.getColor(), 560f, Align.center, true);
        fuente.draw(batch, textoExplicacion, 120f, 280f);

        fuente.setColor(1f, 0.85f, 0.2f, 1f);
        usarFuente(Main.fuenteMediana);
        dibujarCentrado("VICTORIAS TOTALES:  P1 [" + victoriasP1 + "]  -  P2 [" + victoriasP2 + "]", 200f);

        fuente.setColor(0.7f, 1f, 0.7f, 1f);
        usarFuente(Main.fuenteChica);
        dibujarCentrado("[ ESPACIO ] Siguiente Ronda       [ ESC ] Menu Principal", 145f);

        usarFuente(Main.fuenteMediana);
        batch.end();
    }

    private void dibujarCaja(float x, float y, float w, float h) {
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.05f, 0.05f, 0.05f, 0.92f);
        formas.rect(x, y, w, h);
        formas.end();
        formas.begin(ShapeRenderer.ShapeType.Line);
        formas.setColor(1f, 0.84f, 0f, 1f);
        formas.rect(x, y, w, h);
        formas.end();
    }

    private void dibujarCentrado(String texto, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, (ANCHO_VIRTUAL - layout.width) / 2f, y);
    }

    protected void dibujarDerecha(String texto, float y) {
        GlyphLayout layout = new GlyphLayout(fuente, texto);
        fuente.draw(batch, layout, ANCHO_VIRTUAL - MARGEN_HUD - layout.width, y);
    }

    @Override public void dispose() {
        batch.dispose();
        formas.dispose();
        if (corazon != null) {
            corazon.dispose();
            corazonVacio.dispose();
        }
    }
    @Override public void show() {}
    @Override public void resize(int width, int height) { vista.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    protected void usarFuente(BitmapFont nueva) {
        nueva.setColor(fuente.getColor());
        fuente = nueva;
    }

    protected String sumarPuntos(int jugador, int puntos) {
        if (jugador == 1) victoriasP1 += puntos;
        else victoriasP2 += puntos;
        return "¡JUGADOR " + jugador + " GANA (+" + puntos + (puntos == 1 ? " PUNTO" : " PUNTOS") + ")!";
    }

    protected String segundosJugados() {
        return String.format("%.1f s", getDuracionRonda() - tiempoRestante);
    }

    protected String reglaTiempo() {
        return "(antes de 3,3 s = 3 pts, antes de 6,6 s = 2, después = 1)";
    }

    protected int puntosPorTiempo() {
        float progreso = 1f - tiempoRestante / getDuracionRonda();
        if (progreso < 1f / 3f) return 3;
        if (progreso < 2f / 3f) return 2;
        return 1;
    }

    protected abstract float getDuracionRonda();
    protected abstract void actualizarJuego(float delta);
    protected abstract void dibujarJuego(SpriteBatch batch);
    protected abstract String determinarGanador();
    protected abstract void reiniciarRonda();
    protected abstract String getTextoP1();
    protected abstract String getTextoP2();
    protected abstract String getResumen();
}