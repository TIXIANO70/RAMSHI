package io.github.some_example_name;

import com.badlogic.gdx.Game;
import io.github.some_example_name.screens.MainMenuScreen;

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
