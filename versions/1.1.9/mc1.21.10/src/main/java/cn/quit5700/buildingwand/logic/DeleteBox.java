package cn.quit5700.buildingwand.logic;
import java.math.BigInteger;

/** Inclusive coordinates; no artificial side-length or volume limit. */
public record DeleteBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public DeleteBox {
        if (minX > maxX || minY > maxY || minZ > maxZ) throw new IllegalArgumentException("Inverted box");
    }
    public static DeleteBox between(int x1,int y1,int z1,int x2,int y2,int z2) {
        return new DeleteBox(Math.min(x1,x2),Math.min(y1,y2),Math.min(z1,z2),
                Math.max(x1,x2),Math.max(y1,y2),Math.max(z1,z2));
    }
    public BigInteger volume() {
        return BigInteger.valueOf((long)maxX-minX+1).multiply(BigInteger.valueOf((long)maxY-minY+1))
                .multiply(BigInteger.valueOf((long)maxZ-minZ+1));
    }
    public boolean contains(int x,int y,int z) {
        return x>=minX && x<=maxX && y>=minY && y<=maxY && z>=minZ && z<=maxZ;
    }
}
