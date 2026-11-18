package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;

public class SalaMenuScreen extends MenuScreen {

    private static final float TARJETA_ANCHO = 300f;
    private static final float TARJETA_ALTO = 190f;
    private static final float TARJETA_Y = 170f;
    private static final float CREAR_X = 70f;
    private static final float UNIRSE_X = 430f;
    private static final float DESCRIPCION_Y = 145f;
    private static final float NOTA_Y = 395f;

    public SalaMenuScreen(Game juego) {
        super(juego);
        botonera.agregar(new Boton(CREAR_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO, "CREAR SALA", Boton.VERDE,
            () -> abrir(new CrearSalaScreen(juego)))).conFuente(Main.fuenteCartel);
        botonera.agregar(new Boton(UNIRSE_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO, "UNIRSE", Boton.NEUTRO,
            () -> abrir(new UnirseSalaScreen(juego)))).conFuente(Main.fuenteCartel);
        agregarVolver(() -> abrir(new JugarMenuScreen(juego)));
    }

    @Override
    protected void dibujar() {
        dibujarTitulo("EN RED");
        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "Las dos computadoras tienen que estar en la misma red", ANCHO / 2f, NOTA_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Sos el jugador 1 y elegís el modo", CREAR_X + TARJETA_ANCHO / 2f, DESCRIPCION_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Sos el jugador 2: buscá la sala o escribí la IP", UNIRSE_X + TARJETA_ANCHO / 2f, DESCRIPCION_Y);
        batch.end();
    }
}
