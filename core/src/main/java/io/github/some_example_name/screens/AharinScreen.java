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

import io.github.some_example_name.managers.SaveManager;

/**
 * Fase final de Aharin.
 *
 * O planeta é desenhado em código: amarelo, cheio de anjos.
 * O diálogo avança somente com ENTER, em estilo de caixa de diálogo de RPG.
 * Depois do diálogo existem três escolhas finais.
 */
public class AharinScreen extends ScreenAdapter {

    private enum State {
        DIALOGUE,
        CHOICE,
        ENDING
    }

    private enum Ending {
        SAVE_EARTH,
        DOMINATE_EARTH,
        STAY_AHARIN
    }

    private final Game game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final Viewport viewport;

    private State state = State.DIALOGUE;
    private Ending ending;
    private int dialogueIndex;
    private int selectedChoice = 0;

    private boolean changingScreen;
    private boolean disposed;
    private float time;

    private final String[] speakers = {
            "ANJO MAIS ANTIGO",
            "ANJO DO ORIENTE",
            "ANJO DAS ESTRELAS",
            "ANJO DA MEMÓRIA",
            "TODOS OS ANJOS"
    };

    private final String[] dialogue = {
            "Nós ajudamos você porque sua jornada mostrou algo que raramente vemos: você continuou mesmo quando não entendia o caminho.",
            "Em Calisto, nossas bênçãos pareceram presentes. Na verdade, eram pedaços do conhecimento que Aharin guarda há muito tempo.",
            "Nós observamos Lua, Marte e Titã. Precisávamos saber se você usaria esse poder para proteger, ou apenas para conquistar.",
            "Ajudamos porque acreditamos que conhecimento não pertence a um único mundo. A Terra também merece uma chance de aprender com o que descobrimos.",
            "Agora você sabe o suficiente. A decisão final não será nossa. Ela será sua."
    };

    private final String[] choices = {
            "VOLTAR À TERRA E AJUDAR A TERRA",
            "VOLTAR À TERRA E DOMINAR A TERRA",
            "FICAR EM AHARIN"
    };

    public AharinScreen(Game game) {
        this(game, null);
    }

    public AharinScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        viewport = new ScreenViewport();

        if (saveData != null) {
            dialogueIndex = MathUtils.clamp(saveData.aharinDialogueIndex, 0, dialogue.length);
            if (saveData.aharinChoice >= 0 && saveData.aharinChoice <= 2) {
                ending = Ending.values()[saveData.aharinChoice];
                state = State.ENDING;
            } else if (saveData.aharinDialogueFinished) {
                state = State.CHOICE;
            }
        }
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

        delta = Math.min(delta, 0.05f);
        time += delta;

        handleInput(delta);

        if (changingScreen) {
            return;
        }

        Gdx.gl.glClearColor(0.035f, 0.018f, 0.005f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawWorld();
        drawHud();
    }

    private void handleInput(float delta) {
        if (state == State.DIALOGUE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                dialogueIndex++;

                if (dialogueIndex >= dialogue.length) {
                    state = State.CHOICE;
                    dialogueIndex = dialogue.length;
                    saveProgress(false);
                } else {
                    saveProgress(false);
                }
            }
            return;
        }

