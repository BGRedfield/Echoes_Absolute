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
    private final Rectangle loadButton = new Rectangle();
    private final Rectangle loadButton2 = new Rectangle();

    private boolean changingScreen;
    private boolean disposed;

    public MenuScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        font = PixelFontFactory.create();
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

        playButton.set(width / 2f - 170f, height / 2f - 70f, 340f, 70f);
        loadButton.set(width / 2f - 170f, height / 2f - 155f, 340f, 60f);
        loadButton2.set(width / 2f - 170f, height / 2f - 225f, 340f, 60f);
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            loadGame(1);
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            loadGame(2);
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            loadGame(SaveManager.getActiveSlot());
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
            } else if (SaveManager.hasSave(1) && loadButton.contains(x, y)) {
                loadGame(1);
            } else if (SaveManager.hasSave(2) && loadButton2.contains(x, y)) {
                loadGame(2);
            }
        }
    }

    private void startGame() {
        if (changingScreen) {
            return;
        }

        changingScreen = true;
        SaveManager.setActiveSlot(1);
        game.setScreen(new LuaScreen(game));
    }

    private void loadGame(int slot) {
        if (changingScreen || !SaveManager.hasSave(slot)) {
            return;
        }

        SaveManager.SaveData data = SaveManager.loadSlot(slot);
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
            case CALISTO:
                game.setScreen(new CalistoScreen(game, data));
                break;
            case AHARIN:
                game.setScreen(new AharinScreen(game, data));
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
        drawCentered("[ NOVO JOGO ]", width, playButton.y + 24f);

        font.getData().setScale(1.0f);

        font.setColor(SaveManager.hasSave(1) ? Color.CYAN : Color.DARK_GRAY);
        drawCentered(
                SaveManager.hasSave(1) ? "[ CONTINUAR SLOT 1 ]" : "[ SLOT 1 VAZIO ]",
                width,
                loadButton.y + 21f
        );

        font.setColor(SaveManager.hasSave(2) ? Color.CYAN : Color.DARK_GRAY);
        drawCentered(
                SaveManager.hasSave(2) ? "[ CONTINUAR SLOT 2 ]" : "[ SLOT 2 VAZIO ]",
                width,
                loadButton2.y + 21f
        );

        font.getData().setScale(0.82f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("ENTER / SPACE = novo jogo", width, 78f);
        drawCentered("1 = CONTINUAR SLOT 1 | 2 = CONTINUAR SLOT 2", width, 55f);
        drawCentered("L = carregar slot ativo | F = selecionar fase", width, 32f);
        drawCentered("ESC = sair", width, 10f);

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
