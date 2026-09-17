package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Debug/test phase selector: jump directly to an implemented phase. */
public class PhaseSelectScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;

    private boolean changingScreen;
    private boolean disposed;

    public PhaseSelectScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new ScreenViewport();
    }

    @Override
    public void show() {
        viewport.apply(true);
        changingScreen = false;
    }

    private void handleInput() {
        if (changingScreen) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            changingScreen = true;
            dispose();
            game.setScreen(new LuaScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            changingScreen = true;
            dispose();
            game.setScreen(new LuaMarteScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            // TitanScreen is not implemented in the current repository yet.
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
        }
    }

    private void drawCentered(String text, float width, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, width / 2f - layout.width / 2f, y);
    }

    @Override
    public void render(float delta) {
        handleInput();
        if (changingScreen) return;

        Gdx.gl.glClearColor(0.03f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.55f);
        drawCentered("SELECIONAR FASE", width, height / 2f + 190f);

        font.getData().setScale(1.15f);
        font.setColor(Color.WHITE);
        drawCentered("1 - LUA", width, height / 2f + 80f);
        drawCentered("2 - MARTE", width, height / 2f + 20f);

        font.setColor(Color.GRAY);
        drawCentered("3 - TITÃ (ainda não implementado)", width, height / 2f - 40f);

        font.getData().setScale(0.95f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("1/2 = entrar direto na fase", width, 70f);
        drawCentered("ESC = voltar ao menu", width, 38f);

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
        if (disposed) return;
        disposed = true;
        batch.dispose();
        font.dispose();
    }
}
