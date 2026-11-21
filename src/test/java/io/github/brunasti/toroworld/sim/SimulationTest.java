package io.github.brunasti.toroworld.sim;

import io.github.brunasti.toroworld.life.Activity;
import io.github.brunasti.toroworld.life.ActivityCosts;
import io.github.brunasti.toroworld.life.Animal;
import io.github.brunasti.toroworld.life.Genome;
import io.github.brunasti.toroworld.life.Mutation;
import io.github.brunasti.toroworld.life.Plant;
import io.github.brunasti.toroworld.life.Species;
import io.github.brunasti.toroworld.world.Position;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    private static final double EPS = 1e-9;
    private static final Position CENTRE = new Position(1, 1);
    private static final Position CORNER = new Position(0, 0);
    private static final ActivityCosts FREE = new ActivityCosts(0, 0, 0, 0, 0);

    /** A 3x3 world with no starting population and explicit species parameters. */
    private static SimulationConfig emptyWorld(SpeciesConfig plants, SpeciesConfig herbivores,
                                               SpeciesConfig carnivores) {
        return new SimulationConfig(3, 3, 7L, 5, 10, 1,
                plants, herbivores, carnivores, Mutation.NONE);
    }

    private static SpeciesConfig species(Genome genome, ActivityCosts costs, double efficiency) {
        return new SpeciesConfig(0, 1, genome, costs, efficiency);
    }

    private static Genome genome(double intake) {
        return new Genome(1.0, intake, 1000, 0.4, 1000);
    }

    @Test
    void plantAbsorbsResourceAndPaysEatAndLiveCosts() {
        SimulationConfig config = emptyWorld(
                species(genome(3), new ActivityCosts(0.5, 0, 0, 0.2, 0), 0.8),
                species(genome(1), FREE, 1), species(genome(1), FREE, 1));
        Simulation sim = new Simulation(config, (self, world, costs, random) ->
                new Intent(Activity.EAT, self.position()));
        Plant plant = new Plant(100, config.plants().genome(), CENTRE, 5);
        sim.world().add(plant);

        sim.step();

        // resource: 5 + 1 regenerated - 3 taken
        assertEquals(3.0, sim.world().resources().level(CENTRE), EPS);
        // energy: 5 + 3 * 0.8 - (0.2 eat + 0.5 live)
        assertEquals(6.7, plant.energy(), EPS);
        assertEquals(1, plant.age());
    }

    @Test
    void herbivoreGrazesThePlantInItsCell() {
        SimulationConfig config = emptyWorld(
                species(genome(0), new ActivityCosts(0.5, 0, 0, 0, 0), 1),
                species(genome(2), new ActivityCosts(0.4, 0, 0, 0.2, 0), 0.5),
                species(genome(1), FREE, 1));
        Simulation sim = new Simulation(config, (self, world, costs, random) ->
                self.species() == Species.HERBIVORE ? new Intent(Activity.EAT, self.position()) : Intent.REST);
        Plant plant = new Plant(100, config.plants().genome(), CENTRE, 5);
        Animal herbivore = new Animal(101, Species.HERBIVORE, config.herbivores().genome(), CENTRE, 10);
        sim.world().add(plant);
        sim.world().add(herbivore);

        sim.step();

        // plants act first: 5 - 0.5 live; then grazed by 2
        assertEquals(2.5, plant.energy(), EPS);
        // 10 + 2 * 0.5 - (0.2 eat + 0.4 live)
        assertEquals(10.4, herbivore.energy(), EPS);
    }

    @Test
    void carnivoreKillsAdjacentHerbivore() {
        SimulationConfig config = emptyWorld(
                species(genome(0), FREE, 1),
                species(genome(1), FREE, 1),
                species(genome(15), new ActivityCosts(0.6, 0, 0, 0.3, 0), 0.5));
        Simulation sim = new Simulation(config, (self, world, costs, random) ->
                self.species() == Species.CARNIVORE ? new Intent(Activity.EAT, CORNER) : Intent.REST);
        Animal prey = new Animal(100, Species.HERBIVORE, config.herbivores().genome(), CORNER, 6);
        Animal hunter = new Animal(101, Species.CARNIVORE, config.carnivores().genome(), CENTRE, 10);
        sim.world().add(prey);
        sim.world().add(hunter);

        Census census = sim.step();

        assertFalse(prey.isAlive());
        assertFalse(sim.world().hasAnimal(CORNER));
        // 10 + 6 * 0.5 - (0.3 eat + 0.6 live)
        assertEquals(12.1, hunter.energy(), EPS);
        assertEquals(0, census.herbivores());
        assertEquals(1, census.carnivores());
    }

    @Test
    void reproductionSplitsEnergyAndOffspringWaitsForNextTick() {
        SimulationConfig config = emptyWorld(
                species(new Genome(1.0, 0, 10, 0.4, 1000), new ActivityCosts(0.5, 0, 0, 0, 1), 1),
                species(genome(1), FREE, 1), species(genome(1), FREE, 1));
        Position childCell = new Position(1, 2);
        Simulation sim = new Simulation(config, (self, world, costs, random) ->
                self.age() == 0 && self.id() == 100 ? new Intent(Activity.REPRODUCE, childCell) : Intent.REST);
        Plant parent = new Plant(100, config.plants().genome(), CENTRE, 20);
        sim.world().add(parent);

        sim.step();

        // available = 20 - 1 reproduce - 0.5 live = 18.5; child gets 40%
        Plant child = sim.world().plantAt(childCell).orElseThrow();
        assertEquals(7.4, child.energy(), EPS);
        assertEquals(0, child.age(), "offspring must not act in the tick it is born");
        assertEquals(20 - 1.5 - 7.4, parent.energy(), EPS);
    }

    @Test
    void offspringGenomeComesFromTheMutation() {
        Genome parentGenome = new Genome(1.0, 0, 10, 0.5, 50);
        Genome mutated = new Genome(1.1, 0, 10, 0.5, 60);
        SimulationConfig config = emptyWorld(
                species(parentGenome, FREE, 1), species(genome(1), FREE, 1), species(genome(1), FREE, 1))
                .withMutation((parent, random) -> mutated);
        Position childCell = new Position(2, 1);
        Simulation sim = new Simulation(config, (self, world, costs, random) ->
                self.id() == 100 && self.age() == 0 ? new Intent(Activity.REPRODUCE, childCell) : Intent.REST);
        sim.world().add(new Plant(100, parentGenome, CENTRE, 20));

        sim.step();

        assertSame(mutated, sim.world().plantAt(childCell).orElseThrow().genome());
    }

    @Test
    void sameSeedGivesTheSameRun() {
        assertEquals(run(SimulationConfig.defaults(), 200), run(SimulationConfig.defaults(), 200));
    }

    @Test
    void differentSeedsGiveDifferentRuns() {
        assertNotEquals(run(SimulationConfig.defaults().withSeed(1), 50),
                run(SimulationConfig.defaults().withSeed(2), 50));
    }

    @Test
    void startsWithTheConfiguredPopulation() {
        Census start = new Simulation(SimulationConfig.defaults()).census();
        assertEquals(0, start.tick());
        assertEquals(30, start.plants());
        assertEquals(10, start.herbivores());
        assertEquals(2, start.carnivores());
    }

    @Test
    void cellsHoldAtMostOnePlantAndOneAnimalThroughoutARun() {
        Simulation sim = new Simulation(SimulationConfig.defaults().withSeed(3));
        for (int t = 0; t < 300; t++) {
            sim.step();
            var world = sim.world();
            assertEquals(world.plants().size(),
                    world.plants().stream().map(Plant::position).distinct().count());
            assertEquals(world.animals().size(),
                    world.animals().stream().map(Animal::position).distinct().count());
            assertTrue(world.plants().stream().allMatch(Plant::isAlive));
            assertTrue(world.animals().stream().allMatch(Animal::isAlive));
        }
    }

    private static List<Census> run(SimulationConfig config, int ticks) {
        Simulation sim = new Simulation(config);
        List<Census> history = new ArrayList<>();
        for (int t = 0; t < ticks; t++) {
            history.add(sim.step());
        }
        return history;
    }
}
