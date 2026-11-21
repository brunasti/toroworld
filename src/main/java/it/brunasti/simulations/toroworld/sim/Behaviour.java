package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.Organism;

import java.util.random.RandomGenerator;

/**
 * Decides what an organism does in a tick. Deciding is kept separate from
 * executing (done by {@link Simulation}), so that decision rules can be replaced,
 * for example by genome-driven ones, without touching the energy bookkeeping.
 */
@FunctionalInterface
public interface Behaviour {

    /**
     * @param self    the organism deciding
     * @param world   the current state of the world, to look at (not to change)
     * @param species the description of the organism's species: costs, mobility, ...
     * @param random  the simulation's random generator, the only source of randomness allowed
     */
    Intent decide(Organism self, World world, SpeciesConfig species, RandomGenerator random);
}
