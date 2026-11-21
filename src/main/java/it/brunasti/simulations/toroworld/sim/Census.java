package it.brunasti.simulations.toroworld.sim;

/** Population counts and total resource at a given tick. */
public record Census(long tick, int plants, int herbivores, int carnivores, double resource) {

    @Override
    public String toString() {
        return String.format("tick %5d  plants %4d  herbivores %4d  carnivores %4d  resource %8.1f",
                tick, plants, herbivores, carnivores, resource);
    }
}
