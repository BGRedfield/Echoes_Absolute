package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Anima o jogador a partir de um sprite sheet 512x256.
 *
 * Cada frame tem 64x64.
 *
 * Linha 0: andando para baixo
 * Linha 1: andando para a esquerda
 * Linha 2: andando para a direita
 * Linha 3: andando para cima
 *
 * Colunas 0-3: caminhada
 * Colunas 4-5: dash
 * Colunas 6-7: dano
 */
public class PlayerSpriteAnimator {

    public static final int FRAME_SIZE = 64;
    public static final int SHEET_COLUMNS = 8;
    public static final int SHEET_ROWS = 4;

    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float DASH_FRAME_TIME = 0.07f;
    private static final float DAMAGE_FRAME_TIME = 0.11f;

    private float animationTime;

    public void update(float delta) {
        animationTime += Math.max(0f, delta);
    }

    public void draw(
            SpriteBatch batch,
            Texture spriteSheet,
            Texture fallbackTexture,
            Player player,
            PlayerStats stats
    ) {
        if (spriteSheet == null
                || spriteSheet.getWidth() < FRAME_SIZE * SHEET_COLUMNS
                || spriteSheet.getHeight() < FRAME_SIZE * SHEET_ROWS) {
            batch.draw(
                    fallbackTexture,
                    player.getX(),
                    player.getY(),
                    player.getWidth(),
                    player.getHeight()
            );
            return;
        }

        TextureRegion[][] frames = TextureRegion.split(
                spriteSheet,
                FRAME_SIZE,
                FRAME_SIZE
        );

        int row = getDirectionRow(player);
        int column;

        if (stats.isInvulnerable()) {
            column = 6 + ((int) (animationTime / DAMAGE_FRAME_TIME) % 2);
        } else if (player.isDashing()) {
            column = 4 + ((int) (animationTime / DASH_FRAME_TIME) % 2);
        } else if (player.isMoving()) {
            column = (int) (animationTime / WALK_FRAME_TIME) % 4;
        } else {
            column = 0;
        }

        batch.draw(
                frames[row][column],
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );
    }

    private int getDirectionRow(Player player) {
        float dx = player.getFacingDirectionX();
        float dy = player.getFacingDirectionY();

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0f ? 1 : 2;
        }

        return dy > 0f ? 3 : 0;
    }
}
