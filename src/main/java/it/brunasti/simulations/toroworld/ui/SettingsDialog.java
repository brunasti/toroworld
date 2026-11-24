package it.brunasti.simulations.toroworld.ui;

import it.brunasti.simulations.toroworld.settings.Parameter;
import it.brunasti.simulations.toroworld.settings.RunSettings;
import it.brunasti.simulations.toroworld.settings.SettingsCodec;
import it.brunasti.simulations.toroworld.sim.Mobility;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serial;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Modal window to edit every setting of the next run, one tab per group
 * (World, Plants, Herbivores, Carnivores, Mutation). The fields are generated from
 * {@link SettingsCodec#PARAMETERS}, so a new setting only needs to be described there.
 */
public final class SettingsDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Map<String, JComponent> fields = new LinkedHashMap<>();
    private final transient RunSettings base;
    private transient RunSettings result;

    public SettingsDialog(JFrame owner, RunSettings current, Path settingsFile) {
        super(owner, "Settings for the next run", true);
        this.base = current;

        JTabbedPane tabs = new JTabbedPane();
        String tab = null;
        JPanel panel = null;
        int row = 0;
        String section = null;
        for (Parameter p : SettingsCodec.PARAMETERS) {
            if (!p.tab().equals(tab)) {
                tab = p.tab();
                panel = new JPanel(new GridBagLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                JScrollPane scroll = new JScrollPane(panel);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                tabs.addTab(tab, scroll);
                row = 0;
                section = null;
            }
            if (!p.section().equals(section)) {
                section = p.section();
                JLabel heading = new JLabel(section);
                heading.setFont(heading.getFont().deriveFont(Font.BOLD));
                panel.add(heading, constraints(0, row++, 3, row == 1 ? 0 : 12));
            }
            JComponent field = field(p);
            field.setToolTipText(p.description());
            fields.put(p.key(), field);
            JLabel label = new JLabel(p.label());
            label.setToolTipText(p.description());
            panel.add(label, constraints(0, row, 1, 2));
            panel.add(field, constraints(1, row, 1, 2));
            JLabel help = new JLabel(p.description());
            help.setForeground(Color.GRAY);
            panel.add(help, constraints(2, row, 1, 2));
            row++;
        }
        for (int i = 0; i < tabs.getTabCount(); i++) {
            JPanel content = (JPanel) ((JScrollPane) tabs.getComponentAt(i)).getViewport().getView();
            GridBagConstraints filler = constraints(0, 1000, 3, 0);
            filler.weighty = 1;
            content.add(new JLabel(), filler);
        }
        fill(current);

        JButton defaults = new JButton("Restore defaults");
        defaults.setToolTipText("Fill in the built-in defaults; nothing changes until you start a new run");
        defaults.addActionListener(e -> fill(RunSettings.defaults()));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton start = new JButton("Save and start new run");
        start.addActionListener(e -> accept());
        getRootPane().setDefaultButton(start);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(defaults);
        buttons.add(cancel);
        buttons.add(start);
        JLabel where = new JLabel(" Saved to " + settingsFile.toAbsolutePath());
        where.setForeground(Color.GRAY);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(where, BorderLayout.WEST);
        bottom.add(buttons, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        pack();
        setSize(Math.max(getWidth(), 820), Math.min(Math.max(getHeight(), 520), 760));
        setLocationRelativeTo(owner);
    }

    /** Shows the window and returns the new settings, or empty if the user cancelled. */
    public Optional<RunSettings> ask() {
        setVisible(true);
        return Optional.ofNullable(result);
    }

    private static JComponent field(Parameter p) {
        return switch (p.kind()) {
            case BOOLEAN -> new JCheckBox();
            case VISION_BASE -> new JComboBox<>(new String[] {
                    Mobility.VisionBase.OWN_SPEED.name(), Mobility.VisionBase.MAX_SPEED.name()});
            case INTEGER, LONG, DECIMAL -> new JTextField(10);
        };
    }

    private void fill(RunSettings settings) {
        Map<String, String> values = SettingsCodec.toMap(settings);
        fields.forEach((key, field) -> {
            String value = values.get(key);
            if (field instanceof JCheckBox box) {
                box.setSelected(Boolean.parseBoolean(value));
            } else if (field instanceof JComboBox<?> combo) {
                combo.setSelectedItem(value);
            } else if (field instanceof JTextField text) {
                text.setText(value);
            }
        });
    }

    private Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        fields.forEach((key, field) -> {
            if (field instanceof JCheckBox box) {
                values.put(key, Boolean.toString(box.isSelected()));
            } else if (field instanceof JComboBox<?> combo) {
                values.put(key, String.valueOf(combo.getSelectedItem()));
            } else if (field instanceof JTextField text) {
                values.put(key, text.getText());
            }
        });
        return values;
    }

    private void accept() {
        try {
            result = SettingsCodec.fromMap(values(), base);
            dispose();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid settings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static GridBagConstraints constraints(int x, int y, int width, int top) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(top, 0, 2, 12);
        c.weightx = x == 2 ? 1 : 0;
        return c;
    }
}
