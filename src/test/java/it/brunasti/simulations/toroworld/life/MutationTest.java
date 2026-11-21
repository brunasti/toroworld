package it.brunasti.simulations.toroworld.life;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutationTest {

    private static final Genome FOUNDER = new Genome(1.0, 4.0, 20.0, 0.4, 100);
    private static final Map<Species, Genome> FOUNDERS = Map.of(Species.HERBIVORE, FOUNDER);

    @Test
    void genomeTraitAccessorsAreConsistent() {
        for (Trait trait : Trait.values()) {
            Genome changed = FOUNDER.with(trait, trait == Trait.OFFSPRING_SHARE ? 0.7 : 7.0);
            assertEquals(trait == Trait.OFFSPRING_SHARE ? 0.7 : 7.0, changed.get(trait), 1e-9);
        }
        assertEquals(101, FOUNDER.with(Trait.MAX_AGE, 100.6).maxAge());
        assertEquals(1.0, FOUNDER.speed());
        assertEquals(1.0, FOUNDER.vision());
    }

    @Test
    void founderHasZeroAdvantage() {
        TraitBudget budget = new TraitBudget(FOUNDERS, 0);
        assertEquals(0.0, budget.advantage(Species.HERBIVORE, FOUNDER), 1e-12);
        assertTrue(budget.allows(Species.HERBIVORE, FOUNDER));
    }

    @Test
    void improvingOneTraitMustBePaidByAnother() {
        TraitBudget budget = new TraitBudget(FOUNDERS, 0);
        Genome greedy = FOUNDER.with(Trait.INTAKE, 8.0);
        assertFalse(budget.allows(Species.HERBIVORE, greedy));
        // double intake, double metabolism: ln 2 - ln 2 = 0
        Genome balanced = greedy.with(Trait.METABOLISM, 2.0);
        assertEquals(0.0, budget.advantage(Species.HERBIVORE, balanced), 1e-12);
        assertTrue(budget.allows(Species.HERBIVORE, balanced));
    }

    @Test
    void strategyTraitsAreOutsideTheBudget() {
        TraitBudget budget = new TraitBudget(FOUNDERS, 0);
        Genome strategy = FOUNDER.with(Trait.REPRODUCTION_THRESHOLD, 5.0).with(Trait.OFFSPRING_SHARE, 0.9);
        assertEquals(0.0, budget.advantage(Species.HERBIVORE, strategy), 1e-12);
    }

    @Test
    void allowanceLetsSomeAdvantageThrough() {
        Genome better = FOUNDER.with(Trait.MAX_AGE, 110);
        assertFalse(new TraitBudget(FOUNDERS, 0).allows(Species.HERBIVORE, better));
        assertTrue(new TraitBudget(FOUNDERS, 0.1).allows(Species.HERBIVORE, better));
    }

    @Test
    void gaussianMutationChangesTraitsAndStaysValid() {
        Mutation mutation = new GaussianMutation(1.0, 0.05, null);
        Random random = new Random(1);
        for (int i = 0; i < 1000; i++) {
            Genome child = mutation.mutate(Species.HERBIVORE, FOUNDER, random);
            assertNotEquals(FOUNDER, child);
            for (Trait trait : Trait.values()) {
                double v = child.get(trait);
                assertTrue(v >= trait.minimum() && v <= trait.maximum(), trait + " out of range: " + v);
            }
        }
    }

    @Test
    void speciesRangesOverrideTheDefaultRanges() {
        Genome hunter = new Genome(1.0, 4.0, 20.0, 0.4, 100, 2.9, 2.9);
        Mutation mutation = new GaussianMutation(1.0, 0.5, null, Map.of(Species.CARNIVORE, Map.of(
                Trait.SPEED, new TraitRange(1, 3), Trait.VISION, new TraitRange(1.5, 3))));
        Random random = new Random(4);
        for (int i = 0; i < 1000; i++) {
            Genome child = mutation.mutate(Species.CARNIVORE, hunter, random);
            assertTrue(child.speed() >= 1 && child.speed() <= 3);
            assertTrue(child.vision() >= 1.5 && child.vision() <= 3);
        }
    }

    @Test
    void fasterMustBePaidForInTheBudget() {
        Genome hunter = new Genome(1.0, 4.0, 20.0, 0.4, 100, 1.0, 1.5);
        TraitBudget budget = new TraitBudget(Map.of(Species.CARNIVORE, hunter), 0);
        assertFalse(budget.allows(Species.CARNIVORE, hunter.with(Trait.SPEED, 2.0)));
        assertTrue(budget.allows(Species.CARNIVORE, hunter.with(Trait.SPEED, 2.0).with(Trait.METABOLISM, 2.0)));
    }

    @Test
    void zeroProbabilityNeverMutates() {
        Mutation mutation = new GaussianMutation(0.0, 0.5, null);
        assertSame(FOUNDER, mutation.mutate(Species.HERBIVORE, FOUNDER, new Random(1)));
    }

    @Test
    void withTradeOffEveryOffspringIsWithinTheBudget() {
        TraitBudget budget = new TraitBudget(FOUNDERS, 0);
        Mutation mutation = new GaussianMutation(0.5, 0.2, budget);
        Random random = new Random(2);
        Genome genome = FOUNDER;
        int changed = 0;
        for (int i = 0; i < 2000; i++) {
            Genome child = mutation.mutate(Species.HERBIVORE, genome, random);
            assertTrue(budget.allows(Species.HERBIVORE, child));
            if (!child.equals(genome)) {
                changed++;
            }
            genome = child;
        }
        assertTrue(changed > 100, "mutations within the budget should still happen, got " + changed);
    }

    @Test
    void settingsBuildTheRightMutation() {
        assertSame(Mutation.NONE, MutationSettings.OFF.create(FOUNDERS));
        Mutation withBudget = MutationSettings.DEFAULT.create(FOUNDERS);
        assertTrue(((GaussianMutation) withBudget).budget().isPresent());
        Mutation free = MutationSettings.DEFAULT.withTradeOff(false).create(FOUNDERS);
        assertTrue(((GaussianMutation) free).budget().isEmpty());
    }
}
