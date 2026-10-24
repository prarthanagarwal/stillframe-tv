package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class DockLayoutTest {
    @Test public void dockThicknessDoesNotChangeGridAspect() {
        assertEquals(101, DockLayout.height(180, 100));
        assertEquals(78, DockLayout.height(180, 77));
        assertEquals(40, DockLayout.height(180, -50));
        assertEquals(141, DockLayout.height(180, 200));
        assertEquals(101, AppAdapter.tileHeightPx(180));
    }
    @Test public void clampsDockToItsAvailableVerticalRange() {
        assertEquals(600, DockLayout.position(600, 40, 0));
        assertEquals(320, DockLayout.position(600, 40, 50));
        assertEquals(40, DockLayout.position(600, 40, 100));
        assertEquals(40, DockLayout.position(600, 40, 500));
        assertEquals(10, DockLayout.position(10, 40, 100));
    }
}
