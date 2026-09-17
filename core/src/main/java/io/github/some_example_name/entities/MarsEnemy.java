package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Melee Martian used by the Mars encounter. */
public class MarsEnemy {

    public static final float WIDTH = 54f;
    public static final float HEIGHT = 54f;
    public static final float MAX_HEALTH = 60f;
    public static final float SPEED = Player.SPEED;
    public static final float MELEE_DAMAGE = 10f;
    private static final float ATTACK_COOLDOWN = 0.55f;

    private final Rectangle hitbox;
    private float health = MAX_HEALTH;
    private float attackTimer;
    private boolean dead;

    public MarsEnemy(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void update(float delta, float playerX, float playerY, PlayerStats stats) {
        if (dead) {
            return;
        }

        float dx = playerX - getCenterX();
        float dy = playerY - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.001f) {
            dx /= distance;
            dy /= distance;

            if (distance > 40f) {
                hitbox.x += dx * SPEED * delta;
                hitbox.y += dy * SPEED * delta;
            }
        }

        attackTimer -= delta;
        if (hitbox.x < 0f) hitbox.x = 0f;
        if (hitbox.y < 0f) hitbox.y = 0f;
        if (attackTimer <= 0f && distance <= 82f) {
            stats.damage(MELEE_DAMAGE, DeathCause.MARTIAN_MELEE);
            attackTimer = ATTACK_COOLDOWN;
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

    public boolean isDead() { return dead; }
    public float getHealth() { return health; }
    public Rectangle getHitbox() { return hitbox; }
    public float getX() { return hitbox.x; }
    public float getY() { return hitbox.y; }
    public float getWidth() { return hitbox.width; }
    public float getHeight() { return hitbox.height; }
    public float getCenterX() { return hitbox.x + hitbox.width / 2f; }
    public float getCenterY() { return hitbox.y + hitbox.height / 2f; }
}
