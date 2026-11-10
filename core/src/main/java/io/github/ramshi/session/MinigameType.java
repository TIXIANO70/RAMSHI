package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.ramshi.screens.*;

import java.util.function.Function;

/** Catálogo de minijuegos. Para agregar uno: una línea acá. */
public enum MinigameType {
    SNAKE("Snake", SnakeScreen::new),
    LIGHTS("Secuencia de luces", LightsScreen::new),
    SHIPS("Lluvia de cañonazos", ShipsScreen::new),
    TUG_OF_WAR("Tira y afloja", TugOfWarScreen::new),
    QUICKDRAW("Quickdraw", QuickdrawScreen::new),   // comentar si faltan los PNG
    RACE("Carrera", RaceScreen::new),               // comentar si faltan los PNG
    RPS("Piedra, papel o tijera", RpsScreen::new),
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