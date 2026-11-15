package cn.quit5700.buildingwand.logic;

/** Constant memory traversal, one chunk at a time; coordinates can be persisted. */
public final class DeleteCursor {
    private final DeleteBox box;
    private int x,y,z;
    private boolean done;
    public DeleteCursor(DeleteBox box) { this(box,box.minX(),box.minY(),box.minZ(),false); }
    public DeleteCursor(DeleteBox box,int x,int y,int z,boolean done) {
        if (!box.contains(x,y,z)) throw new IllegalArgumentException("Cursor outside box");
        this.box=box;this.x=x;this.y=y;this.z=z;this.done=done;
    }
    public int x(){return x;} public int y(){return y;} public int z(){return z;}
    public int chunkX(){return x>>4;} public int chunkZ(){return z>>4;}
    public boolean done(){return done;}
    public void advance() {
        if(done)return;
        if(y<box.maxY()){y++;return;}
        y=box.minY();
        int endZ=(int)Math.min(box.maxZ(),((long)(z>>4)+1)*16-1);
        if(z<endZ){z++;return;}
        int startZ=Math.max(box.minZ(),(z>>4)*16);
        int endX=(int)Math.min(box.maxX(),((long)(x>>4)+1)*16-1);
        if(x<endX){x++;z=startZ;return;}
        int cx=x>>4, cz=z>>4;
        if(cx<(box.maxX()>>4)) {x=(cx+1)*16;z=startZ;return;}
        if(cz<(box.maxZ()>>4)) {x=box.minX();z=(cz+1)*16;return;}
        done=true;
    }
}
