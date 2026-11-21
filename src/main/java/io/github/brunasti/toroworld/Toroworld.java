package io.github.brunasti.toroworld;

import io.github.brunasti.toroworld.sim.Census;
import io.github.brunasti.toroworld.sim.Simulation;
import io.github.brunasti.toroworld.sim.SimulationConfig;
import io.github.brunasti.toroworld.ui.ToroworldFrame;

import javax.swing.SwingUtilities;

/**
 * Entry point.
 * <pre>
 *   toroworld [--width N] [--height N] [--seed N] [--headless] [--ticks N]
 * </pre>
 * Without {@code --headless} the Swing viewer opens. With it, the simulation runs
 * for {@code --ticks} ticks (default 100) and prints one census line per tick.
 */
public final class Toroworld {

    private Toroworld() {
    }

    public static void main(String[] args) {
        SimulationConfig config = SimulationConfig.defaults();
        boolean headless = false;
        int ticks = 100;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--width" -> config = config.withSize(intArg(args, ++i), config.height());
                case "--height" -> config = config.withSize(config.width(), intArg(args, ++i));
                case "--seed" -> config = config.withSeed(Long.parseLong(arg(args, ++i)));
                case "--ticks" -> ticks = intArg(args, ++i);
                case "--headless" -> headless = true;
                case "--help", "-h" -> {
                    System.out.println("Usage: toroworld [--width N] [--height N] [--seed N] [--headless] [--ticks N]");
                    return;
                }
                default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
            }
        }

        if (headless) {
            runHeadless(config, ticks);
        } else {
            SimulationConfig finalConfig = config;
            SwingUtilities.invokeLater(() -> new ToroworldFrame(finalConfig).setVisible(true));
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
