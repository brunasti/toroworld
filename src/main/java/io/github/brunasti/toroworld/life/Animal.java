package io.github.brunasti.toroworld.life;

import io.github.brunasti.toroworld.world.Position;

/** A mobile organism: a herbivore or a carnivore. */
public final class Animal extends Organism {

    public Animal(long id, Species species, Genome genome, Position position, double energy) {
        super(id, requireAnimal(species), genome, position, energy);
    }

    private static Species requireAnimal(Species species) {
        if (species == Species.PLANT) {
            throw new IllegalArgumentException("An animal cannot be a plant");
        }
        return species;
    }

    /** True if this animal feeds on the given organism. */
    public boolean eats(Organism other) {
        return switch (species()) {
            case HERBIVORE -> other.species() == Species.PLANT;
            case CARNIVORE -> other.species() == Species.HERBIVORE;
            case PLANT -> false;
        };
    }
}
