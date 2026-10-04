package cn.quit5700.buildingwand.logic;

/** Starts at the actual first corner; traverses chunk columns toward the second. */
public final class DirectedDeleteCursor {
    private final DeleteBox box;
    private final int dx,dy,dz;
    private int x,y,z;
    private boolean done;
    public DirectedDeleteCursor(DeleteBox box,int dx,int dy,int dz){
        this(box,dx,dy,dz,dx>0?box.minX():box.maxX(),dy>0?box.minY():box.maxY(),dz>0?box.minZ():box.maxZ());
    }
    public DirectedDeleteCursor(DeleteBox box,int dx,int dy,int dz,int x,int y,int z){
        if(Math.abs(dx)!=1||Math.abs(dy)!=1||Math.abs(dz)!=1||!box.contains(x,y,z))throw new IllegalArgumentException("Invalid directed cursor");
        this.box=box;this.dx=dx;this.dy=dy;this.dz=dz;this.x=x;this.y=y;this.z=z;
    }
    public int x(){return x;}public int y(){return y;}public int z(){return z;}
    public int chunkX(){return x>>4;}public int chunkZ(){return z>>4;}
    public int dx(){return dx;}public int dy(){return dy;}public int dz(){return dz;}
    public boolean done(){return done;}
    private int startX(){return dx>0?box.minX():box.maxX();}
    private int startY(){return dy>0?box.minY():box.maxY();}
    private int endX(){return dx>0?box.maxX():box.minX();}
    private int endY(){return dy>0?box.maxY():box.minY();}
    private int endZ(){return dz>0?box.maxZ():box.minZ();}
    public void advance(){
        if(done)return;
        if(y!=endY()){y+=dy;return;}y=startY();
        int cz=z>>4,cx=x>>4;
        int chunkEndZ=dz>0?(int)Math.min(box.maxZ(),((long)cz+1)*16-1):Math.max(box.minZ(),cz*16);
        if(z!=chunkEndZ){z+=dz;return;}
        int chunkStartZ=dz>0?Math.max(box.minZ(),cz*16):(int)Math.min(box.maxZ(),((long)cz+1)*16-1);
        int chunkEndX=dx>0?(int)Math.min(box.maxX(),((long)cx+1)*16-1):Math.max(box.minX(),cx*16);
        if(x!=chunkEndX){x+=dx;z=chunkStartZ;return;}
        if(cx!=(endX()>>4)){x=dx>0?(cx+1)*16:(int)((long)cx*16-1);z=chunkStartZ;return;}
        if(cz!=(endZ()>>4)){x=startX();z=dz>0?(cz+1)*16:(int)((long)cz*16-1);return;}
        done=true;
    }
}
