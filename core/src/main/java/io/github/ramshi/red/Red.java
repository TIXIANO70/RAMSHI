package io.github.ramshi.red;

import com.badlogic.gdx.utils.IntArray;
import io.github.ramshi.session.GameSession;
import io.github.ramshi.session.MinigameType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Cliente de red. El juego nunca hace de servidor: se conecta al servidor dedicado (módulo "server"),
 * crea una sala o se une a una, y todo lo que manda al otro jugador pasa por el servidor.
 * El que crea la sala es el jugador 1 (elige el modo) y el que se une es el jugador 2.
 */
public final class Red {

    /** Primer puerto de servidor. Si está ocupado, el servidor usa los siguientes; el juego los busca a todos. */
    public static final int PUERTO = 9999;
    public static final int CANTIDAD_PUERTOS = 10;
    private static final int PUERTO_MAXIMO = 65535;
    public static final String CONECTADO_SERVIDOR = "CONECTADO_SERVIDOR";
    public static final String CONECTADO = "CONECTADO";
    public static final String DESCONECTADO = "DESCONECTADO";
    public static final String SIN_SERVIDOR = "SIN_SERVIDOR";
    public static final String SIN_RESPUESTA = "SIN_RESPUESTA";
    public static final String LLENA = "LLENA";
    public static final String NO_EXISTE = "NOEXISTE";
    public static final String OCUPADO = "OCUPADO";
    public static final String LIBRE = "LIBRE";
    public static final String ESPERANDO = "ESPERANDO";
    public static final String JUGANDO = "JUGANDO";
    public static final String VOLVER_A_SALA = "SALA";
    public static final String INICIAR = "INICIAR";

    private static final String SEPARADOR = ";";
    private static final String SEPARADOR_ENTRADAS = ",";
    private static final String BUSCAR = "BUSCAR";
    private static final String ANUNCIO = "ANUNCIO";
    private static final String CONECTAR = "CONECTAR";
    private static final String BIENVENIDO = "BIENVENIDO";
    private static final String DESCONECTAR = "DESCONECTAR";
    private static final String LATIDO = "HOLA";
    private static final String CREAR = "CREAR";
    private static final String SALA_CREADA = "CREADA";
    private static final String ESTADO = "ESTADO";
    private static final String UNIRSE = "UNIRSE";
    private static final String UNIDO = "UNIDO";
    private static final String SALIR = "SALIR";
    private static final String CONTROL = "C";
    private static final String RECIBIDO = "OK";
    private static final String ENTRADAS = "E";
    private static final String RONDAS = "RONDAS";
    private static final String ENDLESS = "ENDLESS";
    private static final String DIRECCION_DIFUSION = "255.255.255.255";
    private static final String DIRECCION_LOCAL = "127.0.0.1";
    private static final float INTERVALO_LATIDO = 0.5f;
    private static final float INTERVALO_REENVIO = 0.25f;
    private static final float INTERVALO_PEDIDO = 0.5f;
    private static final float TIEMPO_SIN_NOTICIAS = 5f;
    private static final float TIEMPO_INTENTO = 5f;
    private static final float GRACIA_AL_UNIRSE = 1f;
    private static final float DURACION_AVISO = 4f;
    private static final int COPIAS_SALIR = 3;

    /** ANFITRION = creó la sala (jugador 1); INVITADO = se unió (jugador 2). Ninguno de los dos es servidor. */
    public enum Rol { NINGUNO, ANFITRION, INVITADO }

    private enum Pedido { NINGUNO, CONECTAR, CREAR, UNIRSE }

    private static DatagramSocket socket;
    private static HiloRed hilo;
    private static volatile Rol rol = Rol.NINGUNO;
    private static volatile boolean enServidor;
    private static volatile boolean conectado;
    private static volatile boolean salaCreada;
    private static volatile long ultimoContacto;
    private static InetAddress ipServidor;
    private static int puertoServidor = PUERTO;
    private static String nombreServidor = "";
    private static String nombreOtro = "";
    private static String nombrePropio;
    private static Pedido pedido = Pedido.NINGUNO;
    private static String textoPedido;
    private static float tiempoPedido;
    private static float tiempoIntento;
    private static float tiempoDesdeUnion;
    private static volatile int sesion;
    private static int ultimoIdEnviado;
    private static int ultimoIdRecibido;
    private static float tiempoLatido;
    private static float tiempoReenvio;
    private static String aviso;
    private static float tiempoAviso;

