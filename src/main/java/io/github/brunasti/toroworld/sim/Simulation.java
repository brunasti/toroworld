package io.github.brunasti.toroworld.sim;

import io.github.brunasti.toroworld.life.Activity;
import io.github.brunasti.toroworld.life.ActivityCosts;
import io.github.brunasti.toroworld.life.Animal;
import io.github.brunasti.toroworld.life.Genome;
import io.github.brunasti.toroworld.life.Organism;
import io.github.brunasti.toroworld.life.Plant;
import io.github.brunasti.toroworld.life.Species;
import io.github.brunasti.toroworld.world.Position;
import io.github.brunasti.toroworld.world.ResourceField;
import io.github.brunasti.toroworld.world.Torus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Runs the world in discrete ticks. One tick:
 * <ol>
 *   <li>every cell regenerates resource;</li>
 *   <li>every living plant acts, in random order;</li>
 *   <li>every living animal acts, in random order;</li>
 *   <li>dead organisms are removed.</li>
 * </ol>
 * Acting means: decide an {@link Intent}, carry it out, pay its cost plus the
 * {@link Activity#LIVE} cost, then grow one tick older. Organisms born during a
 * tick act from the next tick. All randomness comes from one generator seeded
 * from the configuration, so a run is reproducible.
 */
public final class Simulation {

    private final SimulationConfig config;
    private final Behaviour behaviour;
    private final Random random;
    private final World world;
    private long tick;
    private long nextId = 1;

    public Simulation(SimulationConfig config) {
        this(config, new SimpleBehaviour());
    }

    public Simulation(SimulationConfig config, Behaviour behaviour) {
        this.config = config;
        this.behaviour = behaviour;
        this.random = new Random(config.seed());
        Torus torus = new Torus(config.width(), config.height());
        this.world = new World(torus, new ResourceField(torus,
                config.resourceInitial(), config.resourceMaximum(), config.resourceRegeneration()));
        populate();
    }

    public SimulationConfig config() {
        return config;
    }

    public World world() {
        return world;
    }

    public long tick() {
        return tick;
    }

    public Census census() {
        int plants = 0;
        int herbivores = 0;
        int carnivores = 0;
        for (Plant p : world.plants()) {
            if (p.isAlive()) {
                plants++;
            }
        }
        for (Animal a : world.animals()) {
            if (!a.isAlive()) {
                continue;
            }
            if (a.species() == Species.HERBIVORE) {
                herbivores++;
            } else {
                carnivores++;
            }
        }
        return new Census(tick, plants, herbivores, carnivores, world.resources().total());
    }

    /** Advances the world by one tick and returns the census after it. */
    public Census step() {
        world.resources().regenerate();

        List<Plant> plants = new ArrayList<>(world.plants());
        Collections.shuffle(plants, random);
        for (Plant plant : plants) {
            if (plant.isAlive()) {
                act(plant);
            }
        }

        List<Animal> animals = new ArrayList<>(world.animals());
        Collections.shuffle(animals, random);
        for (Animal animal : animals) {
            if (animal.isAlive()) {
                act(animal);
            }
        }

        world.removeDead();
        tick++;
        return census();
    }

    // ---------------------------------------------------------------- acting

    private void act(Organism self) {
        SpeciesConfig species = speciesConfig(self.species());
        ActivityCosts costs = species.costs();
        Intent intent = behaviour.decide(self, world, costs, random);

        double handedOver = 0;
        switch (intent.activity()) {
            case REST -> { }
            case MOVE -> world.move((Animal) self, intent.target());
            case EAT -> eat(self, intent.target(), species);
            case REPRODUCE -> handedOver = reproduce(self, intent.target(), costs);
            case LIVE -> throw new IllegalStateException("LIVE cannot be chosen");
        }

        self.spend(self.costOf(intent.activity(), costs) + self.costOf(Activity.LIVE, costs) + handedOver);
        if (self.isAlive()) {
            self.growOlder();
        }
    }

    private void eat(Organism self, Position target, SpeciesConfig species) {
        double intake = self.genome().intake();
        double taken = switch (self.species()) {
            case PLANT -> world.resources().take(self.position(), intake);
            case HERBIVORE -> world.plantAt(target)
                    .map(plant -> {
                        double bite = Math.min(intake, plant.energy());
                        plant.spend(bite);
                        return bite;
                    })
                    .orElse(0.0);
            case CARNIVORE -> world.animalAt(target)
                    .filter(prey -> ((Animal) self).eats(prey))
                    .map(prey -> {
                        double meal = Math.min(intake, prey.energy());
                        prey.die();
                        return meal;
                    })
                    .orElse(0.0);
        };
        self.gain(taken * species.efficiency());
    }

    /** Places the offspring and returns the energy the parent hands over to it. */
    private double reproduce(Organism parent, Position target, ActivityCosts costs) {
        double available = parent.energy()
                - parent.costOf(Activity.REPRODUCE, costs)
                - parent.costOf(Activity.LIVE, costs);
        double childEnergy = available * parent.genome().offspringShare();
        if (childEnergy <= 0) {
            return 0;
        }
        Genome childGenome = config.mutation().mutate(parent.genome(), random);
        if (parent instanceof Plant) {
            world.add(new Plant(nextId++, childGenome, target, childEnergy));
        } else {
            world.add(new Animal(nextId++, parent.species(), childGenome, target, childEnergy));
        }
        return childEnergy;
    }

    // ------------------------------------------------------------- populating

    private void populate() {
        for (Position p : randomCells(config.plants().initialCount())) {
            world.add(new Plant(nextId++, config.plants().genome(), p, config.plants().initialEnergy()));
        }
        int herbivores = config.herbivores().initialCount();
        List<Position> animalCells = randomCells(herbivores + config.carnivores().initialCount());
        for (int i = 0; i < animalCells.size(); i++) {
            SpeciesConfig sc = i < herbivores ? config.herbivores() : config.carnivores();
            Species species = i < herbivores ? Species.HERBIVORE : Species.CARNIVORE;
            world.add(new Animal(nextId++, species, sc.genome(), animalCells.get(i), sc.initialEnergy()));
        }
    }

    private List<Position> randomCells(int count) {
        Torus torus = world.torus();
        List<Position> cells = new ArrayList<>(torus.cellCount());
        for (int i = 0; i < torus.cellCount(); i++) {
            cells.add(torus.position(i));
        }
        Collections.shuffle(cells, random);
        return cells.subList(0, count);
    }

    private SpeciesConfig speciesConfig(Species species) {
        return switch (species) {
            case PLANT -> config.plants();
            case HERBIVORE -> config.herbivores();
            case CARNIVORE -> config.carnivores();
        };
    }
}
