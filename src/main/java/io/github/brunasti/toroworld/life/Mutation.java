package io.github.brunasti.toroworld.life;

import java.util.random.RandomGenerator;

/**
 * Produces the genome of an offspring from the genome of its parent.
 * <p>
 * How mutation works is still an open design decision (see docs/decisions.md),
 * so for now only {@link #NONE} exists: offspring are exact copies.
 * Implementations must draw all randomness from the given generator,
 * so that a run stays reproducible from its seed.
 */
@FunctionalInterface
public interface Mutation {

    Genome mutate(Genome parent, RandomGenerator random);

    /** No mutation: the offspring genome equals the parent genome. */
    Mutation NONE = (parent, random) -> parent;
}
