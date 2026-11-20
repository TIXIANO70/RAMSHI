package io.github.ramshi.screens;

/**
 * Reglas de la pausa, sin nada de gráficos ni sonido (así se pueden probar solas):
 * - jugando (o durante la cuenta 3, 2, 1): cualquiera que apriete pausa, pausa;
 * - en pausa, en red: cada jugador aprueba o retira su aprobación; con los dos de acuerdo arranca la cuenta;
 * - en pausa, en local: apretar pausa (o REANUDAR) arranca la cuenta;
 * - si la pausa dura 5 minutos, la partida termina.
 * En red se llama dentro de cada paso sincronizado, con las teclas de los dos jugadores,
 * así las dos computadoras toman siempre la misma decisión.
 */
public class ControlPausa {

    public static final float PAUSA_MAXIMA = 5f * 60f;
    public static final float SEGUNDOS_CUENTA = 3f;

    /** Lo que pasó en un paso, para que la pantalla haga sonar lo que corresponda. */
    public enum Evento { NADA, PAUSO, EMPEZO_CUENTA, NUMERO_CUENTA, TERMINO_CUENTA, TIEMPO_AGOTADO }

    private final boolean enRed;
    private boolean pausado;
    private float tiempoPausa;
    private final boolean[] listo = new boolean[2];
    private float cuenta;
    private int ultimoNumero;

    public ControlPausa(boolean enRed) {
        this.enRed = enRed;
    }

    public boolean estaPausado() { return pausado; }
    public boolean enCuenta() { return cuenta > 0f; }
    public float getCuenta() { return cuenta; }
    public boolean estaListo(int jugador) { return listo[jugador]; }
    public float getTiempoRestante() { return Math.max(0f, PAUSA_MAXIMA - tiempoPausa); }

    /** @param pausaP1 / pausaP2 si cada jugador apretó pausa (o REANUDAR) en este paso. En local alcanza con pausaP1. */
    public Evento apretar(boolean pausaP1, boolean pausaP2) {
        if (!pausaP1 && !pausaP2) return Evento.NADA;
        if (!pausado) {
            pausar(cuenta <= 0f);
            return Evento.PAUSO;
        }
        if (!enRed) {
            empezarCuenta();
            return Evento.EMPEZO_CUENTA;
        }
        if (pausaP1) listo[0] = !listo[0];
        if (pausaP2) listo[1] = !listo[1];
        if (listo[0] && listo[1]) {
            empezarCuenta();
            return Evento.EMPEZO_CUENTA;
        }
        return Evento.NADA;
    }

    /** REANUDAR en local (en red el botón se manda como una tecla más y entra por apretar). */
    public Evento reanudarLocal() {
        if (enRed || !pausado) return Evento.NADA;
        empezarCuenta();
        return Evento.EMPEZO_CUENTA;
    }

    /** Hace correr el reloj de la pausa o la cuenta. */
    public Evento avanzar(float delta) {
        if (pausado) {
            tiempoPausa += delta;
            return tiempoPausa >= PAUSA_MAXIMA ? Evento.TIEMPO_AGOTADO : Evento.NADA;
        }
        if (cuenta <= 0f) return Evento.NADA;
        cuenta -= delta;
        if (cuenta <= 0f) {
            cuenta = 0f;
            return Evento.TERMINO_CUENTA;
        }
        int numero = (int) Math.ceil(cuenta);
        if (numero < ultimoNumero) {
            ultimoNumero = numero;
            return Evento.NUMERO_CUENTA;
        }
        return Evento.NADA;
    }

    /** @param nueva true si se pausa desde el juego (el reloj de 5 minutos vuelve a cero); false si se cancela la cuenta. */
    private void pausar(boolean nueva) {
        pausado = true;
        cuenta = 0f;
        listo[0] = false;
        listo[1] = false;
        if (nueva) tiempoPausa = 0f;
    }

    private void empezarCuenta() {
        pausado = false;
        cuenta = SEGUNDOS_CUENTA;
        ultimoNumero = (int) SEGUNDOS_CUENTA + 1;
    }
}
