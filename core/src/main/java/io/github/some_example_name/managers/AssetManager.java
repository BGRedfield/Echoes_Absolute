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
    public static final String ALIEN = "alien.png";
    public static final String BOSS_MARS = "bossmarte.png";
    public static final String TRUMP = "trump.png";
    public static final String RIFLE = "ak47.png";
    public static final String PORTAL = "portal.png";
    public static final String RED_KEY = "chave.png";
    public static final String GREEN_KEY = "chave_verde.png";

    // TITÃ — substitua estes PNGs pelos seus sprites/mapas quando criar as imagens.
    public static final String TITA_TILE = "tita_tilemap.png";
    public static final String TITA_INTERIOR_TILE = "tita_interior_tilemap.png";
    public static final String TITA_CASTLE = "tita_castelo.png";
    public static final String TITA_FINAL_CASTLE = "tita_castelo_final.png";
    public static final String TITA_DOOR = "tita_porta.png";
    public static final String TITA_ALTAR = "tita_altar_pedra.png";
    public static final String TITA_RED_CRYSTAL = "cristal_vermelho.png";
    public static final String TITA_BLUE_CRYSTAL = "cristal_azul.png";
    public static final String TITA_YELLOW_CRYSTAL = "cristal_amarelo.png";
    public static final String TITA_OBAMA = "obama.png";
    public static final String TITA_AUTHENTIC = "authentic_games.png";
    public static final String TITA_VERITY = "verity.png";
    public static final String TITA_CR7 = "cr7.png";

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
    private Texture titaTileTexture;
    private Texture titaInteriorTileTexture;
    private Texture titaCastleTexture;
    private Texture titaFinalCastleTexture;
    private Texture titaDoorTexture;
    private Texture titaAltarTexture;
    private Texture redCrystalTexture;
    private Texture blueCrystalTexture;
    private Texture yellowCrystalTexture;
    private Texture obamaTexture;
    private Texture authenticGamesTexture;
    private Texture verityTexture;
    private Texture cr7Texture;

    private boolean playerFallback;

    public void load() {
        playerTexture = loadAsset(PLAYER, Color.CYAN);
        if (!assetExists(PLAYER)) {
            playerTexture = createAstronautFallback();
        }
        playerFallback = !assetExists(PLAYER);
        luaBackgroundTexture = loadAsset(LUA_BACKGROUND, new Color(0.08f, 0.08f, 0.13f, 1f));
        laserTexture = loadAsset(LASER, new Color(1f, 0.90f, 0.10f, 1f));
        lunarBaseTexture = loadAsset(LUNAR_BASE, new Color(0.45f, 0.45f, 0.50f, 1f));
        luaTileTexture = loadAsset(LUA_TILE, new Color(0.20f, 0.20f, 0.23f, 1f));
        marsTileTexture = loadAsset(MARS_TILE, new Color(0.72f, 0.25f, 0.06f, 1f));
        foodTexture = loadAsset(FOOD, new Color(1f, 0.55f, 0f, 1f));
        if (!assetExists(FOOD)) {
            foodTexture = createFoodFallback();
        }

        o2TankTexture = loadAsset(O2_TANK, new Color(0.70f, 0.78f, 0.86f, 1f));
        if (!assetExists(O2_TANK)) {
            o2TankTexture = createO2Fallback();
        }
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

        titaTileTexture = loadAsset(TITA_TILE, new Color(0.26f, 0.02f, 0.03f, 1f));
        titaInteriorTileTexture = loadAsset(TITA_INTERIOR_TILE, new Color(0.16f, 0.16f, 0.18f, 1f));
        titaCastleTexture = loadAsset(TITA_CASTLE, new Color(0.43f, 0.44f, 0.47f, 1f));
        titaFinalCastleTexture = loadAsset(TITA_FINAL_CASTLE, new Color(0.32f, 0.33f, 0.36f, 1f));
        titaDoorTexture = loadAsset(TITA_DOOR, new Color(0.12f, 0.10f, 0.10f, 1f));
        titaAltarTexture = loadAsset(TITA_ALTAR, new Color(0.38f, 0.37f, 0.34f, 1f));
        redCrystalTexture = loadAsset(TITA_RED_CRYSTAL, Color.RED);
        blueCrystalTexture = loadAsset(TITA_BLUE_CRYSTAL, Color.BLUE);
        yellowCrystalTexture = loadAsset(TITA_YELLOW_CRYSTAL, Color.YELLOW);
        obamaTexture = loadAsset(TITA_OBAMA, new Color(0.25f, 0.55f, 0.95f, 1f));
        authenticGamesTexture = loadAsset(TITA_AUTHENTIC, new Color(0.10f, 0.75f, 0.35f, 1f));
        verityTexture = loadAsset(TITA_VERITY, new Color(0.85f, 0.25f, 0.75f, 1f));
        cr7Texture = loadAsset(TITA_CR7, new Color(0.95f, 0.75f, 0.10f, 1f));
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

    private Texture createAstronautFallback() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.05f, 0.25f, 0.35f, 1f));
        pixmap.fill();
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(32, 40, 18);
        pixmap.setColor(new Color(0.15f, 0.65f, 0.95f, 1f));
        pixmap.fillRectangle(18, 20, 28, 22);
        pixmap.setColor(Color.LIGHT_GRAY);
        pixmap.fillRectangle(20, 10, 24, 10);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createFoodFallback() {
        Pixmap pixmap = new Pixmap(72, 72, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(1f, 0.55f, 0.05f, 1f));
        pixmap.fillCircle(36, 36, 30);
        pixmap.setColor(Color.YELLOW);
        pixmap.fillCircle(28, 43, 7);
        pixmap.fillCircle(44, 43, 7);
        pixmap.setColor(new Color(0.75f, 0.12f, 0.04f, 1f));
        pixmap.fillCircle(36, 25, 8);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createO2Fallback() {
        Pixmap pixmap = new Pixmap(46, 110, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.15f, 0.35f, 0.90f, 1f));
        pixmap.fillRectangle(7, 12, 32, 82);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(11, 95, 24, 7);
        pixmap.setColor(new Color(0.55f, 0.90f, 1f, 1f));
        pixmap.fillRectangle(13, 22, 20, 52);
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
    public Texture getTitaTileTexture() { return titaTileTexture; }
    public Texture getTitaInteriorTileTexture() { return titaInteriorTileTexture; }
    public Texture getTitaCastleTexture() { return titaCastleTexture; }
    public Texture getTitaFinalCastleTexture() { return titaFinalCastleTexture; }
    public Texture getTitaDoorTexture() { return titaDoorTexture; }
    public Texture getTitaAltarTexture() { return titaAltarTexture; }
    public Texture getRedCrystalTexture() { return redCrystalTexture; }
    public Texture getBlueCrystalTexture() { return blueCrystalTexture; }
    public Texture getYellowCrystalTexture() { return yellowCrystalTexture; }
    public Texture getObamaTexture() { return obamaTexture; }
    public Texture getAuthenticGamesTexture() { return authenticGamesTexture; }
    public Texture getVerityTexture() { return verityTexture; }
    public Texture getCr7Texture() { return cr7Texture; }
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
        disposeTexture(titaTileTexture);
        disposeTexture(titaInteriorTileTexture);
        disposeTexture(titaCastleTexture);
        disposeTexture(titaFinalCastleTexture);
        disposeTexture(titaDoorTexture);
        disposeTexture(titaAltarTexture);
        disposeTexture(redCrystalTexture);
        disposeTexture(blueCrystalTexture);
        disposeTexture(yellowCrystalTexture);
        disposeTexture(obamaTexture);
        disposeTexture(authenticGamesTexture);
        disposeTexture(verityTexture);
        disposeTexture(cr7Texture);
    }

    private void disposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
