package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.entities.PlayerStats;

/** Generic boss used by the four Titan castle fights. */
public class TitaBoss {

    public enum Type {
        OBAMA,
        AUTHENTIC_GAMES,
        VERITY,
        CR7
    }

    private final Type type;
    private final String name;
    private final float maxHealth;
    private final float speed;
    private final float contactDamage;
    private final float attackInterval;
    private final int projectileCount;
    private final float projectileDamage;
    private final Rectangle hitbox;

    private float health;
    private float attackTimer;
    private boolean dead;

    public TitaBoss(Type type, float x, float y) {
        this.type = type;

        switch (type) {
            case OBAMA:
                name = "BARACK OBAMA";
                maxHealth = 10000f;
                speed = 115f;
                contactDamage = 22f;
                attackInterval = 2.0f;
                projectileCount = 8;
                projectileDamage = 18f;
                break;
            case AUTHENTIC_GAMES:
                name = "AUTHENTIC GAMES";
                maxHealth = 800f;
                speed = 115f;
                contactDamage = 14f;
                attackInterval = 2.15f;
                projectileCount = 10;
                projectileDamage = 18f;
                break;
            case VERITY:
                name = "VERITY";
                maxHealth = 900f;
                speed = 120f;
                contactDamage = 16f;
                attackInterval = 1.95f;
                projectileCount = 12;
                projectileDamage = 20f;
                break;
            case CR7:
            default:
                name = "CR7";
                maxHealth = 2500f;
                speed = 135f;
                contactDamage = 22f;
                attackInterval = 1.35f;
                projectileCount = 16;
                projectileDamage = 28f;
                break;
        }

        this.health = maxHealth;
        hitbox = new Rectangle(x, y, 150f, 150f);
    }

    public void update(float delta, float playerX, float playerY, PlayerStats stats,
                       float minX, float minY, float maxX, float maxY) {
        if (dead) {
            return;
        }

        float dx = playerX - getCenterX();
        float dy = playerY - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.001f) {
            dx /= distance;
            dy /= distance;

            if (distance > 150f) {
                hitbox.x += dx * speed * delta;
                hitbox.y += dy * speed * delta;
            }
        }

        hitbox.x = MathUtils.clamp(hitbox.x, minX, maxX - hitbox.width);
        hitbox.y = MathUtils.clamp(hitbox.y, minY, maxY - hitbox.height);

        attackTimer -= delta;

        if (distance <= 185f && attackTimer <= 0f) {
            stats.damage(contactDamage, DeathCause.UNKNOWN);
            attackTimer = 0.9f;
        }
    }

    public boolean canShoot() {
        return !dead && attackTimer <= 0f;
    }

    public void resetAttackTimer() {
        attackTimer = attackInterval;
    }

    public void takeDamage(float amount) {
        if (dead || amount <= 0f) {
            return;
        }

        health = MathUtils.clamp(health - amount, 0f, maxHealth);
        if (health <= 0f) {
            dead = true;
        }
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return dead;
    }

    public int getProjectileCount() {
        return projectileCount;
    }

    public float getProjectileDamage() {
        return projectileDamage;
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
