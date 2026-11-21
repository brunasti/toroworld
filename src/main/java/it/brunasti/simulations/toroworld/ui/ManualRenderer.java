package it.brunasti.simulations.toroworld.ui;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Converts the Markdown documentation to HTML that Swing's {@code JEditorPane} can show.
 * Swing only supports old HTML (3.2) with a little CSS, so table borders are set as
 * HTML attributes and the styling is kept simple.
 */
public final class ManualRenderer {

    private static final String STYLE = """
            body { font-family: SansSerif; font-size: 13pt; margin: 12px 18px; }
            h1 { font-size: 22pt; }
            h2 { font-size: 17pt; margin-top: 16px; }
            h3 { font-size: 14pt; }
            code { font-family: Monospaced; }
            pre { font-family: Monospaced; background-color: #f2f2f2; padding: 6px; }
            blockquote { margin-left: 8px; padding-left: 8px; color: #444444;
                         border-left-width: 3px; border-left-style: solid; border-left-color: #bbbbbb; }
            th { background-color: #e8e8e8; text-align: left; }
            td, th { padding: 3px 6px; }
            """;

    private final Parser parser;
    private final HtmlRenderer renderer;

    public ManualRenderer() {
        List<Extension> extensions = List.of(TablesExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .attributeProviderFactory(context -> (node, tagName, attributes) -> {
                    if (node instanceof TableBlock) {
                        attributes.put("border", "1");
                        attributes.put("cellspacing", "0");
                        attributes.put("cellpadding", "4");
                    }
                })
                .build();
    }

    /** A complete HTML page for the given Markdown text. */
    public String toHtml(String markdown) {
        String body = renderer.render(parser.parse(markdown));
        return "<html><head><style>" + STYLE + "</style></head><body>" + body + "</body></html>";
    }
}
