package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
 * Quest Log global do Echoes Absolute.
 *
 * Q abre/fecha o painel sem trocar de Screen.
 * O mundo fica congelado enquanto o painel está aberto.
 */
public class QuestLog {

    private static final float DESIGN_WIDTH = 1280f;
    private static final float DESIGN_HEIGHT = 720f;

    private static final float LEFT_PANEL_WIDTH = 315f;
    private static final float OUTER_MARGIN = 24f;
    private static final float TOP_Y = 670f;

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final Viewport viewport;

    private boolean open;
    private boolean disposed;

    private static final String[] QUEST_NAMES = {
            "LUA — COLETAR 5 GELOS",
            "LUA — DERRETER OS GELOS E BEBER A ÁGUA",
            "LUA — DERROTAR O BOSS DA LUA",
            "LUA — PEGAR A CHAVE VERMELHA E IR PARA MARTE",
            "MARTE — COLETAR E REFINAR 5 MINÉRIOS",
            "MARTE — APRIMORAR A ARMA",
            "MARTE — DERROTAR O BOSS SUPREMO",
            "TITÃ — CONCLUIR AS BATALHAS E LIBERAR O CASTELO FINAL",
            "TITÃ — PEGAR A CHAVE AMARELA E ABRIR CALISTO",
            "CALISTO — RECEBER AS 5 BÊNÇÃOS DE LUZ",
            "CALISTO — DERROTAR O BOSS E PEGAR A CHAVE DE LUZ",
            "AHARIN — OUVIR OS ANJOS E ESCOLHER O DESTINO"
    };

    private static final String[] CONTROLS = {
            "W A S D  /  SETAS     MOVER",
            "MOUSE                  MIRAR",
            "CLIQUE ESQUERDO        ATIRAR",
            "E                      INTERAGIR",
            "Q                      QUEST LOG",
            "M                      MAPA / FAST TRAVEL",
            "ESC                    PAUSAR / VOLTAR",
            "ENTER                  CONFIRMAR / DIÁLOGO",
            "ESPAÇO                 CONFIRMAR PAUSA",
            "1 / 2 / 3              ESCOLHAS DE AHARIN",
            "1 - 5                  OPÇÕES DA PAUSA",
            "F                      CONFIRMAR NO MAPA"
    };

