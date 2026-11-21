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
 * Draws the torus as a flat grid. Cell background: the resource left in the cell, from light brown
 * (full) through light grey (low) to medium grey (empty); see {@link #resourceColour(double)}.
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
        fitToWorld();
    }

    /** Sets the preferred size from the grid size: square cells, at most about 800 pixels wide. */
    public void fitToWorld() {
        Torus t = simulation.world().torus();
        int cell = Math.max(2, Math.min(60, 800 / Math.max(t.width(), t.height())));
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

                g.setColor(resourceColour(world.resources().level(p) / maxResource));
                g.fillRect(x0, y0, w, h);
                g.setColor(GRID);
                g.drawRect(x0, y0, w, h);

                world.plantAt(p).ifPresent(plant -> drawPlant(g, plant, x0, y0, w, h));
                world.animalAt(p).ifPresent(animal -> drawAnimal(g, animal, x0, y0, w, h));
            }
        }
    }

    /** Background for an empty cell (no resource left). */
    static final Color EMPTY = new Color(128, 128, 128);
    /** Background for a cell with little resource left (15% of the maximum, 1.5 of 10 by default). */
    static final Color LOW = new Color(200, 200, 200);
    /** Background for a full cell. */
    static final Color FULL = new Color(128, 89, 38);
    /** Fraction of the maximum resource at which the colour is {@link #LOW}. */
    static final double LOW_FRACTION = 0.15;

    /**
     * Background colour for a cell, from the resource as a fraction of the maximum:
     * medium grey when empty, light grey when low (15%), light brown when full,
     * blending linearly in between.
     */
    static Color resourceColour(double fraction) {
        double f = Math.max(0, Math.min(1, fraction));
        if (f <= LOW_FRACTION) {
            return blend(EMPTY, LOW, f / LOW_FRACTION);
        }
        return blend(LOW, FULL, (f - LOW_FRACTION) / (1 - LOW_FRACTION));
    }

    private static Color blend(Color from, Color to, double t) {
        return new Color(
                (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * t),
                (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t),
                (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t));
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
