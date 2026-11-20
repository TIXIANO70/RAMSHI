package io.github.ramshi.servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor dedicado de RAMSHI. No juega ni dibuja nada. Hace lo que antes hacía el anfitrión en su propio juego:
 * abre el puerto, tiene la única sala (un jugador la crea y el otro se une), detecta si alguien se fue
 * y reenvía los mensajes de la partida de un jugador al otro.
 * Los mensajes son texto separado por ';' y tienen que coincidir con los de io.github.ramshi.red.Red en el cliente.
 */
public class Servidor {

    /** Primer puerto. Si está ocupado se usan los siguientes; tiene que coincidir con Red.PUERTO y Red.CANTIDAD_PUERTOS. */
    public static final int PUERTO = 9999;
    public static final int CANTIDAD_PUERTOS = 10;

    static final String SEPARADOR = ";";
    static final String BUSCAR = "BUSCAR";
    static final String ANUNCIO = "ANUNCIO";
    static final String CONECTAR = "CONECTAR";
    static final String BIENVENIDO = "BIENVENIDO";
    static final String DESCONECTAR = "DESCONECTAR";
    static final String LATIDO = "HOLA";
    static final String ESTADO = "ESTADO";
    static final String LIBRE = "LIBRE";
    static final String ESPERANDO = "ESPERANDO";
    static final String JUGANDO = "JUGANDO";
    static final String CREAR = "CREAR";
    static final String SALA_CREADA = "CREADA";
    static final String OCUPADO = "OCUPADO";
    static final String UNIRSE = "UNIRSE";
    static final String UNIDO = "UNIDO";
    static final String LLENA = "LLENA";
    static final String NO_EXISTE = "NOEXISTE";
    static final String SALIR = "SALIR";
    static final String CONTROL = "C";
    static final String RECIBIDO = "OK";
    static final String ENTRADAS = "E";

    private static final int TAMANO_PAQUETE = 2048;
    private static final int ESPERA_RECEPCION_MS = 250;
    private static final long TIEMPO_SIN_NOTICIAS_MS = 5000;
    private static final int COPIAS_SALIR = 3;
    private static final int LARGO_MAXIMO_NOMBRE = 24;

    private static class Cliente {
        final InetSocketAddress direccion;
        final String nombre;
        long ultimoContacto;

        Cliente(InetSocketAddress direccion, String nombre) {
            this.direccion = direccion;
            this.nombre = nombre;
        }
    }

    private final int puerto;
    private final String nombre;
    private final Map<InetSocketAddress, Cliente> clientes = new LinkedHashMap<>();
    /** La única sala del servidor: jugador 1 (la creó) y jugador 2 (se unió). null = no hay sala. */
    private Cliente jugador1;
    private Cliente jugador2;
    private DatagramSocket socket;
    private volatile boolean encendido;

    public Servidor(int puerto, String nombre) {
        this.puerto = puerto;
        this.nombre = limpiar(nombre);
    }

    public int getPuerto() { return puerto; }
    public synchronized int cantidadClientes() { return clientes.size(); }

    public synchronized String estadoSala() {
        if (jugador1 == null) return "Sin sala";
        if (jugador2 == null) return "Sala de " + jugador1.nombre + " esperando jugador";
        return "Partida en curso: " + jugador1.nombre + " contra " + jugador2.nombre;
    }

    public void abrir() throws SocketException {
        socket = new DatagramSocket(puerto);
        socket.setBroadcast(true);
        socket.setSoTimeout(ESPERA_RECEPCION_MS);
        encendido = true;
    }

