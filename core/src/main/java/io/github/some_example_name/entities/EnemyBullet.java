package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Generic blue projectile fired by Lua enemies and rifle barriers. */
public class EnemyBullet {

    private static final float SPEED = 520f;
    private static final float SIZE = 14f;

    private final Rectangle hitbox;
    private final float directionX;
    private final float directionY;
    private final boolean americanBullet;

    public EnemyBullet(float x, float y, float directionX, float directionY) {
        this(x, y, directionX, directionY, false);
    }

    public EnemyBullet(
            float x,
            float y,
            float directionX,
            float directionY,
            boolean americanBullet
    ) {
        this.americanBullet = americanBullet;

        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (length <= 0.001f) {
            directionX = 0f;
            directionY = -1f;
        } else {
            directionX /= length;
            directionY /= length;
        }

        this.directionX = directionX;
        this.directionY = directionY;
        hitbox = new Rectangle(x - SIZE / 2f, y - SIZE / 2f, SIZE, SIZE);
    }

    public boolean isAmericanBullet() {
        return americanBullet;
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

    public void update(float delta) {
        hitbox.x += directionX * SPEED * delta;
        hitbox.y += directionY * SPEED * delta;
    }

    public boolean isOutsideWorld(float worldWidth, float worldHeight) {
        return hitbox.x + hitbox.width < 0f
                || hitbox.y + hitbox.height < 0f
                || hitbox.x > worldWidth
                || hitbox.y > worldHeight;
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
