package cn.quit5700.light.logic;

import java.util.function.IntPredicate;

public final class SensorLineRules {
    private SensorLineRules() {}

    public static Result validateOffset(int dx, int dy, int dz) {
        int axes = (dx == 0 ? 0 : 1) + (dy == 0 ? 0 : 1) + (dz == 0 ? 0 : 1);
        if (axes != 1) {
            return Result.NOT_ALIGNED;
        }
        int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        int airGap = distance - 1;
        if (airGap < 1) {
            return Result.TOO_CLOSE;
        }
        if (airGap > 10) {
            return Result.TOO_FAR;
        }
        return Result.VALID;
    }

    public static Result validatePath(int dx, int dy, int dz, IntPredicate clearAtStep) {
        Result offset = validateOffset(dx, dy, dz);
        if (offset != Result.VALID) return offset;
        int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        for (int step = 1; step < distance; step++) {
            if (!clearAtStep.test(step)) return Result.BLOCKED;
        }
        return Result.VALID;
    }

    public static boolean isClear(boolean air, boolean invisibleLight) {
        return air || invisibleLight;
    }

    public enum Result {
        VALID,
        NOT_ALIGNED,
        TOO_CLOSE,
        TOO_FAR,
        BLOCKED
    }
}