    public QuestLog() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        viewport = new ScreenViewport();
        font.setColor(Color.WHITE);
    }

    public void open() {
        if (!disposed) {
            open = true;
        }
    }

    public void close() {
        open = false;
    }

    public boolean isOpen() {
        return open && !disposed;
    }

    /**
     * Trata somente o fechar do Quest Log.
     * Retorna true quando ele estava aberto antes do input.
     */
    public boolean handleInput() {
        if (!isOpen()) {
            return false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
        }

        return true;
    }

    public void resize(int width, int height) {
        if (disposed) {
            return;
        }
        viewport.update(width, height, true);
    }

    public void render() {
        if (!isOpen()) {
            return;
        }

        viewport.apply(true);

        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        float sx = width / DESIGN_WIDTH;
        float sy = height / DESIGN_HEIGHT;
        float scale = Math.min(sx, sy);

        shapes.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Tela preta total.
        shapes.setColor(Color.BLACK);
        shapes.rect(0f, 0f, width, height);

        // Painel esquerdo: controles.
        shapes.setColor(new Color(0.025f, 0.035f, 0.05f, 1f));
        shapes.rect(
                OUTER_MARGIN * sx,
                OUTER_MARGIN * sy,
                LEFT_PANEL_WIDTH * sx,
                height - OUTER_MARGIN * 2f * sy
        );

        // Painel direito: quests.
        shapes.setColor(new Color(0.018f, 0.022f, 0.032f, 1f));
        shapes.rect(
                (OUTER_MARGIN + LEFT_PANEL_WIDTH + 18f) * sx,
                OUTER_MARGIN * sy,
                width - (OUTER_MARGIN + LEFT_PANEL_WIDTH + 42f) * sx,
                height - OUTER_MARGIN * 2f * sy
        );

        // Separador.
        shapes.setColor(new Color(0.18f, 0.55f, 0.75f, 0.85f));
        shapes.rect(
                (OUTER_MARGIN + LEFT_PANEL_WIDTH + 8f) * sx,
                OUTER_MARGIN * sy,
                2f * sx,
                height - OUTER_MARGIN * 2f * sy
        );

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        SaveManager.SaveData data = SaveManager.load();

        drawProgressBar(width, height, scale, data);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        drawContents(width, height, scale, data);

        batch.end();
    }

    private void drawContents(float width, float height, float scale, SaveManager.SaveData data) {
        float leftX = OUTER_MARGIN * scale + 20f * scale;
        float rightX = (OUTER_MARGIN + LEFT_PANEL_WIDTH + 32f) * scale;

        // Cabeçalho esquerdo.
        drawText("CONTROLES", leftX, height - 46f * scale, 1.35f * scale, Color.CYAN);
        drawText("GUIA RÁPIDO DO JOGO", leftX, height - 76f * scale, 0.72f * scale, Color.GRAY);

        float controlsY = height - 112f * scale;
        for (String control : CONTROLS) {
            drawText(control, leftX, controlsY, 0.72f * scale, Color.LIGHT_GRAY);
            controlsY -= 42f * scale;
        }

        drawText(
                "Q / ESC = fechar o Quest Log",
                leftX,
                44f * scale,
                0.65f * scale,
                Color.GRAY
        );

        // Cabeçalho direito.
        drawText("QUEST LOG", rightX, height - 46f * scale, 1.65f * scale, Color.WHITE);

        String phase = data == null
                ? "SEM SAVE"
                : "FASE: " + phaseName(data.phase) + "  |  SLOT " + SaveManager.getActiveSlot();

        drawText(phase, rightX, height - 78f * scale, 0.78f * scale, Color.GRAY);
        drawText("OBJETIVOS PRINCIPAIS", rightX, height - 112f * scale, 0.82f * scale, Color.CYAN);

        float questY = height - 150f * scale;
        int openQuestIndex = firstOpenQuest(data);

        for (int i = 0; i < QUEST_NAMES.length; i++) {
            boolean done = isQuestDone(i, data);
            String status = done ? "FEITA" : "ABERTA";

            Color statusColor = done ? Color.GREEN : Color.ORANGE;
            Color nameColor = done ? new Color(0.65f, 0.72f, 0.76f, 1f) : Color.WHITE;

            if (!done && i == openQuestIndex) {
                drawText("> " + QUEST_NAMES[i], rightX, questY, 0.82f * scale, Color.YELLOW);
            } else {
                drawText((done ? "✓ " : "  ") + QUEST_NAMES[i], rightX, questY, 0.82f * scale, nameColor);
            }

            font.setColor(statusColor);
            font.getData().setScale(0.70f * scale);
            GlyphLayout statusLayout = new GlyphLayout(font, status);
            font.draw(
                    batch,
                    status,
                    width - 46f * scale - statusLayout.width,
                    questY
            );

            questY -= 44f * scale;
        }

        int doneCount = 0;
        for (int i = 0; i < QUEST_NAMES.length; i++) {
            if (isQuestDone(i, data)) {
                doneCount++;
            }
        }

        drawText(
                doneCount + "/" + QUEST_NAMES.length + " QUESTS FEITAS",
                rightX,
                108f * scale,
                0.68f * scale,
                Color.GRAY
        );

        drawText(
                "Q ou ESC para voltar ao jogo",
                rightX,
                44f * scale,
                0.68f * scale,
                Color.GRAY
        );
    }

    private void drawProgressBar(float width, float height, float scale, SaveManager.SaveData data) {
        int doneCount = 0;
        for (int i = 0; i < QUEST_NAMES.length; i++) {
            if (isQuestDone(i, data)) {
                doneCount++;
            }
        }

        float rightX = (OUTER_MARGIN + LEFT_PANEL_WIDTH + 32f) * scale;
        float barX = rightX;
        float barY = 82f * scale;
        float barWidth = width - rightX - 46f * scale;
        float barHeight = 10f * scale;

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.10f, 0.10f, 0.14f, 1f));
        shapes.rect(barX, barY, barWidth, barHeight);

        shapes.setColor(new Color(0.15f, 0.75f, 0.35f, 1f));
        shapes.rect(
                barX,
                barY,
                barWidth * MathUtils.clamp(doneCount / (float) QUEST_NAMES.length, 0f, 1f),
                barHeight
        );
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private int firstOpenQuest(SaveManager.SaveData data) {
        for (int i = 0; i < QUEST_NAMES.length; i++) {
            if (!isQuestDone(i, data)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isQuestDone(int index, SaveManager.SaveData data) {
        if (data == null) {
            return false;
        }

        SaveManager.Phase phase = data.phase == null
                ? SaveManager.Phase.LUA
                : data.phase;

        switch (index) {
            case 0:
                return data.iceCollected >= 5 || phase.ordinal() > SaveManager.Phase.LUA.ordinal();

            case 1:
                return data.luaMission >= 2
                        || phase.ordinal() > SaveManager.Phase.LUA.ordinal();

            case 2:
                return data.trumpBossHealth <= 0f
                        || phase.ordinal() > SaveManager.Phase.LUA.ordinal();

            case 3:
                return data.redKeyCollected
                        || phase.ordinal() >= SaveManager.Phase.MARTE.ordinal();

            case 4:
                return data.rawOreCount >= 5
                        || phase.ordinal() >= SaveManager.Phase.TITA.ordinal();

            case 5:
                return data.weaponUpgraded
                        || phase.ordinal() >= SaveManager.Phase.TITA.ordinal();

            case 6:
                return data.supremeAlienHealth <= 0f
                        || phase.ordinal() >= SaveManager.Phase.TITA.ordinal();

            case 7:
                return allTitanBossesDefeated(data)
                        || phase.ordinal() >= SaveManager.Phase.CALISTO.ordinal();

            case 8:
                return data.yellowKeyCollected
                        || phase.ordinal() >= SaveManager.Phase.CALISTO.ordinal();

            case 9:
                return allBlessingsCollected(data)
                        || phase.ordinal() >= SaveManager.Phase.AHARIN.ordinal();

            case 10:
                return data.calistoBossDefeated
                        && data.calistoFinalKeyCollected
                        || phase.ordinal() >= SaveManager.Phase.AHARIN.ordinal();

            case 11:
                return data.aharinChoice >= 0;

            default:
                return false;
        }
    }

    private boolean allTitanBossesDefeated(SaveManager.SaveData data) {
        for (boolean defeated : data.titanBossDefeated) {
            if (!defeated) {
                return false;
            }
        }
        return true;
    }

    private boolean allBlessingsCollected(SaveManager.SaveData data) {
        for (boolean blessing : data.calistoAngelBlessings) {
            if (!blessing) {
                return false;
            }
        }
        return true;
    }

    private String phaseName(SaveManager.Phase phase) {
        if (phase == null) {
            return "LUA";
        }

        switch (phase) {
            case LUA:
                return "LUA";
            case MARTE:
                return "MARTE";
            case TITA:
                return "TITÃ";
            case CALISTO:
                return "CALISTO";
            case AHARIN:
                return "AHARIN";
            default:
                return phase.name();
        }
    }

    private void drawText(
            String text,
            float x,
            float y,
            float scale,
            Color color
    ) {
        font.setColor(color);
        font.getData().setScale(scale);
        font.draw(batch, text, x, y);
    }

    public void dispose() {
        if (disposed) {
            return;
        }

        disposed = true;
        open = false;
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