    private static final ConcurrentLinkedQueue<String> eventos = new ConcurrentLinkedQueue<>();
    private static final Map<Integer, String> pendientes = new ConcurrentHashMap<>();
    private static final Map<Long, Integer> entradasRemotas = new ConcurrentHashMap<>();
    private static final Map<String, String> servidoresEncontrados = Collections.synchronizedMap(new LinkedHashMap<String, String>());
    private static volatile String estadoSala = "";
    private static volatile String duenioSala = "";

    private Red() {}

    public static boolean esAnfitrion() { return rol == Rol.ANFITRION; }
    public static boolean esInvitado() { return rol == Rol.INVITADO; }
    public static boolean estaEnServidor() { return enServidor; }
    public static boolean estaConectado() { return conectado; }
    public static boolean salaCreada() { return salaCreada; }
    public static String getNombreOtro() { return nombreOtro; }
    public static String getNombreServidor() { return nombreServidor; }
    /** "IP" si el servidor usa el puerto 9999, o "IP:puerto" si usa otro. */
    public static String getDireccionServidor() { return ipServidor != null ? direccion(ipServidor, puertoServidor) : ""; }

    private static String direccion(InetAddress ip, int puerto) {
        return ip.getHostAddress() + (puerto == PUERTO ? "" : ":" + puerto);
    }
    public static String siguienteEvento() { return eventos.poll(); }
    public static String getAviso() { return aviso; }

    public static void avisar(String texto) {
        aviso = texto;
        tiempoAviso = DURACION_AVISO;
    }

    /** Abre el socket del cliente (puerto libre cualquiera). No escucha conexiones: solo habla con el servidor. */
    public static synchronized boolean prepararCliente() {
        if (socket != null) return true;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (SocketException e) {
            socket = null;
            return false;
        }
        reiniciarPartida();
        hilo = new HiloRed(socket);
        hilo.start();
        return true;
    }

    public static synchronized void buscarServidores() {
        if (!prepararCliente()) return;
        servidoresEncontrados.clear();
        try {
            InetAddress difusion = InetAddress.getByName(DIRECCION_DIFUSION);
            InetAddress local = InetAddress.getByName(DIRECCION_LOCAL);
            for (int puerto = PUERTO; puerto < PUERTO + CANTIDAD_PUERTOS; puerto++) {
                enviar(BUSCAR, difusion, puerto);
                enviar(BUSCAR, local, puerto);
            }
        } catch (IOException e) {
            avisar("No se pudo buscar en la red");
        }
    }

    /** Cada servidor es {dirección ("IP" o "IP:puerto"), nombre}. */
    public static List<String[]> getServidoresEncontrados() {
        List<String[]> lista = new ArrayList<>();
        synchronized (servidoresEncontrados) {
            for (Map.Entry<String, String> s : servidoresEncontrados.entrySet()) {
                lista.add(new String[] {s.getValue(), s.getKey().substring(0, s.getKey().lastIndexOf(':'))});
            }
        }
        return lista;
    }

    /** Recibe "IP" (usa el puerto 9999) o "IP:puerto". */
    public static synchronized boolean conectarServidor(String direccion) {
        if (!prepararCliente()) return false;
        String ip = direccion;
        int puerto = PUERTO;
        int dosPuntos = direccion.lastIndexOf(':');
        if (dosPuntos >= 0) {
            ip = direccion.substring(0, dosPuntos);
            try {
                puerto = Integer.parseInt(direccion.substring(dosPuntos + 1));
            } catch (NumberFormatException e) {
                return false;
            }
            if (puerto <= 0 || puerto > PUERTO_MAXIMO) return false;
        }
        if (ip.isEmpty()) return false;
        try {
            ipServidor = InetAddress.getByName(ip);
        } catch (IOException e) {
            return false;
        }
        puertoServidor = puerto;
        enServidor = false;
        empezarPedido(Pedido.CONECTAR, CONECTAR + SEPARADOR + nombreEquipo());
        return true;
    }

    public static synchronized boolean crearSala() {
        if (!enServidor) return false;
        salirDeSala();
        rol = Rol.ANFITRION;
        salaCreada = false;
        empezarPedido(Pedido.CREAR, CREAR);
        return true;
    }

