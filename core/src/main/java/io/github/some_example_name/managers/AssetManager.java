package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class AssetManager {

    public static final String PLAYER = "textures/player.png";
    public static final String LUA_BACKGROUND = "textures/lua_background.png";
    public static final String LASER = "textures/laser.png";
    public static final String LUNAR_BASE = "textures/lunar_base.png";

    private Texture playerTexture;
    private Texture luaBackgroundTexture;
    private Texture laserTexture;
    private Texture lunarBaseTexture;

    private boolean playerFallback;

    public void load() {
        playerTexture = loadOrFallback(PLAYER, Color.CYAN);
        playerFallback = !Gdx.files.internal(PLAYER).exists();

        luaBackgroundTexture = loadOrFallback(
                LUA_BACKGROUND,
                new Color(0.08f, 0.08f, 0.13f, 1f)
        );

        laserTexture = loadOrFallback(
                LASER,
                new Color(0.95f, 0.95f, 1f, 1f)
        );

        lunarBaseTexture = loadOrFallback(
                LUNAR_BASE,
                new Color(0.45f, 0.45f, 0.50f, 1f)
        );
    }

    private Texture loadOrFallback(String path, Color fallbackColor) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        }

        return createFallbackTexture(fallbackColor);
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

    public Texture getLaserTexture() {
        return laserTexture;
    }

    public Texture getLunarBaseTexture() {
        return lunarBaseTexture;
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
        if (laserTexture != null) {
            laserTexture.dispose();
        }
        if (lunarBaseTexture != null) {
            lunarBaseTexture.dispose();
        }
    }
}
