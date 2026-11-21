package it.brunasti.simulations.toroworld.ui;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldPanelTest {

    @Test
    void emptyCellIsMediumGreyLowCellLightGreyFullCellBrown() {
        assertEquals(new Color(128, 128, 128), WorldPanel.resourceColour(0.0));
        assertEquals(new Color(200, 200, 200), WorldPanel.resourceColour(0.15));
        assertEquals(new Color(128, 89, 38), WorldPanel.resourceColour(1.0));
    }

    @Test
    void valuesOutsideTheRangeAreClamped() {
        assertEquals(WorldPanel.resourceColour(0.0), WorldPanel.resourceColour(-1));
        assertEquals(WorldPanel.resourceColour(1.0), WorldPanel.resourceColour(2));
    }
}
