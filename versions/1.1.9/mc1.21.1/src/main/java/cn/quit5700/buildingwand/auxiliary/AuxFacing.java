package cn.quit5700.buildingwand.aux;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Cardinal facing computed from actual player position, independent of pitch limits. */
public final class AuxFacing {
 private AuxFacing(){}
 public static BlockPos direction(Vec3 offset){
  double x=Math.abs(offset.x),y=Math.abs(offset.y),z=Math.abs(offset.z);
  if(y>x&&y>z)return new BlockPos(0,offset.y<0?-1:1,0);
  if(x>z)return new BlockPos(offset.x<0?-1:1,0,0);
  return new BlockPos(0,0,offset.z<0?-1:1);
 }
 public static BlockPos connectionNormal(List<AuxRow> rows,BlockPos hit,Vec3 eye,Vec3 end){
  var intersection=net.minecraft.world.phys.shapes.Shapes.block().clip(eye,end,hit);
  if(intersection==null)throw new IllegalArgumentException("Connection ray missed selected cell");
  var direction=intersection.getDirection();return new BlockPos(direction.getStepX(),direction.getStepY(),direction.getStepZ());
 }
 public static BlockPos normal(AuxRow row){var origin=row.world(0,0,0);return (row.planar()?row.world(0,1,0):row.world(0,0,-1)).subtract(origin);}
 public static ListTag steps(BlockPos normal,BlockPos desired){
  if(normal.equals(desired))return new ListTag();
  for(int axis:new int[]{1,0,2})for(int turn:new int[]{1,-1,2})if(rotated(normal,axis,turn).equals(desired)){
   var steps=new ListTag();var step=new CompoundTag();step.putInt("Axis",axis);step.putInt("Turns",turn);steps.add(step);return steps;
  }
  throw new IllegalArgumentException("Facing is not cardinal");
 }
 private static BlockPos rotated(BlockPos point,int axis,int turns){
  int x=point.getX(),y=point.getY(),z=point.getZ();
  for(int i=0;i<Math.floorMod(turns,4);i++){int a=x,b=y,c=z;if(axis==0){y=-c;z=b;}else if(axis==1){x=c;z=-a;}else{x=-b;y=a;}}
  return new BlockPos(x,y,z);
 }
 public static BlockPos[] bounds(List<AuxRow> rows){
  if(rows.isEmpty())throw new IllegalArgumentException("No selection");
  BlockPos lo=rows.getFirst().world(0,0,0),hi=lo;
  for(var row:rows)for(int x:new int[]{0,row.w-1})for(int y:new int[]{0,row.h-1})for(int z:new int[]{0,row.d-1}){
   var p=row.world(x,y,z);lo=new BlockPos(Math.min(lo.getX(),p.getX()),Math.min(lo.getY(),p.getY()),Math.min(lo.getZ(),p.getZ()));hi=new BlockPos(Math.max(hi.getX(),p.getX()),Math.max(hi.getY(),p.getY()),Math.max(hi.getZ(),p.getZ()));
  }
  return new BlockPos[]{lo,hi};
 }
 public static BlockPos centerBlock(List<AuxRow> rows){var b=bounds(rows);return new BlockPos((int)Math.floorDiv((long)b[0].getX()+b[1].getX(),2),(int)Math.floorDiv((long)b[0].getY()+b[1].getY(),2),(int)Math.floorDiv((long)b[0].getZ()+b[1].getZ(),2));}
 public static BlockPos[] endpoints(AuxRow row){int x=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(row.data,"AX",0),y=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(row.data,"AY",0),z=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(row.data,"AZ",0);return new BlockPos[]{row.world(x,y,z),row.world(row.w-1-x,row.h-1-y,row.d-1-z)};}
}
