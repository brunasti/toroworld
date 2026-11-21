package io.github.brunasti.toroworld.life;

import io.github.brunasti.toroworld.world.Position;

/** A stationary organism that feeds on the resource of its own cell. */
public final class Plant extends Organism {

    public Plant(long id, Genome genome, Position position, double energy) {
        super(id, Species.PLANT, genome, position, energy);
    }
}
