package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Organism;

import java.util.random.RandomGenerator;

/**
 * Decides what an organism does in a tick. Deciding is kept separate from
 * executing (done by {@link Simulation}), so that decision rules can be replaced,
 * for example by genome-driven ones, without touching the energy bookkeeping.
 */
@FunctionalInterface
public interface Behaviour {

    Intent decide(Organism self, World world, ActivityCosts costs, RandomGenerator random);
}
