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

import io.github.some_example_name.managers.SaveManager;

/**
 * Tela de explicação de Calisto antes de Aharin.
 */
public class CalistoInfoScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;

    private boolean changingScreen;
    private boolean disposed;
    private final SaveManager.SaveData saveData;

    public CalistoInfoScreen(Game game) {
        this(game, SaveManager.load());
    }

    public CalistoInfoScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        this.saveData = saveData;
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
            SaveManager.SaveData data = saveData != null ? saveData : SaveManager.load();
            if (data == null) {
                data = new SaveManager.SaveData();
            }
            data.phase = SaveManager.Phase.AHARIN;
            SaveManager.save(data);

            changingScreen = true;
            dispose();
            game.setScreen(new AharinInfoScreen(game, data));
            return;
        }

        Gdx.gl.glClearColor(0.025f, 0.012f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        font.setColor(Color.valueOf("FFD54A"));
        font.getData().setScale(2.5f);
        drawCentered("CALISTO", w / 2f, h - 95f);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.05f);

        float y = h - 190f;
        String[] lines = {
                "Calisto foi o último mundo da jornada antes da descoberta de Aharin.",
                "Ali, cinco entidades de luz ofereceram bênçãos diferentes para o herói.",
                "Cada bênção representava uma escolha de evolução e sobrevivência.",
                "",
                "Depois do corredor, o antigo poder de Calisto despertou em três formas.",
                "Ao derrotá-lo, uma Chave de Luz revelou o caminho para além daquele mundo.",
                "",
                "A verdade encontrada em Calisto não era apenas sobre poder.",
                "Era sobre compreender por que a humanidade foi conduzida até aqui.",
                "",
                "O próximo destino não é apenas outro planeta.",
                "É Aharin."
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

        font.setColor(Color.valueOf("FFD54A"));
        font.getData().setScale(1.05f);
        drawCentered("[ ENTER ] CONTINUAR PARA AHARIN", w / 2f, 66f);

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
