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

    private Texture pista;
    private Texture car1;
    private Texture car2;
    private static final float ROAD_START_ORIG = 16f;
    private static final float LANE_W_ORIG = 32f;
    private static final float LANE_WIDTH = 128f;
    private static final float TRACK_WIDTH = LANE_WIDTH * 3f;
    private static final float P1_X = 0f;
    private static final float P2_X = 432f;

    private static final float CAR_ORIG_SIZE = 32f;

    private int laneP1 = 1;
    private int laneP2 = 1;
    private float carY = 80f;
    private float acceleration = 5f;
    private float speedP1 = 5f;
    private float speedP2 = 5f;

    private ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private static class Obstacle {
        float x, y, w, h;
        Rectangle rect;

        public Obstacle(float x, float y, float w, float h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.rect = new Rectangle(x, y, w, h);
        }
    }
    private float spawnTimer = 0f;
    private float spawnInterval = 1f;

    private float scaleX;
    private float carW;
    private float carH;
    private float obsW;
    private float obsH;

    public RaceScreen(Game game) {
        super(game);
        this.pista = new Texture("race/pista.png");
        this.pista.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.car1 = new Texture("race/car1.png");
        this.car1.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.car2 = new Texture("race/car2.png");
        this.car2.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        scaleX = TRACK_WIDTH / pista.getWidth();
        carW = CAR_ORIG_SIZE * scaleX;
        carH = CAR_ORIG_SIZE * scaleX;

        obsW = carW;
        obsH = carH;
    }

    @Override
    protected float getRoundDuration() {
        return 30f;
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

        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer -= spawnInterval;
        }

    }
    protected void updateSecond(){
        speedP1+=acceleration;
        speedP2+=acceleration;
        distancia_p1+=speedP1;
        distancia_p2+=speedP2;
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

        float carX1 = P1_X+16*scaleX+32*laneP1*scaleX;
        float carX2 = P2_X+16*scaleX+32*laneP2*scaleX;
        batch.draw(car1, carX1, carY, carW, carH);
        Rectangle rectP1 = new Rectangle(carX1, carY, carW, carH);

        batch.draw(car2, carX2, carY, carW, carH);
        Rectangle rectP2 = new Rectangle(carX2, carY, carW, carH);


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
        laneP1 = 1;
        laneP2 = 1;
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
    }
    public void spawnObstacle(int initial_x){
        int lane = MathUtils.random(0,2);
        float rectX =16*scaleX+initial_x+scaleX*lane*32;
        Rectangle rect = new Rectangle(rectX, VIRTUAL_HEIGHT, 32*scaleX, 32*scaleX);

    }
    public void updateObstacle(float delta, Rectangle rectP1, Rectangle rectP2){
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);

            obs.y -= speedP1 * delta;
            obs.rect.set(obs.x, obs.y, obs.w, obs.h);

            if (rectP1.overlaps(obs.rect)) {
                speedP1 = 0f;
            }
            if (rectP2.overlaps(obs.rect)) {
                speedP2 = 0f;
            }

            if (obs.y + obs.h < 0) {
                obstacles.remove(i);
            }
        }
    }
}

