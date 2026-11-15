package cn.quit5700.light.logic;

public record LightBox(int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {
    public static final int MAX_SIDE = 512;
    public LightBox {
        if ((long)maxX-minX<0 || (long)maxY-minY<0 || (long)maxZ-minZ<0
                || (long)maxX-minX>=MAX_SIDE || (long)maxY-minY>=MAX_SIDE || (long)maxZ-minZ>=MAX_SIDE)
            throw new IllegalArgumentException("Invalid light area bounds");
    }
    public static LightBox of(int x,int y,int z,int a,int b,int c) {
        return new LightBox(Math.min(x,a),Math.min(y,b),Math.min(z,c),Math.max(x,a),Math.max(y,b),Math.max(z,c));
    }
    public boolean contains(int x,int y,int z) {
        return x>=minX && x<=maxX && y>=minY && y<=maxY && z>=minZ && z<=maxZ;
    }
    public long volume() { return ((long)maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1); }
}
