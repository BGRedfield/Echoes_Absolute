package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Giant fictional Martian boss used after the Mars weapon upgrade. */
public class SupremeAlienBoss {

    public static final float MAX_HEALTH = 1000f;
    public static final float WIDTH = 130f;
    public static final float HEIGHT = 130f;
    public static final float SPEED = 105f;
    public static final float BARRIER_MAX_HEALTH = 250f;

    private final Rectangle hitbox;
    private float health = MAX_HEALTH;
    private float barrierHealth;
    private boolean barrierActive;
    private boolean dead;

    public SupremeAlienBoss(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void update(float delta, float playerX, float playerY,
                       float worldWidth, float worldHeight) {
        if (dead) {
            return;
        }

        float dx = playerX - getCenterX();
        float dy = playerY - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.001f) {
            dx /= distance;
            dy /= distance;
            hitbox.x += dx * SPEED * delta;
            hitbox.y += dy * SPEED * delta;
        }

        hitbox.x = MathUtils.clamp(hitbox.x, 0f, worldWidth - WIDTH);
        hitbox.y = MathUtils.clamp(hitbox.y, 0f, worldHeight - HEIGHT);
    }

    /** Returns true when the hit affected the barrier or boss. */
    public boolean takeDamage(float amount) {
        if (dead || amount <= 0f) {
            return false;
        }

        if (barrierActive) {
            barrierHealth = MathUtils.clamp(barrierHealth - amount, 0f, BARRIER_MAX_HEALTH);
            if (barrierHealth <= 0f) {
                barrierActive = false;
            }
            return true;
        }

        health = MathUtils.clamp(health - amount, 0f, MAX_HEALTH);
        if (health <= 0f) {
            dead = true;
        }
        return true;
    }

    public void activateBarrier() {
        barrierActive = true;
        barrierHealth = BARRIER_MAX_HEALTH;
    }

    public boolean isBarrierActive() { return barrierActive; }
    public float getBarrierHealth() { return barrierHealth; }
    public float getHealth() { return health; }
    public boolean isDead() { return dead; }
    public Rectangle getHitbox() { return hitbox; }
    public float getX() { return hitbox.x; }
    public float getY() { return hitbox.y; }
    public float getWidth() { return hitbox.width; }
    public float getHeight() { return hitbox.height; }
    public float getCenterX() { return hitbox.x + hitbox.width / 2f; }
    public float getCenterY() { return hitbox.y + hitbox.height / 2f; }
}
