package cn.quit5700.light.logic;

public final class RealAreaPolicy {
    public static final long MAX_VOLUME = 2_146_689;
    public static final int SPARSE_STEP = 8;
    public static final int PROGRESSIVE_ORDER = 1;
    public static final int CENTER_OUT_ORDER = 2;
    private RealAreaPolicy() { }
    public static boolean accepts(LightBox box) {return box != null && box.volume() <= MAX_VOLUME;}
    public static AreaSelectionRules.Point position(LightBox box,int index) {
        if(index<0 || index>=box.volume())throw new IndexOutOfBoundsException(index);
        int sy=box.maxY()-box.minY()+1, sx=box.maxX()-box.minX()+1;
        return new AreaSelectionRules.Point(box.minX()+(index/sy)%sx,box.minY()+index%sy,box.minZ()+index/(sy*sx));
    }
    public static int sparseCount(LightBox box) {
        return ((box.maxX()-box.minX())/SPARSE_STEP+1)*((box.maxY()-box.minY())/SPARSE_STEP+1)
                *((box.maxZ()-box.minZ())/SPARSE_STEP+1);
    }
    /** Compact bit-reversal permutation: visits distant parts early without allocating a grid. */
    private static int dispersed(int rank,int size) {
        if(size==1)return 0;
        int evens=(size+1)/2;
        return rank<evens ? 2*dispersed(rank,evens) : 2*dispersed(rank-evens,size/2)+1;
    }
    public static int operationCount(LightBox box,int order) {
        return Math.toIntExact(box.volume())+(order==CENTER_OUT_ORDER?centerSparseCount(box):order==PROGRESSIVE_ORDER?sparseCount(box):0);
    }
    private static int centerSparseSize(int size) {return ((size-1)/2)/SPARSE_STEP+(size/2)/SPARSE_STEP+1;}
    public static int centerSparseCount(LightBox box) {
        return centerSparseSize(box.maxX()-box.minX()+1)*centerSparseSize(box.maxY()-box.minY()+1)*centerSparseSize(box.maxZ()-box.minZ()+1);
    }
    private static int sparseCoordinate(int min,int max,int gridCoordinate) {
        int half=(max-min)/2;
        return min+half-(half/SPARSE_STEP)*SPARSE_STEP+gridCoordinate*SPARSE_STEP;
    }
    public static AreaSelectionRules.Point scanPosition(LightBox box,int index,int order) {
        if(index<0 || index>=operationCount(box,order))throw new IndexOutOfBoundsException(index);
        if(order==CENTER_OUT_ORDER) {
            int sparse=centerSparseCount(box),sx=box.maxX()-box.minX()+1,sy=box.maxY()-box.minY()+1,sz=box.maxZ()-box.minZ()+1;
            if(index<sparse) {
                var p=CenterOutPositions.sparsePosition(centerSparseSize(sx),centerSparseSize(sy),centerSparseSize(sz),index);
                return new AreaSelectionRules.Point(sparseCoordinate(box.minX(),box.maxX(),p.x()),
                        sparseCoordinate(box.minY(),box.maxY(),p.y()),sparseCoordinate(box.minZ(),box.maxZ(),p.z()));
            }
            var p=CenterOutPositions.position(sx,sy,sz,index-sparse);
            return new AreaSelectionRules.Point(box.minX()+p.x(),box.minY()+p.y(),box.minZ()+p.z());
        }
        if(order!=PROGRESSIVE_ORDER)return position(box,index);
        int sparse=sparseCount(box);
        if(index>=sparse)return position(box,index-sparse);
        int cell=dispersed(index,sparse);
        int sy=(box.maxY()-box.minY())/SPARSE_STEP+1,sx=(box.maxX()-box.minX())/SPARSE_STEP+1;
        return new AreaSelectionRules.Point(box.minX()+((cell/sy)%sx)*SPARSE_STEP,
                box.minY()+(cell%sy)*SPARSE_STEP,box.minZ()+(cell/(sy*sx))*SPARSE_STEP);
    }
    public static boolean canRemove(boolean owned,boolean desired,boolean otherSource,boolean lightBlock) {
        return owned && !desired && !otherSource && lightBlock;
    }
}
