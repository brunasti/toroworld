package it.brunasti.simulations.toroworld;

import it.brunasti.simulations.toroworld.life.Species;
import it.brunasti.simulations.toroworld.life.Trait;
import it.brunasti.simulations.toroworld.settings.RunSettings;
import it.brunasti.simulations.toroworld.settings.SettingsFile;
import it.brunasti.simulations.toroworld.sim.Census;
import it.brunasti.simulations.toroworld.sim.Simulation;
import it.brunasti.simulations.toroworld.sim.SimulationConfig;
import it.brunasti.simulations.toroworld.sim.SpeciesConfig;
import it.brunasti.simulations.toroworld.ui.ToroworldFrame;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Entry point.
 * <pre>
 *   toroworld [--settings FILE | --no-settings] [--width N] [--height N] [--seed N]
 *             [--mutation on|off] [--trade-off on|off] [--carnivore-max-speed N]
 *             [--headless] [--ticks N]
 * </pre>
 * Settings come, in increasing priority, from the built-in defaults, the settings file
 * ({@code ~/.toroworld/settings.properties}, or {@code --settings FILE}; ignored with
 * {@code --no-settings}) and the command-line options.
 * Without {@code --headless} the Swing viewer opens and pauses at the last tick; its Settings
 * button edits the settings, saves them to the settings file and starts a new run.
 * With {@code --headless}, the simulation prints one census line per tick and, at the end,
 * the mean traits of each surviving species.
 */
public final class Toroworld {

    private static final String USAGE = "Usage: toroworld [--settings FILE | --no-settings]"
            + " [--width N] [--height N] [--seed N] [--mutation on|off] [--trade-off on|off]"
            + " [--carnivore-max-speed N] [--headless] [--ticks N]";

    private Toroworld() {
    }

    public static void main(String[] args) {
        // First pass: which settings file, if any.
        Path settingsFile = SettingsFile.defaultPath();
        boolean useFile = true;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--settings")) {
                settingsFile = Path.of(arg(args, ++i));
            } else if (args[i].equals("--no-settings")) {
                useFile = false;
            } else if (args[i].equals("--help") || args[i].equals("-h")) {
                System.out.println(USAGE);
                System.out.println("Settings file: " + settingsFile.toAbsolutePath());
                return;
            }
        }
        RunSettings settings = RunSettings.defaults();
        if (useFile) {
            try {
                settings = SettingsFile.load(settingsFile, settings);
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Ignoring the settings file " + settingsFile + ": " + e.getMessage());
            }
        }

        // Second pass: command-line options override the file.
        SimulationConfig config = settings.config();
        int ticks = settings.ticks();
        boolean headless = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--settings" -> i++;
                case "--no-settings" -> { }
                case "--width" -> config = config.withSize(intArg(args, ++i), config.height());
                case "--height" -> config = config.withSize(config.width(), intArg(args, ++i));
                case "--seed" -> config = config.withSeed(Long.parseLong(arg(args, ++i)));
                case "--mutation" -> config = config.withMutation(
                        config.mutation().withEnabled(onOff(args, ++i)));
                case "--trade-off" -> config = config.withMutation(
                        config.mutation().withTradeOff(onOff(args, ++i)));
                case "--carnivore-max-speed" -> {
                    SpeciesConfig carnivores = config.carnivores();
                    config = config.withCarnivores(carnivores.withMobility(
                            carnivores.mobility().withMaxSpeed(Double.parseDouble(arg(args, ++i)))));
                }
                case "--ticks" -> ticks = intArg(args, ++i);
                case "--headless" -> headless = true;
                default -> throw new IllegalArgumentException("Unknown option: " + args[i] + "\n" + USAGE);
            }
        }

        RunSettings finalSettings = new RunSettings(config, ticks);
        if (headless) {
            runHeadless(finalSettings.config(), finalSettings.ticks());
        } else {
            Path file = settingsFile;
            SwingUtilities.invokeLater(() -> new ToroworldFrame(finalSettings, file).setVisible(true));
        }
    }

    private static void runHeadless(SimulationConfig config, int ticks) {
        Simulation simulation = new Simulation(config);
        System.out.println(simulation.census());
        for (int t = 0; t < ticks; t++) {
            Census c = simulation.step();
            System.out.println(c);
            if (c.plants() + c.herbivores() + c.carnivores() == 0) {
                System.out.println("All life has ended.");
                break;
            }
        }
        for (Species species : Species.values()) {
            Map<Trait, Double> means = simulation.meanTraits(species);
            if (!means.isEmpty()) {
                StringBuilder line = new StringBuilder(String.format("mean traits %-9s", species));
                means.forEach((trait, value) -> line.append(String.format("  %s %.3f", trait, value)));
                System.out.println(line);
            }
        }
    }

    private static boolean onOff(String[] args, int i) {
        return switch (arg(args, i)) {
            case "on" -> true;
            case "off" -> false;
            default -> throw new IllegalArgumentException("Expected on or off after " + args[i - 1]);
        };
    }

    private static String arg(String[] args, int i) {
        if (i >= args.length) {
            throw new IllegalArgumentException("Missing value after " + args[i - 1]);
        }
        return args[i];
    }

    private static int intArg(String[] args, int i) {
        return Integer.parseInt(arg(args, i));
    }
}
