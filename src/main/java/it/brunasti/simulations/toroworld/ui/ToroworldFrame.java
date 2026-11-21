package it.brunasti.simulations.toroworld.ui;

import it.brunasti.simulations.toroworld.settings.RunSettings;
import it.brunasti.simulations.toroworld.settings.SettingsFile;
import it.brunasti.simulations.toroworld.sim.Simulation;
import it.brunasti.simulations.toroworld.sim.SimulationConfig;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;

/**
 * Main window: the world view plus run controls and the current census.
 * A run lasts a fixed number of ticks; when it is reached the run pauses,
 * and only Reset starts again. The Settings button changes the settings of the next run,
 * saves them to the settings file and starts that run.
 */
public final class ToroworldFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient RunSettings settings;
    private final transient Path settingsFile;
    private transient Simulation simulation;
    private final WorldPanel worldPanel;
    private final JLabel status = new JLabel();
    private final JButton runButton = new JButton("Start");
    private final JButton stepButton = new JButton("Step");
    private final Timer timer;
    private transient ManualWindow manual;

    public ToroworldFrame(RunSettings settings, Path settingsFile) {
        super("Toroworld");
        this.settings = settings;
        this.settingsFile = settingsFile;
        this.simulation = new Simulation(settings.config());
        this.worldPanel = new WorldPanel(simulation);

        JSlider speed = new JSlider(1, 50, 5);
        speed.setToolTipText("Ticks per second");
        timer = new Timer(1000 / speed.getValue(), e -> step());
        speed.addChangeListener(e -> timer.setDelay(1000 / speed.getValue()));

        JButton resetButton = new JButton("Reset");
        JButton settingsButton = new JButton("Settings...");
        settingsButton.setToolTipText("Change the settings and start a new run");
        settingsButton.addActionListener(e -> editSettings());
        JButton manualButton = new JButton("Manual");
        manualButton.addActionListener(e -> showManual());
        runButton.addActionListener(e -> toggleRun());
        stepButton.addActionListener(e -> step());
        resetButton.addActionListener(e -> reset());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(runButton);
        controls.add(stepButton);
        controls.add(resetButton);
        controls.add(new JLabel("Speed"));
        controls.add(speed);
        controls.add(settingsButton);
        controls.add(manualButton);

        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.setFont(status.getFont().deriveFont(13f));

        setLayout(new BorderLayout());
        add(controls, BorderLayout.NORTH);
        add(worldPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        updateStatus();
        pack();
        setLocationByPlatform(true);
    }

    private void toggleRun() {
        if (timer.isRunning()) {
            timer.stop();
            runButton.setText("Start");
        } else {
            timer.start();
            runButton.setText("Pause");
        }
    }

    private void step() {
        if (finished()) {
            return;
        }
        simulation.step();
        worldPanel.repaint();
        if (finished()) {
            timer.stop();
            runButton.setText("Start");
        }
        updateStatus();
    }

    private void showManual() {
        if (manual == null) {
            manual = new ManualWindow();
        }
        manual.setVisible(true);
        manual.toFront();
    }

    private void editSettings() {
        boolean wasRunning = timer.isRunning();
        if (wasRunning) {
            toggleRun();
        }
        new SettingsDialog(this, settings, settingsFile).ask().ifPresentOrElse(newSettings -> {
            settings = newSettings;
            try {
                SettingsFile.save(settingsFile, newSettings);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "The new run starts, but the settings could not be saved to "
                        + settingsFile + ":\n" + e.getMessage(), "Settings not saved", JOptionPane.WARNING_MESSAGE);
            }
            reset();
            worldPanel.fitToWorld();
            pack();
        }, () -> {
            if (wasRunning) {
                toggleRun();
            }
        });
    }

    private void reset() {
        simulation = new Simulation(settings.config());
        worldPanel.setSimulation(simulation);
        updateStatus();
    }

    private boolean finished() {
        return simulation.tick() >= settings.ticks();
    }

    private void updateStatus() {
        var c = simulation.census();
        SimulationConfig config = settings.config();
        // Two lines, so the bar fits the width of a small grid.
        status.setText(String.format(
                "<html>Tick %d / %d%s &nbsp;|&nbsp; Plants %d &nbsp; Herbivores %d &nbsp; Carnivores %d"
                        + " &nbsp;|&nbsp; Resource %.0f<br>%dx%d, seed %d, %s</html>",
                c.tick(), settings.ticks(), finished() ? " (end)" : "",
                c.plants(), c.herbivores(), c.carnivores(), c.resource(),
                config.width(), config.height(), config.seed(), mutationLabel()));
        runButton.setEnabled(!finished());
        stepButton.setEnabled(!finished());
    }

    private String mutationLabel() {
        var m = settings.config().mutation();
        if (!m.enabled()) {
            return "mutation off";
        }
        return "mutation on, trade-off " + (m.tradeOff() ? "on" : "off");
    }
}
