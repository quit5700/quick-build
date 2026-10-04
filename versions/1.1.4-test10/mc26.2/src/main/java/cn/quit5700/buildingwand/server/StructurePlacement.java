package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.logic.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import java.util.*;
/** Complete structures are assembled before notifying neighbours. No source inventory is copied. */
public final class StructurePlacement {
 private static final int QUIET=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
 public static boolean multiple(Block b){return b instanceof BedBlock||b instanceof DoorBlock||b instanceof DoublePlantBlock;}
 public static boolean vertical(Block b){return !(b instanceof BedBlock);}
 public static java.math.BigInteger total(Block b,DeleteBox box,int mode){return multiple(b)?StructureLayout.total(box,vertical(b),mode):BuildPlan.total(box,mode);}
 public static boolean fits(Block b,DeleteBox box,int mode){return !multiple(b)||StructureLayout.fits(box,vertical(b),mode);}
 public static LinkedHashMap<BlockPos,BlockState> single(Block b,BlockPos at){return states(b,at,at.relative(vertical(b)?Direction.UP:Direction.EAST));}
 public static LinkedHashMap<BlockPos,BlockState> plan(Block b,DeleteBox box,BlockPos at,int mode){return plan(b,box,at,mode,1,1,1);}
 public static LinkedHashMap<BlockPos,BlockState> plan(Block b,DeleteBox box,BlockPos at,int mode,int dx,int dy,int dz){
  if(!multiple(b)){var m=new LinkedHashMap<BlockPos,BlockState>();if(BuildPlan.includes(box,at.getX(),at.getY(),at.getZ(),mode))m.put(at,b.defaultBlockState());return m;}
  int axis=StructureLayout.axis(box,vertical(b),mode);if(axis<0)return new LinkedHashMap<>();
  var p=StructureLayout.pair(box,at.getX(),at.getY(),at.getZ(),axis,mode,axis==0?dx:axis==1?dy:dz);if(!p.anchor()||!p.included())return new LinkedHashMap<>();
  var a=new BlockPos(p.x(),p.y(),p.z());var m=new BlockPos(p.mx(),p.my(),p.mz());
  if(p.remainder()){var result=new LinkedHashMap<BlockPos,BlockState>();result.put(a,Blocks.AIR.defaultBlockState());return result;}
  return states(b,a,m);
 }
 private static LinkedHashMap<BlockPos,BlockState> states(Block b,BlockPos a,BlockPos mate){
  var result=new LinkedHashMap<BlockPos,BlockState>();var s=b.defaultBlockState();
  if(b instanceof BedBlock){Direction facing=mate.getX()!=a.getX()?(mate.getX()>a.getX()?Direction.EAST:Direction.WEST):(mate.getZ()>a.getZ()?Direction.SOUTH:Direction.NORTH);s=s.setValue(BedBlock.FACING,facing).setValue(BedBlock.OCCUPIED,false);result.put(a,s.setValue(BedBlock.PART,BedPart.FOOT));result.put(mate,s.setValue(BedBlock.PART,BedPart.HEAD));}
  else if(b instanceof DoorBlock||b instanceof DoublePlantBlock){result.put(a.getY()<mate.getY()?a:mate,s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.LOWER));result.put(a.getY()<mate.getY()?mate:a,s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.UPPER));}
  else result.put(a,s);
  return result;
 }
 public static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){
  if(desired.size()==1){var e=desired.entrySet().iterator().next();if(!multiple(e.getValue().getBlock())&&!(e.getValue().getBlock() instanceof ChestBlock)){
   boolean ok=w.getBlockState(e.getKey()).equals(e.getValue())||w.setBlock(e.getKey(),e.getValue(),Block.UPDATE_ALL);
   if(ok){connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));}return ok;
  }}
  var old=new LinkedHashMap<BlockPos,BlockState>();
  for(var e:desired.entrySet()){if(w.getBlockEntity(e.getKey())!=null&&!w.getBlockState(e.getKey()).equals(e.getValue()))return false;old.put(e.getKey(),w.getBlockState(e.getKey()));}
  for(var e:desired.entrySet())if(!w.getBlockState(e.getKey()).equals(e.getValue())&&!w.setBlock(e.getKey(),e.getValue(),QUIET)){restore(w,old);return false;}
  for(var e:desired.entrySet())if(!e.getValue().isAir()&&!e.getValue().canSurvive(w,e.getKey())){restore(w,old);return false;}
  for(var e:desired.entrySet()){
   var s=w.getBlockState(e.getKey());if(s.getBlock() instanceof ChestBlock&&s.getValue(ChestBlock.TYPE)!=ChestType.SINGLE)w.setBlock(e.getKey(),s.setValue(ChestBlock.TYPE,ChestType.SINGLE),QUIET);
   w.updateNeighborsAt(e.getKey(),s.getBlock());connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));
  }
  for(var e:desired.entrySet())if(w.getBlockState(e.getKey()).getBlock()!=e.getValue().getBlock()){restore(w,old);return false;}
  return true;
 }
 private static void restore(ServerLevel w,Map<BlockPos,BlockState> old){for(var e:old.entrySet())w.setBlock(e.getKey(),e.getValue(),QUIET);}
 private static void connect(ServerLevel w,BlockPos p){var s=w.getBlockState(p);var b=s.getBlock();if(b instanceof FenceBlock||b instanceof WallBlock||b instanceof IronBarsBlock){var updated=Block.updateFromNeighbourShapes(s,w,p);if(!updated.equals(s))w.setBlock(p,updated,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}}
}
