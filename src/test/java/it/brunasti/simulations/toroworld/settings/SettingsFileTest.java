package it.brunasti.simulations.toroworld.settings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsFileTest {

    @TempDir
    Path folder;

    @Test
    void savedSettingsAreLoadedBack() throws IOException {
        Path file = folder.resolve("sub/settings.properties");
        RunSettings changed = RunSettings.defaults()
                .withConfig(RunSettings.defaults().config().withSize(33, 22).withSeed(9))
                .withTicks(77);
        SettingsFile.save(file, changed);

        assertEquals(changed, SettingsFile.load(file, RunSettings.defaults()));
        assertTrue(Files.readString(file).contains("# Width: Grid width, in cells"));
    }

    @Test
    void missingFileGivesTheBase() throws IOException {
        RunSettings base = RunSettings.defaults();
        assertSame(base, SettingsFile.load(folder.resolve("none.properties"), base));
    }

    @Test
    void partialFileOnlyChangesWhatItContains() throws IOException {
        Path file = folder.resolve("partial.properties");
        Files.writeString(file, "# hand written\nworld.seed=123\n");
        RunSettings loaded = SettingsFile.load(file, RunSettings.defaults());
        assertEquals(123, loaded.config().seed());
        assertEquals(RunSettings.defaults().config().width(), loaded.config().width());
    }

    @Test
    void invalidValueIsReported() throws IOException {
        Path file = folder.resolve("bad.properties");
        Files.writeString(file, "world.width=wide\n");
        assertThrows(IllegalArgumentException.class, () -> SettingsFile.load(file, RunSettings.defaults()));
    }
}
