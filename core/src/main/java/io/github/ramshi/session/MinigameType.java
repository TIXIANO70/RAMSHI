package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.ramshi.screens.*;

import java.util.function.Function;

public enum MinigameType {
    SNAKE("Snake", SnakeScreen::new),
    LUCES("Secuencia de luces", LightsScreen::new),
    BARCOS("Lluvia de cañonazos", ShipsScreen::new),
    TIRA_Y_AFLOJA("Tira y afloja", TugOfWarScreen::new),
    QUICKDRAW("Quickdraw", QuickdrawScreen::new),
    CARRERA("Carrera", RaceScreen::new),
    PIEDRA_PAPEL_TIJERA("Piedra, papel o tijera", RpsScreen::new),
    TANQUES("Duelo de Tanques", TankDuelScreen::new),
    PAPA_CALIENTE("La Papa Caliente", BombTagScreen::new),
    PING_PONG("Ping Pong Bomba", PingPongScreen::new),
    SUMO("Lucha de Sumo", SumoScreen::new),
    STOP_IT("Stop It!", StopItScreen::new);

    private final String nombre;
    private final Function<Game, Screen> fabrica;

    MinigameType(String nombre, Function<Game, Screen> fabrica) {
        this.nombre = nombre;
        this.fabrica = fabrica;
    }

    public String getNombre() { return nombre; }

    public Screen crear(Game juego) { return fabrica.apply(juego); }
}
