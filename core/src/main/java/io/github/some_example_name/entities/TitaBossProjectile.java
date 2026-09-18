package io.github.some_example_name.entities;

import com.badlogic.gdx.math.Rectangle;

/** Projectile fired by Titan castle bosses. */
public class TitaBossProjectile {

    public static final float SIZE = 24f;
    public static final float SPEED = 430f;

    private final Rectangle hitbox;
    private final float dx;
    private final float dy;
    private final float damage;
    private boolean hitPlayer;

    public TitaBossProjectile(float x, float y, float dx, float dy, float damage) {
        hitbox = new Rectangle(x - SIZE / 2f, y - SIZE / 2f, SIZE, SIZE);
        this.dx = dx;
        this.dy = dy;
        this.damage = damage;
    }

    public void update(float delta) {
        hitbox.x += dx * SPEED * delta;
        hitbox.y += dy * SPEED * delta;
    }

    public boolean hitsPlayer(Player player) {
        if (hitPlayer || !player.getHitbox().overlaps(hitbox)) {
            return false;
        }
        hitPlayer = true;
        return true;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isOutside(float width, float height) {
        return hitbox.x + hitbox.width < 0f
                || hitbox.y + hitbox.height < 0f
                || hitbox.x > width
                || hitbox.y > height;
    }

    public float getX() {
        return hitbox.x;
    }

    public float getY() {
        return hitbox.y;
    }

    public float getSize() {
        return SIZE;
    }
}
