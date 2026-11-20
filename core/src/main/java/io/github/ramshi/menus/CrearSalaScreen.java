package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.ramshi.Main;
import io.github.ramshi.red.Red;

/** Crea una sala en el servidor dedicado y espera a que otro jugador se una. Quien crea la sala es el jugador 1. */
public class CrearSalaScreen extends MenuScreen {

    private static final float SERVIDOR_Y = 360f;
    private static final float SALA_Y = 310f;
    private static final float INDICACION_Y = 260f;
    private static final float ESPERANDO_Y = 170f;
    private static final float VELOCIDAD_PUNTOS = 2f;

    private boolean fallo;
    private float tiempo;

    public CrearSalaScreen(Game juego) {
        super(juego);
        fallo = !(Red.esAnfitrion() || Red.crearSala());
        agregarVolver(() -> {
            Red.salirDeSala();
            abrir(new SalaMenuScreen(juego));
        });
    }

    @Override
    protected void alEventoRed(String evento) {
        if (evento.equals(Red.CONECTADO)) abrir(new ModoMenuScreen(juego, true));
        else if (evento.equals(Red.SIN_RESPUESTA)) fallo = true;
        else if (evento.equals(Red.OCUPADO)) {
            Red.avisar("Otro jugador creó la sala primero: entrá con UNIRSE");
            abrir(new SalaMenuScreen(juego));
        }
        else super.alEventoRed(evento);
    }

    @Override
    protected void dibujar() {
        tiempo += Gdx.graphics.getDeltaTime();
        dibujarTitulo("CREAR SALA");
        batch.begin();
        escribir(Main.fuenteMediana, COLOR_SUBTITULO, "Servidor " + Red.getNombreServidor() + " (" + Red.getDireccionServidor() + ")", ANCHO / 2f, SERVIDOR_Y);
        int puntos = (int) (tiempo * VELOCIDAD_PUNTOS) % 4;
        if (fallo) {
            escribir(Main.fuenteMediana, COLOR_TEXTO, "El servidor no pudo crear la sala", ANCHO / 2f, SALA_Y);
            escribir(Main.fuenteChica, COLOR_NOTA, "Volvé e intentá de nuevo", ANCHO / 2f, INDICACION_Y);
        } else if (!Red.salaCreada()) {
            escribir(Main.fuenteGrande, COLOR_TEXTO, "Creando la sala" + "...".substring(0, puntos), ANCHO / 2f, SALA_Y);
        } else {
            escribir(Main.fuenteCartel, COLOR_TITULO, "Sala creada", ANCHO / 2f, SALA_Y);
            escribir(Main.fuenteChica, COLOR_NOTA, "El otro jugador se conecta al mismo servidor y toca UNIRSE", ANCHO / 2f, INDICACION_Y);
            escribir(Main.fuenteGrande, COLOR_TEXTO, "Esperando a otro jugador" + "...".substring(0, puntos), ANCHO / 2f, ESPERANDO_Y);
        }
        batch.end();
    }
}
