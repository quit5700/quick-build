package cn.quit5700.light.logic;

/** Keep legacy ordinals stable while exposing two modes plus a brightness option. */
public final class VariableLightPresentation {
    private VariableLightPresentation() { }
    public static boolean showsMode(boolean legacy, VariableLightMode mode) {
        return legacy ? !mode.isAreaMode() : mode != VariableLightMode.HYBRID;
    }
    public static VariableLightMode primaryMode(boolean legacy, VariableLightMode saved) {
        return !legacy && saved == VariableLightMode.HYBRID ? VariableLightMode.VANILLA_INVISIBLE : saved;
    }
    public static VariableLightMode toggleBoost(VariableLightMode saved) {
        return saved == VariableLightMode.HYBRID ? VariableLightMode.VANILLA_INVISIBLE : VariableLightMode.HYBRID;
    }
}
