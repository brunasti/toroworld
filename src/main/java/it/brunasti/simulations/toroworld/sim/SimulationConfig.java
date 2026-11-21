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
        if (plants.initialCount() > cells) {
            throw new IllegalArgumentException("More initial plants than cells");
        }
        if (herbivores.initialCount() + carnivores.initialCount() > cells) {
            throw new IllegalArgumentException("More initial animals than cells");
        }
    }

    /**
     * Development defaults: a 10x10 grid. The numbers are a first guess, adjusted
     * only so that plants and herbivores usually coexist. They are not calibrated:
     * on a 10x10 grid the carnivores currently wipe out the herbivores and then starve.
     */
    public static SimulationConfig defaults() {
        return new SimulationConfig(
                10, 10, 42L,
                5.0, 10.0, 1.0,
                new SpeciesConfig(30, 5.0,
                        new Genome(1.0, 3.0, 8.0, 0.4, 60),
                        new ActivityCosts(0.5, 0.0, 0.0, 0.2, 1.0),
                        0.8),
                new SpeciesConfig(10, 10.0,
                        new Genome(1.0, 1.5, 25.0, 0.4, 80),
                        new ActivityCosts(0.4, 0.0, 0.3, 0.2, 2.0),
                        0.7),
                new SpeciesConfig(2, 20.0,
                        new Genome(1.0, 15.0, 30.0, 0.4, 120),
                        new ActivityCosts(0.6, 0.0, 0.6, 0.3, 3.0),
                        0.6),
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

    public SimulationConfig withPopulation(int plantCount, int herbivoreCount, int carnivoreCount) {
        return new SimulationConfig(width, height, seed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants.withInitialCount(plantCount),
                herbivores.withInitialCount(herbivoreCount),
                carnivores.withInitialCount(carnivoreCount), mutation);
    }

    public SimulationConfig withMutation(MutationSettings newMutation) {
        return new SimulationConfig(width, height, seed, resourceInitial, resourceMaximum,
                resourceRegeneration, plants, herbivores, carnivores, newMutation);
    }
}
