package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;
import io.github.ramshi.red.Red;
import io.github.ramshi.session.GameSession;

public class ModoMenuScreen extends MenuScreen {

    public static final int MIN_RONDAS = 3;
    public static final int MAX_RONDAS = 12;
    private static final int RONDAS_INICIALES = 8;
    private static final float CENTRO_X = ANCHO / 2f;
    private static final float SECCION_RONDAS_Y = 390f;
    private static final float SELECTOR_Y = 320f;
    private static final float SELECTOR_ALTO = 44f;
    private static final float SELECTOR_ANCHO = 50f;
    private static final float SELECTOR_DISTANCIA = 110f;
    private static final float LIMITES_Y = 305f;
    private static final float BOTON_ANCHO = 260f;
    private static final float BOTON_ALTO = 50f;
    private static final float JUGAR_Y = 235f;
    private static final float ENDLESS_Y = 135f;
    private static final float DESCRIPCION_ENDLESS_Y = 120f;
    private static final float CONECTADO_Y = 48f;

    private static int rondas = RONDAS_INICIALES;

    private final boolean enRed;

    public ModoMenuScreen(Game juego, boolean enRed) {
        super(juego);
        this.enRed = enRed;

        float botonX = CENTRO_X - BOTON_ANCHO / 2f;
        botonera.agregar(new Boton(CENTRO_X - SELECTOR_DISTANCIA - SELECTOR_ANCHO / 2f, SELECTOR_Y,
            SELECTOR_ANCHO, SELECTOR_ALTO, "-", Boton.NEUTRO, () -> rondas = Math.max(MIN_RONDAS, rondas - 1)));
        botonera.agregar(new Boton(CENTRO_X + SELECTOR_DISTANCIA - SELECTOR_ANCHO / 2f, SELECTOR_Y,
            SELECTOR_ANCHO, SELECTOR_ALTO, "+", Boton.NEUTRO, () -> rondas = Math.min(MAX_RONDAS, rondas + 1)));
        Boton jugar = botonera.agregar(new Boton(botonX, JUGAR_Y, BOTON_ANCHO, BOTON_ALTO, "JUGAR RONDAS", Boton.VERDE,
            () -> abrir((enRed ? Red.iniciarRondas(rondas) : GameSession.iniciarJugar(rondas)).primeraPantalla(juego))));
        botonera.agregar(new Boton(botonX, ENDLESS_Y, BOTON_ANCHO, BOTON_ALTO, "ENDLESS", Boton.NEUTRO,
            () -> abrir(new EndlessMenuScreen(juego, enRed))));
        agregarVolver(this::volver);
        botonera.enfocar(jugar);
    }

    private void volver() {
        if (enRed) {
            Red.cerrar();
            abrir(new SalaMenuScreen(juego));
        } else {
            abrir(new JugarMenuScreen(juego));
        }
    }

    @Override
    protected void dibujar() {
        dibujarTitulo(enRed ? "SALA EN RED" : "MODO LOCAL");
        batch.begin();
        escribir(Main.fuenteMediana, COLOR_SUBTITULO, "PARTIDA POR RONDAS", CENTRO_X, SECCION_RONDAS_Y);
        escribir(Main.fuenteGrande, COLOR_TEXTO, rondas + " RONDAS", CENTRO_X, SELECTOR_Y + 32f);
        escribir(Main.fuenteChica, COLOR_NOTA, "Mínimo " + MIN_RONDAS + ", máximo " + MAX_RONDAS, CENTRO_X, LIMITES_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Elegí un minijuego y jugalo sin parar", CENTRO_X, DESCRIPCION_ENDLESS_Y);
        if (enRed) escribir(Main.fuenteChica, COLOR_SUBTITULO, "Jugador 2 conectado: " + Red.getIpOtro(), CENTRO_X + 80f, CONECTADO_Y);
        batch.end();
    }
}
