package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Circular red warning area shown before a Trump missile impact. */
public class MissileWarning {

    private final float centerX;
    private final float centerY;
    private final float radius;
    private float remainingTime;

    public MissileWarning(float centerX, float centerY, float radius, float duration) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        remainingTime = duration;
    }

    public void update(float delta) {
        remainingTime -= delta;
    }

    public boolean isReadyToLaunch() {
        return remainingTime <= 0f;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getRadius() {
        return radius;
    }

    public float getRemainingTime() {
        return remainingTime;
    }

    /** Bounding rectangle used as the missile's target impact area. */
    public Rectangle getArea() {
        return new Rectangle(
                centerX - radius,
                centerY - radius,
                radius * 2f,
                radius * 2f
        );
    }

    public float getX() {
        return centerX - radius;
    }

    public float getY() {
        return centerY - radius;
    }

    public float getWidth() {
        return radius * 2f;
    }

    public float getHeight() {
        return radius * 2f;
    }
}
