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
    private static final float WATER_HUNGER_RESTORE = 25f;
    private static final float WATER_HEALTH_RESTORE = 5f;
    private static final float O2_RESTORE = 20f;
    public static final float DAMAGE_INVULNERABILITY_TIME = 1.2f;

    private float health = MAX_HEALTH;
    private float hunger = MAX_HUNGER;
    private float oxygen = MAX_OXYGEN;

    private float oxygenLossTimer;
    private float hungerLossTimer;
    private float oxygenDamageTimer;
    private float starvationDamageTimer;
    private float invulnerabilityTimer;
    private boolean survivalNeedsDisabled;

    private int iceCollected;
    private DeathCause deathCause = DeathCause.NONE;

    public void update(float delta) {
        updateInvulnerability(delta);

        if (isDead() || survivalNeedsDisabled) {
            return;
        }

        oxygenLossTimer += delta;
        hungerLossTimer += delta;

        while (oxygenLossTimer >= RESOURCE_INTERVAL) {
            oxygenLossTimer -= RESOURCE_INTERVAL;
            oxygen = MathUtils.clamp(oxygen - OXYGEN_LOSS, 0f, MAX_OXYGEN);
        }

        while (hungerLossTimer >= RESOURCE_INTERVAL) {
            hungerLossTimer -= RESOURCE_INTERVAL;
            hunger = MathUtils.clamp(hunger - HUNGER_LOSS, 0f, MAX_HUNGER);
        }

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
        oxygen = MathUtils.clamp(oxygen + amount, 0f, MAX_OXYGEN);
        if (oxygen > 0f) {
            oxygenDamageTimer = 0f;
        }
    }

    public void addHunger(float amount) {
        hunger = MathUtils.clamp(hunger + amount, 0f, MAX_HUNGER);
        if (hunger > 0f) {
            starvationDamageTimer = 0f;
        }
    }

    /** Restores the player's health and oxygen completely while at a base. */
    public void restoreAtBase() {
        health = MAX_HEALTH;
        oxygen = MAX_OXYGEN;
        oxygenDamageTimer = 0f;
    }

    public void eatFood() {
        hunger = MathUtils.clamp(hunger + FOOD_HUNGER_RESTORE, 0f, MAX_HUNGER);
        heal(FOOD_HEALTH_RESTORE);
        starvationDamageTimer = 0f;
    }

    public void collectIce() {
        iceCollected++;
    }

    public boolean canConsumeIce(int amount) {
        return amount > 0 && iceCollected >= amount;
    }

    public boolean consumeIce(int amount) {
        if (!canConsumeIce(amount)) {
            return false;
        }

        iceCollected -= amount;
        return true;
    }

    /** Keeps compatibility with the old one-ice interaction. */
    public boolean consumeIce() {
        return consumeIce(1);
    }

    /** Drinks the water produced by melting mission ice and fully restores health/O2. */
    public void drinkWater() {
        hunger = MathUtils.clamp(hunger + WATER_HUNGER_RESTORE, 0f, MAX_HUNGER);
        restoreAtBase();
        starvationDamageTimer = 0f;
    }

    public void heal(float amount) {
        health = MathUtils.clamp(health + amount, 0f, MAX_HEALTH);
    }

    /** Every lethal/non-lethal combat mechanic should call this method. */
    public void damage(float amount, DeathCause cause) {
        if (isDead() || amount <= 0f || isInvulnerable()) {
            return;
        }

        health = MathUtils.clamp(health - amount, 0f, MAX_HEALTH);
        invulnerabilityTimer = DAMAGE_INVULNERABILITY_TIME;

        if (health <= 0f) {
            deathCause = cause == null ? DeathCause.UNKNOWN : cause;
        }
    }

    /** Advances the post-hit invulnerability timer. Safe to call every frame. */
    public void updateInvulnerability(float delta) {
        invulnerabilityTimer = Math.max(0f, invulnerabilityTimer - Math.max(0f, delta));
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0f;
    }

    public float getInvulnerabilityTimer() {
        return invulnerabilityTimer;
    }

    public void setHealth(float value) {
        health = MathUtils.clamp(value, 0f, MAX_HEALTH);
        if (health > 0f) {
            deathCause = DeathCause.NONE;
        }
    }

    public void setHunger(float value) {
        hunger = MathUtils.clamp(value, 0f, MAX_HUNGER);
        if (hunger > 0f) {
            starvationDamageTimer = 0f;
        }
    }

    public void setOxygen(float value) {
        oxygen = MathUtils.clamp(value, 0f, MAX_OXYGEN);
        if (oxygen > 0f) {
            oxygenDamageTimer = 0f;
        }
    }

    /**
     * Disables hunger/O2 consumption and their related damage.
     * Used by the Saciedade blessing in Calisto.
     */
    public void setSurvivalNeedsDisabled(boolean disabled) {
        survivalNeedsDisabled = disabled;
        if (disabled) {
            hunger = MAX_HUNGER;
            oxygen = MAX_OXYGEN;
            hungerLossTimer = 0f;
            oxygenLossTimer = 0f;
            hungerDamageReset();
        }
    }

    public boolean areSurvivalNeedsDisabled() {
        return survivalNeedsDisabled;
    }

    private void hungerDamageReset() {
        oxygenDamageTimer = 0f;
        starvationDamageTimer = 0f;
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
