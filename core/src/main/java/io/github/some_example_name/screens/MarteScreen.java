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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.Player;

/**
 * Simple destination screen so the Lua portal has a working target immediately.
 * Art can be replaced with assets later without changing the progression.
 */
public class MarteScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final Player player;
    private final Vector2 worldSize = new Vector2(2400f, 1400f);
    private final Rectangle exitBox = new Rectangle(1100f, 500f, 200f, 180f);

    public MarteScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        viewport = new ScreenViewport();
        player = new Player(350f, 550f);
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        player.update(delta, worldSize.x, worldSize.y);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.26f, 0.07f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.50f, 0.16f, 0.05f, 1f));
        shapeRenderer.rect(0f, 0f, worldSize.x, worldSize.y);

        shapeRenderer.setColor(new Color(0.15f, 0.80f, 1f, 1f));
        shapeRenderer.rect(exitBox.x, exitBox.y, exitBox.width, exitBox.height);

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        drawCentered("MARTE", viewport.getWorldWidth(), viewport.getWorldHeight() - 70f);
        font.getData().setScale(1f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("Portal da Lua concluído. Esta é a entrada da fase Marte.", viewport.getWorldWidth(), viewport.getWorldHeight() - 120f);
        drawCentered("ESC = voltar ao menu", viewport.getWorldWidth(), 45f);
        batch.end();
    }

    private void drawCentered(String text, float screenWidth, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, screenWidth / 2f - layout.width / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
