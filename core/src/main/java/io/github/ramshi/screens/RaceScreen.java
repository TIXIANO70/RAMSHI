package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class RaceScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final float START_SPEED = 5f;
    private static final float ACCELERATION = 5f;
    private static final float SPEED_INTERVAL = 1f;
    private static final float SPAWN_INTERVAL = 2f;
    private static final float GENERAL_SPEED = 32f;
    private static final int LANES = 3;
    private static final float LANE_WIDTH = 128f;
    private static final float TRACK_WIDTH = LANE_WIDTH * LANES;
    private static final float P1_X = 0f;
    private static final float P2_X = 432f;
    private static final float CAR_Y = 80f;
    private static final float CAR_ORIG_SIZE = 32f;
    private static final int[][] KEYS = {
        {Input.Keys.A, Input.Keys.D},
        {Input.Keys.LEFT, Input.Keys.RIGHT}
    };

    private static class Obstacle {
        float x, y, w, h;
        Rectangle rect;

        Obstacle(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.rect = new Rectangle(x, y, w, h);
        }
    }

    private final Texture pista = loadTexture("race/pista.png");
    private final Texture car1 = loadTexture("race/car1.png");
    private final Texture car2 = loadTexture("race/car2.png");
    private final Texture obstacle = loadTexture("race/obstacle.png");

    private final float scaleX = TRACK_WIDTH / pista.getWidth();
    private final float carSize = CAR_ORIG_SIZE * scaleX;
    private final ArrayList<Obstacle> obstaclesP1 = new ArrayList<Obstacle>();
    private final ArrayList<Obstacle> obstaclesP2 = new ArrayList<Obstacle>();
    private float distanciaP1 = 0;
    private float distanciaP2 = 0;
    private int laneP1 = 1;
    private int laneP2 = 1;
    private float speedP1 = START_SPEED;
    private float speedP2 = START_SPEED;
    private float spawnTimer = 0f;
    private float speedTimer = 0f;
    private float carX1;
    private float carX2;
    private Rectangle rectP1;
    private Rectangle rectP2;

    public RaceScreen(Game game) {
        super(game);
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(path);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    private float laneX(float trackX, int lane) {
        return trackX + CAR_ORIG_SIZE / 2f * scaleX + CAR_ORIG_SIZE * lane * scaleX;
    }

    @Override protected float getRoundDuration() { return ROUND_DURATION; }

    @Override
    protected void updateGame(float delta) {
        if (Gdx.input.isKeyJustPressed(KEYS[0][0])) laneP1 = MathUtils.clamp(laneP1 - 1, 0, LANES - 1);
        if (Gdx.input.isKeyJustPressed(KEYS[0][1])) laneP1 = MathUtils.clamp(laneP1 + 1, 0, LANES - 1);
        if (Gdx.input.isKeyJustPressed(KEYS[1][0])) laneP2 = MathUtils.clamp(laneP2 - 1, 0, LANES - 1);
        if (Gdx.input.isKeyJustPressed(KEYS[1][1])) laneP2 = MathUtils.clamp(laneP2 + 1, 0, LANES - 1);
        carX1 = laneX(P1_X, laneP1);
        carX2 = laneX(P2_X, laneP2);

        speedTimer += delta;
        spawnTimer += delta;
        if (speedTimer >= SPEED_INTERVAL) {
            speedP1 += ACCELERATION;
            speedP2 += ACCELERATION;
            distanciaP1 += speedP1;
            distanciaP2 += speedP2;
            speedTimer -= SPEED_INTERVAL;
        }
        if (spawnTimer >= SPAWN_INTERVAL) {
            spawnObstacle(P1_X, obstaclesP1);
            spawnObstacle(P2_X, obstaclesP2);
            spawnTimer -= SPAWN_INTERVAL;
        }
        speedP1 = updateObstacles(obstaclesP1, rectP1, speedP1, delta);
        speedP2 = updateObstacles(obstaclesP2, rectP2, speedP2, delta);
    }

    private void spawnObstacle(float trackX, ArrayList<Obstacle> list) {
        float x = laneX(trackX, MathUtils.random(0, LANES - 1));
        list.add(new Obstacle(x, VIRTUAL_HEIGHT, carSize, carSize));
    }

    private float updateObstacles(ArrayList<Obstacle> list, Rectangle car, float speed, float delta) {
        float fall = speed * delta * GENERAL_SPEED;
        for (int i = list.size() - 1; i >= 0; i--) {
            Obstacle obs = list.get(i);
            obs.y -= fall;
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);
            if (car.overlaps(obs.rect)) speed = 0f;
            if (obs.y + obs.h < 0) list.remove(i);
        }
        return speed;
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        float pistaHeight = pista.getHeight();
        for (float y = 0; y < VIRTUAL_HEIGHT; y += pistaHeight) {
            batch.draw(pista, P1_X, y, TRACK_WIDTH, pistaHeight);
            batch.draw(pista, P2_X, y, TRACK_WIDTH, pistaHeight);
        }

        batch.draw(car1, carX1, CAR_Y, carSize, carSize);
        rectP1 = new Rectangle(carX1, CAR_Y, carSize, carSize);
        batch.draw(car2, carX2, CAR_Y, carSize, carSize);
        rectP2 = new Rectangle(carX2, CAR_Y, carSize, carSize);

        font.draw(batch, "Velocidad: " + speedP1, P1_X, 30);
        font.draw(batch, "Velocidad: " + speedP2, P2_X, 30);

        for (Obstacle obs : obstaclesP1) batch.draw(obstacle, obs.x, obs.y, obs.w, obs.h);
        for (Obstacle obs : obstaclesP2) batch.draw(obstacle, obs.x, obs.y, obs.w, obs.h);
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (distanciaP1 > distanciaP2) {
            p1GlobalWins++;
            return "¡JUGADOR 1 GANA LA RONDA!";
        } else if (distanciaP1 < distanciaP2) {
            p2GlobalWins++;
            return "¡JUGADOR 2 GANA LA RONDA!";
        }
        return "¡EMPATE EN LA RONDA!";
    }

    @Override
    protected void onRoundReset() {
        distanciaP1 = 0;
        distanciaP2 = 0;
        obstaclesP1.clear();
        obstaclesP2.clear();
        laneP1 = 1;
        laneP2 = 1;
        speedP1 = 0;
        speedP2 = 0;
    }

    @Override protected String getP1StatusText() { return "P1: " + (int) distanciaP1 + " m (Wins: " + p1GlobalWins + ")"; }
    @Override protected String getP2StatusText() { return "P2: " + (int) distanciaP2 + " m (Wins: " + p2GlobalWins + ")"; }
    @Override protected String getScoreSummary() { return "Distancia: P1 (" + (int) distanciaP1 + "m) vs P2 (" + (int) distanciaP2 + "m)"; }

    @Override
    public void dispose() {
        super.dispose();
        pista.dispose();
        car1.dispose();
        car2.dispose();
        obstacle.dispose();
    }
}
