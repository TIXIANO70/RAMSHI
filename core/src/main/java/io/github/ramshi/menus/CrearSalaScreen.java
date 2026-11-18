package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.ramshi.Main;
import io.github.ramshi.red.Red;

import java.util.List;

public class CrearSalaScreen extends MenuScreen {

    private static final float LEYENDA_Y = 380f;
    private static final float PRIMERA_IP_Y = 340f;
    private static final float ALTO_IP = 38f;
    private static final float PUERTO_Y = 200f;
    private static final float ESPERANDO_Y = 150f;
    private static final float VELOCIDAD_PUNTOS = 2f;
    private static final int MAX_IPS = 3;

    private final boolean abierta;
    private final List<String> ips;
    private float tiempo;

    public CrearSalaScreen(Game juego) {
        super(juego);
        abierta = Red.esAnfitrion() || Red.crearSala();
        ips = Red.ipsLocales();
        agregarVolver(() -> {
            Red.cerrar();
            abrir(new SalaMenuScreen(juego));
        });
    }

    @Override
    protected void alEventoRed(String evento) {
        if (evento.equals(Red.CONECTADO)) abrir(new ModoMenuScreen(juego, true));
        else super.alEventoRed(evento);
    }

    @Override
    protected void dibujar() {
        tiempo += Gdx.graphics.getDeltaTime();
        dibujarTitulo("CREAR SALA");
        batch.begin();
        if (!abierta) {
            escribir(Main.fuenteMediana, COLOR_TEXTO, "No se pudo abrir la sala en el puerto " + Red.PUERTO, ANCHO / 2f, PRIMERA_IP_Y);
            escribir(Main.fuenteChica, COLOR_NOTA, "¿Hay otra sala abierta en esta computadora?", ANCHO / 2f, PRIMERA_IP_Y - ALTO_IP);
            batch.end();
            return;
        }
        escribir(Main.fuenteChica, COLOR_NOTA, "El otro jugador se une con esta IP (o con BUSCAR):", ANCHO / 2f, LEYENDA_Y);
        if (ips.isEmpty()) escribir(Main.fuenteCartel, COLOR_TITULO, "127.0.0.1", ANCHO / 2f, PRIMERA_IP_Y);
        for (int i = 0; i < ips.size() && i < MAX_IPS; i++) {
            escribir(Main.fuenteCartel, COLOR_TITULO, ips.get(i), ANCHO / 2f, PRIMERA_IP_Y - i * ALTO_IP);
        }
        escribir(Main.fuenteChica, COLOR_NOTA, "Puerto " + Red.PUERTO, ANCHO / 2f, PUERTO_Y);
        int puntos = (int) (tiempo * VELOCIDAD_PUNTOS) % 4;
        escribir(Main.fuenteGrande, COLOR_TEXTO, "Esperando a otro jugador" + "...".substring(0, puntos), ANCHO / 2f, ESPERANDO_Y);
        batch.end();
    }
}
