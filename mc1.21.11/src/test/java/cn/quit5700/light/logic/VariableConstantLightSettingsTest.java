package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableConstantLightSettingsTest {
    @Test
    void radiusIsClampedToSupportedRange() {
        assertEquals(1, VariableConstantLightSettings.clampRadius(Integer.MIN_VALUE));
        assertEquals(1, VariableConstantLightSettings.clampRadius(0));
        assertEquals(14, VariableConstantLightSettings.clampRadius(14));
        assertEquals(64, VariableConstantLightSettings.clampRadius(65));
        assertEquals(64, VariableConstantLightSettings.clampRadius(Integer.MAX_VALUE));
    }

    @Test
    void sideLengthIsDerivedFromOneEqualAxisRadius() {
        assertEquals(3, VariableConstantLightSettings.sideLength(1));
        assertEquals(29, VariableConstantLightSettings.sideLength(14));
        assertEquals(129, VariableConstantLightSettings.sideLength(64));
    }

    @Test
    void sliderRoundTripsEveryIntegerRadius() {
        for (int radius = VariableConstantLightSettings.MIN_RADIUS;
             radius <= VariableConstantLightSettings.MAX_RADIUS;
             radius++) {
            assertEquals(radius,
                    VariableConstantLightSettings.radiusFromSlider(
                            VariableConstantLightSettings.sliderFromRadius(radius)));
        }
    }

    @Test
    void spacingIsClampedFromOneToSevenAndDefaultsToOne() {
        assertEquals(1, VariableConstantLightSettings.DEFAULT_SPACING);
        assertEquals(1, VariableConstantLightSettings.clampSpacing(Integer.MIN_VALUE));
        assertEquals(1, VariableConstantLightSettings.clampSpacing(0));
        assertEquals(4, VariableConstantLightSettings.clampSpacing(4));
        assertEquals(7, VariableConstantLightSettings.clampSpacing(8));
        assertEquals(7, VariableConstantLightSettings.clampSpacing(Integer.MAX_VALUE));
    }

    @Test
    void sliderRoundTripsEveryIntegerSpacing() {
        for (int spacing = VariableConstantLightSettings.MIN_SPACING;
             spacing <= VariableConstantLightSettings.MAX_SPACING;
             spacing++) {
            assertEquals(spacing,
                    VariableConstantLightSettings.spacingFromSlider(
                            VariableConstantLightSettings.sliderFromSpacing(spacing)));
        }
    }

    @Test
    void radiusAndSpacingRoundTripThroughOneConfigurationButtonId() {
        for (int radius = VariableConstantLightSettings.MIN_RADIUS;
             radius <= VariableConstantLightSettings.MAX_RADIUS;
             radius++) {
            for (int spacing = VariableConstantLightSettings.MIN_SPACING;
                 spacing <= VariableConstantLightSettings.MAX_SPACING;
                 spacing++) {
                for (VariableLightMode mode : VariableLightMode.values()) {
                    int id = VariableConstantLightSettings.configurationButtonId(radius, spacing, mode);
                    assertEquals(radius, VariableConstantLightSettings.radiusFromConfigurationButtonId(id));
                    assertEquals(spacing, VariableConstantLightSettings.spacingFromConfigurationButtonId(id));
                    assertEquals(mode, VariableConstantLightSettings.modeFromConfigurationButtonId(id));
                }
            }
        }
    }
}
