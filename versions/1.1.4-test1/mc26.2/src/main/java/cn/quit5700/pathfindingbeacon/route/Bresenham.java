package cn.quit5700.pathfindingbeacon.route;

import java.util.ArrayList;
import java.util.List;

public final class Bresenham {
    private Bresenham() {
    }

    public static List<Column> line(int startX, int startZ, int endX, int endZ) {
        List<Column> result = new ArrayList<>();
        int x = startX;
        int z = startZ;
        int dx = Math.abs(endX - startX);
        int dz = -Math.abs(endZ - startZ);
        int stepX = startX < endX ? 1 : -1;
        int stepZ = startZ < endZ ? 1 : -1;
        int error = dx + dz;

        while (true) {
            result.add(new Column(x, z));
            if (x == endX && z == endZ) {
                return List.copyOf(result);
            }
            int twiceError = error * 2;
            if (twiceError >= dz) {
                error += dz;
                x += stepX;
            }
            if (twiceError <= dx) {
                error += dx;
                z += stepZ;
            }
        }
    }
}

