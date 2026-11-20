package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;
import io.github.ramshi.red.Red;

/**
 * Menú EN RED, ya conectado al servidor. El servidor tiene una sola sala:
 * CREAR SALA solo se puede si no hay ninguna, y UNIRSE solo si hay una esperando jugador.
 */
public class SalaMenuScreen extends MenuScreen {

    private static final float TARJETA_ANCHO = 300f;
    private static final float TARJETA_ALTO = 190f;
    private static final float TARJETA_Y = 170f;
    private static final float CREAR_X = 70f;
    private static final float UNIRSE_X = 430f;
    private static final float DESCRIPCION_Y = 145f;
    private static final float NOTA_Y = 395f;
    private static final float ESTADO_Y = 110f;
    private static final float INTERVALO_ESTADO = 1f;

    private final Boton crear;
    private final Boton unirse;
    private float tiempoEstado;
    private String estadoAnterior;

    public SalaMenuScreen(Game juego) {
        super(juego);
        crear = botonera.agregar(new Boton(CREAR_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO, "CREAR SALA", Boton.VERDE,
            () -> abrir(new CrearSalaScreen(juego)))).conFuente(Main.fuenteCartel);
        unirse = botonera.agregar(new Boton(UNIRSE_X, TARJETA_Y, TARJETA_ANCHO, TARJETA_ALTO, "UNIRSE", Boton.NEUTRO,
            () -> abrir(new UnirseSalaScreen(juego)))).conFuente(Main.fuenteCartel);
        agregarVolver(() -> {
            Red.cerrar();
            abrir(new ServidorScreen(juego));
        });
        Red.pedirEstadoSala();
        actualizarBotones();
    }

    private void actualizarBotones() {
        String estado = Red.getEstadoSala();
        if (estado.equals(estadoAnterior)) return;
        estadoAnterior = estado;
        crear.activo = estado.equals(Red.LIBRE);
        unirse.activo = estado.equals(Red.ESPERANDO);
        // Para jugar con teclado: cuando se habilita un botón, queda seleccionado (si no, ENTER tocaría VOLVER).
        if (crear.activo) botonera.enfocar(crear);
        else if (unirse.activo) botonera.enfocar(unirse);
    }

    @Override
    protected void actualizar() {
        tiempoEstado += Gdx.graphics.getDeltaTime();
        if (tiempoEstado >= INTERVALO_ESTADO) {
            tiempoEstado = 0f;
            Red.pedirEstadoSala();
        }
        actualizarBotones();
    }

    private String textoEstado() {
        String estado = Red.getEstadoSala();
        if (estado.equals(Red.LIBRE)) return "No hay sala: creala y sos el jugador 1";
        if (estado.equals(Red.ESPERANDO)) return "La sala de " + Red.getDuenioSala() + " espera jugador: unite como jugador 2";
        if (estado.equals(Red.JUGANDO)) return "Ya hay una partida en curso en este servidor";
        return "Consultando la sala del servidor...";
    }

    @Override
    protected void dibujar() {
        dibujarTitulo("EN RED");
        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "Conectado al servidor " + Red.getNombreServidor() + " (" + Red.getDireccionServidor() + ")", ANCHO / 2f, NOTA_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Sos el jugador 1 y elegís el modo", CREAR_X + TARJETA_ANCHO / 2f, DESCRIPCION_Y);
        escribir(Main.fuenteChica, COLOR_NOTA, "Sos el jugador 2", UNIRSE_X + TARJETA_ANCHO / 2f, DESCRIPCION_Y);
        escribir(Main.fuenteMediana, COLOR_TEXTO, textoEstado(), ANCHO / 2f, ESTADO_Y);
        batch.end();
    }
}
