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
 * Mapa de Fast Travel.
 *
 * Planetas ficam liberados quando a chave que abre o próximo mundo
 * já foi obtida no progresso salvo.
 */
public class FastTravelScreen extends ScreenAdapter {

    private final Game game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;
    private final SaveManager.SaveData saveData;

    private final SaveManager.Phase[] phases = {
            SaveManager.Phase.LUA,
            SaveManager.Phase.MARTE,
            SaveManager.Phase.TITA,
            SaveManager.Phase.CALISTO,
            SaveManager.Phase.AHARIN
    };

    private final String[] labels = {
            "LUA",
            "MARTE",
            "TITA",
            "CALISTO",
            "AHARIN"
    };

    private int selectedIndex;
    private boolean changingScreen;
    private boolean disposed;

    public FastTravelScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new ScreenViewport();

        selectedIndex = currentIndex();
        moveToNearestUnlocked();
    }

    @Override
    public void show() {
        viewport.apply(true);
        changingScreen = false;
    }

    private int currentIndex() {
        if (saveData == null) {
            return 0;
        }

        for (int i = 0; i < phases.length; i++) {
            if (phases[i] == saveData.phase) {
                return i;
            }
        }
        return 0;
    }

    private void moveToNearestUnlocked() {
        if (isUnlocked(selectedIndex)) {
            return;
        }

        for (int i = 0; i < phases.length; i++) {
            if (isUnlocked(i)) {
                selectedIndex = i;
                return;
            }
        }

        selectedIndex = 0;
    }

    private boolean isUnlocked(int index) {
        if (saveData == null) {
            return index == 0;
        }

        switch (index) {
            case 0:
                return true;
            case 1:
                return saveData.redKeyCollected
                        || saveData.phase.ordinal() >= SaveManager.Phase.MARTE.ordinal();
            case 2:
                return saveData.greenKeyCollected
                        || saveData.phase.ordinal() >= SaveManager.Phase.TITA.ordinal();
            case 3:
                return saveData.yellowKeyCollected
                        || saveData.phase.ordinal() >= SaveManager.Phase.CALISTO.ordinal();
            case 4:
                return saveData.calistoFinalKeyCollected
                        || saveData.phase == SaveManager.Phase.AHARIN;
            default:
                return false;
        }
    }

    private boolean isCurrent(int index) {
        return saveData != null && saveData.phase == phases[index];
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToCurrentPhase();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            moveSelection(-1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)
                || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
                || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            moveSelection(1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            confirmTravel();
        }
    }

    private void moveSelection(int direction) {
        int next = selectedIndex;

        for (int guard = 0; guard < phases.length; guard++) {
            next = (next + direction + phases.length) % phases.length;
            if (isUnlocked(next)) {
                selectedIndex = next;
                return;
            }
        }
    }

    private void confirmTravel() {
        if (saveData == null || !isUnlocked(selectedIndex)) {
            return;
        }

        SaveManager.Phase target = phases[selectedIndex];

        // Salva antes da viagem rápida.
        boolean changingPlanet = saveData.phase != target;
        saveData.phase = target;

        if (changingPlanet) {
            setSafeSpawn(saveData, target);
        }

        SaveManager.save(saveData);

        changingScreen = true;
        dispose();

        switch (target) {
            case LUA:
                game.setScreen(new LuaScreen(game, saveData));
                break;
            case MARTE:
                game.setScreen(new LuaMarteScreen(game, saveData));
                break;
            case TITA:
                game.setScreen(new titascreen(game, saveData));
                break;
            case CALISTO:
                game.setScreen(new CalistoScreen(game, saveData));
                break;
            case AHARIN:
                game.setScreen(new AharinScreen(game, saveData));
                break;
            default:
                game.setScreen(new LuaScreen(game, saveData));
                break;
        }
    }

    private void setSafeSpawn(SaveManager.SaveData data, SaveManager.Phase target) {
        switch (target) {
            case LUA:
                data.playerX = 400f;
                data.playerY = 400f;
                break;
            case MARTE:
                data.playerX = 400f;
                data.playerY = 400f;
                break;
            case TITA:
                data.playerX = 600f;
                data.playerY = 600f;
                break;
            case CALISTO:
                data.playerX = 210f;
                data.playerY = 620f;
                break;
            case AHARIN:
                data.playerX = 600f;
                data.playerY = 360f;
                break;
            default:
                break;
        }
    }

    private void returnToCurrentPhase() {
        if (saveData == null) {
            changingScreen = true;
            dispose();
            game.setScreen(new MenuScreen(game));
            return;
        }

        changingScreen = true;
        dispose();
        confirmCurrentPhaseWithoutResave();
    }

    private void confirmCurrentPhaseWithoutResave() {
        switch (saveData.phase) {
            case LUA:
                game.setScreen(new LuaScreen(game, saveData));
                break;
            case MARTE:
                game.setScreen(new LuaMarteScreen(game, saveData));
                break;
            case TITA:
                game.setScreen(new titascreen(game, saveData));
                break;
            case CALISTO:
                game.setScreen(new CalistoScreen(game, saveData));
                break;
            case AHARIN:
                game.setScreen(new AharinScreen(game, saveData));
                break;
            default:
                game.setScreen(new MenuScreen(game));
                break;
        }
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        handleInput();
        if (changingScreen) {
            return;
        }

        Gdx.gl.glClearColor(0.015f, 0.02f, 0.055f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(Color.valueOf("FFD54A"));
        font.getData().setScale(2.2f);
        drawCentered("MAPA — FAST TRAVEL", w / 2f, h - 75f);

        font.setColor(Color.WHITE);
        font.getData().setScale(0.95f);
        drawCentered("M = abrir mapa | F / ENTER = viajar | ESC = voltar", w / 2f, h - 112f);

        float startY = h - 210f;
        for (int i = 0; i < phases.length; i++) {
            boolean unlocked = isUnlocked(i);
            boolean current = isCurrent(i);
            boolean selected = selectedIndex == i;

            if (!unlocked) {
                font.setColor(Color.DARK_GRAY);
            } else if (selected) {
                font.setColor(Color.valueOf("FFD54A"));
            } else {
                font.setColor(Color.LIGHT_GRAY);
            }

            font.getData().setScale(selected ? 1.35f : 1.08f);

            String status;
            if (!unlocked) {
                status = "??? BLOQUEADO";
            } else if (current) {
                status = "ATUAL";
            } else {
                status = "LIBERADO";
            }

            drawCentered(
                    (selected ? "> " : "  ") + labels[i] + "  —  " + status,
                    w / 2f,
                    startY - i * 76f
            );
        }

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.82f);
        drawCentered("As chaves liberam os mundos: VERMELHA → MARTE | VERDE → TITÃ | AMARELA → CALISTO | LUZ → AHARIN",
                w / 2f, 68f);

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
