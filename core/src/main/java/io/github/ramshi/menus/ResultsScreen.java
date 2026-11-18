package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;
import io.github.ramshi.red.Red;
import io.github.ramshi.screens.BaseMinigameScreen;
import io.github.ramshi.session.GameSession;

public class ResultsScreen extends MenuScreen {

    private static final float BLOQUEO_INICIAL = 1.5f;
    private static final float GANADOR_Y = 455f;
    private static final float MOTIVO_Y = 418f;
    private static final float JUEGOS_Y = 396f;
    private static final float ENCABEZADO_Y = 370f;
    private static final float PRIMERA_FILA_Y = 350f;
    private static final float ALTO_FILA = 19f;
    private static final float TOTAL_Y = 112f;
    private static final float COL_NUMERO = 60f;
    private static final float COL_NOMBRE = 100f;
    private static final float COL_P1 = 470f;
    private static final float COL_P2 = 550f;
    private static final float COL_GANADOR = 630f;
    private static final float REVANCHA_ANCHO = 200f;
    private static final float REVANCHA_ALTO = 44f;
    private static final float REVANCHA_Y = 20f;
    private static final Color COLOR_P1 = new Color(0.3f, 0.9f, 0.3f, 1f);
    private static final Color COLOR_P2 = new Color(0.3f, 0.7f, 1f, 1f);

    private final GameSession sesion;
    private final String textoGanador;
    private final String textoMotivo;
    private final int juegosP1;
    private final int juegosP2;
    private final Boton menu;
    private final Boton revancha;
    private final boolean enRed;
    private float tiempoBloqueo = BLOQUEO_INICIAL;

    public ResultsScreen(Game juego, GameSession sesion) {
        super(juego);
        this.sesion = sesion;
        fundidoEntrada = BaseMinigameScreen.DURACION_FUNDIDO;

        int p1 = sesion.totalP1();
        int p2 = sesion.totalP2();
        int g1 = 0;
        int g2 = 0;
        for (GameSession.Resultado r : sesion.getResultados()) {
            if (r.puntosP1 > r.puntosP2) g1++;
            else if (r.puntosP2 > r.puntosP1) g2++;
        }
        juegosP1 = g1;
        juegosP2 = g2;

        if (p1 != p2) {
            textoGanador = "¡GANA EL JUGADOR " + (p1 > p2 ? 1 : 2) + "!";
            textoMotivo = "Por " + Math.abs(p1 - p2) + " puntos de diferencia (" + p1 + " a " + p2 + ")";
        } else if (g1 != g2) {
            textoGanador = "¡GANA EL JUGADOR " + (g1 > g2 ? 1 : 2) + "!";
            textoMotivo = "Empate en puntos (" + p1 + "), desempata por juegos ganados (" + g1 + " a " + g2 + ")";
        } else {
            textoGanador = "¡EMPATE!";
            textoMotivo = "Mismos puntos (" + p1 + ") y mismos juegos ganados (" + g1 + ")";
        }

        int rondas = sesion.getTotalJuegos();
        enRed = sesion.esEnRed();
        revancha = botonera.agregar(new Boton((ANCHO - REVANCHA_ANCHO) / 2f, REVANCHA_Y, REVANCHA_ANCHO, REVANCHA_ALTO,
            "REVANCHA", Boton.VERDE, () -> abrir((enRed ? Red.iniciarRondas(rondas) : GameSession.iniciarJugar(rondas))
                .primeraPantalla(juego))))
            .conFuente(Main.fuenteMediana);
        menu = agregarVolver(this::salir);
        menu.texto = enRed ? "SALA" : "MENÚ";
        revancha.activo = false;
        menu.activo = false;
    }

    private void salir() {
        if (!enRed) {
            abrir(new MainMenuScreen(juego));
            return;
        }
        Red.enviarSeguro(Red.VOLVER_A_SALA);
        abrir(Red.esAnfitrion() ? new ModoMenuScreen(juego, true) : new EsperaSalaScreen(juego));
    }

    @Override
    protected void actualizar() {
        tiempoBloqueo -= Gdx.graphics.getDeltaTime();
        boolean listo = tiempoBloqueo <= 0f;
        revancha.activo = listo && (!enRed || Red.esAnfitrion());
        menu.activo = listo;
    }

    @Override
    protected void dibujar() {
        batch.begin();
        escribir(Main.fuenteCartel, COLOR_TITULO, textoGanador, ANCHO / 2f, GANADOR_Y);
        escribir(Main.fuenteChica, COLOR_TEXTO, textoMotivo, ANCHO / 2f, MOTIVO_Y);
        escribir(Main.fuenteChica, COLOR_TEXTO, "Juegos ganados:  P1 " + juegosP1 + " - " + juegosP2 + " P2", ANCHO / 2f, JUEGOS_Y);

        BitmapFont fuente = Main.fuenteChica;
        fuente.setColor(Color.LIGHT_GRAY);
        fuente.draw(batch, "#", COL_NUMERO, ENCABEZADO_Y);
        fuente.draw(batch, "MINIJUEGO", COL_NOMBRE, ENCABEZADO_Y);
        fuente.draw(batch, "P1", COL_P1, ENCABEZADO_Y);
        fuente.draw(batch, "P2", COL_P2, ENCABEZADO_Y);
        fuente.draw(batch, "GANÓ", COL_GANADOR, ENCABEZADO_Y);

        float y = PRIMERA_FILA_Y;
        int numero = 1;
        for (GameSession.Resultado r : sesion.getResultados()) {
            fuente.setColor(Color.WHITE);
            fuente.draw(batch, String.valueOf(numero++), COL_NUMERO, y);
            fuente.draw(batch, r.tipo.getNombre(), COL_NOMBRE, y);
            fuente.setColor(COLOR_P1);
            fuente.draw(batch, "+" + r.puntosP1, COL_P1, y);
            fuente.setColor(COLOR_P2);
            fuente.draw(batch, "+" + r.puntosP2, COL_P2, y);
            fuente.setColor(Color.WHITE);
            fuente.draw(batch, r.puntosP1 > r.puntosP2 ? "P1" : (r.puntosP2 > r.puntosP1 ? "P2" : "-"), COL_GANADOR, y);
            y -= ALTO_FILA;
        }

        BitmapFont total = Main.fuenteMediana;
        total.setColor(COLOR_TITULO);
        total.draw(batch, "TOTAL", COL_NOMBRE, TOTAL_Y);
        total.setColor(COLOR_P1);
        total.draw(batch, String.valueOf(sesion.totalP1()), COL_P1, TOTAL_Y);
        total.setColor(COLOR_P2);
        total.draw(batch, String.valueOf(sesion.totalP2()), COL_P2, TOTAL_Y);
        batch.end();
    }
}
