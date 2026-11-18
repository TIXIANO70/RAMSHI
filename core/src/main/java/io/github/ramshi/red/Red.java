package io.github.ramshi.red;

import com.badlogic.gdx.utils.IntArray;
import io.github.ramshi.session.GameSession;
import io.github.ramshi.session.MinigameType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Red {

    public static final int PUERTO = 9999;
    public static final String CONECTADO = "CONECTADO";
    public static final String DESCONECTADO = "DESCONECTADO";
    public static final String SIN_RESPUESTA = "SIN_RESPUESTA";
    public static final String LLENA = "LLENA";
    public static final String VOLVER_A_SALA = "SALA";
    public static final String INICIAR = "INICIAR";

    private static final String SEPARADOR = ";";
    private static final String SEPARADOR_ENTRADAS = ",";
    private static final String BUSCAR = "BUSCAR";
    private static final String ANUNCIO = "ANUNCIO";
    private static final String CONECTAR = "CONECTAR";
    private static final String BIENVENIDO = "BIENVENIDO";
    private static final String SALIR = "SALIR";
    private static final String SALUDO = "HOLA";
    private static final String CONTROL = "C";
    private static final String RECIBIDO = "OK";
    private static final String ENTRADAS = "E";
    private static final String RONDAS = "RONDAS";
    private static final String ENDLESS = "ENDLESS";
    private static final String DIRECCION_DIFUSION = "255.255.255.255";
    private static final String DIRECCION_LOCAL = "127.0.0.1";
    private static final float INTERVALO_SALUDO = 0.5f;
    private static final float INTERVALO_REENVIO = 0.25f;
    private static final float INTERVALO_CONEXION = 0.5f;
    private static final float TIEMPO_SIN_NOTICIAS = 5f;
    private static final float TIEMPO_INTENTO_CONEXION = 5f;
    private static final float DURACION_AVISO = 4f;
    private static final int COPIAS_SALIR = 3;

    public enum Rol { NINGUNO, ANFITRION, INVITADO }

    private static DatagramSocket socket;
    private static HiloRed hilo;
    private static volatile Rol rol = Rol.NINGUNO;
    private static volatile boolean conectado;
    private static volatile boolean intentandoConectar;
    private static volatile long ultimoContacto;
    private static InetAddress ipOtro;
    private static int puertoOtro;
    private static String nombreOtro = "";
    private static String nombrePropio;
    private static volatile int sesion;
    private static int ultimoIdEnviado;
    private static int ultimoIdRecibido;
    private static float tiempoSaludo;
    private static float tiempoReenvio;
    private static float tiempoConexion;
    private static float tiempoIntento;
    private static String aviso;
    private static float tiempoAviso;

    private static final ConcurrentLinkedQueue<String> eventos = new ConcurrentLinkedQueue<>();
    private static final Map<Integer, String> pendientes = new ConcurrentHashMap<>();
    private static final Map<Long, Integer> entradasRemotas = new ConcurrentHashMap<>();
    private static final Map<String, String> salasEncontradas = Collections.synchronizedMap(new LinkedHashMap<String, String>());

    private Red() {}

    public static boolean esAnfitrion() { return rol == Rol.ANFITRION; }
    public static boolean esInvitado() { return rol == Rol.INVITADO; }
    public static boolean estaConectado() { return conectado; }
    public static String getNombreOtro() { return nombreOtro; }
    public static String getIpOtro() { return ipOtro != null ? ipOtro.getHostAddress() : ""; }
    public static String siguienteEvento() { return eventos.poll(); }
    public static String getAviso() { return aviso; }

    public static void avisar(String texto) {
        aviso = texto;
        tiempoAviso = DURACION_AVISO;
    }

    public static synchronized boolean crearSala() {
        cerrar();
        try {
            socket = new DatagramSocket(PUERTO);
        } catch (SocketException e) {
            return false;
        }
        rol = Rol.ANFITRION;
        nombreEquipo();
        iniciarHilo();
        return true;
    }

    public static synchronized boolean prepararInvitado() {
        cerrar();
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (SocketException e) {
            return false;
        }
        rol = Rol.INVITADO;
        iniciarHilo();
        return true;
    }

    private static void iniciarHilo() {
        reiniciarConexion();
        hilo = new HiloRed(socket);
        hilo.start();
    }

    private static void reiniciarConexion() {
        conectado = false;
        intentandoConectar = false;
        ultimoIdEnviado = 0;
        ultimoIdRecibido = 0;
        pendientes.clear();
        entradasRemotas.clear();
        eventos.clear();
    }

    public static synchronized void buscarSalas() {
        if (rol != Rol.INVITADO) return;
        salasEncontradas.clear();
        try {
            enviar(BUSCAR, InetAddress.getByName(DIRECCION_DIFUSION), PUERTO);
            enviar(BUSCAR, InetAddress.getByName(DIRECCION_LOCAL), PUERTO);
        } catch (IOException e) {
            avisar("No se pudo buscar en la red");
        }
    }

    public static List<String[]> getSalasEncontradas() {
        List<String[]> lista = new ArrayList<>();
        synchronized (salasEncontradas) {
            for (Map.Entry<String, String> sala : salasEncontradas.entrySet()) lista.add(new String[] {sala.getKey(), sala.getValue()});
        }
        return lista;
    }

    public static synchronized boolean conectar(String ip) {
        if (rol != Rol.INVITADO) return false;
        try {
            ipOtro = InetAddress.getByName(ip);
        } catch (IOException e) {
            return false;
        }
        puertoOtro = PUERTO;
        reiniciarConexion();
        intentandoConectar = true;
        tiempoIntento = 0f;
        tiempoConexion = INTERVALO_CONEXION;
        return true;
    }

    public static synchronized void cerrar() {
        if (conectado) {
            for (int i = 0; i < COPIAS_SALIR; i++) enviarAlOtro(SALIR);
        }
        if (hilo != null) hilo.terminar();
        hilo = null;
        socket = null;
        rol = Rol.NINGUNO;
        ipOtro = null;
        nombreOtro = "";
        reiniciarConexion();
    }

    public static synchronized void actualizar(float delta) {
        if (tiempoAviso > 0f) {
            tiempoAviso -= delta;
            if (tiempoAviso <= 0f) aviso = null;
        }
        if (rol == Rol.NINGUNO) return;

        if (intentandoConectar && !conectado) {
            tiempoIntento += delta;
            tiempoConexion += delta;
            if (tiempoConexion >= INTERVALO_CONEXION) {
                tiempoConexion = 0f;
                enviarAlOtro(CONECTAR);
            }
            if (tiempoIntento >= TIEMPO_INTENTO_CONEXION) {
                intentandoConectar = false;
                eventos.add(SIN_RESPUESTA);
            }
        }
        if (!conectado) return;

        tiempoSaludo += delta;
        if (tiempoSaludo >= INTERVALO_SALUDO) {
            tiempoSaludo = 0f;
            enviarAlOtro(SALUDO);
        }
        tiempoReenvio += delta;
        if (tiempoReenvio >= INTERVALO_REENVIO) {
            tiempoReenvio = 0f;
            for (Map.Entry<Integer, String> p : pendientes.entrySet()) {
                enviarAlOtro(CONTROL + SEPARADOR + p.getKey() + SEPARADOR + p.getValue());
            }
        }
        if ((System.nanoTime() - ultimoContacto) / 1e9f > TIEMPO_SIN_NOTICIAS) {
            perderConexion("Se perdió la conexión con el otro jugador");
        }
    }

    private static void perderConexion(String motivo) {
        conectado = false;
        pendientes.clear();
        entradasRemotas.clear();
        avisar(motivo);
        eventos.add(DESCONECTADO);
        if (rol == Rol.INVITADO) intentandoConectar = false;
    }

    public static synchronized void enviarSeguro(String texto) {
        if (!conectado) return;
        int id = ++ultimoIdEnviado;
        pendientes.put(id, texto);
        enviarAlOtro(CONTROL + SEPARADOR + id + SEPARADOR + texto);
    }

    static synchronized void procesarMensaje(String mensaje, InetAddress ip, int puerto) {
        if (socket == null) return;
        String[] partes = mensaje.split(SEPARADOR, -1);
        String tipo = partes[0];
        boolean delOtro = ipOtro != null && ip.equals(ipOtro) && puerto == puertoOtro;
        if (delOtro) ultimoContacto = System.nanoTime();

        if (rol == Rol.ANFITRION) {
            if (tipo.equals(BUSCAR) && !conectado) {
                enviar(ANUNCIO + SEPARADOR + nombreEquipo(), ip, puerto);
                return;
            }
            if (tipo.equals(CONECTAR)) {
                if (!conectado) {
                    ipOtro = ip;
                    puertoOtro = puerto;
                    nombreOtro = ip.getHostAddress();
                    reiniciarConexion();
                    conectado = true;
                    ultimoContacto = System.nanoTime();
                    eventos.add(CONECTADO);
                }
                if (ip.equals(ipOtro) && puerto == puertoOtro) enviar(BIENVENIDO + SEPARADOR + nombreEquipo(), ip, puerto);
                else enviar(LLENA, ip, puerto);
                return;
            }
        } else if (rol == Rol.INVITADO) {
            if (tipo.equals(ANUNCIO) && partes.length > 1) {
                salasEncontradas.put(ip.getHostAddress(), partes[1]);
                return;
            }
            if (tipo.equals(BIENVENIDO) && delOtro && intentandoConectar) {
                intentandoConectar = false;
                conectado = true;
                nombreOtro = partes.length > 1 ? partes[1] : ip.getHostAddress();
                eventos.add(CONECTADO);
                return;
            }
            if (tipo.equals(LLENA) && delOtro && intentandoConectar) {
                intentandoConectar = false;
                eventos.add(LLENA);
                return;
            }
        }

        if (!delOtro || !conectado) return;
        if (tipo.equals(SALIR)) {
            perderConexion(rol == Rol.ANFITRION ? "El otro jugador salió de la sala" : "El anfitrión cerró la sala");
        } else if (tipo.equals(RECIBIDO) && partes.length > 1) {
            pendientes.remove(Integer.parseInt(partes[1]));
        } else if (tipo.equals(CONTROL) && partes.length > 2) {
            int id = Integer.parseInt(partes[1]);
            if (id == ultimoIdRecibido + 1) {
                ultimoIdRecibido = id;
                eventos.add(mensaje.substring(CONTROL.length() + partes[1].length() + 2));
            }
            if (id <= ultimoIdRecibido) enviarAlOtro(RECIBIDO + SEPARADOR + id);
        } else if (tipo.equals(ENTRADAS) && partes.length > 4) {
            guardarEntradas(partes);
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
        enviarAlOtro(texto.toString());
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

    public static List<String> ipsLocales() {
        List<String> ips = new ArrayList<>();
        try {
            for (NetworkInterface interfaz : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!interfaz.isUp() || interfaz.isLoopback() || interfaz.isVirtual()) continue;
                for (InetAddress direccion : Collections.list(interfaz.getInetAddresses())) {
                    if (direccion instanceof Inet4Address) ips.add(direccion.getHostAddress());
                }
            }
        } catch (SocketException e) {
            return ips;
        }
        return ips;
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

    private static void enviarAlOtro(String texto) {
        if (ipOtro != null) enviar(texto, ipOtro, puertoOtro);
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
