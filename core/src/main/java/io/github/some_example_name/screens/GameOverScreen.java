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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.entities.DeathCause;

/**
 * Black Game Over screen. The death cause is passed by the gameplay system.
 */
public class GameOverScreen extends ScreenAdapter {

    private final Game game;
    private final DeathCause deathCause;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;

    private final Rectangle retryButton = new Rectangle();
    private final Rectangle menuButton = new Rectangle();
    private boolean changingScreen = false;

    public GameOverScreen(Game game, DeathCause deathCause) {
        this.game = game;
        this.deathCause = deathCause == null ? DeathCause.UNKNOWN : deathCause;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        viewport = new ScreenViewport();
    }

    @Override
    public void show() {
        viewport.apply(true);
        layoutButtons();
        changingScreen = false;
    }

    private void layoutButtons() {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        float buttonWidth = 300f;
        float buttonHeight = 70f;
        float gap = 30f;
        float totalHeight = buttonHeight * 2f + gap;
        float startY = height / 2f - totalHeight / 2f - 40f;

        retryButton.set(
                width / 2f - buttonWidth / 2f,
                startY + buttonHeight + gap,
                buttonWidth,
                buttonHeight
        );

        menuButton.set(
                width / 2f - buttonWidth / 2f,
                startY,
                buttonWidth,
                buttonHeight
        );
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            retry();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goToMenu();
            return;
        }

        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (retryButton.contains(x, y)) {
                retry();
            } else if (menuButton.contains(x, y)) {
                goToMenu();
            }
        }
    }

    private void retry() {
        if (changingScreen) {
            return;
        }

        changingScreen = true;
        game.setScreen(new LuaScreen(game));
    }

    private void goToMenu() {
        if (changingScreen) {
            return;
        }

        changingScreen = true;
        game.setScreen(new MenuScreen(game));
    }

    @Override
    public void render(float delta) {
        handleInput();

        // A troca de tela aconteceu durante este render.
        // Não podemos continuar usando os objetos desta tela neste frame.
        if (changingScreen) {
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.18f, 0.18f, 0.20f, 1f));
        shapeRenderer.rect(
                retryButton.x,
                retryButton.y,
                retryButton.width,
                retryButton.height
        );
        shapeRenderer.rect(
                menuButton.x,
                menuButton.y,
                menuButton.width,
                menuButton.height
        );
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.getData().setScale(1.6f);
        font.setColor(Color.WHITE);
        drawCentered("VOCÊ MORREU", width, height / 2f + 190f);

        font.getData().setScale(1.1f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered(deathCause.getMessage(), width, height / 2f + 125f);

        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);
        drawCentered("RETRY", width, retryButton.y + 24f);
        drawCentered("VOLTAR AO MENU", width, menuButton.y + 24f);

        font.getData().setScale(1.0f);
        font.setColor(Color.GRAY);
        drawCentered("R = retry   |   M = menu   |   ESC = menu", width, 45f);

        batch.end();
    }

    private void drawCentered(String text, float screenWidth, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, screenWidth / 2f - layout.width / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        layoutButtons();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
