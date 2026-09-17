package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.Laser;
import io.github.some_example_name.entities.LuaItem;
import io.github.some_example_name.entities.MarsOre;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.entities.PlayerStats;
import io.github.some_example_name.managers.AssetManager;

/**
 * Mars is intentionally built as a structural clone of the Lua phase.
 * The same player, survival system, food, O2, base and weapon are reused.
 * Enemies and the boss are intentionally not implemented yet.
 */
public class LuaMarteScreen extends ScreenAdapter {

    private enum MarsMission {
        COLLECT_ORE,
        REFINE_ORE,
        UPGRADE_WEAPON,
        COMPLETE
    }

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;
    private static final float WORLD_WIDTH = 3000f;
    private static final float WORLD_HEIGHT = 2000f;

    private static final float PLAYER_SPAWN_X = 400f;
    private static final float PLAYER_SPAWN_Y = 400f;

    private static final float BASE_X = 500f;
    private static final float BASE_Y = 390f;
    private static final float BASE_WIDTH = 240f;
    private static final float BASE_HEIGHT = 160f;

    private static final float MINE_X = 1980f;
    private static final float MINE_Y = 920f;
    private static final float MINE_WIDTH = BASE_WIDTH;
    private static final float MINE_HEIGHT = BASE_HEIGHT;

    private static final int REQUIRED_ORE = 5;
    private static final float MARS_GROUND_R = 0.72f;
    private static final float MARS_GROUND_G = 0.25f;
    private static final float MARS_GROUND_B = 0.06f;

    private static final float NORMAL_FIRE_INTERVAL = 0.18f;
    private static final float UPGRADED_FIRE_INTERVAL = 0.09f;
    private static final float BURST_SPREAD = 0.045f;

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ScreenViewport hudViewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont hudFont;
    private final AssetManager assets;
    private final Player player;
    private final PlayerStats stats;

    private final Array<LuaItem> resources = new Array<>();
    private final Array<MarsOre> ores = new Array<>();
    private final Array<Laser> lasers = new Array<>();

    private MarsMission mission = MarsMission.COLLECT_ORE;
    private int rawOreCount;
    private int refinedOreCount;
    private boolean weaponUpgraded;
    private float fireTimer;
    private float messageTimer;
    private String missionMessage = "";
    private boolean changingScreen;
    private boolean disposed;

    public LuaMarteScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        hudViewport = new ScreenViewport();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        hudFont = new BitmapFont();

        assets = new AssetManager();
        assets.load();

        player = new Player(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
        stats = new PlayerStats();

        createMarsResources();
        createMarsOres();
        showMessage("MISSÃO MARTE: colete 5 minérios verdes e leve-os até a mina para refinar.");
        updateCamera();
    }

    private void createMarsResources() {
        // Mesmos recursos da Lua.
        addResource(LuaItem.Type.FOOD, 780f, 620f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 1120f, 430f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 1680f, 560f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 2240f, 420f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 720f, 1510f, 56f, 56f);
        addResource(LuaItem.Type.FOOD, 1900f, 1560f, 56f, 56f);

        addResource(LuaItem.Type.O2_TANK, 1050f, 920f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 2050f, 1320f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 1450f, 560f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 2640f, 1150f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 560f, 1180f, 46f, 125f);
        addResource(LuaItem.Type.O2_TANK, 2400f, 1540f, 46f, 125f);
    }

    private void addResource(LuaItem.Type type, float x, float y, float width, float height) {
        resources.add(new LuaItem(type, x, y, width, height));
    }

    private void createMarsOres() {
        ores.add(new MarsOre(900f, 800f));
        ores.add(new MarsOre(1320f, 1260f));
        ores.add(new MarsOre(1740f, 650f));
        ores.add(new MarsOre(2220f, 1200f));
        ores.add(new MarsOre(700f, 1550f));
        ores.add(new MarsOre(2520f, 500f));
        ores.add(new MarsOre(1120f, 1750f));
        ores.add(new MarsOre(2320f, 760f));
    }

