package io.github.brunasti.toroworld.sim;

import io.github.brunasti.toroworld.life.Activity;
import io.github.brunasti.toroworld.life.ActivityCosts;
import io.github.brunasti.toroworld.life.Organism;
import io.github.brunasti.toroworld.life.Species;
import io.github.brunasti.toroworld.world.Position;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * First, deliberately simple decision rules. Organisms only see their own cell
 * and the 8 neighbouring cells. In order of priority:
 * <ol>
 *   <li>reproduce, if energy is above the reproduction threshold and a neighbouring cell is free;</li>
 *   <li>eat, if food is reachable: plants absorb resource, herbivores graze the plant in their
 *       own cell, carnivores attack a herbivore in a neighbouring cell;</li>
 *   <li>animals only: move to a free neighbouring cell, preferring one with a plant (herbivores),
 *       otherwise a random one;</li>
 *   <li>rest.</li>
 * </ol>
 */
public final class SimpleBehaviour implements Behaviour {

    @Override
    public Intent decide(Organism self, World world, ActivityCosts costs, RandomGenerator random) {
        List<Position> neighbours = world.torus().neighbours(self.position());
        boolean plant = self.species() == Species.PLANT;

        if (canAffordReproduction(self, costs)) {
            List<Position> free = neighbours.stream()
                    .filter(p -> plant ? !world.hasPlant(p) : !world.hasAnimal(p))
                    .toList();
            if (!free.isEmpty()) {
                return new Intent(Activity.REPRODUCE, pick(free, random));
            }
        }

        return switch (self.species()) {
            case PLANT -> new Intent(Activity.EAT, self.position());
            case HERBIVORE -> decideHerbivore(self, world, neighbours, random);
            case CARNIVORE -> decideCarnivore(self, world, neighbours, random);
        };
    }

    static boolean canAffordReproduction(Organism self, ActivityCosts costs) {
        double cost = self.costOf(Activity.REPRODUCE, costs) + self.costOf(Activity.LIVE, costs);
        return self.energy() >= self.genome().reproductionThreshold() && self.energy() > cost;
    }

    private Intent decideHerbivore(Organism self, World world, List<Position> neighbours,
                                   RandomGenerator random) {
        if (world.hasPlant(self.position())) {
            return new Intent(Activity.EAT, self.position());
        }
        List<Position> free = neighbours.stream().filter(p -> !world.hasAnimal(p)).toList();
        if (free.isEmpty()) {
            return Intent.REST;
        }
        List<Position> withPlant = free.stream().filter(world::hasPlant).toList();
        if (!withPlant.isEmpty()) {
            double best = withPlant.stream()
                    .mapToDouble(p -> world.plantAt(p).orElseThrow().energy())
                    .max().orElseThrow();
            List<Position> richest = withPlant.stream()
                    .filter(p -> world.plantAt(p).orElseThrow().energy() == best)
                    .toList();
            return new Intent(Activity.MOVE, pick(richest, random));
        }
        return new Intent(Activity.MOVE, pick(free, random));
    }

    private Intent decideCarnivore(Organism self, World world, List<Position> neighbours,
                                   RandomGenerator random) {
        List<Position> prey = neighbours.stream()
                .filter(p -> world.animalAt(p).map(a -> a.species() == Species.HERBIVORE).orElse(false))
                .toList();
        if (!prey.isEmpty()) {
            return new Intent(Activity.EAT, pick(prey, random));
        }
        List<Position> free = neighbours.stream().filter(p -> !world.hasAnimal(p)).toList();
        if (free.isEmpty()) {
            return Intent.REST;
        }
        return new Intent(Activity.MOVE, pick(free, random));
    }

    private static Position pick(List<Position> options, RandomGenerator random) {
        return options.get(random.nextInt(options.size()));
    }
}
