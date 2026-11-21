package io.github.brunasti.toroworld.sim;

import io.github.brunasti.toroworld.life.ActivityCosts;
import io.github.brunasti.toroworld.life.Genome;

/**
 * Starting parameters of one species.
 *
 * @param initialCount  number of organisms placed at the start
 * @param initialEnergy energy of each starting organism
 * @param genome        genome of the starting organisms
 * @param costs         base energy cost of each activity
 * @param efficiency    fraction of the food taken in that becomes energy, in (0, 1]
 */
public record SpeciesConfig(int initialCount, double initialEnergy, Genome genome,
                            ActivityCosts costs, double efficiency) {

    public SpeciesConfig {
        if (initialCount < 0) {
            throw new IllegalArgumentException("initialCount must be >= 0");
        }
        if (initialEnergy <= 0) {
            throw new IllegalArgumentException("initialEnergy must be > 0");
        }
        if (efficiency <= 0 || efficiency > 1) {
            throw new IllegalArgumentException("efficiency must be in (0, 1]");
        }
    }

    public SpeciesConfig withInitialCount(int count) {
        return new SpeciesConfig(count, initialEnergy, genome, costs, efficiency);
    }
}
