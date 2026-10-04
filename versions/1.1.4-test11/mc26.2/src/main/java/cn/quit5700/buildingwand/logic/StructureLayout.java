package cn.quit5700.buildingwand.logic;
/** Two-cell tiling without storing a region-sized plan. Axis: 0=X, 1=Y, 2=Z. */
public final class StructureLayout {
 public record Pair(int x,int y,int z,int mx,int my,int mz,boolean anchor,boolean included,boolean remainder){}
 public static long length(DeleteBox b,int axis){return (long)max(b,axis)-min(b,axis)+1;}
 public static int axis(DeleteBox b,boolean vertical,int mode){
  if(vertical)return 1;
  if(mode==2){if(length(b,0)%2==0)return 0;if(length(b,2)%2==0)return 2;return 0;}
  return length(b,0)>=2?0:2;
 }
 public static boolean fits(DeleteBox b,boolean vertical,int mode){int a=axis(b,vertical,mode);return a>=0;}
 public static Pair pair(DeleteBox b,int x,int y,int z,int axis,int mode){return pair(b,x,y,z,axis,mode,1);}
 public static Pair pair(DeleteBox b,int x,int y,int z,int axis,int mode,int direction){
  int at=axis==0?x:axis==1?y:z,lo=min(b,axis),hi=max(b,axis);long offset=direction>0?(long)at-lo:(long)hi-at;
  boolean rest=(length(b,axis)%2!=0&&at==(direction>0?hi:lo));int first=rest?at:(int)((long)at-direction*(offset%2));
  int ax=axis==0?first:x,ay=axis==1?first:y,az=axis==2?first:z;
  int mx=axis==0&&!rest?first+direction:ax,my=axis==1&&!rest?first+direction:ay,mz=axis==2&&!rest?first+direction:az;
  boolean included=mode!=2||BuildPlan.includes(b,ax,ay,az,2)||BuildPlan.includes(b,mx,my,mz,2);
  return new Pair(ax,ay,az,mx,my,mz,rest||at==first,included,rest);
 }
 public static java.math.BigInteger total(DeleteBox b,boolean vertical,int mode){
  int axis=axis(b,vertical,mode),a=(axis+1)%3,c=(axis+2)%3;long len=length(b,axis),pairs=len/2;
  java.math.BigInteger full=java.math.BigInteger.valueOf(pairs).multiply(java.math.BigInteger.TWO).multiply(java.math.BigInteger.valueOf(length(b,a))).multiply(java.math.BigInteger.valueOf(length(b,c)));
  if(mode!=2)return full;
  long interiorPairs=Math.max(0,pairs-(len%2==0?2:1));
  return full.subtract(java.math.BigInteger.valueOf(interiorPairs).multiply(java.math.BigInteger.TWO).multiply(java.math.BigInteger.valueOf(Math.max(0,length(b,a)-2))).multiply(java.math.BigInteger.valueOf(Math.max(0,length(b,c)-2))));
 }
 private static int min(DeleteBox b,int a){return a==0?b.minX():a==1?b.minY():b.minZ();}
 private static int max(DeleteBox b,int a){return a==0?b.maxX():a==1?b.maxY():b.maxZ();}
}
