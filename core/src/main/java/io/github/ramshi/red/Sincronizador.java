package io.github.ramshi.red;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntArray;

public class Sincronizador {

    public static final float TICK = 1f / 60f;
    private static final int RETRASO = 4;
    private static final int VENTANA = 30;
    private static final int MAX_PASOS_POR_CUADRO = 8;
    private static final int LIMPIEZA = 120;
    private static final float ESPERA_VISIBLE = 0.5f;

    public interface Paso {
        boolean ejecutar(float delta);
    }

    private final int segmento;
    private final EntradaRed entrada = new EntradaRed();
    private final IntArray locales = new IntArray();
    private int tick;
    private float acumulado;
    private float tiempoEsperando;
    private boolean pausaPedida;

    public Sincronizador(int segmento) {
        this.segmento = segmento;
    }

    public void pedirPausa() {
        pausaPedida = true;
    }

    public boolean estaEsperando() {
        return tiempoEsperando > ESPERA_VISIBLE;
    }

    public void actualizar(float delta, Paso paso) {
        int mascara = EntradaRed.leerTeclado(Gdx.input);
        while (locales.size < tick + RETRASO) {
            int propia = mascara;
            if (pausaPedida) {
                propia |= EntradaRed.PAUSA;
                pausaPedida = false;
            }
            locales.add(propia);
        }
        Red.enviarEntradas(segmento, locales, Math.max(0, locales.size - VENTANA));

        acumulado = Math.min(acumulado + delta, TICK * MAX_PASOS_POR_CUADRO);
        boolean avanzo = false;
        while (acumulado >= TICK) {
            int remota = Red.entradaRemota(segmento, tick);
            if (remota < 0) break;
            int propia = locales.get(tick);
            if (Red.esAnfitrion()) entrada.avanzar(propia, remota);
            else entrada.avanzar(remota, propia);

            Input real = Gdx.input;
            Gdx.input = entrada.usar(real);
            boolean sigue;
            try {
                sigue = paso.ejecutar(TICK);
            } finally {
                Gdx.input = real;
            }
            tick++;
            acumulado -= TICK;
            avanzo = true;
            if (tick % LIMPIEZA == 0) Red.olvidarEntradas(segmento, tick - LIMPIEZA);
            if (!sigue) return;
        }
        tiempoEsperando = avanzo ? 0f : tiempoEsperando + delta;
    }
}
