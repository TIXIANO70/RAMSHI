package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.Collection;


public class Food implements Disposable {

    private int gridX;
    private int gridY;
    private final Texture texture;
    public Food() {
        this.texture = new Texture("snake/egg.png");
    }

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

    public void respawn(int gridWidth, int gridHeight, Snake snake) {
        respawn(0, 0, gridWidth - 1, gridHeight - 1, snake);
    }

    public void render(SpriteBatch batch, float tileSize, float offsetX, float offsetY) {
        batch.draw(texture, offsetX + gridX * tileSize, offsetY + gridY * tileSize, tileSize, tileSize);
    }

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
