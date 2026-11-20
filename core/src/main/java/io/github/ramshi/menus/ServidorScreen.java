package io.github.ramshi.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.ramshi.Main;
import io.github.ramshi.menus.Botonera.Boton;
import io.github.ramshi.red.Red;

import java.util.List;

/** Primer paso del modo en red: conectarse al servidor dedicado (escribiendo su IP o buscándolo en la red). */
public class ServidorScreen extends MenuScreen {

    private static final int LARGO_MAXIMO_IP = 21;
    private static final int SERVIDORES_VISIBLES = 3;
    private static final float CENTRO_X = ANCHO / 2f;
    private static final float LEYENDA_IP_Y = 395f;
    private static final float CAJA_ANCHO = 320f;
    private static final float CAJA_ALTO = 46f;
    private static final float CAJA_Y = 335f;
    private static final float BOTON_ANCHO = 200f;
    private static final float BOTON_ALTO = 46f;
    private static final float BOTONES_Y = 270f;
    private static final float SEPARACION = 20f;
    private static final float LEYENDA_SERVIDORES_Y = 245f;
    private static final float SERVIDOR_ANCHO = 420f;
    private static final float SERVIDOR_ALTO = 38f;
    private static final float PRIMER_SERVIDOR_Y = 190f;
    private static final float ESTADO_Y = 85f;
    private static final float PARPADEO = 2f;

    private final Boton[] botonesServidores = new Boton[SERVIDORES_VISIBLES];
    private String ip;
    private String estado = "";
    private float tiempo;
    private boolean conectando;

    public ServidorScreen(Game juego) {
        super(juego);
        ip = AjustesScreen.getUltimaIp();
        if (!Red.prepararCliente()) estado = "No se pudo usar la red en esta computadora";
        Red.buscarServidores();

        Boton conectar = botonera.agregar(new Boton(CENTRO_X - BOTON_ANCHO - SEPARACION / 2f, BOTONES_Y, BOTON_ANCHO, BOTON_ALTO,
            "CONECTAR", Boton.VERDE, () -> conectar(ip))).conFuente(Main.fuenteMediana);
        botonera.agregar(new Boton(CENTRO_X + SEPARACION / 2f, BOTONES_Y, BOTON_ANCHO, BOTON_ALTO,
            "BUSCAR SERVIDOR", Boton.NEUTRO, Red::buscarServidores)).conFuente(Main.fuenteMediana);
        for (int i = 0; i < SERVIDORES_VISIBLES; i++) {
            final int indice = i;
            botonesServidores[i] = botonera.agregar(new Boton(CENTRO_X - SERVIDOR_ANCHO / 2f, PRIMER_SERVIDOR_Y - i * (SERVIDOR_ALTO + 8f),
                SERVIDOR_ANCHO, SERVIDOR_ALTO, "", Boton.NEUTRO, () -> conectarAEncontrado(indice))).conFuente(Main.fuenteChica);
            botonesServidores[i].activo = false;
        }
        agregarVolver(() -> {
            Red.cerrar();
            abrir(new JugarMenuScreen(juego));
        });
        botonera.enfocar(conectar);
    }

    private void conectarAEncontrado(int indice) {
        List<String[]> servidores = Red.getServidoresEncontrados();
        if (indice < servidores.size()) {
            ip = servidores.get(indice)[0];
            conectar(ip);
        }
    }

    private void conectar(String direccion) {
        if (direccion.isEmpty()) {
            estado = "Escribí la IP del servidor";
        } else if (Red.conectarServidor(direccion)) {
            conectando = true;
            estado = "Conectando con el servidor " + direccion + "...";
        } else {
            estado = "Esa IP no es válida";
        }
    }

    @Override
    protected void alEventoRed(String evento) {
        if (evento.equals(Red.CONECTADO_SERVIDOR)) {
            AjustesScreen.setUltimaIp(ip);
            abrir(new SalaMenuScreen(juego));
        } else if (evento.equals(Red.SIN_RESPUESTA)) {
            conectando = false;
            estado = "El servidor no respondió. Revisá la IP y que el servidor esté abierto.";
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
        boolean mayuscula = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.COLON)
            || (mayuscula && (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD) || Gdx.input.isKeyJustPressed(Input.Keys.SEMICOLON)))) {
            escribirCaracter(':');
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) {
            escribirCaracter('.');
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && !ip.isEmpty()) ip = ip.substring(0, ip.length() - 1);

        List<String[]> servidores = Red.getServidoresEncontrados();
        for (int i = 0; i < SERVIDORES_VISIBLES; i++) {
            boolean hay = i < servidores.size();
            botonesServidores[i].activo = hay && !conectando;
            botonesServidores[i].texto = hay ? "Servidor " + servidores.get(i)[1] + "  (" + servidores.get(i)[0] + ")" : "";
        }
    }

    private void escribirCaracter(char caracter) {
        if (ip.length() < LARGO_MAXIMO_IP) ip += caracter;
    }

    @Override
    protected void dibujar() {
        dibujarTitulo("CONECTAR AL SERVIDOR");
        formas.begin(ShapeRenderer.ShapeType.Filled);
        formas.setColor(0.12f, 0.12f, 0.16f, 1f);
        formas.rect(CENTRO_X - CAJA_ANCHO / 2f, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
        formas.end();

        batch.begin();
        escribir(Main.fuenteChica, COLOR_NOTA, "IP del servidor (si no es el puerto 9999, agregá :puerto, ej. 192.168.0.10:10000):", CENTRO_X, LEYENDA_IP_Y);
        boolean cursor = ((int) (tiempo * PARPADEO)) % 2 == 0;
        escribir(Main.fuenteGrande, COLOR_TITULO, ip + (cursor ? "_" : " "), CENTRO_X, CAJA_Y + CAJA_ALTO / 2f + 10f);
        escribir(Main.fuenteChica, COLOR_NOTA, Red.getServidoresEncontrados().isEmpty()
            ? "No se encontraron servidores en la red (todavía)" : "Servidores encontrados en la red:", CENTRO_X, LEYENDA_SERVIDORES_Y);
        escribir(Main.fuenteMediana, COLOR_TEXTO, estado, CENTRO_X, ESTADO_Y);
        batch.end();
    }
}
