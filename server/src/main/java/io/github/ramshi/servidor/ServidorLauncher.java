package io.github.ramshi.servidor;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abre el servidor dedicado. Muestra una ventanita con la IP para conectarse y un botón para apagarlo.
 * Si la computadora no tiene pantalla (por ejemplo un servidor por consola), solo escribe la IP en la consola.
 * Uso: java -jar RAMSHI-Servidor.jar [puerto]
 */
public class ServidorLauncher {

    public static void main(String[] args) {
        // Sin argumento se usa el primer puerto libre desde el 9999, así se pueden abrir varios servidores en la misma compu.
        int primero = Servidor.PUERTO;
        int cantidad = Servidor.CANTIDAD_PUERTOS;
        if (args.length > 0) {
            try {
                primero = Integer.parseInt(args[0]);
                cantidad = 1;
            } catch (NumberFormatException e) {
                System.out.println("Puerto inválido: " + args[0] + ". Se busca uno libre desde el " + Servidor.PUERTO + ".");
            }
        }

        Servidor servidor = null;
        for (int puerto = primero; puerto < primero + cantidad && servidor == null; puerto++) {
            Servidor candidato = new Servidor(puerto, nombreEquipo());
            try {
                candidato.abrir();
                servidor = candidato;
            } catch (SocketException e) {
                // Puerto ocupado (probablemente otro servidor RAMSHI): se prueba el siguiente.
            }
        }
        if (servidor == null) {
            String error = cantidad == 1 ? "No se pudo abrir el puerto " + primero + "."
                : "No hay puertos libres entre " + primero + " y " + (primero + cantidad - 1) + ". Cerrá algún servidor abierto.";
            System.out.println(error);
            if (hayPantalla()) JOptionPane.showMessageDialog(null, error, "Servidor RAMSHI", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        int puerto = servidor.getPuerto();

        List<String> ips = ipsLocales();
        if (ips.isEmpty()) ips.add("127.0.0.1");
        System.out.println("Servidor RAMSHI encendido en el puerto UDP " + puerto + ". IP: " + String.join(", ", ips));
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::cerrar));

        if (hayPantalla()) SwingUtilities.invokeLater(() -> VentanaServidor.mostrar(ips, puerto));
        servidor.atender();
    }

    private static boolean hayPantalla() {
        return !GraphicsEnvironment.isHeadless();
    }

    private static List<String> ipsLocales() {
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
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Servidor RAMSHI";
        }
    }
}
