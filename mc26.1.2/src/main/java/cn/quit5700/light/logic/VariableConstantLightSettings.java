package cn.quit5700.light.logic;

public final class VariableConstantLightSettings {
    private static final int RADIUS_BUTTON_OFFSET = 0;
    private static final int SPACING_BUTTON_OFFSET = 64;
    private static final int MODE_BUTTON_OFFSET = 71;
    public static final int APPLY_BUTTON_ID = 74;
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

    public static int radiusButtonId(int radius) { return RADIUS_BUTTON_OFFSET + clampRadius(radius) - MIN_RADIUS; }
    public static boolean isRadiusButtonId(int id) { return id >= RADIUS_BUTTON_OFFSET && id < SPACING_BUTTON_OFFSET; }
    public static int radiusFromButtonId(int id) { return clampRadius(MIN_RADIUS + id - RADIUS_BUTTON_OFFSET); }
    public static int spacingButtonId(int spacing) { return SPACING_BUTTON_OFFSET + clampSpacing(spacing) - MIN_SPACING; }
    public static boolean isSpacingButtonId(int id) { return id >= SPACING_BUTTON_OFFSET && id < MODE_BUTTON_OFFSET; }
    public static int spacingFromButtonId(int id) { return clampSpacing(MIN_SPACING + id - SPACING_BUTTON_OFFSET); }
    public static int modeButtonId(VariableLightMode mode) { return MODE_BUTTON_OFFSET + mode.ordinal(); }
    public static boolean isModeButtonId(int id) { return id >= MODE_BUTTON_OFFSET && id < APPLY_BUTTON_ID; }
    public static VariableLightMode modeFromButtonId(int id) { return VariableLightMode.fromOrdinal(id - MODE_BUTTON_OFFSET); }
}
