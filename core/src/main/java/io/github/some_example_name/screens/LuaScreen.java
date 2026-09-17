package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
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

    private static final float LASER_INTERVAL = 0.5f;

    private final Game game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final AssetManager assets;
    private final Player player;
    private final Array<Laser> lasers;

    private float laserTimer;

    public LuaScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, camera);
        batch = new SpriteBatch();
        assets = new AssetManager();
        assets.load();

        player = new Player(PLAYER_SPAWN_X, PLAYER_SPAWN_Y);
        lasers = new Array<>();
        laserTimer = 0f;

        updateCamera();
    }

    @Override
    public void show() {
        viewport.apply(true);
        updateCamera();
    }

    private void update(float delta) {
        delta = Math.min(delta, 0.05f);

        player.update(delta, WORLD_WIDTH, WORLD_HEIGHT);

        laserTimer += delta;

        while (laserTimer >= LASER_INTERVAL) {
            laserTimer -= LASER_INTERVAL;

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

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Texture background = assets.getLuaBackgroundTexture();
        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        Texture lunarBase = assets.getLunarBaseTexture();
        batch.draw(
                lunarBase,
                LUNAR_BASE_X,
                LUNAR_BASE_Y,
                LUNAR_BASE_WIDTH,
                LUNAR_BASE_HEIGHT
        );

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