        if (state == State.CHOICE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                selectedChoice = (selectedChoice + choices.length - 1) % choices.length;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                    || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                selectedChoice = (selectedChoice + 1) % choices.length;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                selectedChoice = 0;
                confirmChoice();
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                selectedChoice = 1;
                confirmChoice();
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                selectedChoice = 2;
                confirmChoice();
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                confirmChoice();
            }
            return;
        }

        if (state == State.ENDING
                && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
        }
    }

    private void confirmChoice() {
        ending = Ending.values()[selectedChoice];
        state = State.ENDING;
        saveProgress(true);
    }

    private void saveProgress(boolean choiceMade) {
        SaveManager.SaveData data = new SaveManager.SaveData();
        data.phase = SaveManager.Phase.AHARIN;
        data.aharinDialogueIndex = dialogueIndex;
        data.aharinDialogueFinished = dialogueIndex >= dialogue.length;
        data.aharinChoice = choiceMade && ending != null ? ending.ordinal() : -1;
        SaveManager.save(data);
    }

    private void drawWorld() {
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        // Céu profundo.
        shapes.setColor(Color.valueOf("150A02"));
        shapes.rect(0f, 0f, w, h);

        // Estrelas.
        shapes.setColor(Color.valueOf("FFF0B0"));
        for (int i = 0; i < 85; i++) {
            float x = (i * 83.71f) % w;
            float y = (i * 137.41f) % h;
            float pulse = 0.8f + 0.45f * MathUtils.sin(time * 1.4f + i);
            shapes.circle(x, y, pulse);
        }

        // Planeta amarelo.
        float planetX = w * 0.50f;
        float planetY = -h * 0.08f;
        float planetRadius = h * 0.64f;

        shapes.setColor(Color.valueOf("DFAF1D"));
        shapes.circle(planetX, planetY, planetRadius);

        // Regiões iluminadas do planeta.
        shapes.setColor(Color.valueOf("F4CF50"));
        shapes.circle(planetX - planetRadius * 0.36f, planetY + planetRadius * 0.24f,
                planetRadius * 0.21f);
        shapes.circle(planetX + planetRadius * 0.23f, planetY + planetRadius * 0.18f,
                planetRadius * 0.18f);
        shapes.circle(planetX + planetRadius * 0.38f, planetY - planetRadius * 0.13f,
                planetRadius * 0.14f);

        // Muitos anjos.
        for (int i = 0; i < 10; i++) {
            float angle = i / 10f * MathUtils.PI2 + time * 0.12f;
            float orbit = 230f + (i % 3) * 48f;
            float ax = planetX + MathUtils.cos(angle) * orbit;
            float ay = planetY + MathUtils.sin(angle) * orbit * 0.48f + 250f;
            drawAngel(ax, ay, 0.85f + (i % 3) * 0.09f);
        }

        // Anjo central maior.
        drawAngel(w * 0.50f, h * 0.55f, 1.35f);

        shapes.end();
    }

    private void drawAngel(float x, float y, float scale) {
        float bodyW = 34f * scale;
        float bodyH = 54f * scale;

        shapes.setColor(Color.valueOf("FFF4C2"));
        shapes.circle(x, y + bodyH * 0.66f, 16f * scale);
        shapes.rect(x - bodyW / 2f, y - bodyH / 2f, bodyW, bodyH);

        // Asas.
        shapes.triangle(
                x - bodyW * 0.35f,
                y + bodyH * 0.30f,
                x - bodyW * 2.0f,
                y + bodyH * 0.90f,
                x - bodyW * 0.85f,
                y - bodyH * 0.02f
        );
        shapes.triangle(
                x + bodyW * 0.35f,
                y + bodyH * 0.30f,
                x + bodyW * 2.0f,
                y + bodyH * 0.90f,
                x + bodyW * 0.85f,
                y - bodyH * 0.02f
        );

        // Aura.
        shapes.setColor(new Color(1f, 0.88f, 0.28f, 0.16f));
        shapes.circle(x, y + bodyH * 0.40f, 44f * scale);
    }

    private void drawHud() {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.DIALOGUE) {
            drawDialoguePanel(w);
        } else if (state == State.CHOICE) {
            drawChoicePanel(w);
        }

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (state == State.DIALOGUE) {
            drawDialogueText(w);
        } else if (state == State.CHOICE) {
            drawChoiceText(w);
        } else {
            drawEnding(w, h);
        }

        batch.end();
    }

    private void drawDialoguePanel(float w) {
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.02f, 0.01f, 0.00f, 0.92f));
        shapes.rect(70f, 46f, w - 140f, 190f);
        shapes.setColor(Color.valueOf("FFD43B"));
        shapes.rect(70f, 230f, w - 140f, 4f);
        shapes.end();
    }

    private void drawDialogueText(float w) {
        font.setColor(Color.valueOf("FFD43B"));
        font.getData().setScale(1.15f);
        font.draw(batch, speakers[Math.min(dialogueIndex, speakers.length - 1)], 96f, 205f);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);

        String line = dialogue[Math.min(dialogueIndex, dialogue.length - 1)];
        drawWrapped(line, 96f, 173f, w - 192f, 33f);

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.78f);
        font.draw(batch, "[ ENTER ]", w - 175f, 73f);
    }

    private void drawChoicePanel(float w) {
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.02f, 0.01f, 0.00f, 0.94f));
        shapes.rect(95f, 55f, w - 190f, 300f);
        shapes.setColor(Color.valueOf("FFD43B"));
        shapes.rect(95f, 355f, w - 190f, 4f);
        shapes.end();
    }

    private void drawChoiceText(float w) {
        font.setColor(Color.valueOf("FFD43B"));
        font.getData().setScale(1.35f);
        drawCentered("A DECISÃO FINAL", w / 2f, 323f);

        font.getData().setScale(0.95f);
        for (int i = 0; i < choices.length; i++) {
            float y = 250f - i * 72f;
            font.setColor(i == selectedChoice
                    ? Color.valueOf("FFD43B")
                    : Color.WHITE);

            String prefix = i == selectedChoice ? "> " : "  ";
            font.draw(batch, prefix + (i + 1) + ". " + choices[i], 145f, y);
        }

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.75f);
        drawCentered("↑/↓ + ENTER   ou   1 / 2 / 3", w / 2f, 82f);
    }

    private void drawEnding(float w, float h) {
        font.setColor(Color.valueOf("FFD43B"));
        font.getData().setScale(3.0f);

        String title;
        String[] lines;

        switch (ending) {
            case SAVE_EARTH:
                title = "O CAMINHO DA LUZ";
                lines = new String[] {
                        "Você retorna à Terra para compartilhar",
                        "o conhecimento de Aharin.",
                        "",
                        "A humanidade ganha uma nova chance",
                        "de crescer sem repetir os erros do passado.",
                        "",
                        "FINAL: VOLTAR E AJUDAR A TERRA"
                };
                break;

            case DOMINATE_EARTH:
                title = "O PODER ABSOLUTO";
                lines = new String[] {
                        "Você retorna à Terra levando o poder de Aharin.",
                        "Em vez de compartilhar o conhecimento,",
                        "você decide colocá-lo sob seu próprio controle.",
                        "",
                        "A Terra se curva diante da nova força.",
                        "",
                        "FINAL: VOLTAR E DOMINAR A TERRA"
                };
                break;

            case STAY_AHARIN:
            default:
                title = "A GUARDIÃO DE AHARIN";
                lines = new String[] {
                        "Você decide ficar.",
                        "Aharin se torna sua nova casa.",
                        "",
                        "Você ajuda os anjos a proteger os segredos",
                        "que poderiam mudar o destino dos mundos.",
                        "",
                        "FINAL: FICAR EM AHARIN"
                };
                break;
        }

        drawCentered(title, w / 2f, h - 105f);

        font.setColor(Color.WHITE);
        font.getData().setScale(1.08f);

        float y = h - 205f;
        for (String line : lines) {
            if (line.isEmpty()) {
                y -= 24f;
                continue;
            }
            GlyphLayout layout = new GlyphLayout(font, line);
            font.draw(batch, line, w / 2f - layout.width / 2f, y);
            y -= 47f;
        }

        font.setColor(Color.valueOf("FFD43B"));
        font.getData().setScale(1.0f);
        drawCentered("[ ENTER ] VOLTAR AO MENU", w / 2f, 72f);
    }

    private void drawWrapped(String text, float x, float topY, float maxWidth, float lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float y = topY;

        for (String word : words) {
            String candidate = line.length() == 0
                    ? word
                    : line + " " + word;

            GlyphLayout layout = new GlyphLayout(font, candidate);
            if (layout.width > maxWidth && line.length() > 0) {
                font.draw(batch, line.toString(), x, y);
                y -= lineHeight;
                line.setLength(0);
                line.append(word);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }

        if (line.length() > 0) {
            font.draw(batch, line.toString(), x, y);
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
