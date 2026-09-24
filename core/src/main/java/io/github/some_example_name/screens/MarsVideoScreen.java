package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import io.github.some_example_name.managers.SaveManager;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.FileNotFoundException;

/**
 * Vídeo de transição entre o status da Lua e a introdução de Marte.
 *
 * O vídeo inicia automaticamente e, ao terminar, abre a introdução de Marte.
 */
public class MarsVideoScreen extends ScreenAdapter {

    private static final String VIDEO_FILE = "videomarte.mp4";

    private final Game game;
    private final SaveManager.SaveData saveData;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;

    private VideoPlayer videoPlayer;
    private boolean videoStarted;
    private boolean videoFinished;
    private boolean videoFailed;
    private boolean changingScreen;
    private boolean disposed;
    private float failTimer;

    public MarsVideoScreen(Game game) {
        this(game, SaveManager.load());
    }

    public MarsVideoScreen(Game game, SaveManager.SaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new ScreenViewport();
    }

    @Override
    public void show() {
        changingScreen = false;
        videoStarted = false;
        videoFinished = false;
        videoFailed = false;
        failTimer = 0f;
        viewport.apply(true);
        startVideo();
    }

    private void startVideo() {
        FileHandle videoFile = Gdx.files.internal(VIDEO_FILE);

        if (!videoFile.exists()) {
            videoFailed = true;
            return;
        }

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();

            if (videoPlayer == null || !videoPlayer.load(videoFile)) {
                videoFailed = true;
                return;
            }

            videoPlayer.setOnCompletionListener(file -> videoFinished = true);
            videoPlayer.play();
            videoStarted = true;
        } catch (FileNotFoundException e) {
            videoFailed = true;
        } catch (Exception e) {
            Gdx.app.error("MarsVideoScreen", "Erro ao iniciar videomarte.mp4.", e);
            videoFailed = true;
        }
    }

    private void continueToMars() {
        if (changingScreen) {
            return;
        }

        changingScreen = true;

        SaveManager.SaveData data = saveData != null ? saveData : SaveManager.load();
        if (data == null) {
            data = new SaveManager.SaveData();
        }

        data.phase = SaveManager.Phase.MARTE;
        data.playerX = 400f;
        data.playerY = 400f;
        SaveManager.save(data);

        dispose();
        game.setScreen(new MarsIntroScreen(game, data));
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        delta = Math.min(delta, 0.05f);

        // O ESC não pula o vídeo. O vídeo faz parte da transição da história.
        if (videoFailed) {
            failTimer += delta;

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || failTimer >= 1.5f) {
                continueToMars();
                return;
            }
        }

        if (videoStarted && videoPlayer != null) {
            videoPlayer.update();

            if (videoFinished) {
                continueToMars();
                return;
            }
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(false);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        Texture texture = videoPlayer == null ? null : videoPlayer.getTexture();

        if (texture != null) {
            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();
            float videoWidth = Math.max(1f, videoPlayer.getVideoWidth());
            float videoHeight = Math.max(1f, videoPlayer.getVideoHeight());

            float scale = Math.min(
                    screenWidth / videoWidth,
                    screenHeight / videoHeight
            );

            float drawWidth = videoWidth * scale;
            float drawHeight = videoHeight * scale;
            float x = (screenWidth - drawWidth) / 2f;
            float y = (screenHeight - drawHeight) / 2f;

            batch.draw(texture, x, y, drawWidth, drawHeight);
        } else if (videoFailed) {
            font.setColor(Color.WHITE);
            font.getData().setScale(1.4f);
            font.draw(
                    batch,
                    "CARREGANDO MARTE...",
                    viewport.getWorldWidth() / 2f - 155f,
                    viewport.getWorldHeight() / 2f
            );

            font.setColor(Color.GRAY);
            font.getData().setScale(0.9f);
            font.draw(
                    batch,
                    "ENTER = continuar",
                    viewport.getWorldWidth() / 2f - 80f,
                    viewport.getWorldHeight() / 2f - 55f
            );
        } else {
            font.setColor(Color.WHITE);
            font.getData().setScale(1.1f);
            font.draw(
                    batch,
                    "CARREGANDO VÍDEO...",
                    viewport.getWorldWidth() / 2f - 120f,
                    viewport.getWorldHeight() / 2f
            );
        }

        batch.end();
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

        if (videoPlayer != null) {
            videoPlayer.dispose();
            videoPlayer = null;
        }

        batch.dispose();
        font.dispose();
    }
}
