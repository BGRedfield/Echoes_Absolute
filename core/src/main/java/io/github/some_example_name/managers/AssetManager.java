package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class AssetManager {

    public static final String PLAYER = "textures/astronauta.png";
    public static final String LUA_BACKGROUND = "textures/lua_background.png";
    public static final String LASER = "textures/laser.png";
    public static final String LUNAR_BASE = "textures/baselunar.png";
    public static final String LUA_TILE = "textures/tileset_lua.png";
    public static final String FOOD = "textures/comida.png";
    public static final String O2_TANK = "textures/O2.png";
    public static final String ICE = "textures/gelo.png";
    public static final String AMERICAN = "textures/americano.png";
    public static final String TRUMP = "textures/trump.png";
    public static final String RIFLE = "textures/ak47.png";
    public static final String PORTAL = "textures/portal.png";

    private Texture playerTexture;
    private Texture luaBackgroundTexture;
    private Texture laserTexture;
    private Texture lunarBaseTexture;
    private Texture luaTileTexture;
    private Texture foodTexture;
    private Texture o2TankTexture;
    private Texture iceTexture;
    private Texture americanTexture;
    private Texture trumpTexture;
    private Texture rifleTexture;
    private Texture portalTexture;

    private boolean playerFallback;

    public void load() {
        playerTexture = loadOrFallback(PLAYER, Color.CYAN);
        playerFallback = !Gdx.files.internal(PLAYER).exists();

        luaBackgroundTexture = loadOrFallback(
                LUA_BACKGROUND,
                new Color(0.08f, 0.08f, 0.13f, 1f)
        );

        // Projétil do jogador: amarelo quando o asset ainda não existir.
        laserTexture = loadOrFallback(
                LASER,
                new Color(1f, 0.90f, 0.10f, 1f)
        );

        lunarBaseTexture = loadOrFallback(
                LUNAR_BASE,
                new Color(0.45f, 0.45f, 0.50f, 1f)
        );

        luaTileTexture = loadOrFallback(
                LUA_TILE,
                new Color(0.20f, 0.20f, 0.23f, 1f)
        );

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

        americanTexture = loadOrFallback(
                AMERICAN,
                new Color(0.90f, 0.08f, 0.08f, 1f)
        );

        trumpTexture = loadOrFallback(
                TRUMP,
                new Color(1f, 0.45f, 0.05f, 1f)
        );

        rifleTexture = loadOrFallback(
                RIFLE,
                new Color(0.10f, 0.10f, 0.10f, 1f)
        );

        portalTexture = loadOrFallback(
                PORTAL,
                new Color(0.15f, 0.85f, 1f, 1f)
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

    public Texture getAmericanTexture() {
        return americanTexture;
    }

    public Texture getTrumpTexture() {
        return trumpTexture;
    }

    public Texture getRifleTexture() {
        return rifleTexture;
    }

    public Texture getPortalTexture() {
        return portalTexture;
    }

    public boolean isPlayerFallback() {
        return playerFallback;
    }

    public void dispose() {
        disposeTexture(playerTexture);
        disposeTexture(luaBackgroundTexture);
        disposeTexture(laserTexture);
        disposeTexture(lunarBaseTexture);
        disposeTexture(luaTileTexture);
        disposeTexture(foodTexture);
        disposeTexture(o2TankTexture);
        disposeTexture(iceTexture);
        disposeTexture(americanTexture);
        disposeTexture(trumpTexture);
        disposeTexture(rifleTexture);
        disposeTexture(portalTexture);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
