package it.brunasti.simulations.toroworld.life;

/**
 * Heritable traits of an organism. Offspring receive a copy of the parent's
 * genome, possibly changed by a {@link Mutation}.
 *
 * @param metabolism            multiplier applied to every activity cost (1.0 = base cost)
 * @param intake                maximum amount taken in one feeding: resource for plants,
 *                              plant energy for herbivores, prey energy for carnivores
 * @param reproductionThreshold energy needed before the organism tries to reproduce
 * @param offspringShare        fraction of the parent's energy (after paying the
 *                              reproduction cost) handed to the offspring, in (0, 1)
 * @param maxAge                age in ticks after which the organism dies
 */
public record Genome(double metabolism, double intake, double reproductionThreshold,
                     double offspringShare, int maxAge) {

    public Genome {
        if (metabolism <= 0) {
            throw new IllegalArgumentException("metabolism must be > 0");
        }
        if (intake < 0) {
            throw new IllegalArgumentException("intake must be >= 0");
        }
        if (reproductionThreshold <= 0) {
            throw new IllegalArgumentException("reproductionThreshold must be > 0");
        }
        if (offspringShare <= 0 || offspringShare >= 1) {
            throw new IllegalArgumentException("offspringShare must be in (0, 1)");
        }
        if (maxAge < 1) {
            throw new IllegalArgumentException("maxAge must be >= 1");
        }
    }

    public double get(Trait trait) {
        return switch (trait) {
            case METABOLISM -> metabolism;
            case INTAKE -> intake;
            case REPRODUCTION_THRESHOLD -> reproductionThreshold;
            case OFFSPRING_SHARE -> offspringShare;
            case MAX_AGE -> maxAge;
        };
    }

    /** A copy with one trait changed. {@code MAX_AGE} is rounded to the nearest integer. */
    public Genome with(Trait trait, double value) {
        return switch (trait) {
            case METABOLISM -> new Genome(value, intake, reproductionThreshold, offspringShare, maxAge);
            case INTAKE -> new Genome(metabolism, value, reproductionThreshold, offspringShare, maxAge);
            case REPRODUCTION_THRESHOLD -> new Genome(metabolism, intake, value, offspringShare, maxAge);
            case OFFSPRING_SHARE -> new Genome(metabolism, intake, reproductionThreshold, value, maxAge);
            case MAX_AGE -> new Genome(metabolism, intake, reproductionThreshold, offspringShare,
                    (int) Math.round(value));
        };
    }
}
