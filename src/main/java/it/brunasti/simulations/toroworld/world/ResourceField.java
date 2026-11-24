package it.brunasti.simulations.toroworld.world;

import java.util.Arrays;

/**
 * The renewable resource (nutrients and light) available in each cell.
 * Plants take resource from their cell; every tick each cell regenerates
 * by a fixed amount up to a maximum.
 */
public final class ResourceField {

    private final Torus torus;
    private final double[] level;
    private final double maximum;
    private final double regeneration;

    public ResourceField(Torus torus, double initial, double maximum, double regeneration) {
        if (maximum < 0 || regeneration < 0 || initial < 0 || initial > maximum) {
            throw new IllegalArgumentException("Invalid resource parameters");
        }
        this.torus = torus;
        this.maximum = maximum;
        this.regeneration = regeneration;
        this.level = new double[torus.cellCount()];
        Arrays.fill(level, initial);
    }

    public double level(Position p) {
        return level[torus.index(p)];
    }

    public double maximum() {
        return maximum;
    }

    /** Removes up to {@code amount} from the cell and returns what was actually taken. */
    public double take(Position p, double amount) {
        int i = torus.index(p);
        double taken = Math.min(Math.max(amount, 0), level[i]);
        level[i] -= taken;
        return taken;
    }

    public void regenerate() {
        for (int i = 0; i < level.length; i++) {
            level[i] = Math.min(maximum, level[i] + regeneration);
        }
    }

    public double total() {
        double sum = 0;
        for (double v : level) {
            sum += v;
        }
        return sum;
    }
}
