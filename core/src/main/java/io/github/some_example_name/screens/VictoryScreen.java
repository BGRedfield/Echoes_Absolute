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

/** Final victory screen after defeating CR7 on Titan. */
public class VictoryScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private boolean changingScreen;
    private boolean disposed;
    private final String title;
    private final String subtitle;

    public VictoryScreen(Game game) {
        this(game, "TITÃ CONCLUÍDO", "Você conquistou o planeta Titã.");
    }

    public VictoryScreen(Game game, String title, String subtitle) {
        this.game = game;
        this.title = title;
        this.subtitle = subtitle;
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.12f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(false);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        float centerX = width / 2f;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.55f, 0.03f, 0.06f, 1f));
        shapeRenderer.circle(centerX, height / 2f + 40f, 160f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(centerX, height / 2f + 40f, 18f);
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2.7f);
        drawCentered(title, centerX, height / 2f + 250f);

        font.getData().setScale(1.35f);
        font.setColor(Color.ORANGE);
        drawCentered(
                title.startsWith("CALISTO")
                        ? "O BOSS FINAL foi derrotado."
                        : "CR7 foi derrotado.",
                centerX,
                height / 2f + 150f
        );

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(1.08f);
        if (title.startsWith("CALISTO")) {
            drawCentered("As cinco bênçãos abriram o caminho da evolução.", centerX,
                    height / 2f + 95f);
            drawCentered(subtitle, centerX, height / 2f + 55f);
        } else {
            drawCentered("Os três cristais abriram o castelo final.", centerX,
                    height / 2f + 95f);
            drawCentered(subtitle, centerX, height / 2f + 55f);
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.15f);
        drawCentered("ENTER = voltar ao menu", centerX, height / 2f - 210f);
        font.setColor(Color.GRAY);
        font.getData().setScale(0.90f);
        drawCentered("ESC = menu", centerX, 45f);

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
