package io.github.some_example_name;

import com.badlogic.gdx.Game;
import io.github.some_example_name.screens.LuaScreen;

public class Main extends Game {

    @Override
    public void create() {
        setScreen(new LuaScreen(this));
    }
}
