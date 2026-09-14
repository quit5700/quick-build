package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableLightModeTest {
    @Test
    void modesSelectTheExpectedServerAndClientLighting() {
        assertTrue(VariableLightMode.VANILLA_INVISIBLE.usesInvisibleLight());
        assertFalse(VariableLightMode.VANILLA_INVISIBLE.usesDaylightRendering());
        assertFalse(VariableLightMode.LOCAL_DAYLIGHT.usesInvisibleLight());
        assertTrue(VariableLightMode.LOCAL_DAYLIGHT.usesDaylightRendering());
        assertTrue(VariableLightMode.HYBRID.usesInvisibleLight());
        assertTrue(VariableLightMode.HYBRID.usesDaylightRendering());
        assertFalse(VariableLightMode.AREA_FULL_BRIGHT.usesInvisibleLight());
        assertTrue(VariableLightMode.AREA_FULL_BRIGHT.usesDaylightRendering());
        assertTrue(VariableLightMode.AREA_REAL_LIGHT.usesInvisibleLight());
        assertFalse(VariableLightMode.AREA_REAL_LIGHT.usesDaylightRendering());
        assertFalse(VariableLightMode.AREA_REAL_LIGHT.usesRadiusLight());
    }

    @Test
    void localDaylightHasAnImmediateCubeBoundary() {
        assertTrue(DaylightCoverage.contains(10, 20, 30, 4, 14, 24, 34));
        assertFalse(DaylightCoverage.contains(10, 20, 30, 4, 15, 24, 34));
        assertFalse(DaylightCoverage.contains(10, 20, 30, 4, 14, 25, 34));
        assertFalse(DaylightCoverage.contains(10, 20, 30, 4, 14, 24, 35));
    }
}
