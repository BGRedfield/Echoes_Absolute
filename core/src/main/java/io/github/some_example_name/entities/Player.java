package io.github.some_example_name.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    public static final float WIDTH = 64f;
    public static final float HEIGHT = 64f;
    public static final float SPEED = 320f;

    private static final float DASH_SPEED = 1000f;
    private static final float DASH_DURATION = 0.12f;
    private static final float DASH_COOLDOWN = 0.65f;

    private static Player activePlayer;
    private final Rectangle hitbox;

    private float lastDirectionX = 1f;
    private float lastDirectionY = 0f;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private boolean moving;

    public Player(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
        activePlayer = this;
    }

    public void update(float delta, float worldWidth, float worldHeight) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1f;

        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        moving = length > 0f;
        if (length > 0f) {
            moveX /= length;
            moveY /= length;
            lastDirectionX = moveX;
            lastDirectionY = moveY;
        }

        if (dashCooldownTimer > 0f) {
            dashCooldownTimer -= delta;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                && dashCooldownTimer <= 0f
                && dashTimer <= 0f) {
            dashTimer = DASH_DURATION;
            dashCooldownTimer = DASH_COOLDOWN;
        }

        if (dashTimer > 0f) {
            hitbox.x += lastDirectionX * DASH_SPEED * delta;
            hitbox.y += lastDirectionY * DASH_SPEED * delta;
            dashTimer -= delta;
        } else {
            hitbox.x += moveX * SPEED * delta;
            hitbox.y += moveY * SPEED * delta;
        }

        if (hitbox.x < 0f) hitbox.x = 0f;
        if (hitbox.y < 0f) hitbox.y = 0f;
        if (hitbox.x + hitbox.width > worldWidth) hitbox.x = worldWidth - hitbox.width;
        if (hitbox.y + hitbox.height > worldHeight) hitbox.y = worldHeight - hitbox.height;
    }

    public static Player getActivePlayer() {
        return activePlayer;
    }

    public boolean isDashing() {
        return dashTimer > 0f;
    }

    public float getDashCooldownRemaining() {
        return Math.max(0f, dashCooldownTimer);
    }

    public boolean isMoving() {
        return moving;
    }

    public float getFacingDirectionX() {
        return lastDirectionX;
    }

    public float getFacingDirectionY() {
        return lastDirectionY;
    }

    public Rectangle getHitbox() { return hitbox; }
    public float getX() { return hitbox.x; }
    public float getY() { return hitbox.y; }
    public float getWidth() { return hitbox.width; }
    public float getHeight() { return hitbox.height; }
    public float getCenterX() { return hitbox.x + hitbox.width / 2f; }
    public float getCenterY() { return hitbox.y + hitbox.height / 2f; }
}
