package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Pause menu shared by the playable phases. */
public class PauseMenu {

    public enum Action {
        NONE,
        RESUME,
        PHASES,
        SAVE_SLOT_1,
        SAVE_SLOT_2,
        MENU
    }

    private static final String[] OPTIONS = {
            "VOLTAR",
            "SALVAR 1",
            "SALVAR 2",
            "FASES",
            "SAIR"
    };

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final Rectangle[] optionBoxes = {
            new Rectangle(),
            new Rectangle(),
            new Rectangle(),
            new Rectangle(),
            new Rectangle()
    };

    private boolean open;
    private int selectedOption;
    private boolean disposed;

    public PauseMenu(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = PixelFontFactory.create();
        viewport = new ScreenViewport();
        font.setColor(Color.WHITE);
    }

    public Action handleInput() {
        if (disposed) {
            return Action.NONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            open = !open;
            return Action.NONE;
        }

        if (!open) {
            return Action.NONE;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedOption = (selectedOption + OPTIONS.length - 1) % OPTIONS.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedOption = (selectedOption + 1) % OPTIONS.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectedOption = 0;
            return confirm();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            selectedOption = 1;
            return confirm();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            selectedOption = 2;
            return confirm();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            selectedOption = 3;
            return confirm();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            selectedOption = 4;
            return confirm();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return confirm();
        }

        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = Gdx.graphics.getHeight() - Gdx.input.getY();

            for (int i = 0; i < optionBoxes.length; i++) {
                if (optionBoxes[i].contains(x, y)) {
                    selectedOption = i;
                    return confirm();
                }
            }
        }

        return Action.NONE;
    }

    private Action confirm() {
        open = false;

        switch (selectedOption) {
            case 0:
                return Action.RESUME;
            case 1:
                return Action.SAVE_SLOT_1;
            case 2:
                return Action.SAVE_SLOT_2;
            case 3:
                return Action.PHASES;
            case 4:
                return Action.MENU;
            default:
                return Action.NONE;
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        if (!open || disposed) {
            return;
        }

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0f, 0f, 0f, 0.72f));
        shapeRenderer.rect(0f, 0f, width, height);

        float panelWidth = Math.min(540f, width - 80f);
        float panelHeight = 540f;
        float panelX = (width - panelWidth) / 2f;
        float panelY = (height - panelHeight) / 2f;

        shapeRenderer.setColor(new Color(0.05f, 0.07f, 0.11f, 0.97f));
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.8f);
        drawCentered("PAUSADO", width, panelY + panelHeight - 55f);

        font.getData().setScale(1.2f);

        float firstOptionY = panelY + panelHeight - 140f;
        float gap = 72f;

        for (int i = 0; i < OPTIONS.length; i++) {
            float y = firstOptionY - i * gap;

            optionBoxes[i].set(
                    panelX + 55f,
                    y - 42f,
                    panelWidth - 110f,
                    58f
            );

            if (i == selectedOption) {
                font.setColor(Color.CYAN);
                drawCentered("> " + (i + 1) + " - " + OPTIONS[i] + " <", width, y);
            } else {
                font.setColor(Color.LIGHT_GRAY);
                drawCentered((i + 1) + " - " + OPTIONS[i], width, y);
            }
        }

        font.getData().setScale(0.88f);
        font.setColor(Color.GRAY);
        drawCentered("↑↓ / W S = selecionar   |   1-5 / ENTER = confirmar", width, panelY + 38f);
        drawCentered("ESC = voltar ao jogo", width, panelY + 14f);

        batch.end();
    }

    private void drawCentered(String text, float width, float y) {
        GlyphLayout layout = new GlyphLayout(font, text);
        font.draw(batch, text, width / 2f - layout.width / 2f, y);
    }

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
