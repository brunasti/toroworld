package io.github.brunasti.toroworld.life;

import io.github.brunasti.toroworld.world.Position;

/** State shared by every living thing: identity, genome, position, energy and age. */
public abstract sealed class Organism permits Plant, Animal {

    private final long id;
    private final Species species;
    private final Genome genome;
    private Position position;
    private double energy;
    private int age;
    private boolean alive = true;

    protected Organism(long id, Species species, Genome genome, Position position, double energy) {
        if (energy <= 0) {
            throw new IllegalArgumentException("An organism must start with positive energy");
        }
        this.id = id;
        this.species = species;
        this.genome = genome;
        this.position = position;
        this.energy = energy;
    }

    public long id() {
        return id;
    }

    public Species species() {
        return species;
    }

    public Genome genome() {
        return genome;
    }

    public Position position() {
        return position;
    }

    public double energy() {
        return energy;
    }

    public int age() {
        return age;
    }

    public boolean isAlive() {
        return alive;
    }

    /** Energy cost of an activity for this organism: base cost times metabolism. */
    public double costOf(Activity activity, ActivityCosts costs) {
        return costs.of(activity) * genome.metabolism();
    }

    /** Pays energy; the organism dies when its energy reaches zero. */
    public void spend(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot spend a negative amount");
        }
        energy -= amount;
        if (energy <= 0) {
            energy = 0;
            die();
        }
    }

    public void gain(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot gain a negative amount");
        }
        energy += amount;
    }

    /** Ages the organism by one tick; it dies when it exceeds its maximum age. */
    public void growOlder() {
        age++;
        if (age > genome.maxAge()) {
            die();
        }
    }

    public void die() {
        alive = false;
    }

    /** Only the world may move an organism, because it keeps track of cell occupancy. */
    public void relocate(Position newPosition) {
        this.position = newPosition;
    }

    @Override
    public String toString() {
        return species + "#" + id + "@" + position + String.format(" e=%.1f age=%d", energy, age);
    }
}
