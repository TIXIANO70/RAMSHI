package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import io.github.ramshi.Main;
import io.github.ramshi.Sonidos;
import io.github.ramshi.menus.Botonera.Boton;

public class AjustesScreen extends MenuScreen {

    private static final String ARCHIVO = "ramshi-ajustes";
    private static final String CLAVE_PANTALLA_COMPLETA = "pantallaCompleta";
    private static final String CLAVE_ULTIMA_IP = "ultimaIp";
    private static final String CLAVE_MUSICA = "musica";
    private static final String CLAVE_SONIDOS = "sonidos";
    private static final int ANCHO_VENTANA = 800;
    private static final int ALTO_VENTANA = 480;
    private static final float BOTON_ANCHO = 380f;
    private static final float BOTON_ALTO = 54f;
    private static final float MUSICA_Y = 320f;
    private static final float SONIDOS_Y = 250f;
    private static final float PANTALLA_Y = 180f;
    private static final float NOTA_Y = 145f;

    private final Boton musica;
    private final Boton sonidos;
    private final Boton pantallaCompleta;

    public AjustesScreen(Game juego) {
        super(juego);
        float x = (ANCHO - BOTON_ANCHO) / 2f;
        musica = botonera.agregar(new Boton(x, MUSICA_Y, BOTON_ANCHO, BOTON_ALTO, "",
            Boton.NEUTRO, () -> setMusicaActiva(!musicaActiva())));
        sonidos = botonera.agregar(new Boton(x, SONIDOS_Y, BOTON_ANCHO, BOTON_ALTO, "",
            Boton.NEUTRO, () -> setSonidosActivos(!sonidosActivos())));
        pantallaCompleta = botonera.agregar(new Boton(x, PANTALLA_Y, BOTON_ANCHO, BOTON_ALTO, "",
            Boton.NEUTRO, () -> setPantallaCompleta(!pantallaCompleta())));
        agregarVolver(() -> abrir(new MainMenuScreen(juego)));
    }

    private static Preferences preferencias() {
        return Gdx.app.getPreferences(ARCHIVO);
    }

    public static boolean musicaActiva() {
        return Gdx.app == null || preferencias().getBoolean(CLAVE_MUSICA, true);
    }

    public static void setMusicaActiva(boolean activa) {
        preferencias().putBoolean(CLAVE_MUSICA, activa).flush();
        Sonidos.actualizarAjustes();
    }

    public static boolean sonidosActivos() {
        return Gdx.app == null || preferencias().getBoolean(CLAVE_SONIDOS, true);
    }

    public static void setSonidosActivos(boolean activos) {
        preferencias().putBoolean(CLAVE_SONIDOS, activos).flush();
    }

    public static boolean pantallaCompleta() {
        return preferencias().getBoolean(CLAVE_PANTALLA_COMPLETA, false);
    }

    public static void setPantallaCompleta(boolean activa) {
        preferencias().putBoolean(CLAVE_PANTALLA_COMPLETA, activa).flush();
        aplicar();
    }

    public static String getUltimaIp() {
        return preferencias().getString(CLAVE_ULTIMA_IP, "");
    }

    public static void setUltimaIp(String ip) {
        preferencias().putString(CLAVE_ULTIMA_IP, ip).flush();
    }

    public static void aplicar() {
        if (pantallaCompleta()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        else Gdx.graphics.setWindowedMode(ANCHO_VENTANA, ALTO_VENTANA);
    }

    @Override
    protected void dibujar() {
        musica.texto = "MÚSICA: " + (musicaActiva() ? "SÍ" : "NO");
        sonidos.texto = "SONIDOS: " + (sonidosActivos() ? "SÍ" : "NO");
        pantallaCompleta.texto = "PANTALLA COMPLETA: " + (pantallaCompleta() ? "SÍ" : "NO");
        dibujarTitulo("AJUSTES");
        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "Se guarda para la próxima vez que abras el juego", ANCHO / 2f, NOTA_Y);
        batch.end();
    }
}
