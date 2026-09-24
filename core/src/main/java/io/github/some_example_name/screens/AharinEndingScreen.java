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
 * Cutscenes finais de Aharin.
 *
 * 0 = final bom: volta à Terra e a salva.
 * 1 = final ruim: volta e domina a Terra.
 * 2 = final neutro: fica em Aharin entre os anjos.
 */
public class AharinEndingScreen extends ScreenAdapter {

    private static final float CUTSCENE_DURATION = 8f;

    private final Game game;
    private final int ending;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final Viewport viewport;

    private float time;
    private boolean changingScreen;
    private boolean disposed;

    public AharinEndingScreen(Game game, int ending) {
        this.game = game;
        this.ending = MathUtils.clamp(ending, 0, 2);
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

        if (time >= CUTSCENE_DURATION
                && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.008f, 0.006f, 0.025f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        drawScene(w, h);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawText(w, h);
        batch.end();
    }

    private void drawScene(float w, float h) {
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        drawStars(w, h);

        switch (ending) {
            case 0:
                drawGoodEnding(w, h);
                break;
            case 1:
                drawBadEnding(w, h);
                break;
            case 2:
            default:
                drawNeutralEnding(w, h);
                break;
        }

        shapes.end();
    }

    private void drawStars(float w, float h) {
        for (int i = 0; i < 100; i++) {
            float x = (i * 97.13f) % w;
            float y = (i * 59.27f) % h;
            float pulse = 0.8f + 0.35f * MathUtils.sin(time * 1.7f + i);
            shapes.setColor(new Color(1f, 1f, 1f, 0.65f));
            shapes.circle(x, y, pulse);
        }
    }

    private void drawNeutralEnding(float w, float h) {
        // Aharin.
        float planetX = w * 0.50f;
        float planetY = h * 0.08f;
        float radius = h * 0.67f;

        shapes.setColor(Color.valueOf("E4B52F"));
        shapes.circle(planetX, planetY, radius);

        shapes.setColor(Color.valueOf("F7D95E"));
        shapes.circle(planetX - radius * 0.32f, planetY + radius * 0.12f, radius * 0.20f);
        shapes.circle(planetX + radius * 0.26f, planetY + radius * 0.25f, radius * 0.15f);
        shapes.circle(planetX + radius * 0.36f, planetY - radius * 0.12f, radius * 0.12f);

        // Muitos anjos ao redor.
        for (int i = 0; i < 13; i++) {
            float angle = i / 13f * MathUtils.PI2 + time * 0.15f;
            float orbit = 180f + (i % 3) * 45f;
            float x = planetX + MathUtils.cos(angle) * orbit;
            float y = h * 0.53f + MathUtils.sin(angle) * orbit * 0.35f;
            drawAngel(x, y, 0.72f + (i % 3) * 0.06f);
        }

        // Astronauta entre eles.
        float astronautX = w * 0.50f;
        float astronautY = h * 0.53f + MathUtils.sin(time * 1.2f) * 5f;
        drawAstronaut(astronautX, astronautY, 1.2f);
    }

    private void drawBadEnding(float w, float h) {
        float earthX = w * 0.60f;
        float earthY = h * 0.48f;
        float radius = h * 0.24f;

        drawEarth(earthX, earthY, radius, Color.valueOf("8E1717"));

        // "Bolas correntes de metais": elos orbitando a Terra.
        shapes.setColor(Color.valueOf("8A8F99"));
        for (int i = 0; i < 15; i++) {
            float angle = i / 15f * MathUtils.PI2 + time * 0.12f;
            float orbit = radius * 1.42f + (i % 2) * 34f;
            float x = earthX + MathUtils.cos(angle) * orbit;
            float y = earthY + MathUtils.sin(angle) * orbit * 0.68f;
            shapes.circle(x, y, 18f);
            shapes.setColor(Color.valueOf("373B43"));
            shapes.circle(x, y, 9f);
            shapes.setColor(Color.valueOf("8A8F99"));
        }

        // Correntes ligando os elos.
        for (int i = 0; i < 14; i++) {
            float a1 = i / 15f * MathUtils.PI2 + time * 0.12f;
            float a2 = (i + 1) / 15f * MathUtils.PI2 + time * 0.12f;
            float orbit = radius * 1.42f + (i % 2) * 34f;

            float x1 = earthX + MathUtils.cos(a1) * orbit;
            float y1 = earthY + MathUtils.sin(a1) * orbit * 0.68f;
            float x2 = earthX + MathUtils.cos(a2) * orbit + 28f;
            float y2 = earthY + MathUtils.sin(a2) * orbit * 0.68f;

            shapes.setColor(Color.valueOf("5C606A"));
            shapes.rectLine(x1, y1, x2, y2, 8f);
        }

        // Nave chegando.
        float progress = MathUtils.clamp(time / CUTSCENE_DURATION, 0f, 1f);
        float shipX = MathUtils.lerp(w * 0.08f, earthX - radius - 75f, progress);
        float shipY = h * 0.72f + MathUtils.sin(time * 1.3f) * 7f;
        drawShip(shipX, shipY);

        // Sombra vermelha da atmosfera.
        shapes.setColor(new Color(1f, 0.08f, 0.05f, 0.16f));
        shapes.circle(earthX, earthY, radius + 22f);
    }

    private void drawGoodEnding(float w, float h) {
        float earthX = w * 0.68f;
        float earthY = h * 0.48f;
        float radius = h * 0.24f;

        drawEarth(earthX, earthY, radius, Color.valueOf("D9A92B"));

        // Anjos protegendo e rodeando a Terra.
        for (int i = 0; i < 14; i++) {
            float angle = i / 14f * MathUtils.PI2 + time * 0.18f;
            float orbit = radius * 1.50f;
            float x = earthX + MathUtils.cos(angle) * orbit;
            float y = earthY + MathUtils.sin(angle) * orbit * 0.60f;
            drawAngel(x, y, 0.72f);
        }

        // Nuvem dourada na Terra.
        shapes.setColor(new Color(1f, 0.92f, 0.25f, 0.18f));
        shapes.circle(earthX, earthY, radius + 35f);

        // Nave retornando.
        float progress = MathUtils.clamp(time / CUTSCENE_DURATION, 0f, 1f);
        float shipX = MathUtils.lerp(w * 0.10f, earthX - radius - 78f, progress);
        float shipY = h * 0.70f + MathUtils.sin(time * 1.3f) * 8f;
        drawShip(shipX, shipY);
    }

    private void drawEarth(float x, float y, float radius, Color surface) {
        shapes.setColor(surface);
        shapes.circle(x, y, radius);

        shapes.setColor(Color.valueOf("5FA45A"));
        shapes.circle(x - radius * 0.28f, y + radius * 0.18f, radius * 0.24f);
        shapes.circle(x + radius * 0.18f, y - radius * 0.09f, radius * 0.29f);
        shapes.circle(x + radius * 0.34f, y + radius * 0.35f, radius * 0.12f);

        shapes.setColor(new Color(1f, 1f, 1f, 0.22f));
        shapes.circle(x - radius * 0.23f, y + radius * 0.41f, radius * 0.11f);
        shapes.circle(x + radius * 0.30f, y - radius * 0.37f, radius * 0.10f);
    }

    private void drawAngel(float x, float y, float scale) {
        float bodyW = 30f * scale;
        float bodyH = 48f * scale;

        shapes.setColor(new Color(1f, 0.90f, 0.35f, 0.15f));
        shapes.circle(x, y, 38f * scale);

        shapes.setColor(Color.valueOf("FFF0B2"));
        shapes.circle(x, y + bodyH * 0.52f, 13f * scale);
        shapes.rect(x - bodyW / 2f, y - bodyH / 2f, bodyW, bodyH);

        shapes.triangle(
                x - bodyW * 0.25f, y + bodyH * 0.25f,
                x - bodyW * 1.8f, y + bodyH * 0.82f,
                x - bodyW * 0.78f, y - bodyH * 0.05f
        );
        shapes.triangle(
                x + bodyW * 0.25f, y + bodyH * 0.25f,
                x + bodyW * 1.8f, y + bodyH * 0.82f,
                x + bodyW * 0.78f, y - bodyH * 0.05f
        );
    }

    private void drawAstronaut(float x, float y, float scale) {
        float bodyW = 34f * scale;
        float bodyH = 52f * scale;

        shapes.setColor(Color.valueOf("E9EEF5"));
        shapes.circle(x, y + bodyH * 0.55f, 15f * scale);
        shapes.rect(x - bodyW / 2f, y - bodyH / 2f, bodyW, bodyH);

        shapes.setColor(Color.valueOf("4E9ACD"));
        shapes.rect(x - 11f * scale, y + 4f * scale, 22f * scale, 12f * scale);

        shapes.setColor(Color.valueOf("C9D2DD"));
        shapes.rect(x - bodyW * 0.75f, y - 30f * scale, 10f * scale, 20f * scale);
        shapes.rect(x + bodyW * 0.45f, y - 30f * scale, 10f * scale, 20f * scale);

        shapes.setColor(Color.valueOf("AEB8C4"));
        shapes.rect(x - 14f * scale, y - 43f * scale, 10f * scale, 18f * scale);
        shapes.rect(x + 4f * scale, y - 43f * scale, 10f * scale, 18f * scale);
    }

    private void drawShip(float x, float y) {
        shapes.setColor(Color.valueOf("DEE4EC"));
        shapes.triangle(x + 76f, y, x - 52f, y + 34f, x - 52f, y - 34f);

        shapes.setColor(Color.valueOf("8792A6"));
        shapes.triangle(x + 8f, y + 9f, x - 34f, y + 56f, x - 2f, y + 15f);
        shapes.triangle(x + 8f, y - 9f, x - 34f, y - 56f, x - 2f, y - 15f);

        shapes.setColor(Color.valueOf("4EB6E8"));
        shapes.circle(x + 14f, y, 12f);

        shapes.setColor(Color.valueOf("FFB62F"));
        shapes.circle(x - 44f, y, 14f);
        shapes.setColor(Color.valueOf("FFF0A2"));
        shapes.circle(x - 61f, y, 7f);
    }

    private void drawText(float w, float h) {
        String title;
        String message;

        if (ending == 0) {
            title = "FINAL BOM";
            message = "Você voltou para a Terra levando a sabedoria de Aharin.\nAgora os anjos guardam a Terra ao seu lado.";
        } else if (ending == 1) {
            title = "FINAL RUIM";
            message = "A Terra se tornou um mundo acorrentado.\nO poder que deveria libertar acabou dominando.";
        } else {
            title = "FINAL NEUTRO";
            message = "Você ficou em Aharin.\nEntre os anjos, encontrou um novo lar para proteger.";
        }

        font.setColor(Color.valueOf("FFD54A"));
        font.getData().setScale(2.4f);
        drawCentered(title, w / 2f, h - 70f);

        if (time >= 3f) {
            font.setColor(Color.WHITE);
            font.getData().setScale(1.08f);
            String[] lines = message.split("\n");
            float y = 105f + lines.length * 42f;

            for (String line : lines) {
                drawCentered(line, w / 2f, y);
                y -= 42f;
            }
        }

        if (time >= CUTSCENE_DURATION) {
            font.setColor(Color.valueOf("FFD54A"));
            font.getData().setScale(0.95f);
            drawCentered("[ ENTER ] VOLTAR AO MENU", w / 2f, 45f);
        }
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