    /** El servidor tiene una sola sala. Pide su estado: LIBRE, ESPERANDO (hay jugador 1) o JUGANDO (está llena). */
    public static synchronized void pedirEstadoSala() {
        if (enServidor) enviarAlServidor(ESTADO);
    }

    /** "" mientras no llegó la respuesta del servidor. */
    public static String getEstadoSala() { return estadoSala; }
    public static String getDuenioSala() { return duenioSala; }

    public static synchronized boolean unirse() {
        if (!enServidor) return false;
        salirDeSala();
        rol = Rol.INVITADO;
        empezarPedido(Pedido.UNIRSE, UNIRSE);
        return true;
    }

    /** Deja la sala pero sigue conectado al servidor. */
    public static synchronized void salirDeSala() {
        if (rol != Rol.NINGUNO && enServidor) {
            for (int i = 0; i < COPIAS_SALIR; i++) enviarAlServidor(SALIR);
        }
        rol = Rol.NINGUNO;
        salaCreada = false;
        nombreOtro = "";
        estadoSala = "";
        pedido = Pedido.NINGUNO;
        reiniciarPartida();
    }

    /** Se desconecta del servidor y libera el socket. */
    public static synchronized void cerrar() {
        salirDeSala();
        if (enServidor) {
            for (int i = 0; i < COPIAS_SALIR; i++) enviarAlServidor(DESCONECTAR);
        }
        if (hilo != null) hilo.terminar();
        hilo = null;
        socket = null;
        enServidor = false;
        ipServidor = null;
        nombreServidor = "";
        estadoSala = "";
        duenioSala = "";
        servidoresEncontrados.clear();
        eventos.clear();
    }

    private static void empezarPedido(Pedido nuevo, String texto) {
        pedido = nuevo;
        textoPedido = texto;
        tiempoIntento = 0f;
        tiempoPedido = INTERVALO_PEDIDO;
    }

    private static void reiniciarPartida() {
        conectado = false;
        ultimoIdEnviado = 0;
        ultimoIdRecibido = 0;
        pendientes.clear();
        entradasRemotas.clear();
    }

    public static synchronized void actualizar(float delta) {
        if (tiempoAviso > 0f) {
            tiempoAviso -= delta;
            if (tiempoAviso <= 0f) aviso = null;
        }
        if (socket == null || ipServidor == null) return;

        if (pedido != Pedido.NINGUNO) {
            tiempoIntento += delta;
            tiempoPedido += delta;
            if (tiempoPedido >= INTERVALO_PEDIDO) {
                tiempoPedido = 0f;
                enviarAlServidor(textoPedido);
            }
            if (tiempoIntento >= TIEMPO_INTENTO) {
                if (pedido != Pedido.CONECTAR) rol = Rol.NINGUNO;
                pedido = Pedido.NINGUNO;
                eventos.add(SIN_RESPUESTA);
            }
        }
        if (!enServidor) return;

        tiempoLatido += delta;
        if (tiempoLatido >= INTERVALO_LATIDO) {
            tiempoLatido = 0f;
            enviarAlServidor(LATIDO);
        }
        if ((System.nanoTime() - ultimoContacto) / 1e9f > TIEMPO_SIN_NOTICIAS) {
            perderServidor();
            return;
        }
        if (!conectado) return;

        tiempoDesdeUnion += delta;
        tiempoReenvio += delta;
        if (tiempoReenvio >= INTERVALO_REENVIO) {
            tiempoReenvio = 0f;
            for (Map.Entry<Integer, String> p : pendientes.entrySet()) {
                enviarAlServidor(CONTROL + SEPARADOR + p.getKey() + SEPARADOR + p.getValue());
            }
        }
    }

    private static void perderServidor() {
        boolean estabaJugando = rol != Rol.NINGUNO;
        enServidor = false;
        rol = Rol.NINGUNO;
        salaCreada = false;
        pedido = Pedido.NINGUNO;
        reiniciarPartida();
        avisar(estabaJugando ? "Se perdió la conexión con el servidor" : "El servidor dejó de responder");
        eventos.add(SIN_SERVIDOR);
    }

