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
     * Inicia a tempestade imediatamente, sem a contagem de preparação.
     */
    public void startImmediate(float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.remaining = DURATION;
        this.delayRemaining = 0f;
        this.damageTimer = 1f;
        this.safeRadius = INITIAL_SAFE_RADIUS;
        this.pendingStart = false;
    }

    /**
     * Restaura uma tempestade que estava nos 4 segundos de espera.
     */
    public void restorePending(float delayRemaining, float targetX, float targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.remaining = 0f;
        this.delayRemaining = Math.max(0f, delayRemaining);
        this.damageTimer = 1f;
        this.safeRadius = INITIAL_SAFE_RADIUS;
        this.pendingStart = this.delayRemaining > 0.001f;
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

        // A tempestade nasce nas BORDAS do mapa e avança para dentro.
        // Não desenhamos uma bola/anel centrado no portal.
        float startThickness = 55f;
        float maxThickness = Math.max(worldWidth, worldHeight);
        float thickness = MathUtils.lerp(startThickness, maxThickness, progress);

        float alpha = MathUtils.lerp(0.24f, 0.62f, progress);
        renderer.setColor(new Color(0.38f, 0.02f, 0.55f, alpha));

        // Norte.
        renderer.rect(0f, worldHeight - thickness, worldWidth, thickness);

        // Sul.
        renderer.rect(0f, 0f, worldWidth, thickness);

        // Oeste.
        renderer.rect(0f, thickness, thickness, Math.max(0f, worldHeight - thickness * 2f));

        // Leste.
        renderer.rect(
                worldWidth - thickness,
                thickness,
                thickness,
                Math.max(0f, worldHeight - thickness * 2f)
        );

        // Bordas mais fortes para deixar claro de onde a tempestade está vindo.
        float edgeWidth = Math.min(18f + progress * 22f, thickness);
        renderer.setColor(new Color(0.90f, 0.22f, 1f, 0.88f));

        renderer.rect(0f, worldHeight - edgeWidth, worldWidth, edgeWidth);
        renderer.rect(0f, 0f, worldWidth, edgeWidth);
        renderer.rect(0f, edgeWidth, edgeWidth, Math.max(0f, worldHeight - edgeWidth * 2f));
        renderer.rect(
                worldWidth - edgeWidth,
                edgeWidth,
                edgeWidth,
                Math.max(0f, worldHeight - edgeWidth * 2f)
        );
    }
}
