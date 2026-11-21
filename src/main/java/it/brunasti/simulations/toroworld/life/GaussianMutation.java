package it.brunasti.simulations.toroworld.life;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Small random changes. Each trait, independently, changes with the given
 * probability, by a relative amount drawn from a normal distribution:
 * <pre>
 *   newValue = clamp(value * (1 + N(0, relativeSigma)))
 * </pre>
 * The range used for clamping is the species' own range when one is given
 * (e.g. speed between 1 and the species' maximum speed), otherwise the trait's default range.
 * When a {@link TraitBudget} is given (trade-off on) and the changed genome would
 * exceed it, the mutation is rejected and the offspring gets the parent genome.
 */
public final class GaussianMutation implements Mutation {

    private final double probability;
    private final double relativeSigma;
    private final TraitBudget budget;
    private final Map<Species, Map<Trait, TraitRange>> ranges;

    public GaussianMutation(double probability, double relativeSigma, TraitBudget budget) {
        this(probability, relativeSigma, budget, Map.of());
    }

    public GaussianMutation(double probability, double relativeSigma, TraitBudget budget,
                            Map<Species, Map<Trait, TraitRange>> ranges) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("probability must be in [0, 1]");
        }
        if (relativeSigma < 0) {
            throw new IllegalArgumentException("relativeSigma must be >= 0");
        }
        this.probability = probability;
        this.relativeSigma = relativeSigma;
        this.budget = budget;
        Map<Species, Map<Trait, TraitRange>> copy = new EnumMap<>(Species.class);
        ranges.forEach((species, map) -> copy.put(species, Map.copyOf(map)));
        this.ranges = copy;
    }

    /** The range a trait is clamped to for a species. */
    public TraitRange range(Species species, Trait trait) {
        TraitRange specific = ranges.getOrDefault(species, Map.of()).get(trait);
        return specific != null ? specific : trait.range();
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
                child = child.with(trait, range(species, trait).clamp(child.get(trait) * (1 + noise)));
                changed = true;
            }
        }
        if (changed && budget != null && !budget.allows(species, child)) {
            return parent;
        }
        return child;
    }
}
