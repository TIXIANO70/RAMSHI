package io.github.ramshi.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.github.ramshi.entities.Direction;


public class InputHandler extends InputAdapter {

    private Direction p1BufferedDirection = null;
    private Direction p2BufferedDirection = null;

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.W) {
            p1BufferedDirection = Direction.UP;
            return true;
        }
        if (keycode == Input.Keys.S) {
            p1BufferedDirection = Direction.DOWN;
            return true;
        }
        if (keycode == Input.Keys.A) {
            p1BufferedDirection = Direction.LEFT;
            return true;
        }
        if (keycode == Input.Keys.D) {
            p1BufferedDirection = Direction.RIGHT;
            return true;
        }

        if (keycode == Input.Keys.UP || keycode == Input.Keys.NUMPAD_8) {
            p2BufferedDirection = Direction.UP;
            return true;
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.NUMPAD_2) {
            p2BufferedDirection = Direction.DOWN;
            return true;
        }
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.NUMPAD_4) {
            p2BufferedDirection = Direction.LEFT;
            return true;
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.NUMPAD_6) {
            p2BufferedDirection = Direction.RIGHT;
            return true;
        }

        return false;
    }


    public Direction getP1Direction(Direction currentDirection) {
        Direction dir = p1BufferedDirection;
        p1BufferedDirection = null;

        if (dir == null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) || (Gdx.input.isKeyPressed(Input.Keys.W) && currentDirection != Direction.UP && currentDirection != Direction.DOWN)) {
                dir = Direction.UP;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.S) || (Gdx.input.isKeyPressed(Input.Keys.S) && currentDirection != Direction.DOWN && currentDirection != Direction.UP)) {
                dir = Direction.DOWN;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.A) || (Gdx.input.isKeyPressed(Input.Keys.A) && currentDirection != Direction.LEFT && currentDirection != Direction.RIGHT)) {
                dir = Direction.LEFT;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.D) || (Gdx.input.isKeyPressed(Input.Keys.D) && currentDirection != Direction.RIGHT && currentDirection != Direction.LEFT)) {
                dir = Direction.RIGHT;
            }
        }

        if (dir != null && !dir.isOpposite(currentDirection)) {
            return dir;
        }

        return null;
    }


    public Direction getP2Direction(Direction currentDirection) {
        Direction dir = p2BufferedDirection;
        p2BufferedDirection = null;

        if (dir == null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || (Gdx.input.isKeyPressed(Input.Keys.UP) && currentDirection != Direction.UP && currentDirection != Direction.DOWN)) {
                dir = Direction.UP;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || (Gdx.input.isKeyPressed(Input.Keys.DOWN) && currentDirection != Direction.DOWN && currentDirection != Direction.UP)) {
                dir = Direction.DOWN;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || (Gdx.input.isKeyPressed(Input.Keys.LEFT) && currentDirection != Direction.LEFT && currentDirection != Direction.RIGHT)) {
                dir = Direction.LEFT;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && currentDirection != Direction.RIGHT && currentDirection != Direction.LEFT)) {
                dir = Direction.RIGHT;
            }
        }

        if (dir != null && !dir.isOpposite(currentDirection)) {
            return dir;
        }

        return null;
    }

    public void reset() {
        p1BufferedDirection = null;
        p2BufferedDirection = null;
    }

    public Direction getNewDirection(Direction currentDirection) {
        Direction dir = getP1Direction(currentDirection);
        return dir != null ? dir : currentDirection;
    }
}

