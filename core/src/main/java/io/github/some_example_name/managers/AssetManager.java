package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Centraliza os assets de Lua e Marte. */
public class AssetManager {

    public static final String PLAYER = "astronauta.png";
    public static final String LUA_BACKGROUND = "lua_background.png";
    public static final String LASER = "laser.png";
    public static final String LUNAR_BASE = "baselunar.png";
    public static final String LUA_TILE = "tilemap_lua.png";
    public static final String MARS_TILE = "tilemap_marte.png";
    public static final String FOOD = "comida.png";
    public static final String O2_TANK = "O2.png";
    public static final String ICE = "gelo.png";
    public static final String ORE = "minerio.png";
    public static final String MINE = "mina.png";
    public static final String AMERICAN = "americano.png";
    public static final String ALIEN = "aliens.png";
    public static final String BOSS_MARS = "bossmarte.png";
    public static final String TRUMP = "trump.png";
    public static final String RIFLE = "ak47.png";
    public static final String PORTAL = "portal.png";
    public static final String RED_KEY = "chave.png";
    public static final String GREEN_KEY = "chave_verde.png";

    private Texture playerTexture;
    private Texture luaBackgroundTexture;
    private Texture laserTexture;
    private Texture lunarBaseTexture;
    private Texture luaTileTexture;
    private Texture marsTileTexture;
    private Texture foodTexture;
    private Texture o2TankTexture;
    private Texture iceTexture;
    private Texture oreTexture;
    private Texture mineTexture;
    private Texture americanTexture;
    private Texture alienTexture;
    private Texture bossMarsTexture;
    private Texture trumpTexture;
    private Texture rifleTexture;
    private Texture portalTexture;
    private Texture redKeyTexture;
    private Texture greenKeyTexture;

    private boolean playerFallback;

    public void load() {
        playerTexture = loadAsset(PLAYER, Color.CYAN);
        playerFallback = !assetExists(PLAYER);
        luaBackgroundTexture = loadAsset(LUA_BACKGROUND, new Color(0.08f, 0.08f, 0.13f, 1f));
        laserTexture = loadAsset(LASER, new Color(1f, 0.90f, 0.10f, 1f));
        lunarBaseTexture = loadAsset(LUNAR_BASE, new Color(0.45f, 0.45f, 0.50f, 1f));
        luaTileTexture = loadAsset(LUA_TILE, new Color(0.20f, 0.20f, 0.23f, 1f));
        marsTileTexture = loadAsset(MARS_TILE, new Color(0.72f, 0.25f, 0.06f, 1f));
        foodTexture = loadAsset(FOOD, new Color(1f, 0.55f, 0f, 1f));
        o2TankTexture = loadAsset(O2_TANK, new Color(0.70f, 0.78f, 0.86f, 1f));
        iceTexture = loadAsset(ICE, new Color(0.20f, 0.55f, 1f, 1f));
        oreTexture = loadAsset(ORE, new Color(0.15f, 0.95f, 0.25f, 1f));
        mineTexture = loadAsset(MINE, new Color(0.28f, 0.10f, 0.035f, 1f));
        americanTexture = loadAsset(AMERICAN, new Color(0.90f, 0.08f, 0.08f, 1f));
        alienTexture = loadAsset(ALIEN, new Color(0.45f, 0.95f, 0.35f, 1f));
        bossMarsTexture = loadAsset(BOSS_MARS, new Color(0.35f, 0.85f, 0.25f, 1f));
        trumpTexture = loadAsset(TRUMP, new Color(1f, 0.45f, 0.05f, 1f));
        rifleTexture = loadAsset(RIFLE, new Color(0.10f, 0.10f, 0.10f, 1f));
        portalTexture = loadAsset(PORTAL, new Color(0.15f, 0.85f, 1f, 1f));
        redKeyTexture = loadAsset(RED_KEY, Color.RED);
        greenKeyTexture = loadAsset(GREEN_KEY, Color.GREEN);
    }

    private Texture loadAsset(String fileName, Color fallbackColor) {
        String texturePath = "textures/" + fileName;
        if (Gdx.files.internal(texturePath).exists()) {
            return new Texture(Gdx.files.internal(texturePath));
        }
        if (Gdx.files.internal(fileName).exists()) {
            return new Texture(Gdx.files.internal(fileName));
        }
        return createFallbackTexture(fallbackColor);
    }

    private boolean assetExists(String fileName) {
        return Gdx.files.internal("textures/" + fileName).exists()
                || Gdx.files.internal(fileName).exists();
    }

    private Texture createFallbackTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public Texture getPlayerTexture() { return playerTexture; }
    public Texture getLuaBackgroundTexture() { return luaBackgroundTexture; }
    public Texture getLaserTexture() { return laserTexture; }
    public Texture getLunarBaseTexture() { return lunarBaseTexture; }
    public Texture getLuaTileTexture() { return luaTileTexture; }
    public Texture getMarsTileTexture() { return marsTileTexture; }
    public Texture getFoodTexture() { return foodTexture; }
    public Texture getO2TankTexture() { return o2TankTexture; }
    public Texture getIceTexture() { return iceTexture; }
    public Texture getOreTexture() { return oreTexture; }
    public Texture getMineTexture() { return mineTexture; }
    public Texture getAmericanTexture() { return americanTexture; }
    public Texture getAlienTexture() { return alienTexture; }
    public Texture getBossMarsTexture() { return bossMarsTexture; }
    public Texture getTrumpTexture() { return trumpTexture; }
    public Texture getRifleTexture() { return rifleTexture; }
    public Texture getPortalTexture() { return portalTexture; }
    public Texture getRedKeyTexture() { return redKeyTexture; }
    public Texture getGreenKeyTexture() { return greenKeyTexture; }
    public boolean isPlayerFallback() { return playerFallback; }

    public void dispose() {
        disposeTexture(playerTexture);
        disposeTexture(luaBackgroundTexture);
        disposeTexture(laserTexture);
        disposeTexture(lunarBaseTexture);
        disposeTexture(luaTileTexture);
        disposeTexture(marsTileTexture);
        disposeTexture(foodTexture);
        disposeTexture(o2TankTexture);
        disposeTexture(iceTexture);
        disposeTexture(oreTexture);
        disposeTexture(mineTexture);
        disposeTexture(americanTexture);
        disposeTexture(alienTexture);
        disposeTexture(bossMarsTexture);
        disposeTexture(trumpTexture);
        disposeTexture(rifleTexture);
        disposeTexture(portalTexture);
        disposeTexture(redKeyTexture);
        disposeTexture(greenKeyTexture);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
