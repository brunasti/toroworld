package it.brunasti.simulations.toroworld.life;

/** The kinds of organism living on the torus. */
public enum Species {
    /** Does not move; takes resource from its own cell. */
    PLANT,
    /** Eats plants. */
    HERBIVORE,
    /** Eats herbivores. */
    CARNIVORE
}
