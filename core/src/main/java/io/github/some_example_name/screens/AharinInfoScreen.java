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

/**
 * Tela de explicação de Aharin antes da fase.
 */
public class AharinInfoScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;

    private boolean changingScreen;
    private boolean disposed;

    public AharinInfoScreen(Game game) {
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

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            changingScreen = true;
            dispose();
            game.setScreen(new AharinScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.12f, 0.06f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        font.setColor(Color.valueOf("FFD43B"));
        font.getData().setScale(2.6f);
        drawCentered("AHARIN", w / 2f, h - 95f);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.05f);

        float y = h - 190f;
        String[] lines = {
                "Aharin é um planeta dourado, habitado por entidades de luz.",
                "Os anjos observam os mundos e guardam conhecimentos antigos.",
                "",
                "Foi em Aharin que eles acompanharam a sua jornada à distância.",
                "As bênçãos entregues em Calisto não eram aleatórias.",
                "Elas eram uma forma de preparar você para entender a verdade.",
                "",
                "Agora eles contarão por que decidiram ajudar.",
                "Depois disso, Aharin colocará uma escolha final em suas mãos.",
                "",
                "Sua decisão definirá o destino da Terra",
                "e o destino da própria jornada."
        };

        for (String line : lines) {
            if (line.isEmpty()) {
                y -= 24f;
                continue;
            }
            GlyphLayout layout = new GlyphLayout(font, line);
            font.draw(batch, line, w / 2f - layout.width / 2f, y);
            y -= 43f;
        }

        font.setColor(Color.valueOf("FFD43B"));
        font.getData().setScale(1.05f);
        drawCentered("[ ENTER ] ENTRAR EM AHARIN", w / 2f, 66f);

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
        font.dispose();
    }
}
