package io.github.brunasti.toroworld.sim;

import io.github.brunasti.toroworld.life.Animal;
import io.github.brunasti.toroworld.life.Organism;
import io.github.brunasti.toroworld.life.Plant;
import io.github.brunasti.toroworld.world.Position;
import io.github.brunasti.toroworld.world.ResourceField;
import io.github.brunasti.toroworld.world.Torus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The torus with its resources and inhabitants.
 * Each cell holds at most one living plant and at most one living animal.
 * Dead organisms stay in the lists until {@link #removeDead()} is called at
 * the end of a tick, but never count as occupying a cell.
 */
public final class World {

    private final Torus torus;
    private final ResourceField resources;
    private final Plant[] plantAt;
    private final Animal[] animalAt;
    private final List<Plant> plants = new ArrayList<>();
    private final List<Animal> animals = new ArrayList<>();

    public World(Torus torus, ResourceField resources) {
        this.torus = torus;
        this.resources = resources;
        this.plantAt = new Plant[torus.cellCount()];
        this.animalAt = new Animal[torus.cellCount()];
    }

    public Torus torus() {
        return torus;
    }

    public ResourceField resources() {
        return resources;
    }

    public Optional<Plant> plantAt(Position p) {
        return Optional.ofNullable(alive(plantAt[torus.index(p)]));
    }

    public Optional<Animal> animalAt(Position p) {
        return Optional.ofNullable(alive(animalAt[torus.index(p)]));
    }

    public boolean hasPlant(Position p) {
        return plantAt(p).isPresent();
    }

    public boolean hasAnimal(Position p) {
        return animalAt(p).isPresent();
    }

    public void add(Plant plant) {
        int i = torus.index(plant.position());
        if (alive(plantAt[i]) != null) {
            throw new IllegalStateException("Cell " + plant.position() + " already has a plant");
        }
        plantAt[i] = plant;
        plants.add(plant);
    }

    public void add(Animal animal) {
        int i = torus.index(animal.position());
        if (alive(animalAt[i]) != null) {
            throw new IllegalStateException("Cell " + animal.position() + " already has an animal");
        }
        animalAt[i] = animal;
        animals.add(animal);
    }

    public void move(Animal animal, Position target) {
        int to = torus.index(target);
        if (alive(animalAt[to]) != null) {
            throw new IllegalStateException("Cell " + target + " already has an animal");
        }
        int from = torus.index(animal.position());
        if (animalAt[from] == animal) {
            animalAt[from] = null;
        }
        animalAt[to] = animal;
        animal.relocate(target);
    }

    /** Snapshot of the plants, including any that died during the current tick. */
    public List<Plant> plants() {
        return List.copyOf(plants);
    }

    /** Snapshot of the animals, including any that died during the current tick. */
    public List<Animal> animals() {
        return List.copyOf(animals);
    }

    public void removeDead() {
        plants.removeIf(p -> !p.isAlive());
        animals.removeIf(a -> !a.isAlive());
        for (int i = 0; i < plantAt.length; i++) {
            if (plantAt[i] != null && !plantAt[i].isAlive()) {
                plantAt[i] = null;
            }
            if (animalAt[i] != null && !animalAt[i].isAlive()) {
                animalAt[i] = null;
            }
        }
    }

    private static <T extends Organism> T alive(T organism) {
        return organism != null && organism.isAlive() ? organism : null;
    }
}
