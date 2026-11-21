package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Genome;
import it.brunasti.simulations.toroworld.life.MutationSettings;

/**
 * Everything needed to start a run. The same configuration with the same
 * seed always produces the same run.
 */
public record SimulationConfig(
        int width,
        int height,
        long seed,
        double resourceInitial,
        double resourceMaximum,
        double resourceRegeneration,
        SpeciesConfig plants,
        SpeciesConfig herbivores,
        SpeciesConfig carnivores,
        MutationSettings mutation) {

    public SimulationConfig {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Grid size must be at least 1x1");
        }
        int cells = width * height;
        if (plants.initialCount(cells) > cells) {
            throw new IllegalArgumentException("More initial plants than cells");
        }
        if (herbivores.initialCount(cells) + carnivores.initialCount(cells) > cells) {
            throw new IllegalArgumentException("More initial animals than cells");
        }
    }

    /**
     * Development defaults: a 100x50 grid. Starting populations are densities, so they
     * follow the grid size: 50% of the cells get a plant, 20% a herbivore, 4% a carnivore.
     * The species numbers are a first guess, adjusted only on a 10x10 grid so that plants
     * and herbivores usually coexist there. They are not calibrated.
     * Carnivores start at speed 1 and vision 1.5, and can evolve speed up to 3 and vision up to 3
     * (sight = vision x own speed). Plants and herbivores have speed 1 and see only their neighbours.
     */
    public static SimulationConfig defaults() {
        return new SimulationConfig(
                100, 50, 42L,
                5.0, 10.0, 1.0,
                new SpeciesConfig(0.50, 5.0,
                        new Genome(1.0, 3.0, 8.0, 0.4, 60),
                        new ActivityCosts(0.5, 0.0, 0.0, 0.2, 1.0),
                        0.8),
                new SpeciesConfig(0.20, 10.0,
                        new Genome(1.0, 1.5, 25.0, 0.4, 80),
                        new ActivityCosts(0.4, 0.0, 0.3, 0.2, 2.0),
                        0.7),
                new SpeciesConfig(0.04, 20.0,
                        new Genome(1.0, 15.0, 30.0, 0.4, 120, 1.0, 1.5),
                        new ActivityCosts(0.6, 0.0, 0.6, 0.3, 3.0),
                        0.6,
                        new Mobility(3.0, 1.5, 3.0, Mobility.VisionBase.OWN_SPEED)),
                MutationSettings.DEFAULT);
    }

    public SimulationConfig withSize(int newWidth, int newHeight) {
        return new SimulationConfig(newWidth, newHeight, seed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants, herbivores, carnivores, mutation);
    }

    public SimulationConfig withSeed(long newSeed) {
        return new SimulationConfig(width, height, newSeed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants, herbivores, carnivores, mutation);
    }

    /** Starting densities as fractions of the cells, in [0, 1] (0.10 = 10 per 100 cells). */
    public SimulationConfig withDensities(double plantDensity, double herbivoreDensity,
                                          double carnivoreDensity) {
        return new SimulationConfig(width, height, seed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants.withInitialDensity(plantDensity),
                herbivores.withInitialDensity(herbivoreDensity),
                carnivores.withInitialDensity(carnivoreDensity), mutation);
    }

    public SimulationConfig withCarnivores(SpeciesConfig newCarnivores) {
        return new SimulationConfig(width, height, seed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants, herbivores, newCarnivores, mutation);
    }

    public SimulationConfig withMutation(MutationSettings newMutation) {
        return new SimulationConfig(width, height, seed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants, herbivores, carnivores, newMutation);
    }
}
