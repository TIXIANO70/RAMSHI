package io.github.ramshi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public final class Ajustes {

    private static final String ARCHIVO = "ramshi-ajustes";
    private static final String CLAVE_PANTALLA_COMPLETA = "pantallaCompleta";
    private static final int ANCHO_VENTANA = 800;
    private static final int ALTO_VENTANA = 480;

    private Ajustes() {}

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
}
