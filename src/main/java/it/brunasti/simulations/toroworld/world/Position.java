package it.brunasti.simulations.toroworld.world;

/**
 * A cell coordinate on the torus. Instances are always already wrapped:
 * obtain them through {@link Torus#wrap(int, int)} rather than the constructor.
 */
public record Position(int x, int y) {

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
