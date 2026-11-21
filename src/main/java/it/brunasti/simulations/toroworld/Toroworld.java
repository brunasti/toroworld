package it.brunasti.simulations.toroworld;

import it.brunasti.simulations.toroworld.life.Species;
import it.brunasti.simulations.toroworld.life.Trait;
import it.brunasti.simulations.toroworld.sim.Census;
import it.brunasti.simulations.toroworld.sim.Simulation;
import it.brunasti.simulations.toroworld.sim.SimulationConfig;
import it.brunasti.simulations.toroworld.ui.ToroworldFrame;

import javax.swing.SwingUtilities;
import java.util.Map;

/**
 * Entry point.
 * <pre>
 *   toroworld [--width N] [--height N] [--seed N] [--mutation on|off] [--trade-off on|off]
 *             [--headless] [--ticks N]
 * </pre>
 * A run lasts {@code --ticks} ticks (default {@value #DEFAULT_TICKS}).
 * Without {@code --headless} the Swing viewer opens and pauses at the last tick.
 * With it, the simulation prints one census line per tick and, at the end,
 * the mean traits of each surviving species.
 */
public final class Toroworld {

    /** Length of a run, in ticks, when {@code --ticks} is not given. */
    public static final int DEFAULT_TICKS = 1000;

    private Toroworld() {
    }

    public static void main(String[] args) {
        SimulationConfig config = SimulationConfig.defaults();
        boolean headless = false;
        int ticks = DEFAULT_TICKS;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--width" -> config = config.withSize(intArg(args, ++i), config.height());
                case "--height" -> config = config.withSize(config.width(), intArg(args, ++i));
                case "--seed" -> config = config.withSeed(Long.parseLong(arg(args, ++i)));
                case "--mutation" -> config = config.withMutation(
                        config.mutation().withEnabled(onOff(args, ++i)));
                case "--trade-off" -> config = config.withMutation(
                        config.mutation().withTradeOff(onOff(args, ++i)));
                case "--ticks" -> ticks = intArg(args, ++i);
                case "--headless" -> headless = true;
                case "--help", "-h" -> {
                    System.out.println("Usage: toroworld [--width N] [--height N] [--seed N]"
                            + " [--mutation on|off] [--trade-off on|off] [--headless] [--ticks N]");
                    return;
                }
                default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
            }
        }

        if (ticks < 1) {
            throw new IllegalArgumentException("--ticks must be at least 1");
        }
        if (headless) {
            runHeadless(config, ticks);
        } else {
            SimulationConfig finalConfig = config;
            int finalTicks = ticks;
            SwingUtilities.invokeLater(() -> new ToroworldFrame(finalConfig, finalTicks).setVisible(true));
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
