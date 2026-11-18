package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.ramshi.Main;
import io.github.ramshi.red.Red;

public class EsperaSalaScreen extends MenuScreen {

    private static final float CONECTADO_Y = 360f;
    private static final float JUGADOR_Y = 310f;
    private static final float ESPERANDO_Y = 220f;
    private static final float NOTA_Y = 180f;
    private static final float VELOCIDAD_PUNTOS = 2f;

    private float tiempo;

    public EsperaSalaScreen(Game juego) {
        super(juego);
        agregarVolver(() -> {
            Red.cerrar();
            abrir(new SalaMenuScreen(juego));
        });
    }

    @Override
    protected void dibujar() {
        tiempo += Gdx.graphics.getDeltaTime();
        dibujarTitulo("SALA DE " + Red.getNombreOtro().toUpperCase());
        batch.begin();
        escribir(Main.fuenteMediana, COLOR_SUBTITULO, "Conectado a " + Red.getIpOtro(), ANCHO / 2f, CONECTADO_Y);
        escribir(Main.fuenteGrande, COLOR_TEXTO, "Vas a jugar como JUGADOR 2", ANCHO / 2f, JUGADOR_Y);
        int puntos = (int) (tiempo * VELOCIDAD_PUNTOS) % 4;
        escribir(Main.fuenteMediana, COLOR_TITULO, "Esperando que el anfitrión elija el modo" + "...".substring(0, puntos), ANCHO / 2f, ESPERANDO_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Podés jugar con WASD o con las flechas", ANCHO / 2f, NOTA_Y);
        batch.end();
    }
}
