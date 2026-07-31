package cn.quit5700.light.logic;

public final class DaylightCoverage {
    private DaylightCoverage() { }

    public static boolean contains(int centerX, int centerY, int centerZ, int radius,
                                   int x, int y, int z) {
        int clampedRadius = VariableConstantLightSettings.clampRadius(radius);
        return Math.abs(x - centerX) <= clampedRadius
                && Math.abs(y - centerY) <= clampedRadius
                && Math.abs(z - centerZ) <= clampedRadius;
    }
}
