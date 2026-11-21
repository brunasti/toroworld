package it.brunasti.simulations.toroworld.life;

import it.brunasti.simulations.toroworld.world.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganismTest {

    private static final Genome GENOME = new Genome(2.0, 1, 10, 0.5, 3);
    private static final Position HERE = new Position(0, 0);

    @Test
    void diesWhenEnergyReachesZero() {
        Plant plant = new Plant(1, GENOME, HERE, 5);
        plant.spend(4.5);
        assertTrue(plant.isAlive());
        plant.spend(0.5);
        assertFalse(plant.isAlive());
        assertEquals(0.0, plant.energy());
    }

    @Test
    void diesAfterMaximumAge() {
        Plant plant = new Plant(1, GENOME, HERE, 5);
        for (int i = 0; i < 3; i++) {
            plant.growOlder();
        }
        assertTrue(plant.isAlive());
        plant.growOlder();
        assertFalse(plant.isAlive());
    }

    @Test
    void metabolismMultipliesActivityCosts() {
        Plant plant = new Plant(1, GENOME, HERE, 5);
        ActivityCosts costs = new ActivityCosts(0.5, 0, 1, 0.25, 3);
        assertEquals(1.0, plant.costOf(Activity.LIVE, costs), 1e-9);
        assertEquals(6.0, plant.costOf(Activity.REPRODUCE, costs), 1e-9);
    }

    @Test
    void dietFollowsTheFoodChain() {
        Animal herbivore = new Animal(1, Species.HERBIVORE, GENOME, HERE, 5);
        Animal carnivore = new Animal(2, Species.CARNIVORE, GENOME, HERE, 5);
        Plant plant = new Plant(3, GENOME, HERE, 5);
        assertTrue(herbivore.eats(plant));
        assertFalse(herbivore.eats(carnivore));
        assertTrue(carnivore.eats(herbivore));
        assertFalse(carnivore.eats(plant));
        assertFalse(carnivore.eats(carnivore));
    }

    @Test
    void invalidStatesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Animal(1, Species.PLANT, GENOME, HERE, 5));
        assertThrows(IllegalArgumentException.class, () -> new Plant(1, GENOME, HERE, 0));
        assertThrows(IllegalArgumentException.class, () -> new Genome(1, 1, 10, 1.0, 3));
        assertThrows(IllegalArgumentException.class, () -> new ActivityCosts(-1, 0, 0, 0, 0));
    }
}
