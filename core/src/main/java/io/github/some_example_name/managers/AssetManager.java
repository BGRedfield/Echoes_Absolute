package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class AssetManager {

    public static final String PLAYER = "textures/player.png";
    public static final String LUA_BACKGROUND = "textures/lua_background.png";

    private Texture playerTexture;
    private Texture luaBackgroundTexture;
    private boolean playerFallback;

    public void load() {
        if (Gdx.files.internal(PLAYER).exists()) {
            playerTexture = new Texture(Gdx.files.internal(PLAYER));
            playerFallback = false;
        } else {
            playerTexture = createFallbackTexture(Color.CYAN);
            playerFallback = true;
        }

        if (Gdx.files.internal(LUA_BACKGROUND).exists()) {
            luaBackgroundTexture = new Texture(Gdx.files.internal(LUA_BACKGROUND));
        } else {
            luaBackgroundTexture = createFallbackTexture(new Color(0.08f, 0.08f, 0.13f, 1f));
        }
    }

    private Texture createFallbackTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public Texture getPlayerTexture() {
        return playerTexture;
    }

    public Texture getLuaBackgroundTexture() {
        return luaBackgroundTexture;
    }

    public boolean isPlayerFallback() {
        return playerFallback;
    }

    public void dispose() {
        if (playerTexture != null) {
            playerTexture.dispose();
        }
        if (luaBackgroundTexture != null) {
            luaBackgroundTexture.dispose();
        }
    }
}
