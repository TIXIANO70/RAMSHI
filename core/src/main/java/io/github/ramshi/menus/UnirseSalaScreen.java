package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;
import io.github.ramshi.red.Red;

import java.util.List;

public class UnirseSalaScreen extends MenuScreen {

    private static final int LARGO_MAXIMO_IP = 15;
    private static final int SALAS_VISIBLES = 3;
    private static final float CENTRO_X = ANCHO / 2f;
    private static final float LEYENDA_IP_Y = 395f;
    private static final float CAJA_ANCHO = 320f;
    private static final float CAJA_ALTO = 46f;
    private static final float CAJA_Y = 335f;
    private static final float BOTON_ANCHO = 200f;
    private static final float BOTON_ALTO = 46f;
    private static final float BOTONES_Y = 270f;
    private static final float SEPARACION = 20f;
    private static final float LEYENDA_SALAS_Y = 245f;
    private static final float SALA_ANCHO = 420f;
    private static final float SALA_ALTO = 38f;
    private static final float PRIMERA_SALA_Y = 190f;
    private static final float ESTADO_Y = 85f;
    private static final float PARPADEO = 2f;

    private final Boton[] botonesSalas = new Boton[SALAS_VISIBLES];
    private String ip;
    private String estado = "";
    private float tiempo;
    private boolean conectando;

    public UnirseSalaScreen(Game juego) {
        super(juego);
        ip = AjustesScreen.getUltimaIp();
        if (!Red.prepararInvitado()) estado = "No se pudo usar la red en esta computadora";
        Red.buscarSalas();

        Boton conectar = botonera.agregar(new Boton(CENTRO_X - BOTON_ANCHO - SEPARACION / 2f, BOTONES_Y, BOTON_ANCHO, BOTON_ALTO,
            "CONECTAR", Boton.VERDE, () -> conectar(ip))).conFuente(Main.fuenteMediana);
        botonera.agregar(new Boton(CENTRO_X + SEPARACION / 2f, BOTONES_Y, BOTON_ANCHO, BOTON_ALTO,
            "BUSCAR SALAS", Boton.NEUTRO, Red::buscarSalas)).conFuente(Main.fuenteMediana);
        for (int i = 0; i < SALAS_VISIBLES; i++) {
            final int indice = i;
            botonesSalas[i] = botonera.agregar(new Boton(CENTRO_X - SALA_ANCHO / 2f, PRIMERA_SALA_Y - i * (SALA_ALTO + 8f),
                SALA_ANCHO, SALA_ALTO, "", Boton.NEUTRO, () -> conectarASala(indice))).conFuente(Main.fuenteChica);
            botonesSalas[i].activo = false;
        }
        agregarVolver(() -> {
            Red.cerrar();
            abrir(new SalaMenuScreen(juego));
        });
        botonera.enfocar(conectar);
    }

    private void conectarASala(int indice) {
        List<String[]> salas = Red.getSalasEncontradas();
        if (indice < salas.size()) {
            ip = salas.get(indice)[0];
            conectar(ip);
        }
    }

    private void conectar(String direccion) {
        if (direccion.isEmpty()) {
            estado = "Escribí la IP del anfitrión";
        } else if (Red.conectar(direccion)) {
            conectando = true;
            estado = "Conectando con " + direccion + "...";
        } else {
            estado = "Esa IP no es válida";
        }
    }

    @Override
    protected void alEventoRed(String evento) {
        if (evento.equals(Red.CONECTADO)) {
            AjustesScreen.setUltimaIp(ip);
            abrir(new EsperaSalaScreen(juego));
        } else if (evento.equals(Red.SIN_RESPUESTA)) {
            conectando = false;
            estado = "No hubo respuesta. Revisá la IP y que la sala esté abierta.";
        } else if (evento.equals(Red.LLENA)) {
            conectando = false;
            estado = "Esa sala ya tiene dos jugadores";
        } else {
            super.alEventoRed(evento);
        }
    }

    @Override
    protected void actualizar() {
        tiempo += Gdx.graphics.getDeltaTime();
        for (int tecla = Input.Keys.NUM_0; tecla <= Input.Keys.NUM_9; tecla++) {
            if (Gdx.input.isKeyJustPressed(tecla)) escribirCaracter((char) ('0' + tecla - Input.Keys.NUM_0));
        }
        for (int tecla = Input.Keys.NUMPAD_0; tecla <= Input.Keys.NUMPAD_9; tecla++) {
            if (Gdx.input.isKeyJustPressed(tecla)) escribirCaracter((char) ('0' + tecla - Input.Keys.NUMPAD_0));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) escribirCaracter('.');
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && !ip.isEmpty()) ip = ip.substring(0, ip.length() - 1);

        List<String[]> salas = Red.getSalasEncontradas();
        for (int i = 0; i < SALAS_VISIBLES; i++) {
            boolean hay = i < salas.size();
            botonesSalas[i].activo = hay && !conectando;
            botonesSalas[i].texto = hay ? "Sala de " + salas.get(i)[1] + "  (" + salas.get(i)[0] + ")" : "";
        }
    }

    private void escribirCaracter(char caracter) {
        if (ip.length() < LARGO_MAXIMO_IP) ip += caracter;
    }

    @Override
    protected void dibujar() {
        dibujarTitulo("UNIRSE A SALA");
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.12f, 0.12f, 0.16f, 1f);
        formas.rect(CENTRO_X - CAJA_ANCHO / 2f, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
        formas.end();

        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "IP del anfitrión (escribila con números y puntos):", CENTRO_X, LEYENDA_IP_Y);
        boolean cursor = ((int) (tiempo * PARPADEO)) % 2 == 0;
        escribir(Main.fuenteGrande, COLOR_TITULO, ip + (cursor ? "_" : " "), CENTRO_X, CAJA_Y + CAJA_ALTO / 2f + 10f);
        escribir(Main.fuenteChica, COLOR_NOTA, Red.getSalasEncontradas().isEmpty()
            ? "No se encontraron salas en la red (todavía)" : "Salas encontradas en la red:", CENTRO_X, LEYENDA_SALAS_Y);
        escribir(Main.fuenteMediana, COLOR_TEXTO, estado, CENTRO_X, ESTADO_Y);
        batch.end();
    }
}
