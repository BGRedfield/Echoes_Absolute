package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Red warning area shown two seconds before a missile impact. */
public class MissileWarning {

    private final Rectangle area;
    private float remainingTime;

    public MissileWarning(float x, float y, float width, float height, float duration) {
        area = new Rectangle(x, y, width, height);
        remainingTime = duration;
    }

    public void update(float delta) {
        remainingTime -= delta;
    }

    public boolean isReadyToLaunch() {
        return remainingTime <= 0f;
    }

    public Rectangle getArea() {
        return area;
    }

    public float getX() {
        return area.x;
    }

    public float getY() {
        return area.y;
    }

    public float getWidth() {
        return area.width;
    }

    public float getHeight() {
        return area.height;
    }
}
