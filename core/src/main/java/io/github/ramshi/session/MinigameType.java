package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.ramshi.screens.*;

import java.util.function.Function;

/** Catálogo de minijuegos. */
public enum MinigameType {
    SNAKE("Snake", SnakeScreen::new),
    LIGHTS("Secuencia de luces", LightsScreen::new),
    SHIPS("Lluvia de cañonazos", ShipsScreen::new),
    TUG_OF_WAR("Tira y afloja", TugOfWarScreen::new),
    QUICKDRAW("Quickdraw", QuickdrawScreen::new),
    RACE("Carrera", RaceScreen::new),
    RPS("Piedra, papel o tijera", RpsScreen::new),
    TANKS("Duelo de Tanques", TankDuelScreen::new),
    BOMB_TAG("La Papa Caliente", BombTagScreen::new),
    PINGPONG("Ping Pong Bomba", PingPongScreen::new),
    SUMO("Lucha de Sumo", SumoScreen::new),
    STOP_IT("Stop It!", StopItScreen::new);

    private final String displayName;
    private final Function<Game, Screen> factory;

    MinigameType(String displayName, Function<Game, Screen> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    public String getDisplayName() { return displayName; }

    public Screen create(Game game) { return factory.apply(game); }
}
