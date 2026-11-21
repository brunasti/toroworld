package it.brunasti.simulations.toroworld.life;

import java.util.random.RandomGenerator;

/**
 * Produces the genome of an offspring from the genome of its parent.
 * Implementations must draw all randomness from the given generator,
 * so that a run stays reproducible from its seed.
 */
@FunctionalInterface
public interface Mutation {

    Genome mutate(Species species, Genome parent, RandomGenerator random);

    /** No mutation: the offspring genome equals the parent genome. */
    Mutation NONE = (species, parent, random) -> parent;
}
