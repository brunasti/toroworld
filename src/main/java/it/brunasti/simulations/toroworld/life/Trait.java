package it.brunasti.simulations.toroworld.life;

/**
 * The heritable traits of a {@link Genome}, with their valid range and, for the
 * traits that take part in the trade-off budget, which direction is an advantage.
 */
public enum Trait {

    /** Cost multiplier. Lower is an advantage. */
    METABOLISM(0.05, 20.0, Direction.LOWER_IS_BETTER),
    /** Maximum food per feeding. Higher is an advantage. */
    INTAKE(0.0, 1000.0, Direction.HIGHER_IS_BETTER),
    /** Energy needed to reproduce. A strategy, not an advantage: outside the budget. */
    REPRODUCTION_THRESHOLD(0.1, 10_000.0, Direction.STRATEGY),
    /** Share of energy given to the offspring. A strategy, not an advantage: outside the budget. */
    OFFSPRING_SHARE(0.05, 0.95, Direction.STRATEGY),
    /** Maximum age in ticks. Higher is an advantage. */
    MAX_AGE(1.0, 100_000.0, Direction.HIGHER_IS_BETTER),
    /**
     * Cells an animal can travel in one tick. Higher is an advantage. The upper limit
     * actually used is set per species ({@code Mobility.maxSpeed}).
     */
    SPEED(1.0, 100.0, Direction.HIGHER_IS_BETTER),
    /**
     * Sight distance as a multiple of a base speed (see {@code Mobility.visionBase}).
     * Higher is an advantage. The range actually used is set per species.
     */
    VISION(1.0, 100.0, Direction.HIGHER_IS_BETTER);

    /** How a change of the trait counts against the trade-off budget. */
    public enum Direction {
        HIGHER_IS_BETTER,
        LOWER_IS_BETTER,
        /** Neither higher nor lower is better in itself; not part of the budget. */
        STRATEGY
    }

    private final double minimum;
    private final double maximum;
    private final Direction direction;

    Trait(double minimum, double maximum, Direction direction) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.direction = direction;
    }

    public double minimum() {
        return minimum;
    }

    public double maximum() {
        return maximum;
    }

    public Direction direction() {
        return direction;
    }

    public double clamp(double value) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /** The default range of this trait, used when a species sets no narrower one. */
    public TraitRange range() {
        return new TraitRange(minimum, maximum);
    }
}
