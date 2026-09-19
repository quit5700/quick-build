package cn.quit5700.light.logic;

/** Shared picking and preview policy, independent of game initialization. */
public final class AreaSelectionRules {
    public static final double AIR_DISTANCE = 4.0D;
    private AreaSelectionRules() { }
    public record Point(int x,int y,int z) { }
    public static Point airPoint(double x,double y,double z,double dx,double dy,double dz) {
        return new Point((int)Math.floor(x+dx*AIR_DISTANCE), (int)Math.floor(y+dy*AIR_DISTANCE),
                (int)Math.floor(z+dz*AIR_DISTANCE));
    }
    public static boolean pinPreview(boolean selecting,boolean requested,boolean hasRange) {
        return !selecting && requested && hasRange;
    }
    public static boolean showPreview(boolean selecting,boolean pinned) {return selecting || pinned;}
}
