package io.github.ramshi.session;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import io.github.ramshi.menus.ResultsScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameSession {

    public enum Modo { JUGAR, ENDLESS }

    private static final long MEZCLA_SEMILLA = 7919L;

    public static class Resultado {
        public final MinigameType tipo;
        public final int puntosP1, puntosP2;
        Resultado(MinigameType tipo, int puntosP1, int puntosP2) {
            this.tipo = tipo;
            this.puntosP1 = puntosP1;
            this.puntosP2 = puntosP2;
        }
    }

    private static GameSession actual = new GameSession(Modo.ENDLESS, new ArrayList<MinigameType>(), 0L, false);

    public static GameSession actual() { return actual; }

    public static GameSession iniciarJugar(int rondas) {
        return iniciarJugar(rondas, System.nanoTime(), false);
    }

    public static GameSession iniciarJugar(int rondas, long semilla, boolean enRed) {
        Random azar = new Random(semilla);
        List<MinigameType> cola = new ArrayList<>();
        while (cola.size() < rondas) {
            List<MinigameType> bloque = new ArrayList<>(Arrays.asList(MinigameType.values()));
            Collections.shuffle(bloque, azar);
            if (!cola.isEmpty() && bloque.get(0) == cola.get(cola.size() - 1)) {
                Collections.swap(bloque, 0, bloque.size() - 1);
            }
            cola.addAll(bloque);
        }
        actual = new GameSession(Modo.JUGAR, new ArrayList<>(cola.subList(0, rondas)), semilla, enRed);
        return actual;
    }

    public static GameSession iniciarEndless(MinigameType tipo) {
        return iniciarEndless(tipo, System.nanoTime(), false);
    }

    public static GameSession iniciarEndless(MinigameType tipo, long semilla, boolean enRed) {
        List<MinigameType> cola = new ArrayList<>();
        cola.add(tipo);
        actual = new GameSession(Modo.ENDLESS, cola, semilla, enRed);
        return actual;
    }

    private final Modo modo;
    private final List<MinigameType> cola;
    private final List<Resultado> resultados = new ArrayList<>();
    private final long semilla;
    private final boolean enRed;
    private int indice = 0;

    private GameSession(Modo modo, List<MinigameType> cola, long semilla, boolean enRed) {
        this.modo = modo;
        this.cola = cola;
        this.semilla = semilla;
        this.enRed = enRed;
    }

    public boolean esModoJugar() { return modo == Modo.JUGAR; }
    public boolean esEnRed() { return enRed; }
    public MinigameType tipoActual() { return indice < cola.size() ? cola.get(indice) : null; }
    public int getIndice() { return indice; }
    public int getTotalJuegos() { return cola.size(); }
    public List<Resultado> getResultados() { return resultados; }

    public int totalP1() { int t = 0; for (Resultado r : resultados) t += r.puntosP1; return t; }
    public int totalP2() { int t = 0; for (Resultado r : resultados) t += r.puntosP2; return t; }

    public void registrarResultado(int puntosP1, int puntosP2) {
        resultados.add(new Resultado(tipoActual(), puntosP1, puntosP2));
    }

    public Screen primeraPantalla(Game juego) { return crearActual(juego); }

    public Screen siguientePantalla(Game juego) {
        indice++;
        if (indice >= cola.size()) return new ResultsScreen(juego, this);
        return crearActual(juego);
    }

    private Screen crearActual(Game juego) {
        if (enRed) MathUtils.random.setSeed(semilla + indice * MEZCLA_SEMILLA);
        return cola.get(indice).crear(juego);
    }
}
