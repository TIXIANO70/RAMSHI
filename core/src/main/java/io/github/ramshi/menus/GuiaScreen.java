package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.ramshi.Main;
import io.github.ramshi.screens.Controles;
import io.github.ramshi.session.MinigameType;

import java.util.ArrayList;
import java.util.List;

public class GuiaScreen extends MenuScreen {

    private static final float TEXTO_X = 80f;
    private static final float TEXTO_ANCHO = 640f;
    private static final float SUBTITULO_Y = 405f;
    private static final float PRIMERA_SECCION_Y = 380f;
    private static final float SEPARACION_ENCABEZADO = 24f;
    private static final float SEPARACION_SECCIONES = 26f;
    private static final float CONTROLES_Y = 160f;
    private static final float ETIQUETA_JUGADOR_Y = 262f;
    private static final float PAGINADOR_Y = 42f;

    private static class Pagina {
        final String titulo;
        final String subtitulo;
        final String[] secciones;
        final boolean mostrarControles;

        Pagina(String titulo, String subtitulo, boolean mostrarControles, String... secciones) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.mostrarControles = mostrarControles;
            this.secciones = secciones;
        }
    }

    private final List<Pagina> paginas = new ArrayList<>();
    private final Paginador paginador;
    private final Controles.Control[] controles = { Controles.cruz("mover") };

    public GuiaScreen(Game juego) {
        super(juego);
        paginas.add(new Pagina("¿QUÉ ES RAMSHI?", "", true,
            "EL JUEGO", "Un duelo 1 contra 1 en el mismo teclado. Se juegan microjuegos de 10 segundos y el que gana cada uno suma puntos."));
        paginas.add(new Pagina("CÓMO SE JUEGA", "", false,
            "POR RONDAS", "Elegís de " + ModoMenuScreen.MIN_RONDAS + " a " + ModoMenuScreen.MAX_RONDAS
                + " rondas y salen minijuegos al azar. Al final gana quien juntó más puntos.",
            "ENDLESS", "Elegís un minijuego y lo jugás todas las veces que quieran.",
            "PUNTOS", "Paliza = 3, victoria clara = 2, victoria ajustada = 1, empate = 0.",
            "PAUSA", "Apretá ESC en medio de un minijuego para pausar, reanudar o salir."));

        MinigameType[] tipos = MinigameType.values();
        for (int i = 0; i < tipos.length; i++) {
            MinigameType t = tipos[i];
            paginas.add(new Pagina(t.getNombre().toUpperCase(), "Minijuego " + (i + 1) + " de " + tipos.length, false,
                "CÓMO SE JUEGA", t.getComoSeJuega(),
                "META", t.getMeta(),
                "PUNTOS", t.getPuntos()));
        }

        paginador = new Paginador(paginas.size(), 0, ANCHO / 2f, PAGINADOR_Y);
        agregarVolver(() -> abrir(new MainMenuScreen(juego)));
        botonera.navegarHorizontal = false;
    }

    @Override
    protected void actualizar() {
        paginador.actualizar(mouseX, mouseY, Gdx.input.justTouched());
    }

    @Override
    protected void dibujar() {
        Pagina pagina = paginas.get(paginador.actual());
        dibujarTitulo(pagina.titulo);

        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, pagina.subtitulo, ANCHO / 2f, SUBTITULO_Y);
        float y = PRIMERA_SECCION_Y;
        for (int i = 0; i < pagina.secciones.length; i += 2) {
            Main.fuenteMediana.setColor(COLOR_SUBTITULO);
            Main.fuenteMediana.draw(batch, pagina.secciones[i], TEXTO_X, y);
            y -= SEPARACION_ENCABEZADO;
            y -= escribirParrafo(Main.fuenteMediana, COLOR_TEXTO, pagina.secciones[i + 1], TEXTO_X, y, TEXTO_ANCHO);
            y -= SEPARACION_SECCIONES;
        }
        if (pagina.mostrarControles) {
            escribir(Main.fuenteMediana, MenuScreen.COLOR_TITULO, "JUGADOR 1", ANCHO * 0.25f, ETIQUETA_JUGADOR_Y);
            escribir(Main.fuenteMediana, MenuScreen.COLOR_TITULO, "JUGADOR 2", ANCHO * 0.75f, ETIQUETA_JUGADOR_Y);
            Controles.dibujarGrande(batch, controles, 0, ANCHO * 0.25f, CONTROLES_Y);
            Controles.dibujarGrande(batch, controles, 1, ANCHO * 0.75f, CONTROLES_Y);
        }
        batch.end();

        paginador.dibujar(formas, batch);
    }
}
