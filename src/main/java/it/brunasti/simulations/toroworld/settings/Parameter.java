package it.brunasti.simulations.toroworld.settings;

/**
 * Description of one setting: its key in the settings file, where it appears in the
 * settings window, and how to read its value.
 *
 * @param key         key in the settings file, e.g. {@code carnivores.mobility.maxSpeed}
 * @param tab         tab of the settings window it belongs to
 * @param section     heading inside the tab, e.g. "Genome"
 * @param label       short name shown next to the field
 * @param description one-line explanation, shown as a tooltip and in the settings file
 * @param kind        type of the value
 */
public record Parameter(String key, String tab, String section, String label, String description, Kind kind) {

    /** Type of a setting's value. */
    public enum Kind {
        INTEGER, LONG, DECIMAL, BOOLEAN, VISION_BASE
    }
}
