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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final Rectangle playButton = new Rectangle();

    public MenuScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.6f);
        viewport = new ScreenViewport();
    }

    @Override
    public void show() {
        viewport.apply(true);
        layoutButton();
    }

    private void layoutButton() {
        playButton.set(
                viewport.getWorldWidth() / 2f - 170f,
                viewport.getWorldHeight() / 2f - 55f,
                340f,
                80f
        );
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            startGame();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (playButton.contains(x, y)) {
                startGame();
            }
        }
    }

    private void startGame() {
        dispose();
        game.setScreen(new LuaScreen(game));
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0.03f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.12f, 0.14f, 0.19f, 1f));
        shapeRenderer.rect(
                playButton.x,
                playButton.y,
                playButton.width,
                playButton.height
        );
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        drawCentered("ECHOES ABSOLUTE", width, height / 2f + 120f);

        font.getData().setScale(1.5f);
        drawCentered("JOGAR", width, playButton.y + 27f);

        font.getData().setScale(1.0f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("ENTER / SPACE = jogar   |   ESC = sair", width, 45f);

        batch.end();
    }

    private void drawCentered(String text, float screenWidth, float y) {
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, text);
        font.draw(batch, text, screenWidth / 2f - layout.width / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        layoutButton();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
