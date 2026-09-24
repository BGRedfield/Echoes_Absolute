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

            renderer.triangle(x0Outer, y0Outer, x1Outer, y1Outer, x1Inner, y1Inner);
            renderer.triangle(x0Outer, y0Outer, x1Inner, y1Inner, x0Inner, y0Inner);
        }

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
    }

    /**
     * Mesma tempestade visual da Lua, mas em Marte ela começa pelos
     * quatro cantos e vai ocupando as bordas antes de formar o anel.
     */
    public void drawWorldFromCorners(
            ShapeRenderer renderer,
            float worldWidth,
            float worldHeight,
            float cameraX,
            float cameraY,
            float visibleWidth,
            float visibleHeight
    ) {
        if (!isActive()) {
            return;
        }

        float progress = getProgress();
        float maxRadius = (float) Math.sqrt(
                worldWidth * worldWidth + worldHeight * worldHeight
        ) * 1.15f;
        float innerRadius = MathUtils.lerp(
                INITIAL_SAFE_RADIUS,
                FINAL_SAFE_RADIUS,
                progress
        );

        final int segments = 96;

        float halfVisibleWidth = visibleWidth / 2f;
        float halfVisibleHeight = visibleHeight / 2f;

        float left = Math.max(0f, cameraX - halfVisibleWidth);
        float right = Math.min(worldWidth, cameraX + halfVisibleWidth);
        float bottom = Math.max(0f, cameraY - halfVisibleHeight);
        float top = Math.min(worldHeight, cameraY + halfVisibleHeight);

        // A tempestade entra primeiro pelos quatro cantos da TELA.
        float maxCornerReach = Math.min(visibleWidth, visibleHeight) * 0.70f;
        float cornerReach = MathUtils.lerp(75f, maxCornerReach, progress);

        renderer.setColor(new Color(0.38f, 0.02f, 0.55f, 0.38f));

        renderer.triangle(left, top, left + cornerReach, top, left, top - cornerReach);
        renderer.triangle(right, top, right - cornerReach, top, right, top - cornerReach);
        renderer.triangle(left, bottom, left + cornerReach, bottom, left, bottom + cornerReach);
        renderer.triangle(right, bottom, right - cornerReach, bottom, right, bottom + cornerReach);

        // Depois, ela ganha exatamente o mesmo anel da tempestade da Lua,
        // fechando em direção ao ponto seguro do portal.
        float ringAlpha = 0.23f * progress;
        renderer.setColor(new Color(0.38f, 0.02f, 0.55f, ringAlpha));

        float outerRadius = maxRadius;
        for (int i = 0; i < segments; i++) {
            float a0 = MathUtils.PI2 * i / segments;
            float a1 = MathUtils.PI2 * (i + 1f) / segments;

            float x0Outer = targetX + MathUtils.cos(a0) * outerRadius;
            float y0Outer = targetY + MathUtils.sin(a0) * outerRadius;
            float x1Outer = targetX + MathUtils.cos(a1) * outerRadius;
            float y1Outer = targetY + MathUtils.sin(a1) * outerRadius;

            float x0Inner = targetX + MathUtils.cos(a0) * innerRadius;
            float y0Inner = targetY + MathUtils.sin(a0) * innerRadius;
            float x1Inner = targetX + MathUtils.cos(a1) * innerRadius;
            float y1Inner = targetY + MathUtils.sin(a1) * innerRadius;

            renderer.triangle(x0Outer, y0Outer, x1Outer, y1Outer, x1Inner, y1Inner);
            renderer.triangle(x0Outer, y0Outer, x1Inner, y1Inner, x0Inner, y0Inner);
        }

        renderer.setColor(new Color(0.90f, 0.22f, 1f, 0.90f));
        for (int i = 0; i < segments; i++) {
            float a0 = MathUtils.PI2 * i / segments;
            float a1 = MathUtils.PI2 * (i + 1f) / segments;

            float x0 = targetX + MathUtils.cos(a0) * innerRadius;
            float y0 = targetY + MathUtils.sin(a0) * innerRadius;
            float x1 = targetX + MathUtils.cos(a1) * innerRadius;
            float y1 = targetY + MathUtils.sin(a1) * innerRadius;

            renderer.rectLine(x0, y0, x1, y1, 8f);
        }
    }
}
