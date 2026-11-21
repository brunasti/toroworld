package io.github.brunasti.toroworld.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceFieldTest {

    private final Torus torus = new Torus(2, 2);
    private final Position cell = new Position(1, 1);

    @Test
    void takeNeverExceedsWhatIsThere() {
        ResourceField field = new ResourceField(torus, 3, 10, 1);
        assertEquals(2.0, field.take(cell, 2), 1e-9);
        assertEquals(1.0, field.take(cell, 5), 1e-9);
        assertEquals(0.0, field.level(cell), 1e-9);
    }

    @Test
    void regenerationStopsAtTheMaximum() {
        ResourceField field = new ResourceField(torus, 9.5, 10, 1);
        field.regenerate();
        assertEquals(10.0, field.level(cell), 1e-9);
        assertEquals(40.0, field.total(), 1e-9);
    }
}
