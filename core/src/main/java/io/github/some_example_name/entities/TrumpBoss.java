package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Fictional boss entity used by the Lua game encounter.
 * The boss is represented by a large orange cube until an asset exists.
 */
public class TrumpBoss {

    public static final float MAX_HEALTH = 500f;
    public static final float WIDTH = 520f;
    public static final float HEIGHT = 520f;

    private final Rectangle hitbox;
    private float health = MAX_HEALTH;
    private boolean dead;

    public TrumpBoss(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void takeDamage(float amount) {
        if (dead || amount <= 0f) {
            return;
        }

        health = MathUtils.clamp(health - amount, 0f, MAX_HEALTH);
        if (health <= 0f) {
            dead = true;
        }
    }

    public boolean isDead() {
        return dead;
    }

    public float getHealth() {
        return health;
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
