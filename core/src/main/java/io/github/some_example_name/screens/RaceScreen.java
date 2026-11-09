package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import com.badlogic.gdx.math.Intersector;
public class RaceScreen extends BaseMinigameScreen{
    protected float distancia_p1 = 0;
    protected float distancia_p2 = 0;

    private final Texture pista;
    private final Texture car1;
    private final Texture car2;
    private final Texture obstacle;
    private static final float ROAD_START_ORIG = 16f;
    private static final float LANE_W_ORIG = 32f;
    private static final float LANE_WIDTH = 128f;
    private static final float TRACK_WIDTH = LANE_WIDTH * 3f;
    private static final float P1_X = 0f;
    private static final float P2_X = 432f;

    private static final float CAR_ORIG_SIZE = 32f;

    private int laneP1 = 1;
    private int laneP2 = 1;
    private final float carY = 80f;
    private final float acceleration = 5f;
    private float speedP1 = 5f;
    private float speedP2 = 5f;

    private ArrayList<Obstacle> obstaclesP1 = new ArrayList<Obstacle>();
    private ArrayList<Obstacle> obstaclesP2 = new ArrayList<Obstacle>();
    private static class Obstacle {
        float x, y, w, h;
        Rectangle rect;

        public Obstacle(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.rect = new Rectangle(x, y, w, h);
        }
    }
    private float spawnTimer = 0f;
    private float speedTimer = 0f;
    private float spawnInterval = 2f;
    private float speedInterval = 1f;
    private float scaleX;
    private final float carW;
    private final float carH;
    private final float obsW;
    private final float obsH;
    private float carX1;
    private float carX2;
    private Rectangle rectP1;
    private Rectangle rectP2;
    public RaceScreen(Game game) {
        super(game);
        this.pista = new Texture("race/pista.png");
        this.pista.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.car1 = new Texture("race/car1.png");
        this.car1.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.car2 = new Texture("race/car2.png");
        this.car2.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.obstacle = new Texture("race/obstacle.png");
        this.obstacle.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        scaleX = TRACK_WIDTH / pista.getWidth();
        carW = CAR_ORIG_SIZE * scaleX;
        carH = CAR_ORIG_SIZE * scaleX;

        obsW = carW;
        obsH = carH;
    }

    @Override
    protected float getRoundDuration() {
        return 10f;
    }

    @Override
    protected void updateGame(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            laneP1 = MathUtils.clamp(laneP1 - 1, 0, 2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            laneP1 = MathUtils.clamp(laneP1 + 1, 0, 2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            laneP2 = MathUtils.clamp(laneP2 - 1, 0, 2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            laneP2 = MathUtils.clamp(laneP2 + 1, 0, 2);
        }
        carX1 = P1_X+16*scaleX+32*laneP1*scaleX;
        carX2 = P2_X+16*scaleX+32*laneP2*scaleX;

        speedTimer += delta;
        spawnTimer += delta;
        if(speedTimer>=speedInterval){
            speedP1+=acceleration;
            speedP2+=acceleration;
            distancia_p1+=speedP1;
            distancia_p2+=speedP2;
            speedTimer-=speedInterval;
        }
        if (spawnTimer >= spawnInterval) {
            spawnObstacle((int) P1_X, obstaclesP1);
            spawnObstacle((int) P2_X, obstaclesP2);
            spawnTimer -= spawnInterval;
        }
        updateObstacle(delta, rectP1, rectP2);
    }
    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        float pistaHeight = pista.getHeight();
        for (float y = 0; y < VIRTUAL_HEIGHT; y += pistaHeight) {
            batch.draw(pista, 0, y, TRACK_WIDTH, pistaHeight);
        }
        for (float y = 0; y < VIRTUAL_HEIGHT; y += pistaHeight) {
            batch.draw(pista, 432, y, TRACK_WIDTH, pistaHeight);
        }

        batch.draw(car1, carX1, carY, carW, carH);
        rectP1 = new Rectangle(carX1, carY, carW, carH);

        batch.draw(car2, carX2, carY, carW, carH);
        rectP2 = new Rectangle(carX2, carY, carW, carH);

        font.draw(batch, "Velocidad: "+speedP1, P1_X,30);
        font.draw(batch, "Velocidad: "+speedP2, P2_X,30);

        for(Obstacle obs : obstaclesP1){
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);
            batch.draw(obstacle, obs.x, obs.y, obs.w, obs.h);
        }
        for(Obstacle obs : obstaclesP2){
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);
            batch.draw(obstacle, obs.x, obs.y, obs.w, obs.h);
        }
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if(distancia_p1>distancia_p2){
            p1GlobalWins++;
            return "¡JUGADOR 1 GANA LA RONDA!";
        }else if(distancia_p1<distancia_p2){
            p2GlobalWins++;
            return "¡JUGADOR 2 GANA LA RONDA!";
        }else{
            return "¡EMPATE EN LA RONDA!";
        }
    }

    @Override
    protected void onRoundReset() {
        distancia_p1 = 0;
        distancia_p2 = 0;
        obstaclesP1.clear();
        obstaclesP2.clear();
        laneP1 = 1;
        laneP2 = 1;
        speedP1=0;
        speedP2=0;
    }

    @Override
    protected String getP1StatusText() {
        return "P1: " + (int)distancia_p1 + " m (Wins: " + p1GlobalWins + ")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2: " + (int)distancia_p2 + " m (Wins: " + p2GlobalWins + ")";
    }

    @Override
    protected String getScoreSummary() {
        return "Distancia: P1 (" + (int)distancia_p1 + "m) vs P2 (" + (int)distancia_p2 + "m)";
    }

    @Override
    public void dispose(){
        super.dispose();
        if (pista != null) pista.dispose();
        if (car1 != null) car1.dispose();
        if (car2 != null) car2.dispose();
        if(obstacle != null) obstacle.dispose();
    }
    public void spawnObstacle(int initial_x, ArrayList<Obstacle> obstaclesList){
        int lane=0;
        float rectX=0;
        Obstacle obs = new Obstacle(0,0,0,0);
        lane = MathUtils.random(0,2);
        rectX =16*scaleX+initial_x+scaleX*lane*32;
        obs = new Obstacle(rectX, VIRTUAL_HEIGHT, 32*scaleX, 32*scaleX);
        obstaclesList.add(obs);
    }
    public void updateObstacle(float delta, Rectangle rectP1, Rectangle rectP2){
        for (int i = obstaclesP1.size() - 1; i >= 0; i--) {
            Obstacle obs = obstaclesP1.get(i);

            obs.y -= speedP1 * delta*32;
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);

            if (rectP1.overlaps(obs.rect)) {
                speedP1 = 0f;
            }

            if (obs.y + obs.h < 0) {
                obstaclesP1.remove(i);
            }
        }
        for (int i = obstaclesP2.size() - 1; i >= 0; i--) {
            Obstacle obs = obstaclesP2.get(i);

            obs.y -= speedP2 * delta*32;
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);

            if (rectP2.overlaps(obs.rect)) {
                speedP2 = 0f;
            }

            if (obs.y + obs.h < 0) {
                obstaclesP2.remove(i);
            }
        }
    }
}

