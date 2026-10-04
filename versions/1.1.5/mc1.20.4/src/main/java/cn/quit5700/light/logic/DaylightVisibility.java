package cn.quit5700.light.logic;

/** Traverses voxel cells between block centres; the emitting and receiving cells are excluded. */
public final class DaylightVisibility {
    private DaylightVisibility() { }

    @FunctionalInterface
    public interface Obstacle { boolean blocks(int x, int y, int z); }

    public static boolean visible(int sx, int sy, int sz, int tx, int ty, int tz,
                                  Obstacle obstacle) {
        int[] p = {sx, sy, sz};
        int[] end = {tx, ty, tz};
        int[] distance = {Math.abs(tx - sx), Math.abs(ty - sy), Math.abs(tz - sz)};
        int[] step = {Integer.signum(tx - sx), Integer.signum(ty - sy), Integer.signum(tz - sz)};
        int[] crossing = {1, 1, 1};
        while (p[0] != tx || p[1] != ty || p[2] != tz) {
            int first = -1;
            for (int axis = 0; axis < 3; axis++) {
                if (p[axis] == end[axis]) continue;
                if (first < 0 || (long) crossing[axis] * distance[first]
                        < (long) crossing[first] * distance[axis]) first = axis;
            }
            int mask = 0;
            for (int axis = 0; axis < 3; axis++) {
                if (distance[axis] != 0 && p[axis] != end[axis]
                        && (long) crossing[axis] * distance[first]
                        == (long) crossing[first] * distance[axis]) mask |= 1 << axis;
            }
            // Check every touched cell at an edge/corner, preventing diagonal wall leaks.
            for (int subset = mask; subset != 0; subset = (subset - 1) & mask) {
                int x = p[0] + ((subset & 1) != 0 ? step[0] : 0);
                int y = p[1] + ((subset & 2) != 0 ? step[1] : 0);
                int z = p[2] + ((subset & 4) != 0 ? step[2] : 0);
                if ((x != tx || y != ty || z != tz) && obstacle.blocks(x, y, z)) return false;
            }
            for (int axis = 0; axis < 3; axis++) {
                if ((mask & (1 << axis)) != 0) {
                    p[axis] += step[axis];
                    crossing[axis] += 2;
                }
            }
        }
        return true;
    }
}
