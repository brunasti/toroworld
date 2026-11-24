package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.Genome;
import it.brunasti.simulations.toroworld.life.Trait;
import it.brunasti.simulations.toroworld.life.TraitRange;

/**
 * How a species moves and sees: part of the species description, so that different
 * kinds of animal (for example several kinds of carnivore) can follow different rules.
 * <p>
 * Speed ({@link Genome#speed()}) evolves between 1 and {@code maxSpeed}.
 * Vision ({@link Genome#vision()}) evolves between {@code minVision} and {@code maxVision}
 * and is a multiple of a base speed, chosen by {@code visionBase}:
 * <pre>
 *   sight distance (cells) = floor(vision x base)
 * </pre>
 *
 * @param maxSpeed   the highest speed the species can evolve, at least 1
 * @param minVision  lowest vision multiple
 * @param maxVision  highest vision multiple
 * @param visionBase which speed the vision multiple applies to
 */
public record Mobility(double maxSpeed, double minVision, double maxVision, VisionBase visionBase) {

    /** What the vision multiple is applied to. */
    public enum VisionBase {
        /** The organism's own (evolved) speed: faster individuals see further. */
        OWN_SPEED,
        /** The species' maximum speed: sight does not depend on the individual's speed. */
        MAX_SPEED
    }

    /** Speed 1 and vision 1 (sees only its neighbours), nothing evolves. The rule before speed existed. */
    public static final Mobility FIXED = new Mobility(1.0, 1.0, 1.0, VisionBase.OWN_SPEED);

    public Mobility {
        if (maxSpeed < 1) {
            throw new IllegalArgumentException("maxSpeed must be >= 1");
        }
        if (minVision < 1 || minVision > maxVision) {
            throw new IllegalArgumentException("vision range must satisfy 1 <= minVision <= maxVision");
        }
    }

    public TraitRange speedRange() {
        return new TraitRange(Trait.SPEED.minimum(), maxSpeed);
    }

    public TraitRange visionRange() {
        return new TraitRange(minVision, maxVision);
    }

    /** Sight distance in cells, at least 1. */
    public int sightDistance(Genome genome) {
        double base = visionBase == VisionBase.OWN_SPEED ? genome.speed() : maxSpeed;
        return Math.max(1, (int) Math.floor(genome.vision() * base));
    }

    public Mobility withMaxSpeed(double newMaxSpeed) {
        return new Mobility(newMaxSpeed, minVision, maxVision, visionBase);
    }

    public Mobility withVisionBase(VisionBase newBase) {
        return new Mobility(maxSpeed, minVision, maxVision, newBase);
    }
}
