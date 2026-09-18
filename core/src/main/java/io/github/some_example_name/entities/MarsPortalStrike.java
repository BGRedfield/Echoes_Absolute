package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Portal projectile launched by the Mars boss. It travels to the targeted player position, then activates. */
public class MarsPortalStrike {

    public static final float SIZE = 150f;
    public static final float WARNING_TIME = 0.75f;
    public static final float ACTIVE_TIME = 0.65f;
    public static final float DAMAGE = 50f;
    public static final float SPEED = 520f;

    private final Rectangle hitbox;
    private final float targetX;
    private final float targetY;
    private float timer;
    private boolean arrived;
    private boolean hitPlayer;

    public MarsPortalStrike(float startX, float startY, float targetX, float targetY) {
        hitbox = new Rectangle(startX - SIZE / 2f, startY - SIZE / 2f, SIZE, SIZE);
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void update(float delta) {
        if (!arrived) {
            float dx = targetX - (hitbox.x + hitbox.width / 2f);
            float dy = targetY - (hitbox.y + hitbox.height / 2f);
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= SPEED * delta || distance <= 8f) {
                hitbox.x = targetX - SIZE / 2f;
                hitbox.y = targetY - SIZE / 2f;
                arrived = true;
                timer = 0f;
            } else if (distance > 0.001f) {
                dx /= distance;
                dy /= distance;
                hitbox.x += dx * SPEED * delta;
                hitbox.y += dy * SPEED * delta;
            }
            return;
        }

        timer += delta;
    }

    public boolean isActive() {
        return arrived && timer >= WARNING_TIME && timer <= WARNING_TIME + ACTIVE_TIME;
    }

    public boolean isFinished() {
        return arrived && timer > WARNING_TIME + ACTIVE_TIME;
    }

    public boolean shouldDamagePlayer() {
        if (!isActive() || hitPlayer) {
            return false;
        }
        hitPlayer = true;
        return true;
    }

    public boolean hasArrived() {
        return arrived;
    }

    public boolean isExploding() {
        return arrived && timer >= WARNING_TIME;
    }

    public float getExplosionProgress() {
        if (!arrived || timer < WARNING_TIME) {
            return 0f;
        }
        return MathUtils.clamp(
                (timer - WARNING_TIME) / ACTIVE_TIME,
                0f,
                1f
        );
    }

    public Rectangle getHitbox() { return hitbox; }
    public float getX() { return hitbox.x; }
    public float getY() { return hitbox.y; }
    public float getWidth() { return hitbox.width; }
    public float getHeight() { return hitbox.height; }
    public float getProgress() {
        if (!arrived) return 0f;
        return MathUtils.clamp(timer / (WARNING_TIME + ACTIVE_TIME), 0f, 1f);
    }
}
