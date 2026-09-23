package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Portal that becomes available after the Lua boss is defeated. */
public class MarsPortal {

    public static final float WIDTH = 170f;
    public static final float HEIGHT = 170f;

    private final Rectangle hitbox;

    public MarsPortal(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
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
