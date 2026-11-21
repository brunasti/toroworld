package it.brunasti.simulations.toroworld.ui;

import it.brunasti.simulations.toroworld.life.Animal;
import it.brunasti.simulations.toroworld.life.Plant;
import it.brunasti.simulations.toroworld.life.Species;
import it.brunasti.simulations.toroworld.sim.Simulation;
import it.brunasti.simulations.toroworld.sim.World;
import it.brunasti.simulations.toroworld.world.Position;
import it.brunasti.simulations.toroworld.world.Torus;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;

/**
 * Draws the torus as a flat grid. Cell background: darker brown means less resource.
 * Plants are green squares (brighter = more energy), herbivores blue circles,
 * carnivores red circles.
 */
public final class WorldPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Color GRID = new Color(60, 50, 40);
    private static final Color HERBIVORE = new Color(40, 110, 230);
    private static final Color CARNIVORE = new Color(220, 50, 40);

    private transient Simulation simulation;

    public WorldPanel(Simulation simulation) {
        this.simulation = simulation;
        setBackground(Color.BLACK);
        Torus t = simulation.world().torus();
        int cell = Math.max(8, Math.min(60, 600 / Math.max(t.width(), t.height())));
        setPreferredSize(new Dimension(t.width() * cell, t.height() * cell));
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        World world = simulation.world();
        Torus torus = world.torus();
        double cw = (double) getWidth() / torus.width();
        double ch = (double) getHeight() / torus.height();
        double maxResource = Math.max(world.resources().maximum(), 1e-9);

        for (int y = 0; y < torus.height(); y++) {
            for (int x = 0; x < torus.width(); x++) {
                Position p = new Position(x, y);
                int x0 = (int) Math.round(x * cw);
                int y0 = (int) Math.round(y * ch);
                int w = (int) Math.round((x + 1) * cw) - x0;
                int h = (int) Math.round((y + 1) * ch) - y0;

                float r = (float) Math.min(1.0, world.resources().level(p) / maxResource);
                g.setColor(new Color(0.15f + 0.35f * r, 0.10f + 0.25f * r, 0.05f + 0.10f * r));
                g.fillRect(x0, y0, w, h);
                g.setColor(GRID);
                g.drawRect(x0, y0, w, h);

                world.plantAt(p).ifPresent(plant -> drawPlant(g, plant, x0, y0, w, h));
                world.animalAt(p).ifPresent(animal -> drawAnimal(g, animal, x0, y0, w, h));
            }
        }
    }

    private void drawPlant(Graphics2D g, Plant plant, int x0, int y0, int w, int h) {
        double threshold = plant.genome().reproductionThreshold();
        float vigour = (float) Math.min(1.0, plant.energy() / threshold);
        g.setColor(new Color(0.1f, 0.35f + 0.6f * vigour, 0.1f));
        int inset = Math.max(1, Math.min(w, h) / 8);
        g.fillRect(x0 + inset, y0 + inset, w - 2 * inset, h - 2 * inset);
    }

    private void drawAnimal(Graphics2D g, Animal animal, int x0, int y0, int w, int h) {
        g.setColor(animal.species() == Species.HERBIVORE ? HERBIVORE : CARNIVORE);
        int d = (int) (Math.min(w, h) * 0.55);
        g.fillOval(x0 + (w - d) / 2, y0 + (h - d) / 2, d, d);
        g.setColor(Color.WHITE);
        g.drawOval(x0 + (w - d) / 2, y0 + (h - d) / 2, d, d);
    }
}
