package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.some_example_name.entities.Direction;

public class InputHandler {

    /**
     * Lee el teclado y devuelve la nueva dirección si es válida.
     *
     * @param currentDirection Dirección actual en la que se mueve la serpiente.
     * @return La nueva dirección o la actual si no se presionó una tecla válida.
     */
    public Direction getNewDirection(Direction currentDirection) {
        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W))
            && currentDirection != Direction.DOWN) {
            System.out.println("w");
            return Direction.UP;
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S))
            && currentDirection != Direction.UP) {
            return Direction.DOWN;
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A))
            && currentDirection != Direction.RIGHT) {
            return Direction.LEFT;
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D))
            && currentDirection != Direction.LEFT) {
            return Direction.RIGHT;
        }

        return currentDirection;
    }
}
