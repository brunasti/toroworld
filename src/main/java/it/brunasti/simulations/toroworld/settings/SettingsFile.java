package it.brunasti.simulations.toroworld.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Reads and writes the settings file, a plain Java properties file with one line per setting
 * and a comment explaining each one. By default it is {@code ~/.toroworld/settings.properties}.
 */
public final class SettingsFile {

    /** {@code ~/.toroworld/settings.properties}. */
    public static Path defaultPath() {
        return Path.of(System.getProperty("user.home"), ".toroworld", "settings.properties");
    }

    private SettingsFile() {
    }

    /**
     * Reads settings from the file. Settings missing from the file keep their value from
     * {@code base}; a missing file gives {@code base} unchanged.
     *
     * @throws IOException              if the file exists but cannot be read
     * @throws IllegalArgumentException if a value in the file is invalid; the message names it
     */
    public static RunSettings load(Path file, RunSettings base) throws IOException {
        if (!Files.exists(file)) {
            return base;
        }
        Properties properties = new Properties();
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(in);
        }
        Map<String, String> values = new HashMap<>();
        properties.stringPropertyNames().forEach(name -> values.put(name, properties.getProperty(name)));
        return SettingsCodec.fromMap(values, base);
    }

    /** Writes all settings to the file, creating its folder if needed. */
    public static void save(Path file, RunSettings settings) throws IOException {
        Path folder = file.toAbsolutePath().getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        Map<String, String> values = SettingsCodec.toMap(settings);
        try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            out.write("# Toroworld settings, written by the settings window on "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ".\n");
            out.write("# Loaded at every start. Command-line options override these values.\n");
            out.write("# Delete this file to go back to the built-in defaults.\n");
            String tab = "";
            for (Parameter p : SettingsCodec.PARAMETERS) {
                if (!p.tab().equals(tab)) {
                    tab = p.tab();
                    out.write("\n# ---- " + tab + "\n");
                }
                out.write("\n# " + p.label() + ": " + p.description() + "\n");
                out.write(p.key() + "=" + values.get(p.key()) + "\n");
            }
        }
    }
}
