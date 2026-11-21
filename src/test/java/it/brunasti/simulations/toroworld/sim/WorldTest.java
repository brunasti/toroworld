package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.Animal;
import it.brunasti.simulations.toroworld.life.Genome;
import it.brunasti.simulations.toroworld.life.Species;
import it.brunasti.simulations.toroworld.world.Position;
import it.brunasti.simulations.toroworld.world.ResourceField;
import it.brunasti.simulations.toroworld.world.Torus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest {

    private static final Genome GENOME = new Genome(1.0, 1, 10, 0.5, 10);
    private final Torus torus = new Torus(3, 3);
    private final World world = new World(torus, new ResourceField(torus, 1, 1, 0));
    private final Position a = new Position(0, 0);
    private final Position b = new Position(1, 0);

    @Test
    void cellHoldsOnlyOneLivingAnimal() {
        world.add(new Animal(1, Species.HERBIVORE, GENOME, a, 5));
        assertThrows(IllegalStateException.class,
                () -> world.add(new Animal(2, Species.CARNIVORE, GENOME, a, 5)));
    }

    @Test
    void deadOrganismDoesNotOccupyItsCell() {
        Animal dead = new Animal(1, Species.HERBIVORE, GENOME, a, 5);
        world.add(dead);
        dead.die();
        assertFalse(world.hasAnimal(a));
        world.add(new Animal(2, Species.HERBIVORE, GENOME, a, 5));
        world.removeDead();
        assertEquals(1, world.animals().size());
        assertTrue(world.hasAnimal(a));
    }

    @Test
    void moveUpdatesOccupancy() {
        Animal animal = new Animal(1, Species.HERBIVORE, GENOME, a, 5);
        world.add(animal);
        world.move(animal, b);
        assertFalse(world.hasAnimal(a));
        assertTrue(world.hasAnimal(b));
        assertEquals(b, animal.position());
    }
}
