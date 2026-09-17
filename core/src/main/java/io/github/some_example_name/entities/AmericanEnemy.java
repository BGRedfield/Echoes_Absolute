package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Red cube enemy for the Lua encounter. It fires rapid one-second bursts,
 * followed by a two-second pause. It takes two player shots to defeat.
 */
public class AmericanEnemy {

    public static final float WIDTH = 58f;
    public static final float HEIGHT = 58f;
    public static final float MAX_HEALTH = 20f;

    private static final float MOVE_SPEED = 105f;
    private static final float BURST_DURATION = 1f;
    private static final float PAUSE_DURATION = 2f;
    private static final float SHOT_INTERVAL = 0.14f;

    private final Rectangle hitbox;
    private float health = MAX_HEALTH;
    private float cycleTimer;
    private float shotTimer;
    private boolean dead;

    public AmericanEnemy(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
        // Começa a atirar imediatamente quando aparece.
        cycleTimer = 0f;
        shotTimer = SHOT_INTERVAL;
    }

    public void update(float delta, float playerX, float playerY, Array<EnemyBullet> bullets) {
        if (dead) {
            return;
        }

        float dx = playerX - getCenterX();
        float dy = playerY - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 1f) {
            hitbox.x += dx / distance * MOVE_SPEED * delta;
            hitbox.y += dy / distance * MOVE_SPEED * delta;
        }

        cycleTimer += delta;

        if (cycleTimer < BURST_DURATION) {
            shotTimer += delta;
            while (shotTimer >= SHOT_INTERVAL) {
                shotTimer -= SHOT_INTERVAL;
                bullets.add(new EnemyBullet(
                        getCenterX(),
                        getCenterY(),
                        dx,
                        dy
                ));
            }
        } else if (cycleTimer >= BURST_DURATION + PAUSE_DURATION) {
            cycleTimer = 0f;
            shotTimer = 0f;
        }
    }

    public void takeDamage(float amount) {
        if (dead || amount <= 0f) {
            return;
        }

        health = MathUtils.clamp(health - amount, 0f, MAX_HEALTH);
        if (health <= 0f) {
            dead = true;
        }
    }

    public boolean isDead() {
        return dead;
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
