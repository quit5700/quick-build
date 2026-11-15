package cn.quit5700.buildingwand.aux;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Distinguish the silhouette of a voxel surface from coplanar grid lines. */
public final class AuxSurface {
 private AuxSurface(){}
 public static int outlineColor(int color){int r=color>>16&255,g=color>>8&255,b=color&255;return r*299+g*587+b*114>210000?0x455C65:color;}
 public static double outlineWidth(double distance){return Math.max(.018,Math.min(.12,distance*.0018));}
 public static Direction[] edges(Direction face){return switch(face){
  case DOWN->new Direction[]{Direction.SOUTH,Direction.EAST,Direction.NORTH,Direction.WEST};
  case UP->new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
  case NORTH->new Direction[]{Direction.DOWN,Direction.WEST,Direction.UP,Direction.EAST};
  case SOUTH->new Direction[]{Direction.DOWN,Direction.EAST,Direction.UP,Direction.WEST};
  case WEST->new Direction[]{Direction.DOWN,Direction.SOUTH,Direction.UP,Direction.NORTH};
  case EAST->new Direction[]{Direction.DOWN,Direction.NORTH,Direction.UP,Direction.SOUTH};
 };}
 public static int edgeMask(AuxRow row,BlockPos at,Direction face){
  int mask=0;Direction[] edges=edges(face);
  for(int i=0;i<4;i++){
   BlockPos neighbor=at.relative(edges[i]);int[] cell=row.local(neighbor),outside=row.local(neighbor.relative(face));
   if(!row.contains(cell[0],cell[1],cell[2])||row.contains(outside[0],outside[1],outside[2]))mask|=1<<i;
  }
  return mask;
 }
 public static boolean frontFacing(BlockPos pos,Direction face,double x,double y,double z){
  return (x-pos.getX()-.5-face.getStepX()*.5)*face.getStepX()+(y-pos.getY()-.5-face.getStepY()*.5)*face.getStepY()+(z-pos.getZ()-.5-face.getStepZ()*.5)*face.getStepZ()>0;
 }
}
