package it.brunasti.simulations.toroworld.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManualTest {

    private final ManualRenderer renderer = new ManualRenderer();

    @Test
    void rendersHeadingsEmphasisAndLists() {
        String html = renderer.toHtml("# Title\n\nSome **bold** text.\n\n- one\n- two\n");
        assertTrue(html.contains("<h1>Title</h1>"));
        assertTrue(html.contains("<strong>bold</strong>"));
        assertTrue(html.contains("<li>one</li>"));
    }

    @Test
    void tablesGetVisibleBorders() {
        String html = renderer.toHtml("| A | B |\n|---|---|\n| 1 | 2 |\n");
        assertTrue(html.contains("<table border=\"1\""), html);
        assertTrue(html.contains("<td>1</td>"));
    }

    @Test
    void theManualAndItsImageAreBundledWithTheApplication() {
        assertNotNull(ManualWindow.class.getResource(ManualWindow.RESOURCE_FOLDER + ManualWindow.HOME),
                "docs/manual.md must be copied into the resources by the build");
        assertNotNull(ManualWindow.class.getResource(ManualWindow.RESOURCE_FOLDER + "images/legend.png"));
    }
}
