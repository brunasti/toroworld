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
 * @param speed                 cells an animal can travel in one tick, at least 1; a fractional
 *                              part gives a matching chance of one extra cell (1.3 = one cell,
 *                              plus a second one 30% of the time)
 * @param vision                sight distance as a multiple of a base speed, at least 1;
 *                              see {@code Mobility}
 */
public record Genome(double metabolism, double intake, double reproductionThreshold,
                     double offspringShare, int maxAge, double speed, double vision) {

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
        if (speed < 1) {
            throw new IllegalArgumentException("speed must be >= 1");
        }
        if (vision < 1) {
            throw new IllegalArgumentException("vision must be >= 1");
        }
    }

    /** A genome with speed 1 and vision 1: an organism that moves one cell and sees its neighbours. */
    public Genome(double metabolism, double intake, double reproductionThreshold,
                  double offspringShare, int maxAge) {
        this(metabolism, intake, reproductionThreshold, offspringShare, maxAge, 1.0, 1.0);
    }

    public double get(Trait trait) {
        return switch (trait) {
            case METABOLISM -> metabolism;
            case INTAKE -> intake;
            case REPRODUCTION_THRESHOLD -> reproductionThreshold;
            case OFFSPRING_SHARE -> offspringShare;
            case MAX_AGE -> maxAge;
            case SPEED -> speed;
            case VISION -> vision;
        };
    }

    /** A copy with one trait changed. {@code MAX_AGE} is rounded to the nearest integer. */
    public Genome with(Trait trait, double value) {
        return switch (trait) {
            case METABOLISM -> new Genome(value, intake, reproductionThreshold, offspringShare, maxAge,
                    speed, vision);
            case INTAKE -> new Genome(metabolism, value, reproductionThreshold, offspringShare, maxAge,
                    speed, vision);
            case REPRODUCTION_THRESHOLD -> new Genome(metabolism, intake, value, offspringShare, maxAge,
                    speed, vision);
            case OFFSPRING_SHARE -> new Genome(metabolism, intake, reproductionThreshold, value, maxAge,
                    speed, vision);
            case MAX_AGE -> new Genome(metabolism, intake, reproductionThreshold, offspringShare,
                    (int) Math.round(value), speed, vision);
            case SPEED -> new Genome(metabolism, intake, reproductionThreshold, offspringShare, maxAge,
                    value, vision);
            case VISION -> new Genome(metabolism, intake, reproductionThreshold, offspringShare, maxAge,
                    speed, value);
        };
    }
}
