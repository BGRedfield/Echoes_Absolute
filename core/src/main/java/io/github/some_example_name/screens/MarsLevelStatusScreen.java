package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.managers.SaveManager;

/** Mars completion screen. The next planet will be added later. */
public class MarsLevelStatusScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;
    private final float health;
    private final float hunger;
    private final float oxygen;
    private boolean changingScreen;
    private boolean disposed;

    public MarsLevelStatusScreen(Game game, float health, float hunger, float oxygen) {
        this.game = game;
        this.health = health;
        this.hunger = hunger;
        this.oxygen = oxygen;
        batch = new SpriteBatch();
        font = PixelFontFactory.create();
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
            data.phase = SaveManager.Phase.TITA;
            data.playerX = 600f;
            data.playerY = 600f;
            data.health = health;
            data.hunger = hunger;
            data.oxygen = oxygen;
            SaveManager.save(data);

            dispose();
            game.setScreen(new TitaIntroScreen(game, data));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.14f, 0.035f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(false);

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f;

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.5f);
        font.draw(batch, "MARTE CONCLUÍDA", centerX - 215f, centerY + 105f);

        font.getData().setScale(1.15f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "O Alien Supremo foi derrotado.", centerX - 165f, centerY + 55f);
        font.draw(batch, String.format("HP %.0f/100 | Fome %.0f/100 | O2 %.0f/100", health, hunger, oxygen),
                centerX - 215f, centerY + 15f);
        font.draw(batch, "Próxima fase: TITÃ — mundo vermelho-escuro.", centerX - 220f, centerY - 30f);
        font.draw(batch, "Quatro fortalezas aguardam: Obama, Authentic, Verity e CR7.", centerX - 255f, centerY - 70f);

        font.getData().setScale(1.15f);
        font.setColor(Color.ORANGE);
        font.draw(batch, "ENTER = ver o status e a descrição de TITÃ", centerX - 225f, centerY - 135f);
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
        font.dispose();
    }
}
