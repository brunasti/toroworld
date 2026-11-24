package it.brunasti.simulations.toroworld.settings;

import it.brunasti.simulations.toroworld.life.ActivityCosts;
import it.brunasti.simulations.toroworld.life.Genome;
import it.brunasti.simulations.toroworld.life.MutationSettings;
import it.brunasti.simulations.toroworld.sim.Mobility;
import it.brunasti.simulations.toroworld.sim.SimulationConfig;
import it.brunasti.simulations.toroworld.sim.SpeciesConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts {@link RunSettings} to and from a flat map of named values, the common format of the
 * settings file and the settings window. {@link #PARAMETERS} lists every setting, in display order.
 * <p>
 * Plants and herbivores have no speed or vision settings: they always move one cell (herbivores)
 * and see only their neighbours. Plants have no move cost.
 */
public final class SettingsCodec {

    public static final String WORLD = "World";
    public static final String PLANTS = "Plants";
    public static final String HERBIVORES = "Herbivores";
    public static final String CARNIVORES = "Carnivores";
    public static final String MUTATION = "Mutation";

    /** Every setting, in the order of the settings window and file. */
    public static final List<Parameter> PARAMETERS = buildParameters();

    private SettingsCodec() {
    }

    // ------------------------------------------------------------------ descriptions

    private static List<Parameter> buildParameters() {
        List<Parameter> list = new ArrayList<>();
        list.add(p("world.width", WORLD, "Grid", "Width", "Grid width, in cells", Parameter.Kind.INTEGER));
        list.add(p("world.height", WORLD, "Grid", "Height", "Grid height, in cells", Parameter.Kind.INTEGER));
        list.add(p("world.seed", WORLD, "Run", "Seed",
                "Random seed: the same seed and settings always give the same run", Parameter.Kind.LONG));
        list.add(p("world.ticks", WORLD, "Run", "Ticks", "Length of the run, in ticks", Parameter.Kind.INTEGER));
        list.add(p("resource.initial", WORLD, "Resource (every cell)", "Initial",
                "Resource in each cell at tick 0", Parameter.Kind.DECIMAL));
        list.add(p("resource.maximum", WORLD, "Resource (every cell)", "Maximum",
                "Regeneration never goes above this", Parameter.Kind.DECIMAL));
        list.add(p("resource.regeneration", WORLD, "Resource (every cell)", "Regeneration",
                "Added to every cell at the start of each tick", Parameter.Kind.DECIMAL));
        addSpecies(list, "plants", PLANTS, false, false);
        addSpecies(list, "herbivores", HERBIVORES, true, false);
        addSpecies(list, "carnivores", CARNIVORES, true, true);
        list.add(p("mutation.enabled", MUTATION, "Mutation", "Enabled",
                "Off: offspring are exact copies of their parent", Parameter.Kind.BOOLEAN));
        list.add(p("mutation.probability", MUTATION, "Mutation", "Probability",
                "Chance that each trait changes at birth, from 0 to 1", Parameter.Kind.DECIMAL));
        list.add(p("mutation.relativeSigma", MUTATION, "Mutation", "Relative sigma",
                "Size of a change: standard deviation as a fraction of the value (0.05 = 5%)",
                Parameter.Kind.DECIMAL));
        list.add(p("mutation.tradeOff", MUTATION, "Trade-off", "Enabled",
                "On: a species cannot improve all its traits at once (one shared budget)",
                Parameter.Kind.BOOLEAN));
        list.add(p("mutation.allowance", MUTATION, "Trade-off", "Allowance",
                "Total advantage over the founder genome allowed; 0 = none", Parameter.Kind.DECIMAL));
        return Collections.unmodifiableList(list);
    }

    private static void addSpecies(List<Parameter> list, String prefix, String tab, boolean moves, boolean mobile) {
        String start = "Start";
        list.add(p(prefix + ".density", tab, start, "Density",
                "Fraction of the cells that get one at tick 0 (0.20 = 20 per 100 cells)", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".initialEnergy", tab, start, "Initial energy",
                "Energy of each organism at tick 0", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".efficiency", tab, start, "Efficiency",
                "Fraction of the food taken in that becomes energy, above 0 and up to 1", Parameter.Kind.DECIMAL));
        String genome = "Founder genome";
        list.add(p(prefix + ".genome.metabolism", tab, genome, "Metabolism",
                "Multiplier on every activity cost", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".genome.intake", tab, genome, "Intake",
                "Maximum food taken in one meal", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".genome.reproductionThreshold", tab, genome, "Reproduction threshold",
                "Energy needed before trying to reproduce", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".genome.offspringShare", tab, genome, "Offspring share",
                "Share of the parent's remaining energy given to the offspring, between 0 and 1",
                Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".genome.maxAge", tab, genome, "Maximum age",
                "Age in ticks after which the organism dies", Parameter.Kind.INTEGER));
        if (mobile) {
            list.add(p(prefix + ".genome.speed", tab, genome, "Speed",
                    "Cells travelled per tick; a fraction gives a matching chance of one more cell",
                    Parameter.Kind.DECIMAL));
            list.add(p(prefix + ".genome.vision", tab, genome, "Vision",
                    "Sight as a multiple of the base speed", Parameter.Kind.DECIMAL));
        }
        String costs = "Activity costs (times metabolism)";
        list.add(p(prefix + ".cost.live", tab, costs, "Live", "Paid every tick", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".cost.rest", tab, costs, "Rest", "Doing nothing else", Parameter.Kind.DECIMAL));
        if (moves) {
            list.add(p(prefix + ".cost.move", tab, costs, "Move", "Per cell travelled", Parameter.Kind.DECIMAL));
        }
        list.add(p(prefix + ".cost.eat", tab, costs, "Eat", "Feeding", Parameter.Kind.DECIMAL));
        list.add(p(prefix + ".cost.reproduce", tab, costs, "Reproduce", "Producing one offspring",
                Parameter.Kind.DECIMAL));
        if (mobile) {
            String mobility = "Mobility";
            list.add(p(prefix + ".mobility.maxSpeed", tab, mobility, "Maximum speed",
                    "Highest speed the species can evolve (the lowest is 1)", Parameter.Kind.DECIMAL));
            list.add(p(prefix + ".mobility.minVision", tab, mobility, "Minimum vision",
                    "Lowest value the vision trait can evolve to", Parameter.Kind.DECIMAL));
            list.add(p(prefix + ".mobility.maxVision", tab, mobility, "Maximum vision",
                    "Highest value the vision trait can evolve to", Parameter.Kind.DECIMAL));
            list.add(p(prefix + ".mobility.visionBase", tab, mobility, "Sight based on",
                    "OWN_SPEED: sight = vision x own speed. MAX_SPEED: sight = vision x maximum speed",
                    Parameter.Kind.VISION_BASE));
        }
    }

    private static Parameter p(String key, String tab, String section, String label, String description,
                               Parameter.Kind kind) {
        return new Parameter(key, tab, section, label, description, kind);
    }

    // ------------------------------------------------------------------ to map

    /** All settings as text values, keyed as in {@link #PARAMETERS}, in display order. */
    public static Map<String, String> toMap(RunSettings settings) {
        Map<String, String> m = allValues(settings);
        // Keep only the keys that are settings (e.g. no speed for plants), in display order.
        Map<String, String> ordered = new LinkedHashMap<>();
        for (Parameter p : PARAMETERS) {
            ordered.put(p.key(), m.get(p.key()));
        }
        return ordered;
    }

    /** Every value needed to rebuild the settings, including those that are not editable settings. */
    private static Map<String, String> allValues(RunSettings settings) {
        SimulationConfig c = settings.config();
        Map<String, String> m = new LinkedHashMap<>();
        m.put("world.width", Integer.toString(c.width()));
        m.put("world.height", Integer.toString(c.height()));
        m.put("world.seed", Long.toString(c.seed()));
        m.put("world.ticks", Integer.toString(settings.ticks()));
        m.put("resource.initial", num(c.resourceInitial()));
        m.put("resource.maximum", num(c.resourceMaximum()));
        m.put("resource.regeneration", num(c.resourceRegeneration()));
        putSpecies(m, "plants", c.plants());
        putSpecies(m, "herbivores", c.herbivores());
        putSpecies(m, "carnivores", c.carnivores());
        MutationSettings mu = c.mutation();
        m.put("mutation.enabled", Boolean.toString(mu.enabled()));
        m.put("mutation.probability", num(mu.probability()));
        m.put("mutation.relativeSigma", num(mu.relativeSigma()));
        m.put("mutation.tradeOff", Boolean.toString(mu.tradeOff()));
        m.put("mutation.allowance", num(mu.allowance()));
        return m;
    }

    private static void putSpecies(Map<String, String> m, String prefix, SpeciesConfig s) {
        m.put(prefix + ".density", num(s.initialDensity()));
        m.put(prefix + ".initialEnergy", num(s.initialEnergy()));
        m.put(prefix + ".efficiency", num(s.efficiency()));
        Genome g = s.genome();
        m.put(prefix + ".genome.metabolism", num(g.metabolism()));
        m.put(prefix + ".genome.intake", num(g.intake()));
        m.put(prefix + ".genome.reproductionThreshold", num(g.reproductionThreshold()));
        m.put(prefix + ".genome.offspringShare", num(g.offspringShare()));
        m.put(prefix + ".genome.maxAge", Integer.toString(g.maxAge()));
        m.put(prefix + ".genome.speed", num(g.speed()));
        m.put(prefix + ".genome.vision", num(g.vision()));
        ActivityCosts k = s.costs();
        m.put(prefix + ".cost.live", num(k.live()));
        m.put(prefix + ".cost.rest", num(k.rest()));
        m.put(prefix + ".cost.move", num(k.move()));
        m.put(prefix + ".cost.eat", num(k.eat()));
        m.put(prefix + ".cost.reproduce", num(k.reproduce()));
        Mobility mo = s.mobility();
        m.put(prefix + ".mobility.maxSpeed", num(mo.maxSpeed()));
        m.put(prefix + ".mobility.minVision", num(mo.minVision()));
        m.put(prefix + ".mobility.maxVision", num(mo.maxVision()));
        m.put(prefix + ".mobility.visionBase", mo.visionBase().name());
    }

    private static String num(double value) {
        // Shortest exact form, always with a dot as decimal separator.
        return value == Math.rint(value) && Math.abs(value) < 1e15
                ? String.format(Locale.ROOT, "%.1f", value)
                : Double.toString(value);
    }

    // ------------------------------------------------------------------ from map

    /**
     * Builds settings from text values. Keys that are missing take their value from {@code base},
     * and so do the values that are not settings (e.g. the speed of plants); unknown keys are ignored.
     *
     * @throws IllegalArgumentException if a value cannot be read, or the resulting settings are
     *                                  invalid; the message names the setting where possible
     */
    public static RunSettings fromMap(Map<String, String> values, RunSettings base) {
        Map<String, String> v = allValues(base);
        for (Parameter p : PARAMETERS) {
            String value = values.get(p.key());
            if (value != null) {
                v.put(p.key(), value.trim());
            }
        }
        Reader r = new Reader(v);
        SimulationConfig config = new SimulationConfig(
                r.integer("world.width"), r.integer("world.height"), r.longValue("world.seed"),
                r.decimal("resource.initial"), r.decimal("resource.maximum"), r.decimal("resource.regeneration"),
                species(r, "plants"),
                species(r, "herbivores"),
                species(r, "carnivores"),
                new MutationSettings(r.bool("mutation.enabled"), r.decimal("mutation.probability"),
                        r.decimal("mutation.relativeSigma"), r.bool("mutation.tradeOff"),
                        r.decimal("mutation.allowance")));
        return new RunSettings(config, r.integer("world.ticks"));
    }

    private static SpeciesConfig species(Reader r, String prefix) {
        try {
            Genome genome = new Genome(r.decimal(prefix + ".genome.metabolism"), r.decimal(prefix + ".genome.intake"),
                    r.decimal(prefix + ".genome.reproductionThreshold"), r.decimal(prefix + ".genome.offspringShare"),
                    r.integer(prefix + ".genome.maxAge"), r.decimal(prefix + ".genome.speed"),
                    r.decimal(prefix + ".genome.vision"));
            ActivityCosts costs = new ActivityCosts(r.decimal(prefix + ".cost.live"), r.decimal(prefix + ".cost.rest"),
                    r.decimal(prefix + ".cost.move"), r.decimal(prefix + ".cost.eat"),
                    r.decimal(prefix + ".cost.reproduce"));
            Mobility mobility = new Mobility(r.decimal(prefix + ".mobility.maxSpeed"),
                    r.decimal(prefix + ".mobility.minVision"), r.decimal(prefix + ".mobility.maxVision"),
                    r.visionBase(prefix + ".mobility.visionBase"));
            return new SpeciesConfig(r.decimal(prefix + ".density"), r.decimal(prefix + ".initialEnergy"),
                    genome, costs, r.decimal(prefix + ".efficiency"), mobility);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Setting ")) {
                throw e;
            }
            throw new IllegalArgumentException(capitalise(prefix) + ": " + e.getMessage(), e);
        }
    }

    private static String capitalise(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Reads typed values, naming the setting in any error. */
    private record Reader(Map<String, String> values) {

        private String raw(String key) {
            String value = values.get(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("Setting " + key + " has no value");
            }
            return value;
        }

        int integer(String key) {
            try {
                return Integer.parseInt(raw(key));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Setting " + key + ": \"" + values.get(key)
                        + "\" is not a whole number", e);
            }
        }

        long longValue(String key) {
            try {
                return Long.parseLong(raw(key));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Setting " + key + ": \"" + values.get(key)
                        + "\" is not a whole number", e);
            }
        }

        double decimal(String key) {
            try {
                double d = Double.parseDouble(raw(key).replace(',', '.'));
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    throw new NumberFormatException();
                }
                return d;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Setting " + key + ": \"" + values.get(key)
                        + "\" is not a number", e);
            }
        }

        boolean bool(String key) {
            return switch (raw(key).toLowerCase(Locale.ROOT)) {
                case "true", "on", "yes" -> true;
                case "false", "off", "no" -> false;
                default -> throw new IllegalArgumentException("Setting " + key + ": \"" + values.get(key)
                        + "\" is not true or false");
            };
        }

        Mobility.VisionBase visionBase(String key) {
            try {
                return Mobility.VisionBase.valueOf(raw(key).toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("Setting ")) {
                    throw e;
                }
                throw new IllegalArgumentException("Setting " + key + ": \"" + values.get(key)
                        + "\" must be OWN_SPEED or MAX_SPEED", e);
            }
        }
    }
}
