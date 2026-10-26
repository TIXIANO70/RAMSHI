package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RaceScreen extends BaseMinigameScreen{
    protected float distancia_p1 = 0;
    protected float distancia_p2 = 0;
    private Texture pista;

    private static final float LANE_WIDTH = 128f;
    private static final float TRACK_WIDTH = LANE_WIDTH * 3f;
    private static final float P1_X = 24f;
    private static final float P2_X = 424f;

    public RaceScreen(Game game) {
        super(game);
        this.pista = new Texture("race/pista.png");
        this.pista.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    }

    @Override
    protected float getRoundDuration() {
        return 30f;
    }

    @Override
    protected void updateGame(float delta) {

    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        float pistaHeight = pista.getHeight();
        // Dibujar pista tileada para P1 (izquierda)
        for (float y = 0; y < VIRTUAL_HEIGHT; y += pistaHeight) {
            batch.draw(pista, P1_X, y, TRACK_WIDTH, pistaHeight);
        }
        // Dibujar pista tileada para P2 (derecha)
        for (float y = 0; y < VIRTUAL_HEIGHT; y += pistaHeight) {
            batch.draw(pista, P2_X, y, TRACK_WIDTH, pistaHeight);
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
        if (pista != null) {
            pista.dispose();
        }
    }
}
