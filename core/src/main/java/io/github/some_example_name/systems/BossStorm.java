package io.github.some_example_name.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import io.github.some_example_name.entities.DeathCause;
import io.github.some_example_name.entities.PlayerStats;

/**
 * Tempestade roxa pós-boss.
 *
 * Após o boss morrer, espera 4 segundos.
 * Depois a tempestade aparece na borda do mapa e fecha em direção ao portal.
 *
 * O centro da tempestade fica LIMPO:
 * somente o anel externo é roxo e causa dano.
 */
public class BossStorm {

    public static final float START_DELAY = 4f;
    public static final float DURATION = 12f;
    public static final float DAMAGE_PER_SECOND = 10f;

    private static final float INITIAL_SAFE_RADIUS = 1550f;
    private static final float FINAL_SAFE_RADIUS = 180f;

    private float remaining;
    private float delayRemaining;
    private float damageTimer;

    private float targetX;
    private float targetY;
    private float safeRadius;

    private boolean pendingStart;

    public BossStorm() {
        this(0f);
    }

    public BossStorm(float remaining) {
        this.remaining = Math.max(0f, remaining);
        this.damageTimer = 1f;
        this.pendingStart = false;

        if (this.remaining > 0f) {
            this.safeRadius = MathUtils.lerp(
                    FINAL_SAFE_RADIUS,
                    INITIAL_SAFE_RADIUS,
                    1f - MathUtils.clamp(this.remaining / DURATION, 0f, 1f)
            );
        } else {
            this.safeRadius = INITIAL_SAFE_RADIUS;
        }
    }

    /**
     * Agenda a tempestade. Ela NÃO aparece imediatamente.
     */
    public void start(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.remaining = 0f;
        this.delayRemaining = START_DELAY;
        this.damageTimer = 1f;
        this.safeRadius = INITIAL_SAFE_RADIUS;
        this.pendingStart = true;
    }

    /**
     * Restaura uma tempestade que já estava ativa no save.
     */
    public void restore(float remaining, float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.remaining = Math.max(0f, remaining);
        this.delayRemaining = 0f;
        this.damageTimer = 1f;
        this.safeRadius = MathUtils.lerp(
                FINAL_SAFE_RADIUS,
                INITIAL_SAFE_RADIUS,
                getProgress()
        );
        this.pendingStart = false;
    }

    public void update(
            float delta,
            PlayerStats stats,
            float playerX,
            float playerY
    ) {
        if (pendingStart) {
            delayRemaining -= delta;

            if (delayRemaining <= 0f) {
                delayRemaining = 0f;
                pendingStart = false;
                remaining = DURATION;
                damageTimer = 1f;
                safeRadius = INITIAL_SAFE_RADIUS;
            }

            return;
        }

        if (!isActive()) {
            return;
        }

        remaining = Math.max(0f, remaining - delta);

        // A zona segura diminui do interior para o ponto do portal.
        safeRadius = MathUtils.lerp(
                INITIAL_SAFE_RADIUS,
                FINAL_SAFE_RADIUS,
                getProgress()
        );

        damageTimer -= delta;

        float dx = playerX - targetX;
        float dy = playerY - targetY;
        float distanceSquared = dx * dx + dy * dy;
        float safeSquared = safeRadius * safeRadius;
        boolean playerInsideSafeZone = distanceSquared <= safeSquared;

        while (damageTimer <= 0f && isActive()) {
            damageTimer += 1f;

            if (!playerInsideSafeZone) {
                stats.damage(DAMAGE_PER_SECOND, DeathCause.UNKNOWN);
            }
        }
    }

    public boolean isActive() {
        return !pendingStart && remaining > 0.001f;
    }

    public boolean isPending() {
        return pendingStart;
    }

    /**
     * Só fica concluída quando a tempestade já terminou.
     * Durante os 4 segundos de espera, continua pendente.
     */
    public boolean isFinished() {
        return !pendingStart && remaining <= 0.001f;
    }

    public float getRemaining() {
        return remaining;
    }

    public float getDelayRemaining() {
        return delayRemaining;
    }

    public float getProgress() {
        return 1f - MathUtils.clamp(remaining / DURATION, 0f, 1f);
    }

    /**
     * Desenha SOMENTE o anel externo da tempestade.
     * O centro permanece completamente livre para o mundo aparecer.
     */
    public void drawWorld(ShapeRenderer renderer, float worldWidth, float worldHeight) {
        if (!isActive()) {
            return;
        }

        float progress = getProgress();

        float maxRadius = (float) Math.sqrt(
                worldWidth * worldWidth + worldHeight * worldHeight
        ) * 1.15f;

        float outerRadius = maxRadius;
        float innerRadius = MathUtils.lerp(
                INITIAL_SAFE_RADIUS,
                FINAL_SAFE_RADIUS,
                progress
        );

        final int segments = 96;

        // Apenas o anel: cada pedaço é um quadrilátero entre o raio interno
        // e o raio externo. O centro não recebe a pintura roxa.
        renderer.setColor(new Color(0.38f, 0.02f, 0.55f, 0.23f));

        for (int i = 0; i < segments; i++) {
            float a0 = MathUtils.PI2 * i / segments;
            float a1 = MathUtils.PI2 * (i + 1) / segments;

            float x0Outer = targetX + MathUtils.cos(a0) * outerRadius;
            float y0Outer = targetY + MathUtils.sin(a0) * outerRadius;
            float x1Outer = targetX + MathUtils.cos(a1) * outerRadius;
            float y1Outer = targetY + MathUtils.sin(a1) * outerRadius;

            float x0Inner = targetX + MathUtils.cos(a0) * innerRadius;
            float y0Inner = targetY + MathUtils.sin(a0) * innerRadius;
            float x1Inner = targetX + MathUtils.cos(a1) * innerRadius;
            float y1Inner = targetY + MathUtils.sin(a1) * innerRadius;

            renderer.triangle(
                    x0Outer, y0Outer,
                    x1Outer, y1Outer,
                    x1Inner, y1Inner
            );

            renderer.triangle(
                    x0Outer, y0Outer,
                    x1Inner, y1Inner,
                    x0Inner, y0Inner
            );
        }

        // Borda brilhante do olho da tempestade.
        renderer.setColor(new Color(0.90f, 0.22f, 1f, 0.90f));
        for (int i = 0; i < segments; i++) {
            float a0 = MathUtils.PI2 * i / segments;
            float a1 = MathUtils.PI2 * (i + 1) / segments;

            float x0 = targetX + MathUtils.cos(a0) * innerRadius;
            float y0 = targetY + MathUtils.sin(a0) * innerRadius;
            float x1 = targetX + MathUtils.cos(a1) * innerRadius;
            float y1 = targetY + MathUtils.sin(a1) * innerRadius;

            renderer.rectLine(x0, y0, x1, y1, 8f);
        }

        // Faixas diagonais movimentando dentro da área da tempestade.
        float stripeOffset = progress * 260f;
        renderer.setColor(new Color(0.65f, 0.05f, 0.90f, 0.16f));

        for (int i = -16; i <= 16; i++) {
            float x = i * 150f + stripeOffset - 500f;

            renderer.rectLine(
                    x,
                    -500f,
                    x + worldHeight + 900f,
                    worldHeight + 400f,
                    16f
            );
        }
    }
}
