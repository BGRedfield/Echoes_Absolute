package io.github.some_example_name.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    public static final float WIDTH = 64f;
    public static final float HEIGHT = 64f;

    private static final float SPEED = 320f;

    private final Rectangle hitbox;

    public Player(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void update(float delta, float worldWidth, float worldHeight) {
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveY += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveY -= 1f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= 1f;
        }

        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        if (length > 0f) {
            moveX /= length;
            moveY /= length;
        }

        hitbox.x += moveX * SPEED * delta;
        hitbox.y += moveY * SPEED * delta;

        if (hitbox.x < 0f) {
            hitbox.x = 0f;
        }
        if (hitbox.y < 0f) {
            hitbox.y = 0f;
        }
        if (hitbox.x + hitbox.width > worldWidth) {
            hitbox.x = worldWidth - hitbox.width;
        }
        if (hitbox.y + hitbox.height > worldHeight) {
            hitbox.y = worldHeight - hitbox.height;
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getX() {
        return hitbox.x;
    }

    public float getY() {
        return hitbox.y;
    }

    public float getWidth() {
        return hitbox.width;
    }

    public float getHeight() {
        return hitbox.height;
    }

    public float getCenterX() {
        return hitbox.x + hitbox.width / 2f;
    }

    public float getCenterY() {
        return hitbox.y + hitbox.height / 2f;
    }
}
