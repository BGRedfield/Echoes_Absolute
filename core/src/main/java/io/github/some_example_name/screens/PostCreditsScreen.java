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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Pós-créditos:
 * uma arte 2D desenhada em código mostra a nave deixando Aharin
 * e seguindo em direção à Terra.
 */
public class PostCreditsScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final Viewport viewport;

    private float time;
    private boolean changingScreen;
    private boolean disposed;

    public PostCreditsScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = PixelFontFactory.create();
        viewport = new ScreenViewport();
    }

    @Override
    public void show() {
        viewport.apply(true);
        time = 0f;
        changingScreen = false;
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        delta = Math.min(delta, 0.05f);
        time += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            changingScreen = true;
            dispose();
            game.setScreen(new AharinInfoScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.005f, 0.008f, 0.025f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        drawScene(w, h);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.05f);
        drawCentered("Depois dos créditos...", w / 2f, h - 78f);

        font.setColor(Color.valueOf("FFD54A"));
        font.getData().setScale(1.25f);
        drawCentered("A jornada ainda não terminou.", w / 2f, 92f);

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.9f);
        drawCentered("[ ENTER ] descobrir o destino de Aharin", w / 2f, 48f);

        batch.end();
    }

    private void drawScene(float w, float h) {
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Estrelas.
        shapes.setColor(Color.WHITE);
        for (int i = 0; i < 70; i++) {
            float x = (i * 137.31f) % w;
            float y = (i * 83.17f) % h;
            float twinkle = 0.8f + 0.2f * MathUtils.sin(time * 2f + i);
            shapes.circle(x, y, twinkle);
        }

        // Terra.
        float earthX = w * 0.82f;
        float earthY = h * 0.52f;
        float earthR = Math.min(w, h) * 0.16f;

        shapes.setColor(Color.valueOf("153A9B"));
        shapes.circle(earthX, earthY, earthR);

        shapes.setColor(Color.valueOf("4FA64F"));
        shapes.circle(earthX - earthR * 0.28f, earthY + earthR * 0.20f, earthR * 0.23f);
        shapes.circle(earthX + earthR * 0.15f, earthY - earthR * 0.10f, earthR * 0.30f);
        shapes.circle(earthX + earthR * 0.30f, earthY + earthR * 0.37f, earthR * 0.14f);

        shapes.setColor(new Color(1f, 1f, 1f, 0.22f));
        shapes.circle(earthX - earthR * 0.26f, earthY + earthR * 0.43f, earthR * 0.10f);
        shapes.circle(earthX + earthR * 0.31f, earthY - earthR * 0.42f, earthR * 0.12f);

        // Nave animada.
        float shipProgress = MathUtils.clamp(time / 8f, 0f, 1f);
        float shipStartX = w * 0.17f;
        float shipEndX = earthX - earthR - 85f;
        float shipX = MathUtils.lerp(shipStartX, shipEndX, shipProgress);
        float shipY = h * 0.37f + MathUtils.sin(time * 1.7f) * 10f;

        drawShip(shipX, shipY);

        // Linha de luz atrás da nave.
        shapes.setColor(new Color(1f, 0.85f, 0.25f, 0.35f));
        shapes.rect(
                shipX - 150f,
                shipY - 4f,
                110f,
                8f
        );

        shapes.end();
    }

    private void drawShip(float x, float y) {
        // Fuselagem.
        shapes.setColor(Color.valueOf("D9DDE7"));
        shapes.triangle(x + 80f, y, x - 55f, y + 38f, x - 55f, y - 38f);

        // Asa superior/inferior.
        shapes.setColor(Color.valueOf("8C95A8"));
        shapes.triangle(x + 10f, y + 10f, x - 35f, y + 62f, x - 4f, y + 18f);
        shapes.triangle(x + 10f, y - 10f, x - 35f, y - 62f, x - 4f, y - 18f);

        // Cockpit.
        shapes.setColor(Color.valueOf("53B8E8"));
        shapes.circle(x + 15f, y, 13f);

        // Motor.
        shapes.setColor(Color.valueOf("FFB52E"));
        shapes.circle(x - 47f, y, 15f);

        shapes.setColor(Color.valueOf("FFF0A5"));
        shapes.circle(x - 62f, y, 8f);
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
        shapes.dispose();
        font.dispose();
    }
}
