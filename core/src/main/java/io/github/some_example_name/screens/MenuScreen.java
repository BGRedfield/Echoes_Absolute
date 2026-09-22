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
import com.badlogic.gdx.math.Rectangle;

import io.github.some_example_name.managers.SaveManager;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;
    private final Rectangle playButton = new Rectangle();

    private boolean changingScreen;
    private boolean disposed;

    public MenuScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new ScreenViewport();
        font.setColor(Color.WHITE);
    }

    @Override
    public void show() {
        viewport.apply(true);
        layoutButton();
        changingScreen = false;
    }

    private void layoutButton() {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        playButton.set(width / 2f - 170f, height / 2f - 55f, 340f, 80f);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            loadGame();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            openPhaseSelect();
            return;
        }

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
        if (changingScreen) {
            return;
        }

        changingScreen = true;
        game.setScreen(new LuaScreen(game));
    }

    private void loadGame() {
        if (changingScreen || !SaveManager.hasSave()) {
            return;
        }

        SaveManager.SaveData data = SaveManager.load();
        if (data == null) {
            return;
        }

        changingScreen = true;
        dispose();

        switch (data.phase) {
            case LUA:
                game.setScreen(new LuaScreen(game, data));
                break;
            case MARTE:
                game.setScreen(new LuaMarteScreen(game, data));
                break;
            case TITA:
                game.setScreen(new titascreen(game, data));
                break;
            default:
                game.setScreen(new LuaScreen(game, data));
                break;
        }
    }

    private void openPhaseSelect() {
        if (changingScreen) {
            return;
        }

        changingScreen = true;
        dispose();
        game.setScreen(new PhaseSelectScreen(game));
    }

    private void drawCentered(String text, float screenWidth, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, screenWidth / 2f - layout.width / 2f, y);
    }

    @Override
    public void render(float delta) {
        handleInput();

        if (changingScreen) {
            return;
        }

        Gdx.gl.glClearColor(0.03f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.getData().setScale(1.6f);
        font.setColor(Color.WHITE);
        drawCentered("ECHOES ABSOLUTE", width, height / 2f + 120f);

        font.getData().setScale(1.15f);
        drawCentered("[ JOGAR ]", width, playButton.y + 27f);

        font.getData().setScale(1.0f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("ENTER / SPACE = novo jogo", width, 78f);

        if (SaveManager.hasSave()) {
            font.setColor(Color.CYAN);
            drawCentered("L = carregar último save", width, 52f);
        } else {
            font.setColor(Color.DARK_GRAY);
            drawCentered("L = carregar (nenhum save)", width, 52f);
        }

        font.setColor(Color.LIGHT_GRAY);
        drawCentered("F = selecionar fase", width, 28f);
        drawCentered("ESC = sair", width, 8f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        layoutButton();
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
