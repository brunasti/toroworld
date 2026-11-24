package it.brunasti.simulations.toroworld.settings;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsCodecTest {

    private final RunSettings defaults = RunSettings.defaults();

    @Test
    void defaultsSurviveARoundTrip() {
        assertEquals(defaults, SettingsCodec.fromMap(SettingsCodec.toMap(defaults), defaults));
    }

    @Test
    void everyParameterHasAValueAndNoOtherKeysExist() {
        Map<String, String> map = SettingsCodec.toMap(defaults);
        assertEquals(SettingsCodec.PARAMETERS.size(), map.size());
        SettingsCodec.PARAMETERS.forEach(p -> assertTrue(map.get(p.key()) != null, p.key()));
    }

    @Test
    void onlyCarnivoresHaveSpeedAndVisionSettings() {
        Map<String, String> map = SettingsCodec.toMap(defaults);
        assertTrue(map.containsKey("carnivores.genome.speed"));
        assertTrue(map.containsKey("carnivores.mobility.visionBase"));
        assertFalse(map.containsKey("herbivores.genome.speed"));
        assertFalse(map.containsKey("plants.mobility.maxSpeed"));
        assertFalse(map.containsKey("plants.cost.move"));
    }

    @Test
    void givenValuesOverrideTheBaseAndUnknownKeysAreIgnored() {
        RunSettings s = SettingsCodec.fromMap(Map.of(
                "world.width", "40",
                "world.ticks", "250",
                "carnivores.mobility.maxSpeed", "5",
                "carnivores.mobility.visionBase", "max_speed",
                "mutation.tradeOff", "off",
                "something.else", "whatever"), defaults);
        assertEquals(40, s.config().width());
        assertEquals(defaults.config().height(), s.config().height());
        assertEquals(250, s.ticks());
        assertEquals(5.0, s.config().carnivores().mobility().maxSpeed());
        assertEquals("MAX_SPEED", s.config().carnivores().mobility().visionBase().name());
        assertFalse(s.config().mutation().tradeOff());
    }

    @Test
    void aCommaIsAcceptedAsDecimalSeparator() {
        RunSettings s = SettingsCodec.fromMap(Map.of("plants.density", "0,4"), defaults);
        assertEquals(0.4, s.config().plants().initialDensity());
    }

    @Test
    void errorsNameTheSetting() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SettingsCodec.fromMap(Map.of("herbivores.genome.intake", "lots"), defaults));
        assertTrue(e.getMessage().contains("herbivores.genome.intake"), e.getMessage());

        e = assertThrows(IllegalArgumentException.class,
                () -> SettingsCodec.fromMap(Map.of("carnivores.genome.speed", "4"), defaults));
        assertTrue(e.getMessage().startsWith("Carnivores:"), e.getMessage());

        assertThrows(IllegalArgumentException.class,
                () -> SettingsCodec.fromMap(Map.of("world.ticks", "0"), defaults));
    }
}
