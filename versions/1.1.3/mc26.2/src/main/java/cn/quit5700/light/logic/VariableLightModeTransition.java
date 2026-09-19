package cn.quit5700.light.logic;

public final class VariableLightModeTransition {
    private VariableLightModeTransition() { }

    public static boolean needsDaylightSync(VariableLightMode oldMode, VariableLightMode newMode) {
        return oldMode.usesDaylightRendering() || newMode.usesDaylightRendering();
    }
}
