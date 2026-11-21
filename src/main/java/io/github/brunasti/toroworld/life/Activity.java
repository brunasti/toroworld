package io.github.brunasti.toroworld.life;

/**
 * What an organism can do in one tick. Each activity has an energy cost,
 * configured per species group in {@link ActivityCosts}.
 */
public enum Activity {
    /** Basal cost of staying alive, paid every tick on top of the chosen activity. */
    LIVE,
    /** Doing nothing else this tick. */
    REST,
    /** Moving to a neighbouring cell (animals only). */
    MOVE,
    /** Feeding: absorbing resource (plants), grazing (herbivores) or hunting (carnivores). */
    EAT,
    /** Producing one offspring in a neighbouring cell. */
    REPRODUCE
}
