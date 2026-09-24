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
    private static final float SPREAD_DURATION = 1.35f;
    private static final float SPREAD_SPEED = 180f;

    private final Rectangle hitbox;
    private final float spreadAngle;
    private final float lateralSign;

    private float health = MAX_HEALTH;
    private float cycleTimer;
    private float shotTimer;
    private float spreadTimer;
    private boolean dead;

    public AmericanEnemy(float x, float y) {
        this(x, y, MathUtils.random(0f, MathUtils.PI2), MathUtils.randomBoolean() ? 1f : -1f);
    }

    public AmericanEnemy(float x, float y, float spreadAngle, float lateralSign) {
        hitbox = new Rectangle(x, y, WIDTH, HEIGHT);
        this.spreadAngle = spreadAngle;
        this.lateralSign = lateralSign >= 0f ? 1f : -1f;

        // Começa a se espalhar a partir do centro do Obama.
        spreadTimer = SPREAD_DURATION;
        cycleTimer = 0f;
        shotTimer = SHOT_INTERVAL;
    }

    public void update(float delta, float playerX, float playerY, Array<EnemyBullet> bullets) {
        if (dead) {
            return;
        }

        if (spreadTimer > 0f) {
            spreadTimer -= delta;

            float forwardX = MathUtils.cos(spreadAngle);
            float forwardY = MathUtils.sin(spreadAngle);

            // Cada americano tem uma direção radial e uma pequena
            // componente lateral diferente, evitando que todos sigam a mesma rota.
            float sideX = -forwardY * lateralSign;
            float sideY = forwardX * lateralSign;
            float sideWave = MathUtils.sin((SPREAD_DURATION - spreadTimer) * 4.5f);

            hitbox.x += (forwardX + sideX * 0.30f * sideWave) * SPREAD_SPEED * delta;
            hitbox.y += (forwardY + sideY * 0.30f * sideWave) * SPREAD_SPEED * delta;
        } else {
            float dx = playerX - getCenterX();
            float dy = playerY - getCenterY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 1f) {
                float dirX = dx / distance;
                float dirY = dy / distance;

                // Depois de se espalharem, cada um mantém uma deriva lateral
                // própria enquanto se aproxima do jogador.
                float sideX = -dirY * lateralSign;
                float sideY = dirX * lateralSign;

                hitbox.x += (dirX + sideX * 0.22f) * MOVE_SPEED * delta;
                hitbox.y += (dirY + sideY * 0.22f) * MOVE_SPEED * delta;
            }
        }

        cycleTimer += delta;

        if (cycleTimer < BURST_DURATION) {
            shotTimer += delta;
            while (shotTimer >= SHOT_INTERVAL) {
                shotTimer -= SHOT_INTERVAL;

                // A direção do tiro é recalculada aqui porque o americano
                // pode estar na fase de espalhamento e não tem dx/dy
                // definidos naquele bloco de movimento.
                float shotDx = playerX - getCenterX();
                float shotDy = playerY - getCenterY();

                bullets.add(new EnemyBullet(
                        getCenterX(),
                        getCenterY(),
                        shotDx,
                        shotDy,
                        true
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
