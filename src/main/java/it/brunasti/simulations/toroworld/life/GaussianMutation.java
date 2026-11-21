package it.brunasti.simulations.toroworld.life;

import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Small random changes. Each trait, independently, changes with the given
 * probability, by a relative amount drawn from a normal distribution:
 * <pre>
 *   newValue = clamp(value * (1 + N(0, relativeSigma)))
 * </pre>
 * When a {@link TraitBudget} is given (trade-off on) and the changed genome would
 * exceed it, the mutation is rejected and the offspring gets the parent genome.
 */
public final class GaussianMutation implements Mutation {

    private final double probability;
    private final double relativeSigma;
    private final TraitBudget budget;

    public GaussianMutation(double probability, double relativeSigma, TraitBudget budget) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("probability must be in [0, 1]");
        }
        if (relativeSigma < 0) {
            throw new IllegalArgumentException("relativeSigma must be >= 0");
        }
        this.probability = probability;
        this.relativeSigma = relativeSigma;
        this.budget = budget;
    }

    public Optional<TraitBudget> budget() {
        return Optional.ofNullable(budget);
    }

    @Override
    public Genome mutate(Species species, Genome parent, RandomGenerator random) {
        Genome child = parent;
        boolean changed = false;
        for (Trait trait : Trait.values()) {
            // Always draw both numbers, so that the random sequence does not depend on the outcome.
            boolean mutates = random.nextDouble() < probability;
            double noise = random.nextGaussian() * relativeSigma;
            if (mutates) {
                child = child.with(trait, trait.clamp(child.get(trait) * (1 + noise)));
                changed = true;
            }
        }
        if (changed && budget != null && !budget.allows(species, child)) {
            return parent;
        }
        return child;
    }
}
