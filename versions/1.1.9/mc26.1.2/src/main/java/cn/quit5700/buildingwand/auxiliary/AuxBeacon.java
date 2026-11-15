package cn.quit5700.buildingwand.aux;
import java.util.Set;

/** Selection-sized stepped tower; the vanilla beacon itself still caps effects at level four. */
public final class AuxBeacon {
 public static final int SHAPE=12;
 public static final String DEFAULT_BASE="minecraft:iron_block";
 /** Vanilla activation materials, not a construction whitelist. */
 public static final Set<String> BASES=Set.of(DEFAULT_BASE,"minecraft:gold_block","minecraft:diamond_block","minecraft:emerald_block","minecraft:netherite_block");
 private AuxBeacon(){}
 public static int levels(int w,int h,int d){return Math.max(0,Math.min(h-1,(Math.min(w,d)-1)/2));}
 public static int side(int levels){return levels*2+1;}
 public static boolean contains(int w,int h,int d,int x,int y,int z){
  int levels=levels(w,h,d);if(levels==0||y<0||y>levels)return false;
  int side=side(levels),ox=(w-side)/2,oz=(d-side)/2;
  if(y==levels)return x==ox+levels&&z==oz+levels;
  return x>=ox+y&&x<ox+side-y&&z>=oz+y&&z<oz+side-y;
 }
 public static String material(int w,int h,int d,int x,int y,int z,String base){
  if(!contains(w,h,d,x,y,z))return null;
  if(base.isEmpty())return null;
  if(y==levels(w,h,d))return "minecraft:beacon";
  return base;
 }
}
