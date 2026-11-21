package io.github.brunasti.toroworld.ui;

import io.github.brunasti.toroworld.sim.SimulationConfig;
import io.github.brunasti.toroworld.sim.Simulation;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.Serial;

/** Main window: the world view plus run controls and the current census. */
public final class ToroworldFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient SimulationConfig config;
    private transient Simulation simulation;
    private final WorldPanel worldPanel;
    private final JLabel status = new JLabel();
    private final JButton runButton = new JButton("Start");
    private final Timer timer;

    public ToroworldFrame(SimulationConfig config) {
        super("Toroworld");
        this.config = config;
        this.simulation = new Simulation(config);
        this.worldPanel = new WorldPanel(simulation);

        JSlider speed = new JSlider(1, 50, 5);
        speed.setToolTipText("Ticks per second");
        timer = new Timer(1000 / speed.getValue(), e -> step());
        speed.addChangeListener(e -> timer.setDelay(1000 / speed.getValue()));

        JButton stepButton = new JButton("Step");
        JButton resetButton = new JButton("Reset");
        runButton.addActionListener(e -> toggleRun());
        stepButton.addActionListener(e -> step());
        resetButton.addActionListener(e -> reset());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(runButton);
        controls.add(stepButton);
        controls.add(resetButton);
        controls.add(new JLabel("Speed"));
        controls.add(speed);

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
        simulation.step();
        worldPanel.repaint();
        updateStatus();
    }

    private void reset() {
        simulation = new Simulation(config);
        worldPanel.setSimulation(simulation);
        updateStatus();
    }

    private void updateStatus() {
        var c = simulation.census();
        status.setText(String.format(
                "Tick %d   |   Plants %d   Herbivores %d   Carnivores %d   |   Resource %.0f   |   %dx%d, seed %d",
                c.tick(), c.plants(), c.herbivores(), c.carnivores(), c.resource(),
                config.width(), config.height(), config.seed()));
    }
}
