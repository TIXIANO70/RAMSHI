package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;


public class SnakeArena implements Disposable {

    private final int playerNumber;
    private final float originPixelX;
    private final float originPixelY;
    private final int gridWidth;
    private final int gridHeight;

    private final Snake snake;
    private final Food food;
    private final Texture tile1;
    private final Texture tile2;

    private Direction currentDirection;
    private Direction nextDirection;
    private int score = 0;

    private final int initialSpawnX;
    private final int initialSpawnY;
    private final Direction initialDirection;


    public SnakeArena(int playerNumber, float originPixelX, float originPixelY, int gridWidth, int gridHeight, Texture tile1, Texture tile2) {
        this.playerNumber = playerNumber;
        this.originPixelX = originPixelX;
        this.originPixelY = originPixelY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.tile1 = tile1;
        this.tile2 = tile2;

        this.initialSpawnY = gridHeight / 2;
        if (playerNumber == 1) {
            this.initialSpawnX = 4;
            this.initialDirection = Direction.RIGHT;
        } else {
            this.initialSpawnX = gridWidth - 5;
            this.initialDirection = Direction.LEFT;
        }

        this.currentDirection = initialDirection;
        this.nextDirection = initialDirection;

        this.snake = new Snake(playerNumber, initialSpawnX, initialSpawnY, initialDirection);
        this.food = new Food();
        this.food.respawn(gridWidth, gridHeight, this.snake);
    }


    public void setNextDirection(Direction dir) {
        if (dir != null && !dir.isOpposite(currentDirection)) {
            this.nextDirection = dir;
        }
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public Direction getNextDirection() {
        return nextDirection;
    }



    public void tick() {
        currentDirection = nextDirection;
        snake.step(currentDirection);

        if (snake.getHead().getGridX() == food.getGridX() && snake.getHead().getGridY() == food.getGridY()) {
            snake.grow();
            score++;
            food.respawn(gridWidth, gridHeight, snake);
        }

        if (snake.checkOutOfBounds(gridWidth, gridHeight) || snake.checkSelfCollision()) {
            respawnSnake();
        }
    }

    public void respawnSnake() {
        snake.reset(initialSpawnX, initialSpawnY, initialDirection);
        currentDirection = initialDirection;
        nextDirection = initialDirection;
        score = 0; // Se reinicia el marcador de la ronda al morir
        food.respawn(gridWidth, gridHeight, snake);
    }

    public void resetForNewRound() {
        respawnSnake();
    }


    public void render(SpriteBatch batch) {
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Texture currentTile = ((x + y) % 2 == 0) ? tile1 : tile2;
                batch.draw(currentTile, originPixelX + x * Snake.TILE_SIZE, originPixelY + y * Snake.TILE_SIZE, Snake.TILE_SIZE, Snake.TILE_SIZE);
            }
        }

        // 2. Dibujar comida
        food.render(batch, Snake.TILE_SIZE, originPixelX, originPixelY);

        // 3. Dibujar serpiente
        snake.render(batch, originPixelX, originPixelY);
    }

    public int getScore() {
        return score;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public float getOriginPixelX() {
        return originPixelX;
    }

    public float getOriginPixelY() {
        return originPixelY;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public void dispose() {
        snake.dispose();
        food.dispose();
    }
}
