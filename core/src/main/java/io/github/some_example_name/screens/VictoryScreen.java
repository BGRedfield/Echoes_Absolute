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
 * Ending cinematic after the final boss.
 *
 * The ending starts with a Star-Wars-inspired text crawl:
 * the story rises slowly from the bottom toward the top of the screen.
 * When every line has passed, the final VITORIA screen appears.
 */
public class VictoryScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float CRAWL_SPEED = 52f;
    private static final float LINE_SPACING = 58f;
    private static final float START_Y = -120f;

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;

    private final String[] crawlLines = {
            "ECHOES ABSOLUTE",
            "",
            "Depois de atravessar mundos desconhecidos,",
            "enfrentar inimigos e superar desafios impossíveis,",
            "você chegou ao fim da maior jornada da humanidade.",
            "",
            "Lua.",
            "Marte.",
            "Titã.",
            "Calisto.",
            "",
            "Em cada mundo, você encontrou respostas,",
            "mas também descobriu novas perguntas.",
            "",
            "A Sabedoria Santa e Gloriosa do espaço",
            "agora está em suas mãos.",
            "",
            "O conhecimento conquistado nesta jornada",
            "poderá transformar o futuro da humanidade.",
            "",
            "Novas tecnologias poderão nascer.",
            "Novos mundos poderão ser explorados.",
            "E a humanidade poderá avançar ainda mais",
            "rumo às estrelas.",
            "",
            "Você não apenas sobreviveu.",
            "Você abriu um novo caminho.",
            "",
            "A Terra espera por você.",
            "",
            "Sua jornada terminou.",
            "Mas o futuro da humanidade",
            "está apenas começando."
    };

    private float crawlOffset;
    private boolean finished;
    private boolean changingScreen;
    private boolean disposed;

    public VictoryScreen(Game game) {
        this(game, "VITÓRIA", "A jornada chegou ao fim.");
    }

    public VictoryScreen(Game game, String title, String subtitle) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new ScreenViewport();

        // Keep the existing VictoryScreen constructor API so CalistoScreen
        // and the other phases do not need to be changed.
    }

    @Override
    public void show() {
        changingScreen = false;
        finished = false;
        crawlOffset = 0f;
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        delta = Math.min(delta, 0.05f);

        if (!finished) {
            crawlOffset += CRAWL_SPEED * delta;

            // Once the final line has completely left the screen,
            // replace the crawl with the final victory screen.
            float lastLineY = START_Y
                    + crawlOffset
                    - (crawlLines.length - 1) * LINE_SPACING;

            if (lastLineY > viewport.getWorldHeight() + 120f) {
                finished = true;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (!finished) {
            drawCrawl();
        } else {
            drawFinalVictory();
        }

        batch.end();
    }

    private void drawCrawl() {
        font.setColor(Color.valueOf("FFD54A"));
        font.getData().setScale(1.05f);

        float centerX = viewport.getWorldWidth() / 2f;

        for (int i = 0; i < crawlLines.length; i++) {
            float y = START_Y
                    + crawlOffset
                    - i * LINE_SPACING;

            if (y < -70f || y > viewport.getWorldHeight() + 70f) {
                continue;
            }

            String line = crawlLines[i];

            if (line.isEmpty()) {
                continue;
            }

            // Slightly smaller text near the bottom and top creates a
            // simple cinematic depth effect while keeping the code 2D-safe.
            float normalized = y / viewport.getWorldHeight();
            float scale = 0.82f + 0.35f * Math.max(0f, Math.min(1f, normalized));
            font.getData().setScale(scale);

            GlyphLayout layout = new GlyphLayout(font, line);
            font.draw(batch, line, centerX - layout.width / 2f, y);
        }
    }

    private void drawFinalVictory() {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        float centerX = width / 2f;

        font.setColor(Color.valueOf("FFD54A"));

        font.getData().setScale(3.8f);
        drawCentered("VITORIA", centerX, height / 2f + 90f);

        font.getData().setScale(1.25f);
        font.setColor(Color.WHITE);
        drawCentered("A jornada de ECHOES ABSOLUTE chegou ao fim.", centerX,
                height / 2f - 20f);

        font.getData().setScale(1.25f);
        font.setColor(Color.valueOf("FFD54A"));
        drawCentered("[ ENTER ]", centerX, height / 2f - 125f);

        font.getData().setScale(0.95f);
        font.setColor(Color.LIGHT_GRAY);
        drawCentered("VOLTAR AO MENU", centerX, height / 2f - 170f);
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
