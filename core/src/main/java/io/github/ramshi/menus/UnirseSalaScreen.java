package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.ramshi.Main;
import io.github.ramshi.red.Red;

/** Entra a la sala del servidor como jugador 2. Si no se puede, vuelve al menú EN RED con el aviso. */
public class UnirseSalaScreen extends MenuScreen {

    private static final float ESTADO_Y = 280f;
    private static final float VELOCIDAD_PUNTOS = 2f;

    private float tiempo;

    public UnirseSalaScreen(Game juego) {
        super(juego);
        if (!Red.unirse()) Red.avisar("No estás conectado al servidor");
        agregarVolver(this::volver);
    }

    private void volver() {
        Red.salirDeSala();
        abrir(new SalaMenuScreen(juego));
    }

    private void fallar(String motivo) {
        Red.avisar(motivo);
        volver();
    }

    @Override
    protected void alEventoRed(String evento) {
        if (evento.equals(Red.CONECTADO)) abrir(new EsperaSalaScreen(juego));
        else if (evento.equals(Red.SIN_RESPUESTA)) fallar("El servidor no respondió. Probá de nuevo.");
        else if (evento.equals(Red.LLENA)) fallar("La sala ya tiene dos jugadores");
        else if (evento.equals(Red.NO_EXISTE)) fallar("La sala ya no existe");
        else super.alEventoRed(evento);
    }

    @Override
    protected void dibujar() {
        tiempo += Gdx.graphics.getDeltaTime();
        dibujarTitulo("UNIRSE A SALA");
        batch.begin();
        int puntos = (int) (tiempo * VELOCIDAD_PUNTOS) % 4;
        escribir(Main.fuenteGrande, COLOR_TEXTO, "Entrando a la sala de " + Red.getDuenioSala() + "...".substring(0, puntos), ANCHO / 2f, ESTADO_Y);
        batch.end();
    }
}
