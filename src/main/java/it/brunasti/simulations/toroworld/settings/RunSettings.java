package it.brunasti.simulations.toroworld.settings;

import it.brunasti.simulations.toroworld.sim.SimulationConfig;

/**
 * Everything needed to start a run: the simulation configuration and the length of the run.
 *
 * @param config the world, the species and the mutation settings
 * @param ticks  length of the run, in ticks, at least 1
 */
public record RunSettings(SimulationConfig config, int ticks) {

    /** Length of a run when nothing else is set. */
    public static final int DEFAULT_TICKS = 1000;

    public RunSettings {
        if (ticks < 1) {
            throw new IllegalArgumentException("ticks must be at least 1");
        }
    }

    public static RunSettings defaults() {
        return new RunSettings(SimulationConfig.defaults(), DEFAULT_TICKS);
    }

    public RunSettings withConfig(SimulationConfig newConfig) {
        return new RunSettings(newConfig, ticks);
    }

    public RunSettings withTicks(int newTicks) {
        return new RunSettings(config, newTicks);
    }
}
