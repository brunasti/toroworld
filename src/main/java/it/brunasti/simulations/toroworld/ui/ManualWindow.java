package it.brunasti.simulations.toroworld.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Shows the user manual. The Markdown documents in {@code docs/} are copied into the
 * application at build time (see {@code build.gradle.kts}), so the window shows the
 * same text as the repository. Links to other bundled documents open in this window;
 * web links open in the system browser.
 */
public final class ManualWindow extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Folder on the classpath holding the bundled documents. */
    static final String RESOURCE_FOLDER = "/manual/";
    static final String HOME = "manual.md";

    private final transient ManualRenderer renderer = new ManualRenderer();
    private final JEditorPane pane = new JEditorPane();

    public ManualWindow() {
        super("Toroworld manual");
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.addHyperlinkListener(this::followLink);

        JButton home = new JButton("Manual home");
        home.addActionListener(e -> show(HOME));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(home);

        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        setLayout(new BorderLayout());
        add(bar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        setPreferredSize(new Dimension(820, 760));
        pack();
        setLocationByPlatform(true);
        show(HOME);
    }

    /** Shows a bundled document by file name, e.g. {@code manual.md}. */
    public void show(String document) {
        URL url = ManualWindow.class.getResource(RESOURCE_FOLDER + document);
        if (url == null) {
            pane.setText("<html><body><h2>Document not found</h2><p>" + document
                    + " is not bundled with the application. Build with Gradle"
                    + " (<code>./gradlew run</code>) so that the documents in docs/ are included.</p>"
                    + "</body></html>");
            return;
        }
        try (InputStream in = url.openStream()) {
            String html = renderer.toHtml(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            pane.setText(html);
            // Relative links and images (e.g. images/legend.png) resolve against the document.
            ((HTMLDocument) pane.getDocument()).setBase(url);
            SwingUtilities.invokeLater(() -> pane.scrollRectToVisible(new java.awt.Rectangle(0, 0, 1, 1)));
        } catch (IOException e) {
            pane.setText("<html><body><h2>Cannot read " + document + "</h2><p>" + e.getMessage()
                    + "</p></body></html>");
        }
    }

    private void followLink(HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }
        String target = event.getDescription();
        if (target.startsWith("http://") || target.startsWith("https://")) {
            openInBrowser(target);
        } else if (target.endsWith(".md")) {
            show(target.substring(target.lastIndexOf('/') + 1));
        }
    }

    private static void openInBrowser(String address) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(address));
            } catch (IOException | URISyntaxException ignored) {
                // Nothing sensible to do: the link simply does not open.
            }
        }
    }
}