    private static void perderRival(String motivo) {
        conectado = false;
        nombreOtro = "";
        pendientes.clear();
        entradasRemotas.clear();
        avisar(motivo);
        eventos.add(DESCONECTADO);
        if (rol == Rol.INVITADO) rol = Rol.NINGUNO;
    }

    private static void unirRival(String nombre) {
        reiniciarPartida();
        conectado = true;
        tiempoDesdeUnion = 0f;
        nombreOtro = nombre;
        eventos.add(CONECTADO);
    }

    public static synchronized void enviarSeguro(String texto) {
        if (!conectado) return;
        int id = ++ultimoIdEnviado;
        pendientes.put(id, texto);
        enviarAlServidor(CONTROL + SEPARADOR + id + SEPARADOR + texto);
    }

    static synchronized void procesarMensaje(String mensaje, InetAddress ip, int puerto) {
        if (socket == null) return;
        String[] partes = mensaje.split(SEPARADOR, -1);
        String tipo = partes[0];

        if (tipo.equals(ANUNCIO)) {
            // El mismo servidor puede contestar por la red y por 127.0.0.1: se guarda una vez, con la IP de la red.
            String clave = (partes.length > 1 ? partes[1] : ip.getHostAddress()) + ":" + puerto;
            String anterior = servidoresEncontrados.get(clave);
            if (anterior == null || (anterior.startsWith(DIRECCION_LOCAL) && !ip.isLoopbackAddress())) {
                servidoresEncontrados.put(clave, direccion(ip, puerto));
            }
            return;
        }
        boolean delServidor = ipServidor != null && puerto == puertoServidor
            && (ip.equals(ipServidor) || (ipServidor.isLoopbackAddress() && ip.isLoopbackAddress()));
        if (!delServidor) return;
        ultimoContacto = System.nanoTime();

        switch (tipo) {
            case BIENVENIDO:
                if (pedido == Pedido.CONECTAR) {
                    pedido = Pedido.NINGUNO;
                    enServidor = true;
                    nombreServidor = partes.length > 1 ? partes[1] : getDireccionServidor();
                    eventos.add(CONECTADO_SERVIDOR);
                }
                return;
            case SALA_CREADA:
                if (pedido == Pedido.CREAR) {
                    pedido = Pedido.NINGUNO;
                    salaCreada = true;
                }
                return;
            case OCUPADO:
                if (pedido == Pedido.CREAR) {
                    pedido = Pedido.NINGUNO;
                    rol = Rol.NINGUNO;
                    eventos.add(OCUPADO);
                }
                return;
            case ESTADO:
                estadoSala = partes.length > 1 ? partes[1] : "";
                duenioSala = partes.length > 2 ? partes[2] : "";
                return;
            case UNIDO:
                if (pedido == Pedido.UNIRSE) {
                    pedido = Pedido.NINGUNO;
                    unirRival(partes.length > 1 ? partes[1] : "");
                }
                return;
            case LLENA:
            case NO_EXISTE:
                if (pedido == Pedido.UNIRSE) {
                    pedido = Pedido.NINGUNO;
                    rol = Rol.NINGUNO;
                    eventos.add(tipo);
                }
                return;
            case LATIDO:
                atenderLatido(partes.length > 1 ? partes[1] : "");
                return;
            default:
                break;
        }

        if (!conectado) return;
        if (tipo.equals(SALIR)) {
            perderRival(rol == Rol.ANFITRION ? "El otro jugador salió de la sala" : "El jugador 1 cerró la sala");
        } else if (tipo.equals(RECIBIDO) && partes.length > 1) {
            pendientes.remove(Integer.parseInt(partes[1]));
        } else if (tipo.equals(CONTROL) && partes.length > 2) {
            int id = Integer.parseInt(partes[1]);
            if (id == ultimoIdRecibido + 1) {
                ultimoIdRecibido = id;
                eventos.add(mensaje.substring(CONTROL.length() + partes[1].length() + 2));
            }
            if (id <= ultimoIdRecibido) enviarAlServidor(RECIBIDO + SEPARADOR + id);
        } else if (tipo.equals(ENTRADAS) && partes.length > 4) {
            guardarEntradas(partes);
        }
    }

