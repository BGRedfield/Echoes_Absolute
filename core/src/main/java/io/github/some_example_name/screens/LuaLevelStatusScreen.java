package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.managers.SaveManager;

/** Shows the Lua level results before the Mars introduction. */
public class LuaLevelStatusScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final float health;
    private final float hunger;
    private final float oxygen;
    private final int iceCollected;
    private boolean changingScreen;
    private boolean disposed;

    public LuaLevelStatusScreen(
            Game game,
            float health,
            float hunger,
            float oxygen,
            int iceCollected
    ) {
        this.game = game;
        this.health = health;
        this.hunger = hunger;
        this.oxygen = oxygen;
        this.iceCollected = iceCollected;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        viewport = new ScreenViewport();
    }

    @Override
    public void show() {
        changingScreen = false;
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            changingScreen = true;
            SaveManager.SaveData data = SaveManager.load();
            if (data == null) {
                data = new SaveManager.SaveData();
            }
            data.phase = SaveManager.Phase.MARTE;
            data.playerX = 400f;
            data.playerY = 400f;
            data.health = health;
            data.hunger = hunger;
            data.oxygen = oxygen;
            SaveManager.save(data);

            dispose();
            game.setScreen(new MarsVideoScreen(game, data));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.025f, 0.025f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(false);

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.05f, 0.06f, 0.10f, 1f));
        shapeRenderer.rect(centerX - 380f, centerY - 245f, 760f, 490f);
        shapeRenderer.setColor(new Color(0.85f, 0.10f, 0.10f, 1f));
        shapeRenderer.rect(centerX - 380f, centerY + 185f, 760f, 8f);
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2.4f);
        font.draw(batch, "LUA CONCLUÍDA", centerX - 190f, centerY + 135f);

        font.getData().setScale(1.25f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, String.format("HP final: %.0f / 100", health), centerX - 230f, centerY + 70f);
        font.draw(batch, String.format("Fome final: %.0f / 100", hunger), centerX - 230f, centerY + 25f);
        font.draw(batch, String.format("O2 final: %.0f / 100", oxygen), centerX - 230f, centerY - 20f);
        font.draw(batch, "Gelos usados na missão: " + iceCollected + "/5", centerX - 230f, centerY - 65f);
        font.setColor(Color.ORANGE);
        font.draw(batch, "TRUMP: DERROTADO", centerX - 230f, centerY - 110f);

        font.getData().setScale(1.15f);
        font.setColor(Color.WHITE);
        font.draw(batch, "ENTER = continuar para a introdução de Marte", centerX - 250f, centerY - 185f);
        font.setColor(Color.GRAY);
        font.getData().setScale(0.95f);
        font.draw(batch, "ESC = voltar ao menu", centerX - 90f, 45f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        font.dispose();
    }
}
