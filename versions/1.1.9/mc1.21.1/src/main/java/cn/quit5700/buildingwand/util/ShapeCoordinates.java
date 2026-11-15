package cn.quit5700.buildingwand.util;

import java.util.ArrayList;
import java.util.List;

public final class ShapeCoordinates {
    private ShapeCoordinates() {
    }

    public static List<ShapeCoordinate> between(
            int firstX,
            int firstY,
            int firstZ,
            int secondX,
            int secondY,
            int secondZ,
            ShapeMode mode
    ) {
        int minX = Math.min(firstX, secondX);
        int minY = Math.min(firstY, secondY);
        int minZ = Math.min(firstZ, secondZ);
        int maxX = Math.max(firstX, secondX);
        int maxY = Math.max(firstY, secondY);
        int maxZ = Math.max(firstZ, secondZ);

        if (mode == ShapeMode.PLANE) {
            int dx = maxX - minX;
            int dy = maxY - minY;
            int dz = maxZ - minZ;
            if (dx <= dy && dx <= dz) {
                minX = firstX;
                maxX = firstX;
            } else if (dy <= dx && dy <= dz) {
                minY = firstY;
                maxY = firstY;
            } else {
                minZ = firstZ;
                maxZ = firstZ;
            }
        }

        int dimensions = dimensions(minX, minY, minZ, maxX, maxY, maxZ);
        List<ShapeCoordinate> result = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (mode == ShapeMode.HOLLOW && dimensions >= 2
                            && !isActiveBoundary(x, y, z, minX, minY, minZ, maxX, maxY, maxZ)) {
                        continue;
                    }
                    result.add(new ShapeCoordinate(x, y, z));
                }
            }
        }
        return List.copyOf(result);
    }

    public static int dimensions(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        int count = 0;
        if (firstX != secondX) {
            count++;
        }
        if (firstY != secondY) {
            count++;
        }
        if (firstZ != secondZ) {
            count++;
        }
        return count;
    }

    private static boolean isActiveBoundary(
            int x,
            int y,
            int z,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        return (minX != maxX && (x == minX || x == maxX))
                || (minY != maxY && (y == minY || y == maxY))
                || (minZ != maxZ && (z == minZ || z == maxZ));
    }
}
