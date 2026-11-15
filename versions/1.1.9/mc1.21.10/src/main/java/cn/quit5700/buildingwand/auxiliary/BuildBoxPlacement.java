package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.logic.DeleteBox;
import net.minecraft.core.BlockPos;
import java.util.function.Predicate;
/** Placement policy shared by the server and regression tests. */
public final class BuildBoxPlacement {
 private BuildBoxPlacement(){}
 public static BlockPos find(DeleteBox bounds,BlockPos player,Predicate<BlockPos> available){
  int px=Math.max(bounds.minX(),Math.min(bounds.maxX(),player.getX())),py=Math.max(bounds.minY(),Math.min(bounds.maxY(),player.getY())),pz=Math.max(bounds.minZ(),Math.min(bounds.maxZ(),player.getZ()));
  var edge=new java.util.HashSet<BlockPos>();
  long[][] seeds={{(long)bounds.minX()-1,py,pz},{(long)bounds.maxX()+1,py,pz},{px,(long)bounds.minY()-1,pz},{px,(long)bounds.maxY()+1,pz},{px,py,(long)bounds.minZ()-1},{px,py,(long)bounds.maxZ()+1}};
  for(var seed:seeds)for(int dx=-4;dx<=4;dx++)for(int dy=-4;dy<=4;dy++)for(int dz=-4;dz<=4;dz++){
   long x=seed[0]+dx,y=seed[1]+dy,z=seed[2]+dz;
   if(x<Integer.MIN_VALUE||x>Integer.MAX_VALUE||y<Integer.MIN_VALUE||y>Integer.MAX_VALUE||z<Integer.MIN_VALUE||z>Integer.MAX_VALUE)continue;
   long distance=Math.max(Math.max(Math.max((long)bounds.minX()-x,x-bounds.maxX()),Math.max((long)bounds.minY()-y,y-bounds.maxY())),Math.max((long)bounds.minZ()-z,z-bounds.maxZ()));
   if(distance==1)edge.add(new BlockPos((int)x,(int)y,(int)z));
  }
  var ordered=new java.util.ArrayList<>(edge);ordered.sort(java.util.Comparator.comparingDouble((BlockPos at)->at.distSqr(player)).thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
  for(var at:ordered)if(available.test(at))return at;
  var nearby=new java.util.ArrayList<BlockPos>();
  for(int x=-6;x<=6;x++)for(int y=-6;y<=6;y++)for(int z=-6;z<=6;z++)if(x!=0||z!=0||Math.abs(y)>1)nearby.add(player.offset(x,y,z));
  nearby.sort(java.util.Comparator.comparingDouble((BlockPos at)->at.distSqr(player)+Math.abs(at.getY()-player.getY())*.25+(at.getY()<player.getY()?1:0)).thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
  for(var at:nearby)if(!bounds.contains(at.getX(),at.getY(),at.getZ())&&available.test(at))return at;
  for(var anchor:java.util.List.of(player,new BlockPos(bounds.minX(),bounds.minY(),bounds.minZ())))for(int radius=1;radius<=64;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
   if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;var at=anchor.offset(dx,0,dz);
   if(!bounds.contains(at.getX(),at.getY(),at.getZ())&&available.test(at))return at;
  }return null;
 }
}