    @Override
    public void show() {
        changingScreen = false;
        viewport.apply(true);
        hudViewport.apply(true);
        updateCamera();
    }

    private boolean update(float delta) {
        delta = Math.min(delta, 0.05f);

        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        collectResources();
        collectOres();
        handleMissionInteraction();
        updateShooting(delta);
        updateLasers(delta);
        stats.update(delta);

        if (stats.isDead()) {
            changingScreen = true;
            DeathCause cause = stats.getDeathCause();
            dispose();
            game.setScreen(new GameOverScreen(game, cause));
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return false;
        }

        updateCamera();
        return true;
    }

    private void collectResources() {
        for (int i = resources.size - 1; i >= 0; i--) {
            LuaItem item = resources.get(i);
            if (!player.getHitbox().overlaps(item.getHitbox())) {
                continue;
            }

            switch (item.getType()) {
                case FOOD:
                    stats.eatFood();
                    showMessage("Comida coletada: fome e vida restauradas.");
                    break;
                case O2_TANK:
                    stats.addOxygen(20f);
                    showMessage("O2 coletado: oxigênio restaurado.");
                    break;
                default:
                    break;
            }

            resources.removeIndex(i);
        }
    }

    private void collectOres() {
        for (int i = ores.size - 1; i >= 0; i--) {
            MarsOre ore = ores.get(i);
            if (!player.getHitbox().overlaps(ore.getHitbox())) {
                continue;
            }

            rawOreCount++;
            ores.removeIndex(i);

            if (mission == MarsMission.COLLECT_ORE) {
                if (rawOreCount >= REQUIRED_ORE) {
                    mission = MarsMission.REFINE_ORE;
                    showMessage("5 minérios coletados! Vá até a mina e pressione E para refinar.");
                } else {
                    showMessage("Minério verde coletado: " + rawOreCount + "/" + REQUIRED_ORE + ".");
                }
            }
        }
    }

    private void handleMissionInteraction() {
        Rectangle mine = new Rectangle(MINE_X, MINE_Y, MINE_WIDTH, MINE_HEIGHT);
        Rectangle base = new Rectangle(BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);

        if (mission == MarsMission.REFINE_ORE
                && rawOreCount >= REQUIRED_ORE
                && player.getHitbox().overlaps(mine)
                && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            refinedOreCount = rawOreCount;
            rawOreCount = 0;
            mission = MarsMission.UPGRADE_WEAPON;
            showMessage("Minérios refinados! Agora volte à base e pressione E para melhorar a arma.");
            return;
        }

        if (mission == MarsMission.UPGRADE_WEAPON
                && refinedOreCount >= REQUIRED_ORE
                && player.getHitbox().overlaps(base)
                && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            weaponUpgraded = true;
            mission = MarsMission.COMPLETE;
            showMessage("ARMA APRIMORADA! Agora ela dispara 2 tiros por rajada e com o dobro da cadência.");
        }
    }