    public void atender() {
        byte[] datos = new byte[TAMANO_PAQUETE];
        while (encendido) {
            DatagramPacket paquete = new DatagramPacket(datos, datos.length);
            try {
                socket.receive(paquete);
                String mensaje = new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8).trim();
                InetSocketAddress origen = new InetSocketAddress(paquete.getAddress(), paquete.getPort());
                synchronized (this) {
                    procesar(mensaje, origen);
                }
            } catch (SocketTimeoutException e) {
                // Sin mensajes: solo se revisa si alguien dejó de responder.
            } catch (IOException e) {
                if (!encendido || socket.isClosed()) break;
            } catch (RuntimeException e) {
                registrar("Mensaje inválido ignorado: " + e.getMessage());
            }
            synchronized (this) {
                quitarInactivos();
            }
        }
    }

    public void cerrar() {
        encendido = false;
        if (socket != null) socket.close();
    }

    private void procesar(String mensaje, InetSocketAddress origen) {
        String tipo = mensaje.split(SEPARADOR, -1)[0];

        if (tipo.equals(BUSCAR)) {
            enviar(ANUNCIO + SEPARADOR + nombre, origen);
            return;
        }
        if (tipo.equals(CONECTAR)) {
            String[] partes = mensaje.split(SEPARADOR, -1);
            conectar(origen, partes.length > 1 ? partes[1] : "");
            return;
        }

        Cliente cliente = clientes.get(origen);
        if (cliente == null) return;
        cliente.ultimoContacto = System.currentTimeMillis();

        switch (tipo) {
            case LATIDO:
                Cliente otro = otro(cliente);
                enviar(LATIDO + SEPARADOR + (otro != null ? otro.nombre : ""), origen);
                break;
            case ESTADO:
                enviar(estado(), origen);
                break;
            case CREAR:
                crearSala(cliente);
                break;
            case UNIRSE:
                unirse(cliente);
                break;
            case SALIR:
                salirDeSala(cliente);
                break;
            case DESCONECTAR:
                desconectar(cliente, "se desconectó");
                break;
            case CONTROL:
            case RECIBIDO:
            case ENTRADAS:
                Cliente destino = otro(cliente);
                if (destino != null) enviar(mensaje, destino.direccion);
                break;
            default:
                break;
        }
    }

    /** Devuelve el rival de un jugador que está en la sala, o null. */
    private Cliente otro(Cliente cliente) {
        if (cliente == jugador1) return jugador2;
        if (cliente == jugador2) return jugador1;
        return null;
    }

    private String estado() {
        if (jugador1 == null) return ESTADO + SEPARADOR + LIBRE + SEPARADOR;
        return ESTADO + SEPARADOR + (jugador2 == null ? ESPERANDO : JUGANDO) + SEPARADOR + jugador1.nombre;
    }

    private void conectar(InetSocketAddress origen, String nombreCliente) {
        Cliente cliente = clientes.get(origen);
        if (cliente == null) {
            cliente = new Cliente(origen, nombreCliente.isEmpty() ? origen.getAddress().getHostAddress() : limpiar(nombreCliente));
            clientes.put(origen, cliente);
            registrar(cliente.nombre + " (" + texto(origen) + ") se conectó");
        }
        cliente.ultimoContacto = System.currentTimeMillis();
        enviar(BIENVENIDO + SEPARADOR + nombre, origen);
    }

    private void crearSala(Cliente cliente) {
        if (jugador1 == null) {
            jugador1 = cliente;
            registrar(cliente.nombre + " creó la sala");
        }
        enviar(jugador1 == cliente ? SALA_CREADA : OCUPADO, cliente.direccion);
    }

    private void unirse(Cliente cliente) {
        if (jugador1 == null) {
            enviar(NO_EXISTE, cliente.direccion);
        } else if (jugador2 == cliente) {
            enviar(UNIDO + SEPARADOR + jugador1.nombre, cliente.direccion);
        } else if (jugador2 != null || jugador1 == cliente) {
            enviar(LLENA, cliente.direccion);
        } else {
            jugador2 = cliente;
            registrar(cliente.nombre + " se unió a la sala de " + jugador1.nombre);
            enviar(UNIDO + SEPARADOR + jugador1.nombre, cliente.direccion);
        }
    }

    private void salirDeSala(Cliente cliente) {
        if (cliente == jugador1) {
            registrar("Se cerró la sala (" + cliente.nombre + " salió)");
            Cliente invitado = jugador2;
            jugador1 = null;
            jugador2 = null;
            if (invitado != null) avisarSalida(invitado);
        } else if (cliente == jugador2) {
            registrar(cliente.nombre + " salió de la sala; la sala vuelve a esperar jugador");
            jugador2 = null;
            avisarSalida(jugador1);
        }
    }

    private void avisarSalida(Cliente cliente) {
        for (int i = 0; i < COPIAS_SALIR; i++) enviar(SALIR, cliente.direccion);
    }

    private void desconectar(Cliente cliente, String motivo) {
        salirDeSala(cliente);
        clientes.remove(cliente.direccion);
        registrar(cliente.nombre + " " + motivo);
    }

    private void quitarInactivos() {
        long ahora = System.currentTimeMillis();
        List<Cliente> inactivos = new ArrayList<>();
        for (Cliente cliente : clientes.values()) {
            if (ahora - cliente.ultimoContacto > TIEMPO_SIN_NOTICIAS_MS) inactivos.add(cliente);
        }
        for (Cliente cliente : inactivos) desconectar(cliente, "dejó de responder y se lo desconectó");
    }

    private void enviar(String texto, InetSocketAddress destino) {
        if (socket == null || socket.isClosed()) return;
        byte[] datos = texto.getBytes(StandardCharsets.UTF_8);
        try {
            socket.send(new DatagramPacket(datos, datos.length, destino.getAddress(), destino.getPort()));
        } catch (IOException e) {
            registrar("No se pudo enviar a " + texto(destino));
        }
    }

    private static String limpiar(String texto) {
        String limpio = texto.replace(SEPARADOR, " ").trim();
        if (limpio.length() > LARGO_MAXIMO_NOMBRE) limpio = limpio.substring(0, LARGO_MAXIMO_NOMBRE);
        return limpio.isEmpty() ? "RAMSHI" : limpio;
    }

    private static String texto(InetSocketAddress direccion) {
        return direccion.getAddress().getHostAddress() + ":" + direccion.getPort();
    }

    static void registrar(String texto) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + texto);
    }
}
