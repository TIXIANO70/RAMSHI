package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import io.github.ramshi.Main;

public class MainMenuScreen extends MenuScreen {

    private static final String RUTA_LOGO = "ui/logo.png";
    private static final float LOGO_ALTO = 130f;
    private static final float LOGO_Y = 320f;
    private static final float TITULO_Y = 425f;
    private static final float SUBTITULO_Y = 345f;
    private static final float BOTON_ANCHO = 260f;
    private static final float BOTON_ALTO = 50f;
    private static final float PRIMER_BOTON_Y = 245f;
    private static final float SEPARACION_BOTONES = 62f;

    private Texture logo;

    public MainMenuScreen(Game juego) {
        super(juego);
        if (Gdx.files.internal(RUTA_LOGO).exists()) logo = new Texture(RUTA_LOGO);

        float x = (ANCHO - BOTON_ANCHO) / 2f;
        botonera.agregar(new Boton(x, PRIMER_BOTON_Y, BOTON_ANCHO, BOTON_ALTO, "JUGAR", Boton.VERDE,
            () -> abrir(new JugarMenuScreen(juego))));
        botonera.agregar(new Boton(x, PRIMER_BOTON_Y - SEPARACION_BOTONES, BOTON_ANCHO, BOTON_ALTO, "GUÍA", Boton.NEUTRO,
            () -> abrir(new GuiaScreen(juego))));
        botonera.agregar(new Boton(x, PRIMER_BOTON_Y - SEPARACION_BOTONES * 2, BOTON_ANCHO, BOTON_ALTO, "AJUSTES", Boton.NEUTRO,
            () -> abrir(new AjustesScreen(juego))));
        botonera.agregar(new Boton(x, PRIMER_BOTON_Y - SEPARACION_BOTONES * 3, BOTON_ANCHO, BOTON_ALTO, "SALIR", Boton.ROJO,
            () -> Gdx.app.exit()));
    }

    @Override
    protected void dibujar() {
        batch.begin();
        if (logo != null) {
            float ancho = LOGO_ALTO * logo.getWidth() / logo.getHeight();
            batch.draw(logo, (ANCHO - ancho) / 2f, LOGO_Y, ancho, LOGO_ALTO);
        } else {
            escribir(Main.fuenteTitulo, COLOR_TITULO, "RAMSHI", ANCHO / 2f, TITULO_Y);
            escribir(Main.fuenteMediana, COLOR_SUBTITULO, "MICROJUEGOS 1 VS 1", ANCHO / 2f, SUBTITULO_Y);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (logo != null) logo.dispose();
    }
}
