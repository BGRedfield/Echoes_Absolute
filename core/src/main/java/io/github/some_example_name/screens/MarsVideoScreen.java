package io.github.some_example_name.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.managers.SaveManager;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.LibgdxFrameConverter;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Reproduz videomarte.mp4 entre a tela de status da Lua e a introdução de Marte.
 */
public class MarsVideoScreen extends ScreenAdapter {

    private static final String VIDEO_FILE = "videomarte.mp4";
    private static final float FALLBACK_MESSAGE_DELAY = 1.5f;

    private final Game game;
    private final SaveManager.SaveData saveData;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;

    private FFmpegFrameGrabber grabber;
    private LibgdxFrameConverter converter;
    private Texture videoTexture;
    private Pixmap framePixmap;

    private boolean changingScreen;
    private boolean finished;
    private boolean failed;
    private boolean disposed;

    private float frameAccumulator;
    private float frameDuration = 1f / 30f;
    private float fallbackTimer;

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
        finished = false;
        failed = false;
        frameAccumulator = 0f;
        fallbackTimer = 0f;
        viewport.apply(true);

        try {
            File file = Gdx.files.internal(VIDEO_FILE).file();

            if (!file.exists()) {
                throw new IllegalStateException("Arquivo não encontrado: " + VIDEO_FILE);
            }

            grabber = new FFmpegFrameGrabber(file);
            grabber.start();

            double fps = grabber.getFrameRate();
            if (fps > 0.1 && fps < 240.0) {
                frameDuration = 1f / (float) fps;
            }

            converter = new LibgdxFrameConverter();

            // Mostra o primeiro frame imediatamente.
            grabNextFrame();
        } catch (Exception e) {
            failed = true;
            Gdx.app.error(
                    "MarsVideoScreen",
                    "Não foi possível reproduzir " + VIDEO_FILE + ".",
                    e
            );
        }
    }

    private void grabNextFrame() throws Exception {
        Frame frame;

        do {
            frame = grabber.grabImage();
            if (frame == null) {
                finished = true;
                return;
            }
        } while (frame.image == null || frame.image.length == 0 || frame.image[0] == null);

        Pixmap newPixmap = converter.convert(frame);
        if (newPixmap == null) {
            return;
        }

        if (videoTexture == null
                || videoTexture.getWidth() != newPixmap.getWidth()
                || videoTexture.getHeight() != newPixmap.getHeight()) {

            if (videoTexture != null) {
                videoTexture.dispose();
            }

            videoTexture = new Texture(newPixmap);
            videoTexture.setFilter(
                    Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear
            );
        } else {
            videoTexture.draw(newPixmap, 0, 0);
        }

        if (framePixmap != null) {
            framePixmap.dispose();
        }

        framePixmap = newPixmap;
    }

    private void continueToMars() {
        if (changingScreen) {
            return;
        }

        changingScreen = true;
        dispose();
        game.setScreen(new MarsIntroScreen(game, saveData));
    }

    @Override
    public void render(float delta) {
        if (changingScreen) {
            return;
        }

        delta = Math.min(delta, 0.05f);

        if (failed) {
            fallbackTimer += delta;

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                continueToMars();
                return;
            }
        } else if (!finished && grabber != null) {
            frameAccumulator += delta;

            while (frameAccumulator >= frameDuration && !finished) {
                frameAccumulator -= frameDuration;

                try {
                    grabNextFrame();
                } catch (Exception e) {
                    failed = true;
                    Gdx.app.error(
                            "MarsVideoScreen",
                            "Erro durante a reprodução de " + VIDEO_FILE + ".",
                            e
                    );
                    break;
                }
            }

            if (finished) {
                continueToMars();
                return;
            }
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(false);

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (videoTexture != null && !failed) {
            float videoWidth = videoTexture.getWidth();
            float videoHeight = videoTexture.getHeight();

            float scale = Math.min(
                    screenWidth / videoWidth,
                    screenHeight / videoHeight
            );

            float drawWidth = videoWidth * scale;
            float drawHeight = videoHeight * scale;
            float x = (screenWidth - drawWidth) / 2f;
            float y = (screenHeight - drawHeight) / 2f;

            batch.draw(videoTexture, x, y, drawWidth, drawHeight);
        } else {
            font.setColor(Color.WHITE);
            font.getData().setScale(1.35f);
            String message = failed
                    ? "NÃO FOI POSSÍVEL REPRODUZIR O VÍDEO"
                    : "CARREGANDO VÍDEO DE MARTE...";

            float messageWidth = font.getSpaceXadvance() * message.length() * 0.55f;

            font.draw(
                    batch,
                    message,
                    screenWidth / 2f - messageWidth / 2f,
                    screenHeight / 2f + 20f
            );

            if (failed) {
                font.setColor(Color.LIGHT_GRAY);
                font.getData().setScale(0.95f);
                font.draw(
                        batch,
                        "ENTER = continuar para Marte",
                        screenWidth / 2f - 135f,
                        screenHeight / 2f - 35f
                );
            }
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

        if (grabber != null) {
            try {
                grabber.stop();
            } catch (Exception ignored) {
            }

            try {
                grabber.release();
            } catch (Exception ignored) {
            }

            grabber = null;
        }

        if (converter != null) {
            converter.close();
            converter = null;
        }

        if (framePixmap != null) {
            framePixmap.dispose();
            framePixmap = null;
        }

        if (videoTexture != null) {
            videoTexture.dispose();
            videoTexture = null;
        }

        batch.dispose();
        font.dispose();
    }
}
