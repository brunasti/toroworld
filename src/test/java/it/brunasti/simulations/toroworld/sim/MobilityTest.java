package it.brunasti.simulations.toroworld.sim;

import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Genome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MobilityTest {

    private static final ActivityCosts COSTS = new ActivityCosts(0, 0, 0, 0, 0);

    @Test
    void sightIsVisionTimesTheChosenBaseRoundedDown() {
        Genome g = new Genome(1, 1, 10, 0.5, 10, 2.0, 1.5);
        assertEquals(3, new Mobility(3, 1.5, 3, Mobility.VisionBase.OWN_SPEED).sightDistance(g));
        assertEquals(4, new Mobility(3, 1.5, 3, Mobility.VisionBase.MAX_SPEED).sightDistance(g));
        assertEquals(1, Mobility.FIXED.sightDistance(new Genome(1, 1, 10, 0.5, 10)));
    }

    @Test
    void speciesRejectsAFounderOutsideItsRanges() {
        Mobility m = new Mobility(3, 1.5, 3, Mobility.VisionBase.OWN_SPEED);
        assertThrows(IllegalArgumentException.class,
                () -> new SpeciesConfig(0, 1, new Genome(1, 1, 10, 0.5, 10, 4.0, 1.5), COSTS, 1, m));
        assertThrows(IllegalArgumentException.class,
                () -> new SpeciesConfig(0, 1, new Genome(1, 1, 10, 0.5, 10, 1.0, 1.0), COSTS, 1, m));
        assertThrows(IllegalArgumentException.class, () -> new Mobility(0.5, 1, 1, Mobility.VisionBase.OWN_SPEED));
        assertThrows(IllegalArgumentException.class, () -> new Mobility(3, 2, 1.5, Mobility.VisionBase.OWN_SPEED));
    }

    @Test
    void defaultsMatchTheAgreedCarnivoreRanges() {
        SpeciesConfig carnivores = SimulationConfig.defaults().carnivores();
        assertEquals(3.0, carnivores.mobility().maxSpeed());
        assertEquals(1.5, carnivores.mobility().minVision());
        assertEquals(3.0, carnivores.mobility().maxVision());
        assertEquals(1.0, carnivores.genome().speed());
        assertEquals(1.5, carnivores.genome().vision());
    }
}
