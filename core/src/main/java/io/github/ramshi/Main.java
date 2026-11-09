package io.github.ramshi;

import com.badlogic.gdx.Game;
import io.github.ramshi.screens.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
