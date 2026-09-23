package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Centraliza os assets de Lua, Marte, Titã e Calisto.
 *
 * As texturas são compartilhadas entre as Screens durante toda a execução
 * do jogo. Isso evita criar/destruir dezenas de recursos OpenGL a cada
 * troca de fase, o que pode causar crash nativo no Windows.
 */
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
    public static final String PORTAL_TITA = "portaltita.png";
    public static final String PORTAL_CALISTO = "portalcalisto.png";
    public static final String PORTAL_AHARIN = "portalaharin.png";
    public static final String RED_KEY = "chave.png";
    public static final String GREEN_KEY = "chave_verde.png";

    public static final String TITA_TILE = "tileset_tita.png";
    public static final String TITA_INTERIOR_TILE = "castelo_tileset.png";
    public static final String TITA_CASTLE = "castelo.png";
    public static final String TITA_FINAL_CASTLE = "bastiao.png";
    public static final String TITA_DOOR = "tita_porta.png";
    public static final String TITA_ALTAR = "tita_altar_pedra.png";
    public static final String TITA_RED_CRYSTAL = "cristal_vermelho.png";
    public static final String TITA_BLUE_CRYSTAL = "cristal_azul.png";
    public static final String TITA_YELLOW_CRYSTAL = "cristal_amarelo.png";
    public static final String TITA_OBAMA = "barackobama.png";
    public static final String TITA_AUTHENTIC = "authentic_games.png";
    public static final String TITA_VERITY = "verity.png";
    public static final String TITA_CR7 = "cr7.png";

    private static boolean sharedLoaded;
    private static Texture sharedPlayerTexture;
    private static Texture sharedLuaBackgroundTexture;
    private static Texture sharedLaserTexture;
    private static Texture sharedLunarBaseTexture;
    private static Texture sharedLuaTileTexture;
    private static Texture sharedMarsTileTexture;
    private static Texture sharedFoodTexture;
    private static Texture sharedO2TankTexture;
    private static Texture sharedIceTexture;
    private static Texture sharedOreTexture;
    private static Texture sharedMineTexture;
    private static Texture sharedAmericanTexture;
    private static Texture sharedAlienTexture;
    private static Texture sharedBossMarsTexture;
    private static Texture sharedTrumpTexture;
    private static Texture sharedRifleTexture;
    private static Texture sharedPortalTexture;
    private static Texture sharedPortalTitaTexture;
    private static Texture sharedPortalCalistoTexture;
    private static Texture sharedPortalAharinTexture;
    private static Texture sharedRedKeyTexture;
    private static Texture sharedGreenKeyTexture;
    private static Texture sharedTitaTileTexture;
    private static Texture sharedTitaInteriorTileTexture;
    private static Texture sharedTitaCastleTexture;
    private static Texture sharedTitaFinalCastleTexture;
    private static Texture sharedTitaDoorTexture;
    private static Texture sharedTitaAltarTexture;
    private static Texture sharedRedCrystalTexture;
    private static Texture sharedBlueCrystalTexture;
    private static Texture sharedYellowCrystalTexture;
    private static Texture sharedObamaTexture;
    private static Texture sharedAuthenticGamesTexture;
    private static Texture sharedVerityTexture;
    private static Texture sharedCr7Texture;

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
    private Texture portalTitaTexture;
    private Texture portalCalistoTexture;
    private Texture portalAharinTexture;
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
        if (sharedLoaded) {
            attachSharedTextures();
            Gdx.app.log("AssetManager", "Texturas compartilhadas reutilizadas.");
            return;
        }

        synchronized (AssetManager.class) {
            if (!sharedLoaded) {
                Gdx.app.log("AssetManager", "Carregando pacote de texturas compartilhado.");

                sharedPlayerTexture = !assetExists(PLAYER)
                        ? createAstronautFallback()
                        : loadAsset(PLAYER, Color.CYAN);

                sharedLuaBackgroundTexture = loadAsset(
                        LUA_BACKGROUND, new Color(0.08f, 0.08f, 0.13f, 1f));
                sharedLaserTexture = loadAsset(
                        LASER, new Color(1f, 0.90f, 0.10f, 1f));
                sharedLunarBaseTexture = loadAsset(
                        LUNAR_BASE, new Color(0.45f, 0.45f, 0.50f, 1f));
                sharedLuaTileTexture = loadAsset(
                        LUA_TILE, new Color(0.20f, 0.20f, 0.23f, 1f));
                sharedMarsTileTexture = loadAsset(
                        MARS_TILE, new Color(0.72f, 0.25f, 0.06f, 1f));

                sharedFoodTexture = assetExists(FOOD)
                        ? loadAsset(FOOD, new Color(1f, 0.55f, 0f, 1f))
                        : createFoodFallback();

                sharedO2TankTexture = assetExists(O2_TANK)
                        ? loadAsset(O2_TANK, new Color(0.70f, 0.78f, 0.86f, 1f))
                        : createO2Fallback();

                sharedIceTexture = loadAsset(ICE, new Color(0.20f, 0.55f, 1f, 1f));
                sharedOreTexture = loadAsset(ORE, new Color(0.15f, 0.95f, 0.25f, 1f));
                sharedMineTexture = loadAsset(MINE, new Color(0.28f, 0.10f, 0.035f, 1f));
                sharedAmericanTexture = loadAsset(AMERICAN, new Color(0.90f, 0.08f, 0.08f, 1f));
                sharedAlienTexture = loadAsset(ALIEN, new Color(0.45f, 0.95f, 0.35f, 1f));
                sharedBossMarsTexture = loadAsset(BOSS_MARS, new Color(0.35f, 0.85f, 0.25f, 1f));
                sharedTrumpTexture = loadAsset(TRUMP, new Color(1f, 0.45f, 0.05f, 1f));
                sharedRifleTexture = loadAsset(RIFLE, new Color(0.10f, 0.10f, 0.10f, 1f));
                sharedPortalTexture = loadAsset(PORTAL, new Color(0.15f, 0.85f, 1f, 1f));
                sharedPortalTitaTexture = loadAsset(PORTAL_TITA, new Color(0.75f, 0.20f, 1f, 1f));
                sharedPortalCalistoTexture = loadAsset(PORTAL_CALISTO, new Color(0.35f, 1f, 0.95f, 1f));
                sharedPortalAharinTexture = loadAsset(PORTAL_AHARIN, new Color(1f, 0.75f, 0.15f, 1f));
                sharedRedKeyTexture = loadAsset(RED_KEY, Color.RED);
                sharedGreenKeyTexture = loadAsset(GREEN_KEY, Color.GREEN);

                sharedTitaTileTexture = loadAsset(
                        TITA_TILE, new Color(0.26f, 0.02f, 0.03f, 1f));
                sharedTitaInteriorTileTexture = loadAsset(
                        TITA_INTERIOR_TILE, new Color(0.16f, 0.16f, 0.18f, 1f));
                sharedTitaCastleTexture = loadAsset(
                        TITA_CASTLE, new Color(0.43f, 0.44f, 0.47f, 1f));
                sharedTitaFinalCastleTexture = loadAsset(
                        TITA_FINAL_CASTLE, new Color(0.32f, 0.33f, 0.36f, 1f));
                sharedTitaDoorTexture = loadAsset(
                        TITA_DOOR, new Color(0.12f, 0.10f, 0.10f, 1f));
                sharedTitaAltarTexture = loadAsset(
                        TITA_ALTAR, new Color(0.38f, 0.37f, 0.34f, 1f));
                sharedRedCrystalTexture = loadAsset(TITA_RED_CRYSTAL, Color.RED);
                sharedBlueCrystalTexture = loadAsset(TITA_BLUE_CRYSTAL, Color.BLUE);
                sharedYellowCrystalTexture = loadAsset(TITA_YELLOW_CRYSTAL, Color.YELLOW);
                sharedObamaTexture = loadAsset(
                        TITA_OBAMA, new Color(0.25f, 0.55f, 0.95f, 1f));
                sharedAuthenticGamesTexture = loadAsset(
                        TITA_AUTHENTIC, new Color(0.10f, 0.75f, 0.35f, 1f));
                sharedVerityTexture = loadAsset(
                        TITA_VERITY, new Color(0.85f, 0.25f, 0.75f, 1f));
                sharedCr7Texture = loadAsset(
                        TITA_CR7, new Color(0.95f, 0.75f, 0.10f, 1f));

                sharedLoaded = true;
            }
        }

        attachSharedTextures();
    }

    private void attachSharedTextures() {
        playerTexture = sharedPlayerTexture;
        luaBackgroundTexture = sharedLuaBackgroundTexture;
        laserTexture = sharedLaserTexture;
        lunarBaseTexture = sharedLunarBaseTexture;
        luaTileTexture = sharedLuaTileTexture;
        marsTileTexture = sharedMarsTileTexture;
        foodTexture = sharedFoodTexture;
        o2TankTexture = sharedO2TankTexture;
        iceTexture = sharedIceTexture;
        oreTexture = sharedOreTexture;
        mineTexture = sharedMineTexture;
        americanTexture = sharedAmericanTexture;
        alienTexture = sharedAlienTexture;
        bossMarsTexture = sharedBossMarsTexture;
        trumpTexture = sharedTrumpTexture;
        rifleTexture = sharedRifleTexture;
        portalTexture = sharedPortalTexture;
        portalTitaTexture = sharedPortalTitaTexture;
        portalCalistoTexture = sharedPortalCalistoTexture;
        portalAharinTexture = sharedPortalAharinTexture;
        redKeyTexture = sharedRedKeyTexture;
        greenKeyTexture = sharedGreenKeyTexture;
        titaTileTexture = sharedTitaTileTexture;
        titaInteriorTileTexture = sharedTitaInteriorTileTexture;
        titaCastleTexture = sharedTitaCastleTexture;
        titaFinalCastleTexture = sharedTitaFinalCastleTexture;
        titaDoorTexture = sharedTitaDoorTexture;
        titaAltarTexture = sharedTitaAltarTexture;
        redCrystalTexture = sharedRedCrystalTexture;
        blueCrystalTexture = sharedBlueCrystalTexture;
        yellowCrystalTexture = sharedYellowCrystalTexture;
        obamaTexture = sharedObamaTexture;
        authenticGamesTexture = sharedAuthenticGamesTexture;
        verityTexture = sharedVerityTexture;
        cr7Texture = sharedCr7Texture;
        playerFallback = playerTexture == sharedPlayerTexture && !assetExists(PLAYER);
    }

    private Texture loadAsset(String fileName, Color fallbackColor) {
        String[] candidates = {
                "textures/" + fileName,
                "textures/tita/" + fileName,
                "tita/" + fileName,
                fileName
        };

        for (String path : candidates) {
            if (Gdx.files.internal(path).exists()) {
                Gdx.app.log("AssetManager", "Carregando textura: " + path);
                return new Texture(Gdx.files.internal(path));
            }
        }

        Gdx.app.error(
                "AssetManager",
                "Textura não encontrada: " + fileName
                        + " | procurei em textures/, textures/tita/, tita/ e raiz."
        );
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
    public Texture getPortalTitaTexture() { return portalTitaTexture; }
    public Texture getPortalCalistoTexture() { return portalCalistoTexture; }
    public Texture getPortalAharinTexture() { return portalAharinTexture; }
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

    /**
     * Do not dispose shared textures during a Screen transition.
     * They belong to the whole game process and will be released by the
     * operating system when the game exits.
     */
    public void dispose() {
        // Intentionally empty: textures are shared across Screens.
    }
}
