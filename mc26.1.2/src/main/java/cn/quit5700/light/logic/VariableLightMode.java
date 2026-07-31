package cn.quit5700.light.logic;

public enum VariableLightMode {
    VANILLA_INVISIBLE(true, false),
    LOCAL_DAYLIGHT(false, true),
    HYBRID(true, true);

    private final boolean invisibleLight;
    private final boolean daylightRendering;

    VariableLightMode(boolean invisibleLight, boolean daylightRendering) {
        this.invisibleLight = invisibleLight;
        this.daylightRendering = daylightRendering;
    }

    public boolean usesInvisibleLight() { return invisibleLight; }
    public boolean usesDaylightRendering() { return daylightRendering; }

    public static VariableLightMode fromOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : VANILLA_INVISIBLE;
    }
}
