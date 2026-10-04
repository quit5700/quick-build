package cn.quit5700.light.logic;

/** Random access to expanding, clipped box shells; no volume-sized position list. */
public final class CenterOutPositions {
    private CenterOutPositions() { }
    private record Box(int x0,int y0,int z0,int x1,int y1,int z1) {
        int volume(){return (x1-x0+1)*(y1-y0+1)*(z1-z0+1);}
        AreaSelectionRules.Point at(int index) {
            int sy=y1-y0+1,sx=x1-x0+1;
            return new AreaSelectionRules.Point(x0+(index/sy)%sx,y0+index%sy,z0+index/(sy*sx));
        }
    }
    private static Box bounds(int sx,int sy,int sz,int radius,boolean singleCenter) {
        return new Box(Math.max(0,(sx-1)/2-radius),Math.max(0,(sy-1)/2-radius),Math.max(0,(sz-1)/2-radius),
                Math.min(sx-1,(singleCenter?(sx-1)/2:sx/2)+radius),
                Math.min(sy-1,(singleCenter?(sy-1)/2:sy/2)+radius),Math.min(sz-1,(singleCenter?(sz-1)/2:sz/2)+radius));
    }
    private static int dispersed(int rank,int size) {
        if(size==1)return 0;
        int evens=(size+1)/2;
        return rank<evens?2*dispersed(rank,evens):2*dispersed(rank-evens,size/2)+1;
    }
    public static AreaSelectionRules.Point position(int sx,int sy,int sz,int index) {
        return position(sx,sy,sz,index,false);
    }
    public static AreaSelectionRules.Point sparsePosition(int sx,int sy,int sz,int index) {
        return position(sx,sy,sz,index,true);
    }
    private static AreaSelectionRules.Point position(int sx,int sy,int sz,int index,boolean singleCenter) {
        if(sx<1 || sy<1 || sz<1 || index<0 || index>=(long)sx*sy*sz)throw new IndexOutOfBoundsException(index);
        int low=0,high=Math.max(sx,Math.max(sy,sz))/2;
        while(low<high) {
            int middle=(low+high)/2;
            if(bounds(sx,sy,sz,middle,singleCenter).volume()>index)high=middle;else low=middle+1;
        }
        Box outer=bounds(sx,sy,sz,low,singleCenter);
        if(low==0)return outer.at(index);
        Box inner=bounds(sx,sy,sz,low-1,singleCenter);
        int local=dispersed(index-inner.volume(),outer.volume()-inner.volume());
        for(int face=0;face<6;face++) {
            Box slab=switch(face) {
                case 0 -> new Box(outer.x0,outer.y0,outer.z0,outer.x1,outer.y1,inner.z0-1);
                case 1 -> new Box(outer.x0,outer.y0,inner.z1+1,outer.x1,outer.y1,outer.z1);
                case 2 -> new Box(outer.x0,outer.y0,inner.z0,inner.x0-1,outer.y1,inner.z1);
                case 3 -> new Box(inner.x1+1,outer.y0,inner.z0,outer.x1,outer.y1,inner.z1);
                case 4 -> new Box(inner.x0,outer.y0,inner.z0,inner.x1,inner.y0-1,inner.z1);
                default -> new Box(inner.x0,inner.y1+1,inner.z0,inner.x1,outer.y1,inner.z1);
            };
            if(local<slab.volume())return slab.at(local);
            local-=slab.volume();
        }
        throw new IllegalStateException("Invalid shell rank");
    }
}
