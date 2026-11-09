package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class QuickdrawScreen extends BaseMinigameScreen{
    private int healthP1=3;
    private int healthP2=3;
    private final Texture heart;
    private final Texture empty_heart;
    private final Texture blue_shotgun;
    private final Texture red_shotgun;
    private float P1_X = 0f;
    private float P2_X = 600f;
    private float gunY = 180f;
    private float gunH = 128f;
    private float gunW = gunH;
    private float triggerInterval=1;
    private float triggerTimer;
    private boolean canShoot;
    private float reactionInterval=0.5f;
    private float reactionTimer;
    private boolean addReactionTimer=false;
    private boolean shooted;
    private int shootOption;
    private int heartY = 300;
    private int heartW = 32;
    private int heartH = heartW;
    private String[] P1_keys = {"W","A","S","D"};
    private String[] P2_keys = {"UP","LEFT","DOWN","RIGHT"};
    public QuickdrawScreen(Game game){
        super(game);
        this.heart = new Texture("quickdraw/heart.png");
        this.heart.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.empty_heart = new Texture("quickdraw/empty_heart.png");
        this.empty_heart.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.blue_shotgun = new Texture("quickdraw/blue_shotgun.png");
        this.blue_shotgun.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.red_shotgun = new Texture("quickdraw/red_shotgun.png");
        this.red_shotgun.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    }

    @Override
    protected float getRoundDuration() {
        return 10;
    }

    @Override
    protected void updateGame(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            shoot(1,1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            shoot(1,2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            shoot(1,3);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            shoot(1,4);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            shoot(2,1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            shoot(2,2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            shoot(2,3);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            shoot(2,4);
        }
        triggerTimer+=delta;
        if(addReactionTimer){
            reactionTimer+=delta;
        }
        if(reactionTimer>reactionInterval){
            addReactionTimer=false;
            reactionTimer=0;
            shooted = false;
        }
        if(triggerInterval<triggerTimer){
            triggerTimer-=triggerInterval;
            if(!canShoot){
                triggerInterval = 1+MathUtils.random(0.1f,2f);
                unlockTrigger();
            }
        }
    }
    private void takeHealth(int player){
        if(player==1 && !shooted){
            healthP1--;
            shooted = false;
        }else if(player ==2 && !shooted){
            healthP2--;
            shooted = false;
        }
    }
    private void shoot(int player, int key) {

        if(key==shootOption && canShoot){
            canShoot=false;
            if(player==1){
                takeHealth(2);
            }else{
                takeHealth(1);
            }
        }else{
            if(player==1){
                takeHealth(1);
            }else{
                takeHealth(2);
            }
        }
        if(!addReactionTimer){
            addReactionTimer=true;
            shooted = true;
        }
        if(healthP1==0 || healthP2==0){
            finishRound();
        }
    }

    private void unlockTrigger() {
        shootOption = MathUtils.random(1,4);
        canShoot=true;
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();

        batch.draw(blue_shotgun, P1_X, gunY, gunW, gunH);
        batch.draw(red_shotgun, P2_X, gunY, gunW, gunH);
        for(int i=0; i<healthP1;i++){
            batch.draw(heart, P1_X+i*48, heartY, heartW, heartH);
        }
        for(int i=0; i<healthP2;i++){
            batch.draw(heart, P2_X+i*48, heartY, heartW, heartH);
        }
        if(canShoot){
            font.draw(batch,P1_keys[shootOption-1],400,200);
            font.draw(batch,P2_keys[shootOption-1],400,250);
        }
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if(healthP1>healthP2){
            p1GlobalWins++;
            return "GANA JUGADOR 1";
        }else if(healthP1<healthP2){
            p2GlobalWins++;
            return "GANA JUGADOR 2!";
        }else{
            return "EMPATE";
        }
    }

    @Override
    protected void onRoundReset() {
        healthP1=3;
        healthP2=3;
        triggerInterval=1;
        triggerTimer=0;
    }

    @Override
    protected String getP1StatusText() {
        return "Health: "+healthP1;
    }

    @Override
    protected String getP2StatusText() {
        return "Health: "+healthP2;
    }

    @Override
    protected String getScoreSummary() {
        return "P1("+healthP1+"hp) vs P2("+healthP2+"hp)";
    }
    @Override
    public void dispose(){
        super.dispose();
        if(red_shotgun != null) red_shotgun.dispose();
        if(blue_shotgun != null) blue_shotgun.dispose();
        if(heart != null) heart.dispose();
        if(empty_heart != null) empty_heart.dispose();
    }
}
