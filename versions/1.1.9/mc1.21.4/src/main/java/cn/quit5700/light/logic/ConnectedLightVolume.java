package cn.quit5700.light.logic;

import java.util.BitSet;

/** Incremental six-neighbour traversal; coordinates and connectivity are independent of Minecraft. */
public final class ConnectedLightVolume {
    @FunctionalInterface public interface Passage {
        boolean open(int x, int y, int z, int nx, int ny, int nz);
    }
    private static final int[][] DIRECTIONS = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    private final int radius, side;
    private final BitSet reached;
    private int[] queue;
    private int head, tail;

    public ConnectedLightVolume(int radius) {
        if (radius < 1 || radius > 64) throw new IllegalArgumentException("radius must be 1..64");
        this.radius = radius;
        side = radius * 2 + 1;
        reached = new BitSet(side * side * side);
        queue = new int[side * side * side];
        int center = index(0,0,0);
        reached.set(center);
        queue[tail++] = center;
    }

    public int advance(int budget, Passage passage) {
        int count = 0;
        while (head < tail && count < budget) {
            int current = queue[head++];
            int x = current % side - radius;
            int y = (current / side) % side - radius;
            int z = current / (side * side) - radius;
            for (int[] direction : DIRECTIONS) {
                int nx = x + direction[0], ny = y + direction[1], nz = z + direction[2];
                if (!inside(nx,ny,nz)) continue;
                int next = index(nx,ny,nz);
                if (!reached.get(next) && passage.open(x,y,z,nx,ny,nz)) {
                    reached.set(next);
                    queue[tail++] = next;
                }
            }
            count++;
        }
        if (head == tail) queue = null;
        return count;
    }

    public boolean complete() { return head == tail; }
    public boolean contains(int x, int y, int z) { return inside(x,y,z) && reached.get(index(x,y,z)); }
    private boolean inside(int x, int y, int z) {
        return Math.abs(x) <= radius && Math.abs(y) <= radius && Math.abs(z) <= radius;
    }
    private int index(int x, int y, int z) { return x+radius + side*(y+radius + side*(z+radius)); }
}
