package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;

public class AjustesScreen extends MenuScreen {

    private static final String ARCHIVO = "ramshi-ajustes";
    private static final String CLAVE_PANTALLA_COMPLETA = "pantallaCompleta";
    private static final int ANCHO_VENTANA = 800;
    private static final int ALTO_VENTANA = 480;
    private static final float BOTON_ANCHO = 380f;
    private static final float BOTON_ALTO = 54f;
    private static final float PANTALLA_Y = 270f;
    private static final float NOTA_Y = 250f;

    private final Boton pantallaCompleta;

    public AjustesScreen(Game juego) {
        super(juego);
        pantallaCompleta = botonera.agregar(new Boton((ANCHO - BOTON_ANCHO) / 2f, PANTALLA_Y, BOTON_ANCHO, BOTON_ALTO, "",
            Boton.NEUTRO, () -> setPantallaCompleta(!pantallaCompleta())));
        agregarVolver(() -> abrir(new MainMenuScreen(juego)));
    }

    private static Preferences preferencias() {
        return Gdx.app.getPreferences(ARCHIVO);
    }

    public static boolean pantallaCompleta() {
        return preferencias().getBoolean(CLAVE_PANTALLA_COMPLETA, false);
    }

    public static void setPantallaCompleta(boolean activa) {
        preferencias().putBoolean(CLAVE_PANTALLA_COMPLETA, activa).flush();
        aplicar();
    }

    public static void aplicar() {
        if (pantallaCompleta()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        else Gdx.graphics.setWindowedMode(ANCHO_VENTANA, ALTO_VENTANA);
    }

    @Override
    protected void dibujar() {
        pantallaCompleta.texto = "PANTALLA COMPLETA: " + (pantallaCompleta() ? "SÍ" : "NO");
        dibujarTitulo("AJUSTES");
        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "Se guarda para la próxima vez que abras el juego", ANCHO / 2f, NOTA_Y);
        batch.end();
    }
}
