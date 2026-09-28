package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.Collection;

/**
 * Representa el ítem de comida (huevo/manzana) que aparece en la cuadrícula.
 * Gestiona su posición, textura y lógica de reaparición aleatoria evitando superposiciones con la serpiente.
 */
public class Food implements Disposable {

    private int gridX;
    private int gridY;
    private final Texture texture;

    /**
     * Constructor que carga la textura de la comida.
     */
    public Food() {
        this.texture = new Texture("snake/egg.png");
    }

    /**
     * Reaparece la comida en una coordenada aleatoria dentro de los límites dados,
     * garantizando que no coincida con ninguna casilla ocupada por la serpiente.
     *
     * @param minX  Límite mínimo en X (inclusivo).
     * @param minY  Límite mínimo en Y (inclusivo).
     * @param maxX  Límite máximo en X (inclusivo).
     * @param maxY  Límite máximo en Y (inclusivo).
     * @param snake Serpiente a verificar para no superponerse.
     */
    public void respawn(int minX, int minY, int maxX, int maxY, Snake snake) {
        int attempts = 0;
        int newX;
        int newY;

        do {
            newX = MathUtils.random(minX, maxX);
            newY = MathUtils.random(minY, maxY);
            attempts++;
        } while (snake != null && snake.occupies(newX, newY) && attempts < 100);

        this.gridX = newX;
        this.gridY = newY;
    }

    /**
     * Sobrecarga de reaparición para tableros que inician en (0, 0).
     *
     * @param gridWidth  Ancho del tablero en casillas.
     * @param gridHeight Alto del tablero en casillas.
     * @param snake      Serpiente a verificar.
     */
    public void respawn(int gridWidth, int gridHeight, Snake snake) {
        respawn(0, 0, gridWidth - 1, gridHeight - 1, snake);
    }

    /**
     * Dibuja la comida en la cuadrícula usando el tamaño de baldosa indicado y desplazamiento de arena.
     *
     * @param batch    SpriteBatch para renderizado 2D.
     * @param tileSize Tamaño en píxeles de cada casilla.
     * @param offsetX  Desplazamiento horizontal en píxeles para la arena.
     * @param offsetY  Desplazamiento vertical en píxeles para la arena.
     */
    public void render(SpriteBatch batch, float tileSize, float offsetX, float offsetY) {
        batch.draw(texture, offsetX + gridX * tileSize, offsetY + gridY * tileSize, tileSize, tileSize);
    }

    /**
     * Dibuja la comida en la cuadrícula usando el tamaño de baldosa indicado en el origen base.
     *
     * @param batch    SpriteBatch para renderizado 2D.
     * @param tileSize Tamaño en píxeles de cada casilla.
     */
    public void render(SpriteBatch batch, float tileSize) {
        render(batch, tileSize, 0, 0);
    }


    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
