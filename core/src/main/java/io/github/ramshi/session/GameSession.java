package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.ramshi.screens.ResultsScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Estado de una partida: modo, cola de minijuegos y puntos acumulados. */
public class GameSession {

    public enum Mode { CAMPAIGN, ENDLESS }

    public static class Result {
        public final MinigameType type;
        public final int p1Points, p2Points;
        Result(MinigameType type, int p1Points, int p2Points) {
            this.type = type;
            this.p1Points = p1Points;
            this.p2Points = p2Points;
        }
    }

    // Sesión por defecto (si una pantalla se abre sin pasar por el menú)
    private static GameSession current = new GameSession(Mode.ENDLESS, new ArrayList<MinigameType>());

    public static GameSession current() { return current; }

    /** Modo Jugar: cola aleatoria de 'rounds' juegos, sin repetir seguido. */
    public static GameSession startCampaign(int rounds) {
        List<MinigameType> queue = new ArrayList<>();
        while (queue.size() < rounds) {
            List<MinigameType> block = new ArrayList<>(Arrays.asList(MinigameType.values()));
            Collections.shuffle(block);
            if (!queue.isEmpty() && block.get(0) == queue.get(queue.size() - 1)) {
                Collections.swap(block, 0, block.size() - 1);
            }
            queue.addAll(block);
        }
        current = new GameSession(Mode.CAMPAIGN, new ArrayList<>(queue.subList(0, rounds)));
        return current;
    }

    /** Modo Endless: un solo minijuego en bucle. */
    public static GameSession startEndless(MinigameType type) {
        List<MinigameType> queue = new ArrayList<>();
        queue.add(type);
        current = new GameSession(Mode.ENDLESS, queue);
        return current;
    }

    private final Mode mode;
    private final List<MinigameType> queue;
    private final List<Result> results = new ArrayList<>();
    private int index = 0;

    private GameSession(Mode mode, List<MinigameType> queue) {
        this.mode = mode;
        this.queue = queue;
    }

    public boolean isCampaign() { return mode == Mode.CAMPAIGN; }
    public MinigameType currentType() { return index < queue.size() ? queue.get(index) : null; }
    public int getIndex() { return index; }
    public int getTotalGames() { return queue.size(); }
    public List<Result> getResults() { return results; }

    public int totalP1() { int t = 0; for (Result r : results) t += r.p1Points; return t; }
    public int totalP2() { int t = 0; for (Result r : results) t += r.p2Points; return t; }

    public void recordResult(int p1Points, int p2Points) {
        results.add(new Result(currentType(), p1Points, p2Points));
    }

    public Screen firstScreen(Game game) { return queue.get(index).create(game); }

    /** Avanza al siguiente juego, o a la pantalla de resultados si era el último. */
    public Screen nextScreen(Game game) {
        index++;
        if (index >= queue.size()) return new ResultsScreen(game, this);
        return queue.get(index).create(game);
    }
}