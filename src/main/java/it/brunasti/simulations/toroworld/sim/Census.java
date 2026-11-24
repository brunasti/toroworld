package it.brunasti.simulations.toroworld.sim;

/**
 * Population counts and where the energy is at a given tick.
 *
 * @param tick            the tick the census was taken at
 * @param plants          number of living plants
 * @param herbivores      number of living herbivores
 * @param carnivores      number of living carnivores
 * @param resource        total resource left in the cells
 * @param plantEnergy     total energy held by the living plants
 * @param herbivoreEnergy total energy held by the living herbivores
 * @param carnivoreEnergy total energy held by the living carnivores
 */
public record Census(long tick, int plants, int herbivores, int carnivores, double resource,
                     double plantEnergy, double herbivoreEnergy, double carnivoreEnergy) {

    @Override
    public String toString() {
        return String.format("tick %5d  plants %4d  herbivores %4d  carnivores %4d  resource %8.1f"
                        + "  energy: plants %8.1f  herbivores %7.1f  carnivores %7.1f",
                tick, plants, herbivores, carnivores, resource, plantEnergy, herbivoreEnergy, carnivoreEnergy);
    }
}
