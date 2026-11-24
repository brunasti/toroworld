package it.brunasti.simulations.toroworld.life;

import java.util.EnumMap;
import java.util.Map;

/**
 * The trade-off: a species cannot improve all its traits at once.
 * <p>
 * The <em>advantage</em> of a genome is measured against the founder genome of its
 * species (the genome the species starts with). For each trait with a direction,
 * the advantage is the logarithm of the ratio to the founder value, with the sign
 * chosen so that a better value counts as positive:
 * <pre>
 *   advantage = ln(intake / intake0) + ln(maxAge / maxAge0) - ln(metabolism / metabolism0)
 * </pre>
 * Logarithms make the measure independent of each trait's scale: doubling the intake
 * is worth as much as halving the metabolism. Strategy traits (reproduction threshold,
 * offspring share) are not counted.
 * <p>
 * A genome is allowed when its advantage does not exceed the allowance. With an
 * allowance of 0, the founders sit exactly on the limit: any improvement of one
 * trait must be paid for by a worsening of another.
 */
public final class TraitBudget {

    private final Map<Species, Genome> founders;
    private final double allowance;

    public TraitBudget(Map<Species, Genome> founders, double allowance) {
        if (allowance < 0) {
            throw new IllegalArgumentException("allowance must be >= 0");
        }
        this.founders = new EnumMap<>(founders);
        this.allowance = allowance;
    }

    public double allowance() {
        return allowance;
    }

    /** Total advantage of a genome over the founder of its species (0 for the founder itself). */
    public double advantage(Species species, Genome genome) {
        Genome founder = founders.get(species);
        if (founder == null) {
            throw new IllegalArgumentException("No founder genome for " + species);
        }
        double total = 0;
        for (Trait trait : Trait.values()) {
            double reference = founder.get(trait);
            // A founder value of 0 gives no scale to compare with: the trait is left out.
            if (trait.direction() == Trait.Direction.STRATEGY || reference <= 0) {
                continue;
            }
            double logRatio = Math.log(genome.get(trait) / reference);
            total += trait.direction() == Trait.Direction.HIGHER_IS_BETTER ? logRatio : -logRatio;
        }
        return total;
    }

    public boolean allows(Species species, Genome genome) {
        // Tolerance for rounding, so that a founder genome is always allowed.
        return advantage(species, genome) <= allowance + 1e-12;
    }
}
