package it.brunasti.simulations.toroworld.life;

import java.util.Map;

/**
 * How offspring genomes are derived.
 *
 * @param enabled       false: offspring are exact copies of the parent
 * @param probability   chance that each trait changes at birth, in [0, 1]
 * @param relativeSigma standard deviation of the relative change (0.05 = about 5%)
 * @param tradeOff      true: genomes must stay within the {@link TraitBudget}
 * @param allowance     how much total advantage over the founder is allowed (0 = none)
 */
public record MutationSettings(boolean enabled, double probability, double relativeSigma,
                               boolean tradeOff, double allowance) {

    /** Small random changes, with the trade-off on. */
    public static final MutationSettings DEFAULT = new MutationSettings(true, 0.1, 0.05, true, 0.0);

    /** No mutation at all. */
    public static final MutationSettings OFF = new MutationSettings(false, 0, 0, false, 0);

    public MutationSettings {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("probability must be in [0, 1]");
        }
        if (relativeSigma < 0 || allowance < 0) {
            throw new IllegalArgumentException("relativeSigma and allowance must be >= 0");
        }
    }

    public MutationSettings withEnabled(boolean value) {
        return new MutationSettings(value, probability, relativeSigma, tradeOff, allowance);
    }

    public MutationSettings withTradeOff(boolean value) {
        return new MutationSettings(enabled, probability, relativeSigma, value, allowance);
    }

    /** Builds the mutation, using the starting genome of each species as its founder. */
    public Mutation create(Map<Species, Genome> founders) {
        if (!enabled) {
            return Mutation.NONE;
        }
        TraitBudget budget = tradeOff ? new TraitBudget(founders, allowance) : null;
        return new GaussianMutation(probability, relativeSigma, budget);
    }
}
