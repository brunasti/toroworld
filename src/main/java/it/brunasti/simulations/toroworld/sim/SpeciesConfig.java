package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Genome;

/**
 * Starting parameters of one species.
 *
 * @param initialDensity fraction of the cells that get one of these organisms at the start,
 *                       in [0, 1] (0.10 = 10 organisms per 100 cells)
 * @param initialEnergy energy of each starting organism
 * @param genome        genome of the starting organisms
 * @param costs         base energy cost of each activity
 * @param efficiency    fraction of the food taken in that becomes energy, in (0, 1]
 */
public record SpeciesConfig(double initialDensity, double initialEnergy, Genome genome,
                            ActivityCosts costs, double efficiency) {

    public SpeciesConfig {
        if (initialDensity < 0 || initialDensity > 1) {
            throw new IllegalArgumentException("initialDensity must be in [0, 1]");
        }
        if (initialEnergy <= 0) {
            throw new IllegalArgumentException("initialEnergy must be > 0");
        }
        if (efficiency <= 0 || efficiency > 1) {
            throw new IllegalArgumentException("efficiency must be in (0, 1]");
        }
    }

    /** Number of organisms to place at the start on a grid with the given number of cells (rounded). */
    public int initialCount(int cells) {
        return (int) Math.round(initialDensity * cells);
    }

    public SpeciesConfig withInitialDensity(double density) {
        return new SpeciesConfig(density, initialEnergy, genome, costs, efficiency);
    }
}
