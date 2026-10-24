package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DockOpacityTest {
    @Test public void aChoiceMadeOnTheTvWinsOverTheTheme() {
        // Theme says 80, the owner picked 30 in Launcher settings.
        assertEquals(30, Config.chosenDockOpacity(30, 80));
        assertEquals(100, Config.chosenDockOpacity(100, 80));
    }

    @Test public void noChoiceKeepsTheThemeValue() {
        // -1 is what Config passes when nothing is saved (or a theme push cleared it).
        assertEquals(80, Config.chosenDockOpacity(-1, 80));
    }

    @Test public void sliderSupportsTransparentAndIntermediateGlass() {
        assertEquals(0, Config.chosenDockOpacity(0, 80));
        assertEquals(29, Config.chosenDockOpacity(29, 80));
        assertEquals(56, Config.chosenDockOpacity(56, 80));
    }

    @Test public void anOutOfRangeChoiceIsIgnored() {
        assertEquals(80, Config.chosenDockOpacity(-20, 80));
        assertEquals(80, Config.chosenDockOpacity(101, 80));
    }
}
