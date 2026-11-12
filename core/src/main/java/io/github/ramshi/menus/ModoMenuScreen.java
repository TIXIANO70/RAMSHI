package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;
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
    private static final float PROXIMAMENTE_Y = 55f;

    private static int rondas = RONDAS_INICIALES;

    private final boolean enRed;

    public ModoMenuScreen(Game juego, boolean enRed) {
        super(juego);
        this.enRed = enRed;

        float botonX = CENTRO_X - BOTON_ANCHO / 2f;
        Boton menos = botonera.agregar(new Boton(CENTRO_X - SELECTOR_DISTANCIA - SELECTOR_ANCHO / 2f, SELECTOR_Y,
            SELECTOR_ANCHO, SELECTOR_ALTO, "-", Boton.NEUTRO, () -> rondas = Math.max(MIN_RONDAS, rondas - 1)));
        Boton mas = botonera.agregar(new Boton(CENTRO_X + SELECTOR_DISTANCIA - SELECTOR_ANCHO / 2f, SELECTOR_Y,
            SELECTOR_ANCHO, SELECTOR_ALTO, "+", Boton.NEUTRO, () -> rondas = Math.min(MAX_RONDAS, rondas + 1)));
        Boton jugar = botonera.agregar(new Boton(botonX, JUGAR_Y, BOTON_ANCHO, BOTON_ALTO, "JUGAR RONDAS", Boton.VERDE,
            () -> abrir(GameSession.iniciarJugar(rondas).primeraPantalla(juego))));
        Boton endless = botonera.agregar(new Boton(botonX, ENDLESS_Y, BOTON_ANCHO, BOTON_ALTO, "ENDLESS", Boton.NEUTRO,
            () -> abrir(new EndlessMenuScreen(juego))));
        agregarVolver(() -> abrir(new JugarMenuScreen(juego)));
        botonera.enfocar(jugar);

        if (enRed) {
            menos.activo = false;
            mas.activo = false;
            jugar.activo = false;
            endless.activo = false;
        }
    }

    @Override
    protected void dibujar() {
        dibujarTitulo(enRed ? "MODO EN RED" : "MODO LOCAL");
        batch.begin();
        escribir(Main.fuenteMediana, COLOR_SUBTITULO, "PARTIDA POR RONDAS", CENTRO_X, SECCION_RONDAS_Y);
        escribir(Main.fuenteGrande, COLOR_TEXTO, rondas + " RONDAS", CENTRO_X, SELECTOR_Y + 32f);
        escribir(Main.fuenteChica, COLOR_NOTA, "Mínimo " + MIN_RONDAS + ", máximo " + MAX_RONDAS, CENTRO_X, LIMITES_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Elegí un minijuego y jugalo sin parar", CENTRO_X, DESCRIPCION_ENDLESS_Y);
        if (enRed) escribir(Main.fuenteCartel, COLOR_TITULO, "Próximamente...", CENTRO_X + 80f, PROXIMAMENTE_Y);
        batch.end();
    }
}
