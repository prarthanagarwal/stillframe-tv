package com.gathin.tvhome;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class DockSelectionTest {
    @Test public void validatesAndPreservesOwnerOrder() {
        assertEquals(Arrays.asList("com.stremio.one", "com.spotify.tv.android"), DockSelection.parse("com.stremio.one, bad;command,com.spotify.tv.android,com.stremio.one"));
    }
    @Test public void boundsDockAndHandlesMissingChoices() {
        assertTrue(DockSelection.parse(null).isEmpty());
        assertEquals(7, DockSelection.parse("com.a,com.b,com.c,com.d,com.e,com.f,com.g,com.h").size());
    }
}
