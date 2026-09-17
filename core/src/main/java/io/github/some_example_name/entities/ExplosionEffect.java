package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;

/** Visual explosion effect used when a Trump missile reaches its impact point. */
public class ExplosionEffect {

    private static final float DURATION = 0.65f;
    private static final float MAX_RADIUS = 150f;

    private final float centerX;
    private final float centerY;
    private float timer = DURATION;

    public ExplosionEffect(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void update(float delta) {
        timer -= delta;
    }

    public boolean isFinished() {
        return timer <= 0f;
    }

    public float getProgress() {
        return 1f - MathUtils.clamp(timer / DURATION, 0f, 1f);
    }

    public float getRadius() {
        return 28f + getProgress() * MAX_RADIUS;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }
}
