package cn.quit5700.light.light;

import java.util.ArrayList;
import java.util.List;

public final class ConstantLightPositions {
    public static final int RADIUS = 14;
    private static final List<Offset> RELATIVE_OFFSETS = createOffsets();

    private ConstantLightPositions() {
    }

    public static List<Offset> relativeOffsets() {
        return RELATIVE_OFFSETS;
    }

    public static boolean containsOffset(int dx, int dy, int dz) {
        return Math.abs(dx) + Math.abs(dy) + Math.abs(dz) <= RADIUS;
    }

    private static List<Offset> createOffsets() {
        List<Offset> offsets = new ArrayList<>();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if ((dx != 0 || dy != 0 || dz != 0) && containsOffset(dx, dy, dz)) {
                        offsets.add(new Offset(dx, dy, dz));
                    }
                }
            }
        }
        return List.copyOf(offsets);
    }

    public record Offset(int dx, int dy, int dz) {
    }
}
