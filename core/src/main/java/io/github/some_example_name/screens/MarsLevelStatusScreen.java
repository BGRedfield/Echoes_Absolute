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

/** Mars completion screen. The next planet will be added later. */
public class MarsLevelStatusScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;
    private boolean changingScreen;
    private boolean disposed;

    public MarsLevelStatusScreen(Game game, float health, float hunger, float oxygen) {
        this.game = game;
        batch = new SpriteBatch();
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

        font.getData().setScale(1.25f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "O Alien Supremo foi derrotado.", centerX - 165f, centerY + 35f);
        font.draw(batch, "A jornada continuará na próxima fase.", centerX - 185f, centerY - 15f);

        font.getData().setScale(1.15f);
        font.setColor(Color.ORANGE);
        font.draw(batch, "ENTER = voltar ao menu", centerX - 130f, centerY - 115f);
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
