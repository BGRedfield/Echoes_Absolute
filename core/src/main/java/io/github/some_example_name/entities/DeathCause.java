package io.github.some_example_name.entities;

/** Every gameplay system that can kill the player uses one of these causes. */
public enum DeathCause {
    NONE(""),
    OXYGEN_DEPLETION("Você morreu por falta de oxigênio."),
    STARVATION("Você morreu de fome."),
    AMERICAN_BULLET("Você morreu atingido pelas rajadas do inimigo lunar."),
    MARTIAN_MELEE("Você morreu atacado corpo a corpo por um marciano."),
    RIFLE_BULLET("Você morreu sob o fogo da barreira de rifles."),
    TRUMP_MISSILE("Você morreu na área de impacto de um míssil."),
    UNKNOWN("Você morreu.");

    private final String message;

    DeathCause(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
