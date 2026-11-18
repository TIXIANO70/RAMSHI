package io.github.ramshi.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class HiloRed extends Thread {

    private static final int TAMANO_PAQUETE = 2048;

    private final DatagramSocket socket;
    private volatile boolean fin = false;

    public HiloRed(DatagramSocket socket) {
        this.socket = socket;
        setDaemon(true);
    }

    @Override
    public void run() {
        byte[] datos = new byte[TAMANO_PAQUETE];
        while (!fin) {
            DatagramPacket paquete = new DatagramPacket(datos, datos.length);
            try {
                socket.receive(paquete);
                String mensaje = new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8).trim();
                Red.procesarMensaje(mensaje, paquete.getAddress(), paquete.getPort());
            } catch (IOException e) {
                if (fin || socket.isClosed()) break;
            } catch (RuntimeException e) {
                continue;
            }
        }
    }

    public void terminar() {
        fin = true;
        socket.close();
    }
}
