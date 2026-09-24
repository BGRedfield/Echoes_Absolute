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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.managers.SaveManager;

/** Status and description screen shown before entering Titan. */
public class TitaIntroScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private boolean changingScreen;
    private boolean disposed;

    private final SaveManager.SaveData saveData;

    public TitaIntroScreen(Game game) {
        this(game, SaveManager.load());
    }

    public TitaIntroScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        this.saveData = saveData;
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
            dispose();
            SaveManager.SaveData data = saveData != null ? saveData : SaveManager.load();
            if (data == null) {
                data = new SaveManager.SaveData();
            }
            data.phase = SaveManager.Phase.TITA;
            SaveManager.save(data);

            dispose();
            game.setScreen(new titascreen(game, data));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.20f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(false);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        float centerX = width / 2f;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.34f, 0.025f, 0.03f, 1f));
        shapeRenderer.circle(centerX, height / 2f + 65f, 185f);
        shapeRenderer.setColor(new Color(0.48f, 0.045f, 0.055f, 1f));
        shapeRenderer.circle(centerX - 95f, height / 2f + 125f, 34f);
        shapeRenderer.circle(centerX + 95f, height / 2f + 35f, 48f);
        shapeRenderer.circle(centerX + 12f, height / 2f - 45f, 31f);
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2.7f);
        drawCentered("TITÃ", centerX, height / 2f + 285f);

        font.getData().setScale(1.25f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("O satélite vermelho-escuro guarda quatro fortalezas.", centerX, height / 2f + 225f);
        drawCentered("Três mini castelos escondem cristais e um castelo final abriga o chefe CR7.", centerX, height / 2f + 185f);

        font.getData().setScale(1.05f);
        drawCentered("MINI CASTELOS", centerX, height / 2f + 95f);
        drawCentered("• Norte: Barack Obama → cristal vermelho", centerX, height / 2f + 55f);
        drawCentered("• Sul: Authentic Games → cristal azul", centerX, height / 2f + 15f);
        drawCentered("• Oeste: Verity → cristal amarelo", centerX, height / 2f - 25f);
        drawCentered("• Centro-leste: castelo final → CR7", centerX, height / 2f - 65f);

        font.setColor(Color.ORANGE);
        font.getData().setScale(1.2f);
        drawCentered("E = entrar nos castelos", centerX, height / 2f - 145f);
        drawCentered("Colete os 3 cristais e coloque-os nos 3 altares de pedra.", centerX, height / 2f - 185f);
        font.setColor(Color.WHITE);
        drawCentered("ENTER = entrar em TITA", centerX, 58f);

        batch.end();
    }

    private void drawCentered(String text, float centerX, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, centerX - layout.width / 2f, y);
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
