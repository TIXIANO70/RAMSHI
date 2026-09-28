package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.some_example_name.entities.Direction;

public class InputHandler {

    /**
     * Lee la entrada del Jugador 1 mediante las teclas W, A, S, D.
     *
     * @param currentDirection Dirección actual de avance de la serpiente P1.
     * @return La nueva dirección elegida o la actual si no se presionó una tecla válida.
     */
    public Direction getP1Direction(Direction currentDirection) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && currentDirection != Direction.DOWN) {
            return Direction.UP;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && currentDirection != Direction.UP) {
            return Direction.DOWN;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A) && currentDirection != Direction.RIGHT) {
            return Direction.LEFT;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D) && currentDirection != Direction.LEFT) {
            return Direction.RIGHT;
        }
        return currentDirection;
    }

    /**
     * Lee la entrada del Jugador 2 mediante las teclas de flechas (UP, DOWN, LEFT, RIGHT).
     *
     * @param currentDirection Dirección actual de avance de la serpiente P2.
     * @return La nueva dirección elegida o la actual si no se presionó una tecla válida.
     */
    public Direction getP2Direction(Direction currentDirection) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && currentDirection != Direction.DOWN) {
            return Direction.UP;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && currentDirection != Direction.UP) {
            return Direction.DOWN;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && currentDirection != Direction.RIGHT) {
            return Direction.LEFT;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && currentDirection != Direction.LEFT) {
            return Direction.RIGHT;
        }
        return currentDirection;
    }

    /**
     * Compatibilidad: lee la entrada general delegando en el Jugador 1.
     */
    public Direction getNewDirection(Direction currentDirection) {
        return getP1Direction(currentDirection);
    }

}
