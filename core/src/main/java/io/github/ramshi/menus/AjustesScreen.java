package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import io.github.ramshi.Ajustes;
import io.github.ramshi.Main;

public class AjustesScreen extends MenuScreen {

    private static final float BOTON_ANCHO = 380f;
    private static final float BOTON_ALTO = 54f;
    private static final float PANTALLA_Y = 270f;
    private static final float NOTA_Y = 250f;

    private final Boton pantallaCompleta;

    public AjustesScreen(Game juego) {
        super(juego);
        pantallaCompleta = botonera.agregar(new Boton((ANCHO - BOTON_ANCHO) / 2f, PANTALLA_Y, BOTON_ANCHO, BOTON_ALTO, "",
            Boton.NEUTRO, () -> Ajustes.setPantallaCompleta(!Ajustes.pantallaCompleta())));
        agregarVolver(() -> abrir(new MainMenuScreen(juego)));
    }

    @Override
    protected void dibujar() {
        pantallaCompleta.texto = "PANTALLA COMPLETA: " + (Ajustes.pantallaCompleta() ? "SÍ" : "NO");
        dibujarTitulo("AJUSTES");
        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "Se guarda para la próxima vez que abras el juego", ANCHO / 2f, NOTA_Y);
        batch.end();
    }
}
