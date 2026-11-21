package it.brunasti.simulations.toroworld.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TorusTest {

    private final Torus torus = new Torus(10, 10);

    @Test
    void wrapsCoordinatesInBothDirections() {
        assertEquals(new Position(9, 9), torus.wrap(-1, -1));
        assertEquals(new Position(0, 0), torus.wrap(10, 10));
        assertEquals(new Position(3, 7), torus.wrap(23, -13));
    }

    @Test
    void cornerHasEightNeighboursAcrossTheEdges() {
        var neighbours = torus.neighbours(new Position(0, 0));
        assertEquals(8, neighbours.size());
        assertTrue(neighbours.contains(new Position(9, 9)));
        assertTrue(neighbours.contains(new Position(0, 9)));
        assertTrue(neighbours.contains(new Position(9, 0)));
        assertTrue(neighbours.contains(new Position(1, 1)));
    }

    @Test
    void smallGridsHaveFewerDistinctNeighbours() {
        assertEquals(0, new Torus(1, 1).neighbours(new Position(0, 0)).size());
        assertEquals(3, new Torus(2, 2).neighbours(new Position(0, 0)).size());
        assertEquals(2, new Torus(3, 1).neighbours(new Position(0, 0)).size());
    }

    @Test
    void distanceTakesTheShortWayRound() {
        assertEquals(1, torus.distance(new Position(0, 0), new Position(9, 9)));
        assertEquals(5, torus.distance(new Position(0, 0), new Position(5, 0)));
        assertEquals(4, torus.distance(new Position(1, 0), new Position(7, 4)));
    }

    @Test
    void indexAndPositionAreInverse() {
        for (int i = 0; i < torus.cellCount(); i++) {
            assertEquals(i, torus.index(torus.position(i)));
        }
    }

    @Test
    void cellsWithinARadiusFormASquareWithoutTheCentre() {
        assertEquals(24, torus.cellsWithin(new Position(0, 0), 2).size());
        assertTrue(torus.cellsWithin(new Position(0, 0), 2).contains(new Position(8, 8)));
        assertEquals(24, new Torus(5, 5).cellsWithin(new Position(2, 2), 7).size());
    }

    @Test
    void rejectsEmptyGrid() {
        assertThrows(IllegalArgumentException.class, () -> new Torus(0, 5));
    }
}
