package io.github.some_example_name.entities;

/**
 * Every gameplay system that can kill the player should use one of these causes.
 * Add new causes here whenever a new lethal mechanic is introduced.
 */
public enum DeathCause {
    NONE(""),
    OXYGEN_DEPLETION("Você morreu por falta de oxigênio."),
    STARVATION("Você morreu de fome."),
    UNKNOWN("Você morreu.");

    private final String message;

    DeathCause(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
