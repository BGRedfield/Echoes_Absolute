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

/** Introduction screen shown immediately before entering the Mars phase. */
public class MarsIntroScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private boolean changingScreen;
    private boolean disposed;

    private final SaveManager.SaveData saveData;

    public MarsIntroScreen(Game game) {
        this(game, SaveManager.load());
    }

    public MarsIntroScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
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
            dispose();
            SaveManager.SaveData data = SaveManager.load();
            if (data == null) {
                data = new SaveManager.SaveData();
            }
            data.phase = SaveManager.Phase.MARTE;
            data.playerX = 400f;
            data.playerY = 400f;
            SaveManager.save(data);
            game.setScreen(new LuaMarteScreen(game, data));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.20f, 0.035f, 0.015f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(false);

        float centerX = viewport.getWorldWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(0.30f, 0.06f, 0.02f, 1f));
        shapeRenderer.circle(centerX, centerY + 45f, 175f);

        shapeRenderer.setColor(new Color(0.42f, 0.10f, 0.025f, 1f));
        shapeRenderer.circle(centerX - 105f, centerY + 115f, 35f);
        shapeRenderer.circle(centerX + 85f, centerY - 15f, 48f);
        shapeRenderer.circle(centerX - 20f, centerY - 85f, 30f);
        shapeRenderer.circle(centerX + 125f, centerY + 95f, 24f);

        shapeRenderer.setColor(new Color(0.65f, 0.20f, 0.05f, 1f));
        shapeRenderer.circle(centerX - 70f, centerY + 30f, 10f);
        shapeRenderer.circle(centerX + 70f, centerY + 75f, 14f);
        shapeRenderer.circle(centerX + 20f, centerY - 55f, 9f);
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2.6f);
        drawCentered("MARTE", centerX, centerY + 285f);

        font.getData().setScale(1.25f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("A próxima fase começa em Marte.", centerX, centerY + 235f);

        font.getData().setScale(1.05f);
        drawCentered("Depois de sobreviver à Lua, você chegou a um mundo vermelho,", centerX, centerY - 190f);
        drawCentered("seco e enorme. Explore o território marciano e descubra o próximo objetivo.", centerX, centerY - 225f);

        font.getData().setScale(1.3f);
        font.setColor(Color.ORANGE);
        drawCentered("ENTER = entrar em Marte", centerX, centerY - 300f);

        font.getData().setScale(0.95f);
        font.setColor(Color.GRAY);
        drawCentered("ESC = voltar ao menu", centerX, 45f);

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
