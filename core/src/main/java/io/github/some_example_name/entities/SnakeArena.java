package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Representa la arena o campo de juego independiente para un jugador en el modo Split-Screen.
 * Encapsula la cuadrícula de baldosas, la serpiente correspondiente, la comida y el marcador de la ronda.
 */
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

    /**
     * Constructor SnakeArena (playerNumber, originPixelX, originPixelY, gridWidth, gridHeight, tile1, tile2)
     * Construye una nueva arena de juego para el jugador indicado.
     * Parámetro int playerNumber Identificador del jugador (1 o 2).
     * Parámetro float originPixelX Coordenada X en píxeles donde inicia el tablero.
     * Parámetro float originPixelY Coordenada Y en píxeles donde inicia el tablero.
     * Parámetro int gridWidth Ancho de la arena en casillas.
     * Parámetro int gridHeight Alto de la arena en casillas.
     * Parámetro Texture tile1 Textura de baldosa clara/alternada.
     * Parámetro Texture tile2 Textura de baldosa oscura/alternada.
     */
    public SnakeArena(int playerNumber, float originPixelX, float originPixelY, int gridWidth, int gridHeight, Texture tile1, Texture tile2) {
        this.playerNumber = playerNumber;
        this.originPixelX = originPixelX;
        this.originPixelY = originPixelY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.tile1 = tile1;
        this.tile2 = tile2;

        // Configuración de punto de aparición simétrico
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

    /**
     * Metodo setNextDirection (dir)
     * Actualiza la dirección deseada para el próximo paso evitando giros directos de 180°.
     * Parámetro Direction dir Nueva dirección solicitada por el usuario.
     */
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


    /**
     * Metodo tick ()
     * Ejecuta un paso de simulación para la arena avanzando la serpiente, comprobando comida y gestionando colisiones.
     */
    public void tick() {
        currentDirection = nextDirection;
        snake.step(currentDirection);

        // 1. Verificación de recolección de comida (cabeza sobre el huevo/manzana)
        if (snake.getHead().getGridX() == food.getGridX() && snake.getHead().getGridY() == food.getGridY()) {
            snake.grow();
            score++;
            food.respawn(gridWidth, gridHeight, snake);
        }

        // 2. Verificación de muerte por choque contra límites o auto-mordedura
        if (snake.checkOutOfBounds(gridWidth, gridHeight) || snake.checkSelfCollision()) {
            respawnSnake();
        }
    }

    /**
     * Metodo respawnSnake ()
     * Reaparece a la serpiente en su posición base y reinicia el marcador de la ronda a 0.
     */
    public void respawnSnake() {
        snake.reset(initialSpawnX, initialSpawnY, initialDirection);
        currentDirection = initialDirection;
        nextDirection = initialDirection;
        score = 0; // Se reinicia el marcador de la ronda al morir
        food.respawn(gridWidth, gridHeight, snake);
    }

    /**
     * Metodo resetForNewRound ()
     * Reinicia por completo la arena para una nueva ronda (score a 0 y posición inicial).
     */
    public void resetForNewRound() {
        respawnSnake();
    }

    /**
     * Metodo render (batch)
     * Dibuja el fondo de la arena, la comida y la serpiente con sus desplazamientos en pantalla.
     * Parámetro SpriteBatch batch SpriteBatch para renderizado 2D.
     */
    public void render(SpriteBatch batch) {
        // 1. Dibujar baldosas alternadas del tablero
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
