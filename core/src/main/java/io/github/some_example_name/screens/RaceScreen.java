package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;


public class RaceScreen extends BaseMinigameScreen{
    protected float distancia_p1 = 0;
    protected float distancia_p2 = 0;

    public RaceScreen(Game game) {
        super(game);
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
        font.draw(batch, "RACE - WIP", 0f, 0f); //no entiendo que hacer
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if(distancia_p1>distancia_p2){
            p1GlobalWins++;
            return "P1 gana: "+distancia_p1+"m";
        }else if(distancia_p1<distancia_p2){
            p2GlobalWins++;
            return "P2 gana: "+distancia_p2+"m";
        }else{
            return "Empate";
        }
    }

    @Override
    protected void onRoundReset() {
        distancia_p1 = 0;
        distancia_p2 = 0;
    }

    @Override
    protected String getP1StatusText() {
        return "P1 ("+distancia_p1+")";
    }

    @Override
    protected String getP2StatusText() {
        return "P2 ("+distancia_p2+")";
    }

    @Override
    protected String getScoreSummary() {
        return "P1 ("+distancia_p1+") vs P2 ("+distancia_p2+")";
    }
}
