package cn.quit5700.light.logic;

public final class RealAreaPolicy {
    public static final long MAX_VOLUME = 65_536;
    private RealAreaPolicy() { }
    public static boolean accepts(LightBox box) {return box != null && box.volume() <= MAX_VOLUME;}
    public static AreaSelectionRules.Point position(LightBox box,int index) {
        if(index<0 || index>=box.volume())throw new IndexOutOfBoundsException(index);
        int sy=box.maxY()-box.minY()+1, sx=box.maxX()-box.minX()+1;
        return new AreaSelectionRules.Point(box.minX()+(index/sy)%sx,box.minY()+index%sy,box.minZ()+index/(sy*sx));
    }
    public static boolean canRemove(boolean owned,boolean desired,boolean otherSource,boolean lightBlock) {
        return owned && !desired && !otherSource && lightBlock;
    }
}
