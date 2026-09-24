package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Cube missile that travels from the boss to a marked impact zone. */
public class TrumpMissile {

    public static final float IMPACT_SIZE = 180f;

    private static final float SPEED = 700f;
    private static final float SIZE = 30f;

    private final Rectangle hitbox;
    private final float targetX;
    private final float targetY;
    private final float directionX;
    private final float directionY;
    private final Rectangle impactArea;
    private boolean arrived;

    public TrumpMissile(float startX, float startY, float targetX, float targetY) {
        hitbox = new Rectangle(
                startX - SIZE / 2f,
                startY - SIZE / 2f,
                SIZE,
                SIZE
        );
        this.targetX = targetX;
        this.targetY = targetY;

        float directionLength = (float) Math.sqrt(
                (targetX - startX) * (targetX - startX)
                        + (targetY - startY) * (targetY - startY)
        );
        if (directionLength <= 0.001f) {
            this.directionX = 1f;
            this.directionY = 0f;
        } else {
            this.directionX = (targetX - startX) / directionLength;
            this.directionY = (targetY - startY) / directionLength;
        }

        impactArea = new Rectangle(
                targetX - IMPACT_SIZE / 2f,
                targetY - IMPACT_SIZE / 2f,
                IMPACT_SIZE,
                IMPACT_SIZE
        );
    }

    public void update(float delta) {
        if (arrived) {
            return;
        }

        float dx = targetX - getCenterX();
        float dy = targetY - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= SPEED * delta) {
            hitbox.x = targetX - SIZE / 2f;
            hitbox.y = targetY - SIZE / 2f;
            arrived = true;
            return;
        }

        if (distance > 0.001f) {
            hitbox.x += dx / distance * SPEED * delta;
            hitbox.y += dy / distance * SPEED * delta;
        }
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

    public boolean hasArrived() {
        return arrived;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public Rectangle getImpactArea() {
        return impactArea;
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