    private void updateShooting(float delta) {
        fireTimer -= delta;
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) || fireTimer > 0f) {
            return;
        }

        Vector3 mouseWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseWorld);

        float dx = mouseWorld.x - player.getCenterX();
        float dy = mouseWorld.y - player.getCenterY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.001f) {
            return;
        }

        dx /= length;
        dy /= length;

        if (weaponUpgraded) {
            addLaserWithAngle(dx, dy, BURST_SPREAD);
            addLaserWithAngle(dx, dy, -BURST_SPREAD);
            fireTimer = UPGRADED_FIRE_INTERVAL;
        } else {
            lasers.add(new Laser(
                    player.getCenterX(),
                    player.getCenterY(),
                    dx,
                    dy
            ));
            fireTimer = NORMAL_FIRE_INTERVAL;
        }
    }

    private void addLaserWithAngle(float dx, float dy, float angle) {
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float rotatedX = dx * cos - dy * sin;
        float rotatedY = dx * sin + dy * cos;

        lasers.add(new Laser(
                player.getCenterX(),
                player.getCenterY(),
                rotatedX,
                rotatedY
        ));
    }

    private void updateLasers(float delta) {
        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);
            if (laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                lasers.removeIndex(i);
            }
        }
    }

    private void updateCamera() {
        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;

        float cameraX = MathUtils.clamp(
                player.getCenterX(),
                halfViewportWidth,
                WORLD_WIDTH - halfViewportWidth
        );

        float cameraY = MathUtils.clamp(
                player.getCenterY(),
                halfViewportHeight,
                WORLD_HEIGHT - halfViewportHeight
        );

        camera.position.set(cameraX, cameraY, 0f);
        camera.update();
    }

    private void drawMarsFloor() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(MARS_GROUND_R, MARS_GROUND_G, MARS_GROUND_B, 1f));
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        // Crateres simples para manter a aparência de terreno, sem substituir os sprites da Lua.
        shapeRenderer.setColor(new Color(0.48f, 0.13f, 0.035f, 0.75f));
        shapeRenderer.circle(420f, 1450f, 95f);
        shapeRenderer.circle(1050f, 520f, 70f);
        shapeRenderer.circle(1700f, 1450f, 110f);
        shapeRenderer.circle(2500f, 1200f, 90f);
        shapeRenderer.circle(2250f, 420f, 62f);
        shapeRenderer.end();
    }

    private void drawWorld() {
        drawMarsFloor();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Texture baseTexture = assets.getLunarBaseTexture();
        batch.draw(baseTexture, BASE_X, BASE_Y, BASE_WIDTH, BASE_HEIGHT);

        drawResources();

        Texture laserTexture = assets.getLaserTexture();
        for (Laser laser : lasers) {
            batch.draw(
                    laserTexture,
                    laser.getX(),
                    laser.getY(),
                    laser.getWidth(),
                    laser.getHeight()
            );
        }

        Texture playerTexture = assets.getPlayerTexture();
        batch.draw(
                playerTexture,
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

        batch.end();

        // Mina: cubo marrom com o mesmo tamanho da base lunar.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.28f, 0.10f, 0.035f, 1f));
        shapeRenderer.rect(MINE_X, MINE_Y, MINE_WIDTH, MINE_HEIGHT);
        shapeRenderer.setColor(new Color(0.42f, 0.16f, 0.05f, 1f));
        shapeRenderer.rect(MINE_X + 18f, MINE_Y + 18f, MINE_WIDTH - 36f, MINE_HEIGHT - 36f);

        // Minérios verdes.
        shapeRenderer.setColor(new Color(0.15f, 0.95f, 0.25f, 1f));
        for (MarsOre ore : ores) {
            shapeRenderer.rect(ore.getX(), ore.getY(), ore.getWidth(), ore.getHeight());
        }
        shapeRenderer.end();
    }

    private void drawResources() {
        for (LuaItem item : resources) {
            Texture texture;
            switch (item.getType()) {
                case FOOD:
                    texture = assets.getFoodTexture();
                    break;
                case O2_TANK:
                    texture = assets.getO2TankTexture();
                    break;
                default:
                    continue;
            }

            batch.draw(texture, item.getX(), item.getY(), item.getWidth(), item.getHeight());
        }
    }

    private void drawBar(
            float x,
            float y,
            float width,
            float height,
            float value,
            float maxValue,
            Color color
    ) {
        shapeRenderer.setColor(new Color(0.08f, 0.08f, 0.08f, 0.92f));
        shapeRenderer.rect(x, y, width, height);

        float percent = MathUtils.clamp(value / maxValue, 0f, 1f);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width * percent, height);
    }

    private void drawHud() {
        hudViewport.apply(false);
        shapeRenderer.setProjectionMatrix(hudViewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float x = 28f;
        float width = 330f;
        float height = 28f;
        float firstY = hudViewport.getWorldHeight() - 52f;
        float gap = 48f;

        drawBar(x, firstY, width, height, stats.getHealth(), PlayerStats.MAX_HEALTH, Color.RED);
        drawBar(x, firstY - gap, width, height, stats.getHunger(), PlayerStats.MAX_HUNGER, Color.ORANGE);
        drawBar(x, firstY - gap * 2f, width, height, stats.getOxygen(), PlayerStats.MAX_OXYGEN, Color.CYAN);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        hudFont.getData().setScale(1.05f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, String.format("HP: %.0f / 100", stats.getHealth()), x + 10f, firstY + 20f);
        hudFont.draw(batch, String.format("FOME: %.0f / 100", stats.getHunger()), x + 10f, firstY - gap + 20f);
        hudFont.draw(batch, String.format("O2: %.0f / 100", stats.getOxygen()), x + 10f, firstY - gap * 2f + 20f);

        hudFont.getData().setScale(1.0f);
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "MISSÃO MARTE", 28f, hudViewport.getWorldHeight() - 205f);

        hudFont.getData().setScale(0.92f);
        hudFont.setColor(Color.LIGHT_GRAY);

        String objective;
        switch (mission) {
            case COLLECT_ORE:
                objective = "Colete minérios verdes: " + rawOreCount + "/" + REQUIRED_ORE;
                break;
            case REFINE_ORE:
                objective = "Vá à mina marrom e pressione E para refinar " + REQUIRED_ORE + " minérios";
                break;
            case UPGRADE_WEAPON:
                objective = "Volte à base e pressione E para melhorar a arma (refinados: " + refinedOreCount + "/" + REQUIRED_ORE + ")";
                break;
            case COMPLETE:
                objective = "MISSÃO CONCLUÍDA: arma aprimorada";
                break;
            default:
                objective = "";
                break;
        }

        hudFont.draw(batch, objective, 28f, hudViewport.getWorldHeight() - 235f);

        hudFont.setColor(Color.ORANGE);
        hudFont.getData().setScale(1.0f);
        hudFont.draw(batch, "MINA", MINE_X - 5f, MINE_Y + MINE_HEIGHT + 24f);

        hudFont.setColor(Color.LIME);
        hudFont.draw(batch, "MINÉRIO", hudViewport.getWorldWidth() - 190f, hudViewport.getWorldHeight() - 34f);

        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(0.95f);
        if (weaponUpgraded) {
            hudFont.draw(batch, "ARMA: APRIMORADA | 2 tiros | cadência 2x", 28f, hudViewport.getWorldHeight() - 280f);
        } else {
            hudFont.draw(batch, "ARMA: padrão | 1 tiro", 28f, hudViewport.getWorldHeight() - 280f);
        }

        if (messageTimer > 0f) {
            hudFont.getData().setScale(1.0f);
            hudFont.setColor(Color.WHITE);
            GlyphLayout layout = new GlyphLayout(hudFont, missionMessage);
            hudFont.draw(
                    batch,
                    missionMessage,
                    hudViewport.getWorldWidth() / 2f - layout.width / 2f,
                    42f
            );
        }

        hudFont.getData().setScale(0.86f);
        hudFont.setColor(Color.LIGHT_GRAY);
        hudFont.draw(batch, "WASD / SETAS = mover | Mouse = mirar + segurar para atirar | E = interagir | ESC = menu", 28f, 18f);

        batch.end();
        messageTimer = Math.max(0f, messageTimer - Gdx.graphics.getDeltaTime());
    }

    private void showMessage(String message) {
        missionMessage = message;
        messageTimer = 4f;
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        if (!update(delta)) {
            return;
        }

        ScreenUtils.clear(MARS_GROUND_R, MARS_GROUND_G, MARS_GROUND_B, 1f);
        drawWorld();
        drawHud();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        updateCamera();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        batch.dispose();
        shapeRenderer.dispose();
        hudFont.dispose();
        assets.dispose();
    }
}

/** Compatibility class used by LuaScreen and MarsIntroScreen. */
class MarteScreen extends LuaMarteScreen {
    public MarteScreen(Game game) {
        super(game);
    }
}
