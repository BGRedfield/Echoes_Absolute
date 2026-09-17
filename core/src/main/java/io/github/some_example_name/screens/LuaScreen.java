package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.Laser;
import io.github.some_example_name.entities.LuaItem;
import io.github.some_example_name.entities.Player;
import io.github.some_example_name.entities.PlayerStats;
import io.github.some_example_name.managers.AssetManager;

public class LuaScreen extends ScreenAdapter {

    private static final float VIEW_WIDTH = 1280f;
    private static final float VIEW_HEIGHT = 720f;

    private static final float WORLD_WIDTH = 3000f;
    private static final float WORLD_HEIGHT = 2000f;

    private static final float PLAYER_SPAWN_X = 400f;
    private static final float PLAYER_SPAWN_Y = 400f;

    private static final float LUNAR_BASE_X = 500f;
    private static final float LUNAR_BASE_Y = 390f;
    private static final float LUNAR_BASE_WIDTH = 240f;
    private static final float LUNAR_BASE_HEIGHT = 160f;

    private static final float TILE_SIZE = 128f;

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
    private final Array<Laser> lasers;
    private final Array<LuaItem> luaItems;

    public LuaScreen(Game game) {
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
        lasers = new Array<>();
        luaItems = new Array<>();

        createLuaResources();
        updateCamera();
    }

    private void createLuaResources() {
        // Comida: +50 fome e +10 HP.
        luaItems.add(new LuaItem(
                LuaItem.Type.FOOD,
                780f,
                620f,
                52f,
                52f
        ));

        luaItems.add(new LuaItem(
                LuaItem.Type.FOOD,
                1540f,
                420f,
                52f,
                52f
        ));

        // Tanques de O2: +20 oxigenio.
        luaItems.add(new LuaItem(
                LuaItem.Type.O2_TANK,
                1050f,
                920f,
                42f,
                118f
        ));

        luaItems.add(new LuaItem(
                LuaItem.Type.O2_TANK,
                2050f,
                1320f,
                42f,
                118f
        ));

        // Gelo: coletavel e contabilizado para futuras missoes.
        luaItems.add(new LuaItem(
                LuaItem.Type.ICE,
                1350f,
                1300f,
                70f,
                70f
        ));

        luaItems.add(new LuaItem(
                LuaItem.Type.ICE,
                2380f,
                760f,
                70f,
                70f
        ));
    }

    @Override
    public void show() {
        viewport.apply(true);
        hudViewport.apply(true);
        updateCamera();
    }

    private void update(float delta) {
        delta = Math.min(delta, 0.05f);

        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);
        collectItems();

        // Sobrevivencia: fome e oxigenio sofrem seus descontos por tempo.
        stats.update(delta);

        if (stats.isDead()) {
            openGameOver();
            return;
        }

        // Clique esquerdo dispara exatamente um laser.
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            lasers.add(new Laser(
                    player.getCenterX(),
                    player.getCenterY(),
                    player.getDirectionX(),
                    player.getDirectionY()
            ));
        }

        for (int i = lasers.size - 1; i >= 0; i--) {
            Laser laser = lasers.get(i);
            laser.update(delta);

            if (laser.isOutsideWorld(WORLD_WIDTH, WORLD_HEIGHT)) {
                lasers.removeIndex(i);
            }
        }

        updateCamera();
    }

    private void collectItems() {
        for (int i = luaItems.size - 1; i >= 0; i--) {
            LuaItem item = luaItems.get(i);

            if (!player.getHitbox().overlaps(item.getHitbox())) {
                continue;
            }

            switch (item.getType()) {
                case FOOD:
                    stats.eatFood();
                    break;
                case O2_TANK:
                    stats.addOxygen(20f);
                    break;
                case ICE:
                    stats.collectIce();
                    break;
                default:
                    break;
            }

            // O item some do mapa ao ser coletado.
            luaItems.removeIndex(i);
        }
    }

    private void openGameOver() {
        DeathCause cause = stats.getDeathCause();
        dispose();
        game.setScreen(new GameOverScreen(game, cause));
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

    private void drawLuaFloor() {
        Texture tile = assets.getLuaTileTexture();

        for (float x = 0f; x < WORLD_WIDTH; x += TILE_SIZE) {
            for (float y = 0f; y < WORLD_HEIGHT; y += TILE_SIZE) {
                float width = Math.min(TILE_SIZE, WORLD_WIDTH - x);
                float height = Math.min(TILE_SIZE, WORLD_HEIGHT - y);

                batch.draw(tile, x, y, width, height);
            }
        }
    }

    private void drawLuaItems() {
        for (LuaItem item : luaItems) {
            Texture texture;

            switch (item.getType()) {
                case FOOD:
                    texture = assets.getFoodTexture();
                    break;
                case O2_TANK:
                    texture = assets.getO2TankTexture();
                    break;
                case ICE:
                    texture = assets.getIceTexture();
                    break;
                default:
                    continue;
            }

            batch.draw(
                    texture,
                    item.getX(),
                    item.getY(),
                    item.getWidth(),
                    item.getHeight()
            );
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

        drawBar(
                x,
                firstY,
                width,
                height,
                stats.getHealth(),
                PlayerStats.MAX_HEALTH,
                Color.RED
        );

        drawBar(
                x,
                firstY - gap,
                width,
                height,
                stats.getHunger(),
                PlayerStats.MAX_HUNGER,
                Color.ORANGE
        );

        drawBar(
                x,
                firstY - gap * 2f,
                width,
                height,
                stats.getOxygen(),
                PlayerStats.MAX_OXYGEN,
                Color.CYAN
        );

        shapeRenderer.end();

        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();

        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.15f);

        hudFont.draw(
                batch,
                String.format("HP: %.0f / 100", stats.getHealth()),
                x + 10f,
                firstY + 20f
        );

        hudFont.draw(
                batch,
                String.format("FOME: %.0f / 100", stats.getHunger()),
                x + 10f,
                firstY - gap + 20f
        );

        hudFont.draw(
                batch,
                String.format("O2: %.0f / 100", stats.getOxygen()),
                x + 10f,
                firstY - gap * 2f + 20f
        );

        hudFont.getData().setScale(0.95f);
        hudFont.setColor(Color.LIGHT_GRAY);
        String iceText = "Gelo coletado: " + stats.getIceCollected();
        GlyphLayout layout = new GlyphLayout(hudFont, iceText);
        hudFont.draw(
                batch,
                iceText,
                hudViewport.getWorldWidth() - layout.width - 28f,
                hudViewport.getWorldHeight() - 35f
        );

        batch.end();
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (stats.isDead()) {
            return;
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Texture background = assets.getLuaBackgroundTexture();
        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        drawLuaFloor();

        Texture lunarBase = assets.getLunarBaseTexture();
        batch.draw(
                lunarBase,
                LUNAR_BASE_X,
                LUNAR_BASE_Y,
                LUNAR_BASE_WIDTH,
                LUNAR_BASE_HEIGHT
        );

        drawLuaItems();

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

        drawHud();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        updateCamera();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        hudFont.dispose();
        assets.dispose();
    }
}