    /** El servidor contesta cada latido con el nombre del rival (vacío si no hay). Así nadie queda colgado si se pierde un aviso. */
    private static void atenderLatido(String rival) {
        if (rol == Rol.ANFITRION && salaCreada && !conectado && !rival.isEmpty()) {
            unirRival(rival);
        } else if (conectado && rival.isEmpty() && tiempoDesdeUnion > GRACIA_AL_UNIRSE) {
            perderRival(rol == Rol.ANFITRION ? "El otro jugador salió de la sala" : "El jugador 1 cerró la sala");
        }
    }

    private static void guardarEntradas(String[] partes) {
        int sesionPaquete = Integer.parseInt(partes[1]);
        if (sesionPaquete < sesion) return;
        int segmento = Integer.parseInt(partes[2]);
        int desde = Integer.parseInt(partes[3]);
        String[] mascaras = partes[4].split(SEPARADOR_ENTRADAS);
        for (int i = 0; i < mascaras.length; i++) {
            entradasRemotas.put(clave(sesionPaquete, segmento, desde + i), Integer.parseInt(mascaras[i]));
        }
    }

    private static long clave(int numeroSesion, int segmento, int tick) {
        return ((long) numeroSesion << 40) | ((long) segmento << 24) | tick;
    }

    public static int entradaRemota(int segmento, int tick) {
        Integer mascara = entradasRemotas.get(clave(sesion, segmento, tick));
        return mascara != null ? mascara : -1;
    }

    public static void olvidarEntradas(int segmento, int hastaTick) {
        long limite = clave(sesion, segmento, Math.max(0, hastaTick));
        entradasRemotas.keySet().removeIf(clave -> clave < limite);
    }

    public static void enviarEntradas(int segmento, IntArray locales, int desde) {
        if (!conectado || desde >= locales.size) return;
        StringBuilder texto = new StringBuilder();
        texto.append(ENTRADAS).append(SEPARADOR).append(sesion).append(SEPARADOR).append(segmento)
            .append(SEPARADOR).append(desde).append(SEPARADOR);
        for (int i = desde; i < locales.size; i++) {
            if (i > desde) texto.append(SEPARADOR_ENTRADAS);
            texto.append(locales.get(i));
        }
        enviarAlServidor(texto.toString());
    }

    public static synchronized GameSession iniciarRondas(int rondas) {
        long semilla = System.nanoTime();
        nuevaSesion(sesion + 1);
        enviarSeguro(INICIAR + SEPARADOR + RONDAS + SEPARADOR + rondas + SEPARADOR + semilla + SEPARADOR + sesion);
        return GameSession.iniciarJugar(rondas, semilla, true);
    }

    public static synchronized GameSession iniciarEndless(MinigameType tipo) {
        long semilla = System.nanoTime();
        nuevaSesion(sesion + 1);
        enviarSeguro(INICIAR + SEPARADOR + ENDLESS + SEPARADOR + tipo.name() + SEPARADOR + semilla + SEPARADOR + sesion);
        return GameSession.iniciarEndless(tipo, semilla, true);
    }

    public static synchronized GameSession crearSesionRecibida(String evento) {
        String[] partes = evento.split(SEPARADOR);
        long semilla = Long.parseLong(partes[3]);
        nuevaSesion(Integer.parseInt(partes[4]));
        if (partes[1].equals(RONDAS)) return GameSession.iniciarJugar(Integer.parseInt(partes[2]), semilla, true);
        return GameSession.iniciarEndless(MinigameType.valueOf(partes[2]), semilla, true);
    }

    private static void nuevaSesion(int numero) {
        sesion = numero;
        final long limite = clave(numero, 0, 0);
        entradasRemotas.keySet().removeIf(clave -> clave < limite);
    }

    private static String nombreEquipo() {
        if (nombrePropio == null) {
            try {
                nombrePropio = InetAddress.getLocalHost().getHostName();
            } catch (IOException e) {
                nombrePropio = "RAMSHI";
            }
        }
        return nombrePropio;
    }

    private static void enviarAlServidor(String texto) {
        if (ipServidor != null) enviar(texto, ipServidor, puertoServidor);
    }

    private static void enviar(String texto, InetAddress ip, int puerto) {
        if (socket == null) return;
        byte[] datos = texto.getBytes(StandardCharsets.UTF_8);
        try {
            socket.send(new DatagramPacket(datos, datos.length, ip, puerto));
        } catch (IOException e) {
            if (socket.isClosed()) socket = null;
        }
    }
}
