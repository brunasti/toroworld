package it.brunasti.simulations.toroworld.life;

/** An inclusive range of valid values for a trait. */
public record TraitRange(double minimum, double maximum) {

    public TraitRange {
        if (minimum > maximum) {
            throw new IllegalArgumentException("minimum " + minimum + " > maximum " + maximum);
        }
    }

    public static TraitRange fixed(double value) {
        return new TraitRange(value, value);
    }

    public boolean contains(double value) {
        return value >= minimum && value <= maximum;
    }

    public double clamp(double value) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
