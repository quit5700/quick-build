package cn.quit5700.light.logic;

import java.util.List;

/** New lamp choices are independent of persisted enum ordinals and legacy lamps. */
public final class AdjustableLightPolicy {
    private AdjustableLightPolicy() { }
    public static List<VariableLightMode> modes(boolean legacy) {
        return legacy ? List.of(VariableLightMode.VANILLA_INVISIBLE, VariableLightMode.LOCAL_DAYLIGHT,
                VariableLightMode.HYBRID)
                : List.of(VariableLightMode.AREA_FULL_BRIGHT, VariableLightMode.AREA_REAL_LIGHT);
    }
    public static VariableLightMode savedMode(boolean legacy, VariableLightMode saved) {
        return modes(legacy).contains(saved) ? saved
                : legacy ? VariableLightMode.VANILLA_INVISIBLE : VariableLightMode.AREA_FULL_BRIGHT;
    }
}
