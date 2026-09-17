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
    public static final String LUA_TILE = "textures/lua_tile.png";
    public static final String FOOD = "textures/comida.png";
    public static final String O2_TANK = "textures/tanquedeO2.png";
    public static final String ICE = "textures/gelo.png";

    private Texture playerTexture;
    private Texture luaBackgroundTexture;
    private Texture laserTexture;
    private Texture lunarBaseTexture;
    private Texture luaTileTexture;
    private Texture foodTexture;
    private Texture o2TankTexture;
    private Texture iceTexture;

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

        // Tile provisório cinza: quando lua_tile.png existir,
        // ele passa automaticamente a ser usado no chão.
        luaTileTexture = loadOrFallback(
                LUA_TILE,
                new Color(0.20f, 0.20f, 0.23f, 1f)
        );

        // Recursos da Lua: cada um tem seu próprio fallback.
        foodTexture = loadOrFallback(
                FOOD,
                new Color(1f, 0.55f, 0f, 1f)
        );

        o2TankTexture = loadOrFallback(
                O2_TANK,
                new Color(0.70f, 0.78f, 0.86f, 1f)
        );

        iceTexture = loadOrFallback(
                ICE,
                new Color(0.20f, 0.55f, 1f, 1f)
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

    public Texture getLuaTileTexture() {
        return luaTileTexture;
    }

    public Texture getFoodTexture() {
        return foodTexture;
    }

    public Texture getO2TankTexture() {
        return o2TankTexture;
    }

    public Texture getIceTexture() {
        return iceTexture;
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
        if (luaTileTexture != null) {
            luaTileTexture.dispose();
        }
        if (foodTexture != null) {
            foodTexture.dispose();
        }
        if (o2TankTexture != null) {
            o2TankTexture.dispose();
        }
        if (iceTexture != null) {
            iceTexture.dispose();
        }
    }
}
