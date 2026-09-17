package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Temporary portal attack placed at the player's position by the Mars boss. */
public class MarsPortalStrike {

    public static final float SIZE = 150f;
    public static final float WARNING_TIME = 0.85f;
    public static final float ACTIVE_TIME = 0.65f;
    public static final float DAMAGE = 30f;

    private final Rectangle hitbox;
    private float timer;
    private boolean hitPlayer;

    public MarsPortalStrike(float x, float y) {
        hitbox = new Rectangle(x - SIZE / 2f, y - SIZE / 2f, SIZE, SIZE);
    }

    public void update(float delta) {
        timer += delta;
    }

    public boolean isActive() {
        return timer >= WARNING_TIME && timer <= WARNING_TIME + ACTIVE_TIME;
    }

    public boolean isFinished() {
        return timer > WARNING_TIME + ACTIVE_TIME;
    }

    public boolean shouldDamagePlayer() {
        if (!isActive() || hitPlayer) {
            return false;
        }
        hitPlayer = true;
        return true;
    }

    public Rectangle getHitbox() { return hitbox; }
    public float getX() { return hitbox.x; }
    public float getY() { return hitbox.y; }
    public float getWidth() { return hitbox.width; }
    public float getHeight() { return hitbox.height; }
    public float getProgress() {
        return MathUtils.clamp(timer / (WARNING_TIME + ACTIVE_TIME), 0f, 1f);
    }
}
