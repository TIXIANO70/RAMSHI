package io.github.ramshi.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class QuickdrawScreen extends BaseMinigameScreen {

    private static final float ROUND_DURATION = 10f;
    private static final int MAX_HEALTH = 3;
    private static final float FIRST_TRIGGER_INTERVAL = 1f;
    private static final float TRIGGER_INTERVAL_BASE = 1f;
    private static final float TRIGGER_RANDOM_MIN = 0.1f;
    private static final float TRIGGER_RANDOM_MAX = 2f;
    private static final float REACTION_INTERVAL = 0.5f;
    private static final float P1_X = 0f;
    private static final float P2_X = 600f;
    private static final float GUN_Y = 180f;
    private static final float GUN_SIZE = 128f;
    private static final float HEART_Y = 300f;
    private static final float HEART_SIZE = 32f;
    private static final float HEART_GAP = 48f;
    private static final int[][] KEYS = {
        {Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D},
        {Input.Keys.UP, Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT}
    };
    private static final String[] P1_KEY_NAMES = {"W", "A", "S", "D"};
    private static final String[] P2_KEY_NAMES = {"UP", "LEFT", "DOWN", "RIGHT"};

    private final Texture heart = loadTexture("quickdraw/heart.png");
    private final Texture emptyHeart = loadTexture("quickdraw/empty_heart.png");
    private final Texture blueShotgun = loadTexture("quickdraw/blue_shotgun.png");
    private final Texture redShotgun = loadTexture("quickdraw/red_shotgun.png");

    private int healthP1 = MAX_HEALTH;
    private int healthP2 = MAX_HEALTH;
    private float triggerInterval = FIRST_TRIGGER_INTERVAL;
    private float triggerTimer;
    private boolean canShoot;
    private float reactionTimer;
    private boolean addReactionTimer = false;
    private boolean shooted;
    private int shootOption;

    public QuickdrawScreen(Game game) {
        super(game);
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(path);
        t.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return t;
    }

    @Override protected float getRoundDuration() { return ROUND_DURATION; }

    @Override
    protected void updateGame(float delta) {
        for (int p = 0; p < 2; p++) {
            for (int k = 0; k < 4; k++) {
                if (Gdx.input.isKeyJustPressed(KEYS[p][k])) shoot(p + 1, k + 1);
            }
        }
        triggerTimer += delta;
        if (addReactionTimer) {
            reactionTimer += delta;
        }
        if (reactionTimer > REACTION_INTERVAL) {
            addReactionTimer = false;
            reactionTimer = 0;
            shooted = false;
        }
        if (triggerInterval < triggerTimer) {
            triggerTimer -= triggerInterval;
            if (!canShoot) {
                triggerInterval = TRIGGER_INTERVAL_BASE + MathUtils.random(TRIGGER_RANDOM_MIN, TRIGGER_RANDOM_MAX);
                unlockTrigger();
            }
        }
    }

    private void takeHealth(int player) {
        if (player == 1 && !shooted) {
            healthP1--;
            shooted = false;
        } else if (player == 2 && !shooted) {
            healthP2--;
            shooted = false;
        }
    }

    private void shoot(int player, int key) {
        if (key == shootOption && canShoot) {
            canShoot = false;
            takeHealth(player == 1 ? 2 : 1);
        } else {
            takeHealth(player);
        }
        if (!addReactionTimer) {
            addReactionTimer = true;
            shooted = true;
        }
        if (healthP1 == 0 || healthP2 == 0) {
            finishRound();
        }
    }

    private void unlockTrigger() {
        shootOption = MathUtils.random(1, 4);
        canShoot = true;
    }

    @Override
    protected void drawGame(SpriteBatch batch) {
        batch.begin();
        batch.draw(blueShotgun, P1_X, GUN_Y, GUN_SIZE, GUN_SIZE);
        batch.draw(redShotgun, P2_X, GUN_Y, GUN_SIZE, GUN_SIZE);
        for (int i = 0; i < healthP1; i++) {
            batch.draw(heart, P1_X + i * HEART_GAP, HEART_Y, HEART_SIZE, HEART_SIZE);
        }
        for (int i = 0; i < healthP2; i++) {
            batch.draw(heart, P2_X + i * HEART_GAP, HEART_Y, HEART_SIZE, HEART_SIZE);
        }
        if (canShoot) {
            font.draw(batch, P1_KEY_NAMES[shootOption - 1], 400, 200);
            font.draw(batch, P2_KEY_NAMES[shootOption - 1], 400, 250);
        }
        batch.end();
    }

    @Override
    protected String determineRoundWinner() {
        if (healthP1 > healthP2) {
            p1GlobalWins++;
            return "GANA JUGADOR 1";
        } else if (healthP1 < healthP2) {
            p2GlobalWins++;
            return "GANA JUGADOR 2!";
        }
        return "EMPATE";
    }

    @Override
    protected void onRoundReset() {
        healthP1 = MAX_HEALTH;
        healthP2 = MAX_HEALTH;
        triggerInterval = FIRST_TRIGGER_INTERVAL;
        triggerTimer = 0;
    }

    @Override protected String getP1StatusText() { return "Health: " + healthP1; }
    @Override protected String getP2StatusText() { return "Health: " + healthP2; }
    @Override protected String getScoreSummary() { return "P1(" + healthP1 + "hp) vs P2(" + healthP2 + "hp)"; }

    @Override
    public void dispose() {
        super.dispose();
        redShotgun.dispose();
        blueShotgun.dispose();
        heart.dispose();
        emptyHeart.dispose();
    }
}
