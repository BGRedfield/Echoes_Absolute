package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Fictional lunar boss that follows the active player. */
public class TrumpBoss {

    public static final float MAX_HEALTH = 500f;
    public static final float WIDTH = 260f;
    public static final float HEIGHT = 260f;
    public static final float SPEED = 90f;

    private static TrumpBoss activeBoss;

    private final Rectangle hitbox;
    private float health = MAX_HEALTH;
    private boolean dead;

    public TrumpBoss(float x, float y) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
        activeBoss = this;
    }

    /** Called from Player.update so the boss follows every frame without changing the screen architecture. */
    public static void updateActive(float delta, float worldWidth, float worldHeight) {
        if (activeBoss == null || activeBoss.dead) {
            return;
        }

        Player player = Player.getActivePlayer();
        if (player == null) {
            return;
        }

        activeBoss.update(delta, player.getCenterX(), player.getCenterY(), worldWidth, worldHeight);
    }

    public void update(float delta, float playerX, float playerY,
                       float worldWidth, float worldHeight) {
        if (dead) return;

        float dx = playerX - getCenterX();
        float dy = playerY - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 0.001f) {
            dx /= distance;
            dy /= distance;
            hitbox.x += dx * SPEED * delta;
            hitbox.y += dy * SPEED * delta;
        }

        hitbox.x = MathUtils.clamp(hitbox.x, 0f, worldWidth - WIDTH);
        hitbox.y = MathUtils.clamp(hitbox.y, 0f, worldHeight - HEIGHT);
    }

    public void takeDamage(float amount) {
        if (dead || amount <= 0f) return;
        health = MathUtils.clamp(health - amount, 0f, MAX_HEALTH);
        if (health <= 0f) {
            dead = true;
            if (activeBoss == this) activeBoss = null;
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
