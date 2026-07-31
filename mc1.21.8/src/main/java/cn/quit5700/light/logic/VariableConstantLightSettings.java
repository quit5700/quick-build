package cn.quit5700.light.logic;

public final class VariableConstantLightSettings {
    private static final int CONFIGURATION_BUTTON_OFFSET = 1_000;
    public static final int MIN_RADIUS = 1;
    public static final int DEFAULT_RADIUS = 14;
    public static final int MAX_RADIUS = 64;
    public static final int MIN_SPACING = 1;
    public static final int DEFAULT_SPACING = 1;
    public static final int MAX_SPACING = 7;

    private VariableConstantLightSettings() {
    }

    public static int clampRadius(int radius) {
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    }

    public static int sideLength(int radius) {
        return clampRadius(radius) * 2 + 1;
    }

    public static double sliderFromRadius(int radius) {
        return (clampRadius(radius) - MIN_RADIUS) / (double) (MAX_RADIUS - MIN_RADIUS);
    }

    public static int radiusFromSlider(double sliderValue) {
        double clamped = Math.max(0.0D, Math.min(1.0D, sliderValue));
        return MIN_RADIUS + (int) Math.round(clamped * (MAX_RADIUS - MIN_RADIUS));
    }

    public static int clampSpacing(int spacing) {
        return Math.max(MIN_SPACING, Math.min(MAX_SPACING, spacing));
    }

    public static double sliderFromSpacing(int spacing) {
        return (clampSpacing(spacing) - MIN_SPACING) / (double) (MAX_SPACING - MIN_SPACING);
    }

    public static int spacingFromSlider(double sliderValue) {
        double clamped = Math.max(0.0D, Math.min(1.0D, sliderValue));
        return MIN_SPACING + (int) Math.round(clamped * (MAX_SPACING - MIN_SPACING));
    }

    public static int configurationButtonId(int radius, int spacing, VariableLightMode mode) {
        int radiusIndex = clampRadius(radius) - MIN_RADIUS;
        int spacingIndex = clampSpacing(spacing) - MIN_SPACING;
        return CONFIGURATION_BUTTON_OFFSET
                + (radiusIndex * (MAX_SPACING - MIN_SPACING + 1) + spacingIndex)
                * VariableLightMode.values().length
                + mode.ordinal();
    }

    public static boolean isConfigurationButtonId(int id) {
        return id >= configurationButtonId(MIN_RADIUS, MIN_SPACING, VariableLightMode.VANILLA_INVISIBLE)
                && id <= configurationButtonId(MAX_RADIUS, MAX_SPACING, VariableLightMode.HYBRID);
    }

    public static int radiusFromConfigurationButtonId(int id) {
        int index = Math.max(0, id - CONFIGURATION_BUTTON_OFFSET);
        int configurationIndex = index / VariableLightMode.values().length;
        return clampRadius(MIN_RADIUS + configurationIndex / (MAX_SPACING - MIN_SPACING + 1));
    }

    public static int spacingFromConfigurationButtonId(int id) {
        int index = Math.max(0, id - CONFIGURATION_BUTTON_OFFSET);
        int configurationIndex = index / VariableLightMode.values().length;
        return clampSpacing(MIN_SPACING + configurationIndex % (MAX_SPACING - MIN_SPACING + 1));
    }

    public static VariableLightMode modeFromConfigurationButtonId(int id) {
        int index = Math.max(0, id - CONFIGURATION_BUTTON_OFFSET);
        return VariableLightMode.fromOrdinal(index % VariableLightMode.values().length);
    }
}
