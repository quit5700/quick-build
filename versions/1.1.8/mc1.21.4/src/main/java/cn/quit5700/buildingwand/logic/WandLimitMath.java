package cn.quit5700.buildingwand.logic;

public final class WandLimitMath {
    private WandLimitMath() {}

    public static int change(int value, boolean increase, int minimum) {
        if (!increase) {
            return Math.max(minimum, value / 2);
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(minimum, (long) value * 2L));
    }
}
