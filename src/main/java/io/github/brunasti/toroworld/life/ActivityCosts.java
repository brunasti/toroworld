package io.github.brunasti.toroworld.life;

/**
 * Base energy cost of each activity. The cost actually paid by an organism
 * is the base cost multiplied by its {@link Genome#metabolism()}.
 */
public record ActivityCosts(double live, double rest, double move, double eat, double reproduce) {

    public ActivityCosts {
        if (live < 0 || rest < 0 || move < 0 || eat < 0 || reproduce < 0) {
            throw new IllegalArgumentException("Activity costs cannot be negative");
        }
    }

    public double of(Activity activity) {
        return switch (activity) {
            case LIVE -> live;
            case REST -> rest;
            case MOVE -> move;
            case EAT -> eat;
            case REPRODUCE -> reproduce;
        };
    }
}
