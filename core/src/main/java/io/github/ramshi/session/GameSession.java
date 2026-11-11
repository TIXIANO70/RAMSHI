package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import io.github.ramshi.screens.ResultsScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameSession {

    public enum Modo { JUGAR, ENDLESS }

    public static class Resultado {
        public final MinigameType tipo;
        public final int puntosP1, puntosP2;
        Resultado(MinigameType tipo, int puntosP1, int puntosP2) {
            this.tipo = tipo;
            this.puntosP1 = puntosP1;
            this.puntosP2 = puntosP2;
        }
    }

    private static GameSession actual = new GameSession(Modo.ENDLESS, new ArrayList<MinigameType>());

    public static GameSession actual() { return actual; }

    public static GameSession iniciarJugar(int rondas) {
        List<MinigameType> cola = new ArrayList<>();
        while (cola.size() < rondas) {
            List<MinigameType> bloque = new ArrayList<>(Arrays.asList(MinigameType.values()));
            Collections.shuffle(bloque);
            if (!cola.isEmpty() && bloque.get(0) == cola.get(cola.size() - 1)) {
                Collections.swap(bloque, 0, bloque.size() - 1);
            }
            cola.addAll(bloque);
        }
        actual = new GameSession(Modo.JUGAR, new ArrayList<>(cola.subList(0, rondas)));
        return actual;
    }

    public static GameSession iniciarEndless(MinigameType tipo) {
        List<MinigameType> cola = new ArrayList<>();
        cola.add(tipo);
        actual = new GameSession(Modo.ENDLESS, cola);
        return actual;
    }

    private final Modo modo;
    private final List<MinigameType> cola;
    private final List<Resultado> resultados = new ArrayList<>();
    private int indice = 0;

    private GameSession(Modo modo, List<MinigameType> cola) {
        this.modo = modo;
        this.cola = cola;
    }

    public boolean esModoJugar() { return modo == Modo.JUGAR; }
    public MinigameType tipoActual() { return indice < cola.size() ? cola.get(indice) : null; }
    public int getIndice() { return indice; }
    public int getTotalJuegos() { return cola.size(); }
    public List<Resultado> getResultados() { return resultados; }

    public int totalP1() { int t = 0; for (Resultado r : resultados) t += r.puntosP1; return t; }
    public int totalP2() { int t = 0; for (Resultado r : resultados) t += r.puntosP2; return t; }

    public void registrarResultado(int puntosP1, int puntosP2) {
        resultados.add(new Resultado(tipoActual(), puntosP1, puntosP2));
    }

    public Screen primeraPantalla(Game juego) { return cola.get(indice).crear(juego); }

    public Screen siguientePantalla(Game juego) {
        indice++;
        if (indice >= cola.size()) return new ResultsScreen(juego, this);
        return cola.get(indice).crear(juego);
    }
}