package io.github.some_example_name.entities;

/**
 * Enumeración que representa las posibles direcciones de movimiento y orientación en la cuadrícula 2D.
 */
public enum Direction {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /**
     * Metodo isOpposite (other)
     * Comprueba si otra dirección es opuesta a la actual para evitar giros de 180 grados.
     * Parámetro Direction other Dirección a comparar.
     * devolvera isOpposite true si es la dirección opuesta, false en caso contrario.
     */
    public boolean isOpposite(Direction other) {
        return other != null && this.dx + other.dx == 0 && this.dy + other.dy == 0;
    }
}

