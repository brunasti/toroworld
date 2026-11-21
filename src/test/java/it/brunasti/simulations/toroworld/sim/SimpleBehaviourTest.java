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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleBehaviourTest {

    private static final Genome GENOME = new Genome(1.0, 2, 20, 0.4, 100);
    private static final ActivityCosts COSTS = new ActivityCosts(0.5, 0, 0.5, 0.2, 2);
    private static final SpeciesConfig SPECIES = new SpeciesConfig(0, 1, GENOME, COSTS, 1);
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

        Intent intent = behaviour.decide(herbivore, world, SPECIES, random);

        assertEquals(new Intent(Activity.MOVE, new Position(1, 1)), intent);
    }

    @Test
    void herbivoreEatsWhenStandingOnAPlant() {
        Animal herbivore = new Animal(1, Species.HERBIVORE, GENOME, new Position(2, 2), 10);
        world.add(herbivore);
        world.add(new Plant(2, GENOME, new Position(2, 2), 4));

        assertEquals(new Intent(Activity.EAT, new Position(2, 2)),
                behaviour.decide(herbivore, world, SPECIES, random));
    }

    @Test
    void carnivoreAttacksAdjacentHerbivore() {
        Animal carnivore = new Animal(1, Species.CARNIVORE, GENOME, new Position(0, 0), 10);
        world.add(carnivore);
        world.add(new Animal(2, Species.HERBIVORE, GENOME, new Position(4, 4), 5));

        assertEquals(new Intent(Activity.EAT, new Position(4, 4)),
                behaviour.decide(carnivore, world, SPECIES, random));
    }

    private SpeciesConfig hunters(Genome genome, Mobility.VisionBase base) {
        return new SpeciesConfig(0, 1, genome, COSTS, 1, new Mobility(3, 1.5, 3, base));
    }

    private final Torus big = new Torus(20, 20);
    private final World open = new World(big, new ResourceField(big, 5, 10, 1));

    @Test
    void carnivorePouncesOnPreyWithinSightAndReach() {
        Genome g = new Genome(1.0, 2, 1000, 0.4, 100, 2.0, 1.5); // sight floor(1.5 x 2) = 3
        Animal hunter = new Animal(1, Species.CARNIVORE, g, new Position(5, 5), 10);
        open.add(hunter);
        open.add(new Animal(2, Species.HERBIVORE, GENOME, new Position(8, 5), 5));

        Intent intent = behaviour.decide(hunter, open, hunters(g, Mobility.VisionBase.OWN_SPEED), random);

        assertEquals(Activity.EAT, intent.activity());
        assertEquals(new Position(8, 5), intent.target());
        assertEquals(java.util.List.of(new Position(6, 5), new Position(7, 5)), intent.route());
    }

    @Test
    void slowCarnivoreSeesFurtherThanItCanRunAndApproaches() {
        Genome g = new Genome(1.0, 2, 1000, 0.4, 100, 1.0, 3.0); // sight 3, speed 1
        Animal hunter = new Animal(1, Species.CARNIVORE, g, new Position(5, 5), 10);
        open.add(hunter);
        open.add(new Animal(2, Species.HERBIVORE, GENOME, new Position(5, 8), 5));

        Intent intent = behaviour.decide(hunter, open, hunters(g, Mobility.VisionBase.OWN_SPEED), random);

        assertEquals(Activity.MOVE, intent.activity());
        assertEquals(1, intent.route().size());
        assertEquals(2, big.distance(intent.target(), new Position(5, 8)));
    }

    @Test
    void preyOutOfSightIsIgnored() {
        Genome g = new Genome(1.0, 2, 1000, 0.4, 100, 1.0, 1.5); // sight floor(1.5) = 1
        Animal hunter = new Animal(1, Species.CARNIVORE, g, new Position(5, 5), 10);
        open.add(hunter);
        open.add(new Animal(2, Species.HERBIVORE, GENOME, new Position(5, 8), 5));
        // With sight based on the species' maximum speed: floor(1.5 x 3) = 4, so the prey is seen.
        Intent wander = behaviour.decide(hunter, open, hunters(g, Mobility.VisionBase.OWN_SPEED), new Random(3));
        Intent hunt = behaviour.decide(hunter, open, hunters(g, Mobility.VisionBase.MAX_SPEED), new Random(3));

        assertEquals(Activity.MOVE, hunt.activity());
        assertEquals(2, big.distance(hunt.target(), new Position(5, 8)));
        assertEquals(Activity.MOVE, wander.activity());
    }

    @Test
    void fractionalSpeedGivesAnExtraCellAtTheMatchingRate() {
        Random r = new Random(5);
        int extra = 0;
        for (int i = 0; i < 10_000; i++) {
            int steps = SimpleBehaviour.stepsThisTick(1.3, r);
            assertTrue(steps == 1 || steps == 2);
            extra += steps - 1;
        }
        assertEquals(0.3, extra / 10_000.0, 0.02);
        assertEquals(2, SimpleBehaviour.stepsThisTick(2.0, r));
    }

    @Test
    void routeGoesAroundOccupiedCells() {
        open.add(new Animal(9, Species.HERBIVORE, GENOME, new Position(6, 5), 5));
        var route = SimpleBehaviour.routeTowards(open, new Position(5, 5), new Position(9, 5), 3, random);
        assertEquals(3, route.size());
        assertFalse(route.contains(new Position(6, 5)));
        assertEquals(1, big.distance(route.get(route.size() - 1), new Position(9, 5)));
    }

    @Test
    void reproductionComesFirstWhenAffordable() {
        Animal herbivore = new Animal(1, Species.HERBIVORE, GENOME, new Position(2, 2), 25);
        world.add(herbivore);
        world.add(new Plant(2, GENOME, new Position(2, 2), 4));

        assertEquals(Activity.REPRODUCE, behaviour.decide(herbivore, world, SPECIES, random).activity());
    }
}
