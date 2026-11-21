package it.brunasti.simulations.toroworld.life;

import it.brunasti.simulations.toroworld.world.Position;

/** A stationary organism that feeds on the resource of its own cell. */
public final class Plant extends Organism {

    public Plant(long id, Genome genome, Position position, double energy) {
        super(id, Species.PLANT, genome, position, energy);
    }
}
