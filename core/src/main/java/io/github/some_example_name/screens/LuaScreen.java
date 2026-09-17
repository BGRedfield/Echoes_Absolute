package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.Laser;
import io.github.some_example_name.entities.LuaItem;
import io.github.some_example_name.entities.Player;
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
    private final SpriteBatch batch;
    private final AssetManager assets;
    private final Player player;
    private final Array<Laser> lasers;
    private final Array<LuaItem> luaItems;

    public LuaScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        batch = new SpriteBatch();
        assets = new AssetManager();
        assets.load();

        player = new Player(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
        lasers = new Array<>();
        luaItems = new Array<>();

        createLuaResources();
        updateCamera();
    }

    private void createLuaResources() {
        // Comida: cubos/itens laranjas.
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

        // Tanques de O2: objetos propositalmente mais altos e estreitos.
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

        // Gelo: cubos azuis.
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
        updateCamera();
    }

    private void update(float delta) {
        delta = Math.min(delta, 0.05f);

        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);

        // Agora cada clique do mouse dispara exatamente um laser.
        if (Gdx.input.justTouched()) {
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

                batch.draw(
                        tile,
                        x,
                        y,
                        width,
                        height
                );
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

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Fundo geral.
        Texture background = assets.getLuaBackgroundTexture();
        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        // Sistema de tiles do chão. Quando lua_tile.png for colocado,
        // ele será repetido por todo o mapa em uma grade de 128x128.
        drawLuaFloor();

        // Base lunar.
        Texture lunarBase = assets.getLunarBaseTexture();
        batch.draw(
                lunarBase,
                LUNAR_BASE_X,
                LUNAR_BASE_Y,
                LUNAR_BASE_WIDTH,
                LUNAR_BASE_HEIGHT
        );

        // Recursos espalhados pela Lua.
        drawLuaItems();

        // Lasers disparados pelos cliques.
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

        // Jogador.
        Texture playerTexture = assets.getPlayerTexture();
        batch.draw(
                playerTexture,
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        updateCamera();
    }

    @Override
    public void dispose() {
        batch.dispose();
        assets.dispose();
    }
}
