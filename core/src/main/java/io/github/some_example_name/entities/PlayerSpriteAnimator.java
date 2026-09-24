package io.github.some_example_name.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Anima o jogador a partir de um sprite sheet 512x768.
 *
 * Cada frame tem 64x64.
 *
 * As 12 linhas são:
 * 0-3: caminhada (baixo, cima, esquerda, direita)
 * 4-7: dash      (baixo, cima, esquerda, direita)
 * 8-11: dano     (baixo, cima, esquerda, direita)
 *
 * Cada animação possui 8 frames por direção.
 */
public class PlayerSpriteAnimator {

    public static final int FRAME_SIZE = 64;
    public static final int SHEET_COLUMNS = 8;
    public static final int SHEET_ROWS = 12;

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

        int directionRow = getDirectionRow(player);
        int row;
        int column;

        if (stats.isInvulnerable()) {
            row = 8 + directionRow;
            column = (int) (animationTime / DAMAGE_FRAME_TIME) % 8;
        } else if (player.isDashing()) {
            row = 4 + directionRow;
            column = (int) (animationTime / DASH_FRAME_TIME) % 8;
        } else if (player.isMoving()) {
            row = directionRow;
            column = (int) (animationTime / WALK_FRAME_TIME) % 8;
        } else {
            row = directionRow;
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
