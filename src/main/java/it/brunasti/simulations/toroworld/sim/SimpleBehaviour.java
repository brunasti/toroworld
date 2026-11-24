package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.Activity;
import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Organism;
import it.brunasti.simulations.toroworld.life.Species;
import it.brunasti.simulations.toroworld.world.Position;
import it.brunasti.simulations.toroworld.world.Torus;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Deliberately simple decision rules. In order of priority:
 * <ol>
 *   <li>reproduce, if energy is above the reproduction threshold and a neighbouring cell is free;</li>
 *   <li>eat, if food is within reach: plants absorb resource, herbivores graze the plant in their
 *       own cell, carnivores attack a herbivore in a neighbouring cell;</li>
 *   <li>carnivores only: <b>hunt</b>. If a herbivore is within sight, run towards the nearest one,
 *       up to the carnivore's speed in cells, and attack it in the same tick if it is reached
 *       (a pounce);</li>
 *   <li>animals: move one cell to a free neighbouring cell. Herbivores prefer the cell with the most
 *       energetic plant; otherwise the cell is random;</li>
 *   <li>rest.</li>
 * </ol>
 * Herbivores see only their 8 neighbours. Carnivores see as far as their {@link Mobility} allows.
 */
public final class SimpleBehaviour implements Behaviour {

    @Override
    public Intent decide(Organism self, World world, SpeciesConfig species, RandomGenerator random) {
        List<Position> neighbours = world.torus().neighbours(self.position());
        boolean plant = self.species() == Species.PLANT;

        if (canAffordReproduction(self, species.costs())) {
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
            case CARNIVORE -> decideCarnivore(self, world, neighbours, species.mobility(), random);
        };
    }

    static boolean canAffordReproduction(Organism self, ActivityCosts costs) {
        double cost = self.costOf(Activity.REPRODUCE, costs) + self.costOf(Activity.LIVE, costs);
        return self.energy() >= self.genome().reproductionThreshold() && self.energy() > cost;
    }

    /**
     * Cells the organism can travel this tick: the whole part of its speed, plus one more cell
     * with a probability equal to the fractional part (speed 1.3: 2 cells 30% of the time).
     */
    static int stepsThisTick(double speed, RandomGenerator random) {
        int whole = (int) Math.floor(speed);
        double fraction = speed - whole;
        return whole + (fraction > 0 && random.nextDouble() < fraction ? 1 : 0);
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
                                   Mobility mobility, RandomGenerator random) {
        List<Position> adjacentPrey = neighbours.stream().filter(p -> hasPrey(world, p)).toList();
        if (!adjacentPrey.isEmpty()) {
            return new Intent(Activity.EAT, pick(adjacentPrey, random));
        }

        Torus torus = world.torus();
        int sight = mobility.sightDistance(self.genome());
        List<Position> visible = torus.cellsWithin(self.position(), sight).stream()
                .filter(p -> hasPrey(world, p))
                .toList();
        if (!visible.isEmpty()) {
            Position prey = nearest(torus, self.position(), visible, random);
            List<Position> route = routeTowards(world, self.position(), prey,
                    stepsThisTick(self.genome().speed(), random), random);
            if (!route.isEmpty()) {
                Position end = route.get(route.size() - 1);
                return torus.distance(end, prey) <= 1 ? Intent.pounce(route, prey) : Intent.move(route);
            }
        }

        // Nothing in sight, or no way to get closer: wander one cell.
        List<Position> free = neighbours.stream().filter(p -> !world.hasAnimal(p)).toList();
        if (free.isEmpty()) {
            return Intent.REST;
        }
        return new Intent(Activity.MOVE, pick(free, random));
    }

    /**
     * Walks greedily towards {@code target}: at each step, to a free neighbouring cell that is
     * closest to the target. Stops when next to the target, when out of steps, or when no step
     * gets closer. The route never enters the target's cell.
     */
    static List<Position> routeTowards(World world, Position from, Position target, int steps,
                                       RandomGenerator random) {
        Torus torus = world.torus();
        List<Position> route = new ArrayList<>();
        Position current = from;
        for (int i = 0; i < steps && torus.distance(current, target) > 1; i++) {
            int now = torus.distance(current, target);
            List<Position> closer = torus.neighbours(current).stream()
                    .filter(p -> !world.hasAnimal(p) && !route.contains(p) && !p.equals(from))
                    .filter(p -> torus.distance(p, target) < now)
                    .toList();
            if (closer.isEmpty()) {
                break;
            }
            int best = closer.stream().mapToInt(p -> torus.distance(p, target)).min().orElseThrow();
            current = pick(closer.stream().filter(p -> torus.distance(p, target) == best).toList(), random);
            route.add(current);
        }
        return route;
    }

    private static Position nearest(Torus torus, Position from, List<Position> cells, RandomGenerator random) {
        int best = cells.stream().mapToInt(p -> torus.distance(from, p)).min().orElseThrow();
        List<Position> nearest = cells.stream().filter(p -> torus.distance(from, p) == best).toList();
        return pick(nearest, random);
    }

    private static boolean hasPrey(World world, Position p) {
        return world.animalAt(p).map(a -> a.species() == Species.HERBIVORE).orElse(false);
    }

    private static Position pick(List<Position> options, RandomGenerator random) {
        return options.get(random.nextInt(options.size()));
    }
}
