package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Green mineral node collected during the Mars mining mission. */
public class MarsOre {

    public static final float SIZE = 62f;

    private final Rectangle hitbox;

    public MarsOre(float x, float y) {
        hitbox = new Rectangle(x, y, SIZE, SIZE);
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
