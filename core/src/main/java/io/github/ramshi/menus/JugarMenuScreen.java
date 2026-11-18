package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;

public class JugarMenuScreen extends MenuScreen {

    private static final float TARJETA_ANCHO = 300f;
    private static final float TARJETA_ALTO = 190f;
    private static final float TARJETA_Y = 170f;
    private static final float LOCAL_X = 70f;
    private static final float RED_X = 430f;
    private static final float DESCRIPCION_Y = 145f;

    public JugarMenuScreen(Game juego) {
        super(juego);
        botonera.agregar(new Boton(LOCAL_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO, "LOCAL", Boton.VERDE,
            () -> abrir(new ModoMenuScreen(juego, false)))).conFuente(Main.fuenteCartel);
        botonera.agregar(new Boton(RED_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO, "EN RED", Boton.NEUTRO,
            () -> abrir(new SalaMenuScreen(juego)))).conFuente(Main.fuenteCartel);
        agregarVolver(() -> abrir(new MainMenuScreen(juego)));
    }

    @Override
    protected void dibujar() {
        dibujarTitulo("JUGAR");
        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "Dos jugadores en el mismo teclado", LOCAL_X + TARJETA_ANCHO / 2f, DESCRIPCION_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Cada jugador en su computadora", RED_X + TARJETA_ANCHO / 2f, DESCRIPCION_Y);
        batch.end();
    }
}
