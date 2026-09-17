package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

public class Laser {

    private static final float SPEED = 750f;
    private static final float VERTICAL_WIDTH = 12f;
    private static final float VERTICAL_HEIGHT = 28f;
    private static final float HORIZONTAL_WIDTH = 28f;
    private static final float HORIZONTAL_HEIGHT = 12f;

    private final Rectangle hitbox;
    private final float directionX;
    private final float directionY;

    public Laser(float x, float y, float directionX, float directionY) {
        this.directionX = directionX;
        this.directionY = directionY;

        boolean horizontal = Math.abs(directionX) > Math.abs(directionY);
        float width = horizontal ? HORIZONTAL_WIDTH : VERTICAL_WIDTH;
        float height = horizontal ? HORIZONTAL_HEIGHT : VERTICAL_HEIGHT;

        hitbox = new Rectangle(
                x - width / 2f,
                y - height / 2f,
                width,
                height
        );
    }

    public void update(float delta) {
        hitbox.x += directionX * SPEED * delta;
        hitbox.y += directionY * SPEED * delta;
    }

    public boolean isOutsideWorld(float worldWidth, float worldHeight) {
        return hitbox.x + hitbox.width < 0f
                || hitbox.y + hitbox.height < 0f
                || hitbox.x > worldWidth
                || hitbox.y > worldHeight;
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
}
