package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.Activity;
import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Animal;
import it.brunasti.simulations.toroworld.life.Genome;
import it.brunasti.simulations.toroworld.life.Plant;
import it.brunasti.simulations.toroworld.life.Species;
import it.brunasti.simulations.toroworld.world.Position;
import it.brunasti.simulations.toroworld.world.ResourceField;
import it.brunasti.simulations.toroworld.world.Torus;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleBehaviourTest {

    private static final Genome GENOME = new Genome(1.0, 2, 20, 0.4, 100);
    private static final ActivityCosts COSTS = new ActivityCosts(0.5, 0, 0.5, 0.2, 2);
    private final Torus torus = new Torus(5, 5);
    private final World world = new World(torus, new ResourceField(torus, 5, 10, 1));
    private final SimpleBehaviour behaviour = new SimpleBehaviour();
    private final Random random = new Random(1);

    @Test
    void herbivoreMovesTowardsTheRichestNeighbouringPlant() {
        Animal herbivore = new Animal(1, Species.HERBIVORE, GENOME, new Position(2, 2), 10);
        world.add(herbivore);
        world.add(new Plant(2, GENOME, new Position(3, 2), 4));
        world.add(new Plant(3, GENOME, new Position(1, 1), 9));

        Intent intent = behaviour.decide(herbivore, world, COSTS, random);

        assertEquals(new Intent(Activity.MOVE, new Position(1, 1)), intent);
    }

    @Test
    void herbivoreEatsWhenStandingOnAPlant() {
        Animal herbivore = new Animal(1, Species.HERBIVORE, GENOME, new Position(2, 2), 10);
        world.add(herbivore);
        world.add(new Plant(2, GENOME, new Position(2, 2), 4));

        assertEquals(new Intent(Activity.EAT, new Position(2, 2)),
                behaviour.decide(herbivore, world, COSTS, random));
    }

    @Test
    void carnivoreAttacksAdjacentHerbivore() {
        Animal carnivore = new Animal(1, Species.CARNIVORE, GENOME, new Position(0, 0), 10);
        world.add(carnivore);
        world.add(new Animal(2, Species.HERBIVORE, GENOME, new Position(4, 4), 5));

        assertEquals(new Intent(Activity.EAT, new Position(4, 4)),
                behaviour.decide(carnivore, world, COSTS, random));
    }

    @Test
    void reproductionComesFirstWhenAffordable() {
        Animal herbivore = new Animal(1, Species.HERBIVORE, GENOME, new Position(2, 2), 25);
        world.add(herbivore);
        world.add(new Plant(2, GENOME, new Position(2, 2), 4));

        assertEquals(Activity.REPRODUCE, behaviour.decide(herbivore, world, COSTS, random).activity());
    }
}
