package io.github.some_example_name.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.PlayerStats;

/**
 * Tempestade roxa pós-boss.
 *
 * Duração: 12 segundos.
 * Dano: 10 por segundo.
 * Enquanto ativa, o portal de saída permanece fechado.
 */
public class BossStorm {

    public static final float DURATION = 12f;
    public static final float DAMAGE_PER_SECOND = 10f;

    private float remaining;
    private float damageTimer;

    private float targetX;
    private float targetY;

    public BossStorm() {
        this(0f);
    }

    public BossStorm(float remaining) {
        this.remaining = Math.max(0f, remaining);
        this.damageTimer = 1f;
    }

    public void start(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.remaining = DURATION;
        this.damageTimer = 1f;
    }

    public void restore(float remaining, float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.remaining = Math.max(0f, remaining);
        this.damageTimer = 1f;
    }

    public void update(float delta, PlayerStats stats) {
        if (!isActive()) {
            return;
        }

        remaining = Math.max(0f, remaining - delta);
        damageTimer -= delta;

        while (damageTimer <= 0f && isActive()) {
            damageTimer += 1f;
            stats.damage(DAMAGE_PER_SECOND, DeathCause.UNKNOWN);
        }
    }

    public boolean isActive() {
        return remaining > 0.001f;
    }

    public boolean isFinished() {
        return remaining <= 0.001f;
    }

    public float getRemaining() {
        return remaining;
    }

    public float getProgress() {
        return 1f - MathUtils.clamp(remaining / DURATION, 0f, 1f);
    }

    /**
     * Desenha um fechamento roxo em direção ao portal.
     * Deve ser chamado com ShapeRenderer já configurado para o mundo.
     */
    public void drawWorld(ShapeRenderer renderer, float worldWidth, float worldHeight) {
        if (!isActive()) {
            return;
        }

        float progress = getProgress();
        float maxRadius = (float) Math.sqrt(
                worldWidth * worldWidth + worldHeight * worldHeight
        ) * 0.72f;
        float safeRadius = 170f;
        float radius = MathUtils.lerp(maxRadius, safeRadius, progress);

        renderer.setColor(new Color(0.38f, 0.02f, 0.55f, 0.17f));
        renderer.circle(targetX, targetY, radius);

        renderer.setColor(new Color(0.72f, 0.10f, 0.95f, 0.30f));
        renderer.circle(targetX, targetY, radius * 0.92f);

        renderer.setColor(new Color(0.88f, 0.35f, 1f, 0.82f));
        renderer.circle(targetX, targetY, Math.max(10f, radius - 16f), 64);

        // Faixas diagonais para dar a sensação de tempestade em movimento.
        float stripeOffset = progress * 220f;
        renderer.setColor(new Color(0.55f, 0.04f, 0.80f, 0.18f));
        for (int i = -8; i <= 8; i++) {
            float x = (i * 180f) + stripeOffset - 400f;
            renderer.rectLine(
                    x,
                    -200f,
                    x + worldHeight + 400f,
                    worldHeight + 200f,
                    22f
            );
        }
    }
}
