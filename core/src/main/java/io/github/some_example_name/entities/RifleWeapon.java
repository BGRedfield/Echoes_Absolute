package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/** A destroyable rifle in the boss barrier. */
public class RifleWeapon {

    public static final float WIDTH = 76f;
    public static final float HEIGHT = 28f;
    public static final float MAX_HEALTH = 20f;
    private static final float SHOT_INTERVAL = 1f;

    /** Names available for the six barrier weapons. */
    public static final String[] DEFAULT_NAMES = {
            "M4",
            "M16",
            "AR-15",
            "HK416",
            "SCAR-L",
            "FAL"
    };

    private final String name;
    private final Rectangle hitbox;
    private float health = MAX_HEALTH;
    private float shotTimer;
    private boolean destroyed;

    public RifleWeapon(String name, float x, float y) {
        this.name = name;
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void update(float delta, float playerX, float playerY, Array<EnemyBullet> bullets) {
        if (destroyed) {
            return;
        }

        shotTimer += delta;
        while (shotTimer >= SHOT_INTERVAL) {
            shotTimer -= SHOT_INTERVAL;

            float dx = playerX - getCenterX();
            float dy = playerY - getCenterY();
            bullets.add(new EnemyBullet(getCenterX(), getCenterY(), dx, dy));
        }
    }

    public void takeDamage(float amount) {
        if (destroyed || amount <= 0f) {
            return;
        }

        health = MathUtils.clamp(health - amount, 0f, MAX_HEALTH);
        if (health <= 0f) {
            destroyed = true;
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public String getName() {
        return name;
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
