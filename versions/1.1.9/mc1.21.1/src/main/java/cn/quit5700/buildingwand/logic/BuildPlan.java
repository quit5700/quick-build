package cn.quit5700.buildingwand.logic;
import java.math.BigInteger;
public final class BuildPlan {
 public static int mode(int value){return value>=0&&value<=3?value:0;}
 public static boolean includes(DeleteBox b,int x,int y,int z,int mode){return b.contains(x,y,z)&&(mode!=2||x==b.minX()||x==b.maxX()||y==b.minY()||y==b.maxY()||z==b.minZ()||z==b.maxZ());}
 public static BigInteger total(DeleteBox b,int mode){
  if(mode!=2)return b.volume();
  BigInteger interior=BigInteger.valueOf(Math.max(0,(long)b.maxX()-b.minX()-1)).multiply(BigInteger.valueOf(Math.max(0,(long)b.maxY()-b.minY()-1))).multiply(BigInteger.valueOf(Math.max(0,(long)b.maxZ()-b.minZ()-1)));
  return b.volume().subtract(interior);
 }
}
