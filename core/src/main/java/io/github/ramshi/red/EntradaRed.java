package io.github.ramshi.red;

import com.badlogic.gdx.Input;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class EntradaRed implements InvocationHandler {

    public static final int ARRIBA = 1;
    public static final int IZQUIERDA = 2;
    public static final int ABAJO = 4;
    public static final int DERECHA = 8;
    public static final int ACCION = 16;
    public static final int PAUSA = 32;

    private static final int[] BOTONES = {ARRIBA, IZQUIERDA, ABAJO, DERECHA, ACCION};
    private static final int[] TECLAS_P1 = {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D, Input.Keys.SPACE};
    private static final int[] TECLAS_P2 = {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT, Input.Keys.ENTER};

    private final Input falsa;
    private final int[] actual = new int[2];
    private final int[] anterior = new int[2];
    private Input real;

    public EntradaRed() {
        falsa = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(), new Class<?>[] {Input.class}, this);
    }

    public Input usar(Input real) {
        this.real = real;
        return falsa;
    }

    public void avanzar(int mascaraP1, int mascaraP2) {
        anterior[0] = actual[0];
        anterior[1] = actual[1];
        actual[0] = mascaraP1;
        actual[1] = mascaraP2;
    }

    public static int leerTeclado(Input teclado) {
        int mascara = 0;
        for (int i = 0; i < BOTONES.length; i++) {
            if (tocada(teclado, TECLAS_P1[i]) || tocada(teclado, TECLAS_P2[i])) mascara |= BOTONES[i];
        }
        if (tocada(teclado, Input.Keys.ESCAPE)) mascara |= PAUSA;
        return mascara;
    }

    private static boolean tocada(Input teclado, int tecla) {
        return teclado.isKeyPressed(tecla) || teclado.isKeyJustPressed(tecla);
    }

    private boolean apretada(int[] mascaras, int tecla) {
        if (tecla == Input.Keys.ESCAPE) return ((mascaras[0] | mascaras[1]) & PAUSA) != 0;
        for (int i = 0; i < BOTONES.length; i++) {
            if (TECLAS_P1[i] == tecla) return (mascaras[0] & BOTONES[i]) != 0;
            if (TECLAS_P2[i] == tecla) return (mascaras[1] & BOTONES[i]) != 0;
        }
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method metodo, Object[] argumentos) throws Throwable {
        if (argumentos != null && argumentos.length == 1 && argumentos[0] instanceof Integer) {
            int tecla = (Integer) argumentos[0];
            if (metodo.getName().equals("isKeyPressed")) return apretada(actual, tecla);
            if (metodo.getName().equals("isKeyJustPressed")) return apretada(actual, tecla) && !apretada(anterior, tecla);
        }
        try {
            return metodo.invoke(real, argumentos);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
