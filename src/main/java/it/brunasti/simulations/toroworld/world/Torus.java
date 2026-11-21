package it.brunasti.simulations.toroworld.world;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Geometry of a width x height grid whose opposite edges are joined,
 * so that moving off one edge brings you back on the opposite one.
 * Neighbourhood is the Moore neighbourhood (8 surrounding cells).
 */
public final class Torus {

    private final int width;
    private final int height;

    public Torus(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Torus size must be at least 1x1, got " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int cellCount() {
        return width * height;
    }

    /** Wraps any integer coordinate onto the torus. */
    public Position wrap(int x, int y) {
        return new Position(Math.floorMod(x, width), Math.floorMod(y, height));
    }

    /** Index of a position in row-major order, in [0, cellCount). */
    public int index(Position p) {
        return p.y() * width + p.x();
    }

    public Position position(int index) {
        return new Position(index % width, index / width);
    }

    /**
     * The distinct cells around {@code p}, excluding {@code p} itself.
     * On very small grids (width or height below 3) the wrapped neighbours
     * overlap, so fewer than 8 cells are returned.
     */
    public List<Position> neighbours(Position p) {
        Set<Position> result = new LinkedHashSet<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                Position n = wrap(p.x() + dx, p.y() + dy);
                if (!n.equals(p)) {
                    result.add(n);
                }
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * The distinct cells at distance 1 to {@code radius} from {@code p} (a square around it,
     * wrapping across the edges), excluding {@code p} itself. They are listed row by row, not by
     * distance: callers that need the nearest cell must compare distances.
     */
    public List<Position> cellsWithin(Position p, int radius) {
        if (radius < 1) {
            return List.of();
        }
        Set<Position> result = new LinkedHashSet<>();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                Position n = wrap(p.x() + dx, p.y() + dy);
                if (!n.equals(p)) {
                    result.add(n);
                }
            }
        }
        return new ArrayList<>(result);
    }

    /** Shortest signed offset from a to b along one axis of the given size. */
    static int shortestDelta(int a, int b, int size) {
        int d = Math.floorMod(b - a, size);
        return d > size / 2 ? d - size : d;
    }

    /** Chebyshev distance (number of king moves) taking wrap-around into account. */
    public int distance(Position a, Position b) {
        return Math.max(
                Math.abs(shortestDelta(a.x(), b.x(), width)),
                Math.abs(shortestDelta(a.y(), b.y(), height)));
    }
}
