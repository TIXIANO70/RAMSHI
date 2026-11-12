package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.ramshi.screens.*;

import java.util.function.Function;

public enum MinigameType {
    SNAKE("Snake", SnakeScreen::new,
        "Cada uno maneja su serpiente en su propia arena. Comé manzanas para crecer; si chocás, reaparecés con 0 manzanas.",
        "Lograr el récord de manzanas más alto de la ronda.",
        "Por diferencia de récord: 4 o más = 3 pts, 2 o 3 = 2, 1 = 1."),
    LIGHTS("Lights", LightsScreen::new,
        "Se muestran 3 secuencias de flechas (de 4, 5 y 6 luces). Repetilas con tus teclas; si te equivocás, empezás esa secuencia de nuevo.",
        "Completar más secuencias que el rival.",
        "Según las secuencias completadas: 3, 2 o 1 punto."),
    SHIPS("Ships", ShipsScreen::new,
        "Mové tu barco de izquierda a derecha para esquivar los cañonazos. Los dos reciben el mismo patrón y la lluvia se acelera.",
        "Que al rival le peguen antes que a vos.",
        "Por rapidez: antes de 3,3 s = 3 pts, antes de 6,6 s = 2, después = 1."),
    TUG_OF_WAR("Tug of War", TugOfWarScreen::new,
        "Alterná tus dos teclas lo más rápido posible para tirar de la soga. Apretar la misma dos veces seguidas no tira.",
        "Llevar la soga hasta tu línea.",
        "Por rapidez (3, 2 o 1). Si nadie llega, 1 punto para quien la tenga de su lado."),
    QUICKDRAW("Quickdraw", QuickdrawScreen::new,
        "Cuando aparece una tecla en el centro, apretala antes que el rival. Si te equivocás o te adelantás, perdés una vida.",
        "Dejar al rival sin sus 3 vidas.",
        "Según las vidas con las que terminás: 3, 2 o 1 punto."),
    RACE("Race", RaceScreen::new,
        "Cambiá de carril para esquivar los obstáculos. La velocidad sube sola y chocar te deja en 0.",
        "Recorrer más distancia que el rival.",
        "Por ventaja: 50% más = 3 pts, 20% más = 2, menos = 1."),
    RPS("RPS", RpsScreen::new,
        "Piedra, papel o tijera rápido: durante la cuenta 3, 2, 1 elegí en secreto. Se juegan unas 6 manos en 10 segundos.",
        "Ganar más manos que el rival.",
        "1 punto por mano ganada, hasta 5."),
    TANK_DUEL("Tank Duel", TankDuelScreen::new,
        "Movete en 4 direcciones y disparale al rival. Las balas se rompen contra las cajas y los bordes. 3 vidas cada uno.",
        "Destruir el tanque del rival.",
        "KO sin perder vidas = 3 pts, KO = 2, más vidas al final = 1."),
    BOMB_TAG("Bomb Tag", BombTagScreen::new,
        "Uno tiene la bomba y se la pasa tocando al otro. Usá el dash para escapar o para alcanzarlo.",
        "No tener la bomba cuando explota a los 10 segundos.",
        "Según cuánto la tuvo el rival: más del 70% = 3 pts, más del 50% = 2, menos = 1."),
    PING_PONG("Ping Pong", PingPongScreen::new,
        "Mové tu paleta arriba y abajo para devolver la bomba. Cada rebote la acelera.",
        "Meter 2 goles.",
        "2-0 = 3 pts, 2-1 = 2, ir ganando al final = 1."),
    SUMO("Sumo", SumoScreen::new,
        "Empujá al rival chocándolo y saltá para esquivarlo. La plataforma se achica con el tiempo.",
        "Tirar al rival de la plataforma.",
        "Por rapidez (3, 2 o 1). Si nadie cae, 1 punto para el más cerca del centro."),
    STOP_IT("Stop It", StopItScreen::new,
        "Frená el marcador dentro de la zona: cuanto más al centro, más vale. La zona se achica y el marcador acelera.",
        "Sumar más que el rival.",
        "Por diferencia: 6 o más = 3 pts, 3 a 5 = 2, 1 o 2 = 1.");

    private static final String CARPETA_IMAGENES = "menu/minijuegos/";

    private final String nombre;
    private final Function<Game, Screen> fabrica;
    private final String comoSeJuega;
    private final String meta;
    private final String puntos;

    MinigameType(String nombre, Function<Game, Screen> fabrica, String comoSeJuega, String meta, String puntos) {
        this.nombre = nombre;
        this.fabrica = fabrica;
        this.comoSeJuega = comoSeJuega;
        this.meta = meta;
        this.puntos = puntos;
    }

    public String getNombre() { return nombre; }
    public String getComoSeJuega() { return comoSeJuega; }
    public String getMeta() { return meta; }
    public String getPuntos() { return puntos; }
    public String getImagen() { return CARPETA_IMAGENES + name().toLowerCase() + ".png"; }

    public Screen crear(Game juego) { return fabrica.apply(juego); }
}
