package io.github.some_example_name.entities;

import com.badlogic.gdx.math.MathUtils;

/**
 * Player survival attributes and their timed effects.
 */
public class PlayerStats {

    public static final float MAX_HEALTH = 100f;
    public static final float MAX_HUNGER = 100f;
    public static final float MAX_OXYGEN = 100f;

    private static final float RESOURCE_INTERVAL = 5f;
    private static final float OXYGEN_LOSS = 15f;
    private static final float HUNGER_LOSS = 5f;

    private static final float OXYGEN_DAMAGE_INTERVAL = 2f;
    private static final float OXYGEN_DAMAGE = 10f;

    private static final float STARVATION_DAMAGE_INTERVAL = 5f;
    private static final float STARVATION_DAMAGE = 5f;

    private static final float FOOD_HUNGER_RESTORE = 50f;
    private static final float FOOD_HEALTH_RESTORE = 10f;
    private static final float O2_RESTORE = 20f;

    private float health = MAX_HEALTH;
    private float hunger = MAX_HUNGER;
    private float oxygen = MAX_OXYGEN;

    private float oxygenLossTimer;
    private float hungerLossTimer;
    private float oxygenDamageTimer;
    private float starvationDamageTimer;

    private int iceCollected;
    private DeathCause deathCause = DeathCause.NONE;

    public void update(float delta) {
        if (isDead()) {
            return;
        }

        oxygenLossTimer += delta;
        hungerLossTimer += delta;

        while (oxygenLossTimer >= RESOURCE_INTERVAL) {
            oxygenLossTimer -= RESOURCE_INTERVAL;
            oxygen = MathUtils.clamp(
                    oxygen - OXYGEN_LOSS,
                    0f,
                    MAX_OXYGEN
            );
        }

        while (hungerLossTimer >= RESOURCE_INTERVAL) {
            hungerLossTimer -= RESOURCE_INTERVAL;
            hunger = MathUtils.clamp(
                    hunger - HUNGER_LOSS,
                    0f,
                    MAX_HUNGER
            );
        }

        // O2 zerado: depois de 2 segundos, perde 10 HP a cada 2 segundos.
        if (oxygen <= 0f) {
            oxygenDamageTimer += delta;
            while (oxygenDamageTimer >= OXYGEN_DAMAGE_INTERVAL && !isDead()) {
                oxygenDamageTimer -= OXYGEN_DAMAGE_INTERVAL;
                damage(OXYGEN_DAMAGE, DeathCause.OXYGEN_DEPLETION);
            }
        } else {
            oxygenDamageTimer = 0f;
        }

        if (isDead()) {
            return;
        }

        // Fome zerada: depois de 5 segundos, perde 5 HP a cada 5 segundos.
        if (hunger <= 0f) {
            starvationDamageTimer += delta;
            while (starvationDamageTimer >= STARVATION_DAMAGE_INTERVAL && !isDead()) {
                starvationDamageTimer -= STARVATION_DAMAGE_INTERVAL;
                damage(STARVATION_DAMAGE, DeathCause.STARVATION);
            }
        } else {
            starvationDamageTimer = 0f;
        }
    }

    public void addOxygen(float amount) {
        oxygen = MathUtils.clamp(
                oxygen + amount,
                0f,
                MAX_OXYGEN
        );

        if (oxygen > 0f) {
            oxygenDamageTimer = 0f;
        }
    }

    public void eatFood() {
        hunger = MathUtils.clamp(
                hunger + FOOD_HUNGER_RESTORE,
                0f,
                MAX_HUNGER
        );

        heal(FOOD_HEALTH_RESTORE);
        starvationDamageTimer = 0f;
    }

    public void collectIce() {
        iceCollected++;
    }

    public void heal(float amount) {
        health = MathUtils.clamp(
                health + amount,
                0f,
                MAX_HEALTH
        );
    }

    /**
     * Use this method for every future mechanic that deals lethal or non-lethal damage.
     * If the damage kills the player, the supplied cause is stored for Game Over.
     */
    public void damage(float amount, DeathCause cause) {
        if (isDead() || amount <= 0f) {
            return;
        }

        health = MathUtils.clamp(
                health - amount,
                0f,
                MAX_HEALTH
        );

        if (health <= 0f) {
            deathCause = cause == null ? DeathCause.UNKNOWN : cause;
        }
    }

    public float getHealth() {
        return health;
    }

    public float getHunger() {
        return hunger;
    }

    public float getOxygen() {
        return oxygen;
    }

    public int getIceCollected() {
        return iceCollected;
    }

    public DeathCause getDeathCause() {
        return deathCause;
    }

    public boolean isDead() {
        return health <= 0f;
    }
}
