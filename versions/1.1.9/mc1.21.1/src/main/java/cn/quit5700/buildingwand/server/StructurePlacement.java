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
 private static final int QUIET=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|QuietPlacement.FLAG;
 public static boolean multiple(Block b){return b.defaultBlockState().hasProperty(BedBlock.PART)||b.defaultBlockState().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);}
 public static boolean vertical(Block b){return !b.defaultBlockState().hasProperty(BedBlock.PART);}
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
  if(s.hasProperty(BedBlock.PART)){Direction facing=mate.getX()!=a.getX()?(mate.getX()>a.getX()?Direction.EAST:Direction.WEST):(mate.getZ()>a.getZ()?Direction.SOUTH:Direction.NORTH);if(s.hasProperty(BedBlock.FACING))s=s.setValue(BedBlock.FACING,facing);if(s.hasProperty(BedBlock.OCCUPIED))s=s.setValue(BedBlock.OCCUPIED,false);result.put(a,s.setValue(BedBlock.PART,BedPart.FOOT));result.put(mate,s.setValue(BedBlock.PART,BedPart.HEAD));}
  else if(s.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)){result.put(a.getY()<mate.getY()?a:mate,s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.LOWER));result.put(a.getY()<mate.getY()?mate:a,s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.UPPER));}
  else result.put(a,s);
  return result;
 }
 public static boolean clearRemainder(ServerLevel w,LinkedHashMap<BlockPos,BlockState> cells){
  for(var e:cells.entrySet()){
   if(!e.getValue().isAir())throw new IllegalArgumentException("Remainder must be air");
   var entity=w.getBlockEntity(e.getKey());
   if(entity instanceof net.minecraft.world.Container container){net.minecraft.world.Containers.dropContents(w,e.getKey(),container);container.clearContent();}
   if(!w.getBlockState(e.getKey()).isAir()&&!QuietPlacement.setBlock(w,e.getKey(),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL))return false;
   connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));
  }
  return true;
 }

 public static boolean placeBuildingNatural(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){
  var old=new LinkedHashMap<BlockPos,BlockState>();
  var tags=new LinkedHashMap<BlockPos,net.minecraft.nbt.CompoundTag>();
  var contents=new LinkedHashMap<BlockPos,java.util.List<net.minecraft.world.item.ItemStack>>();
  for(var e:desired.entrySet()){
   var at=e.getKey();var state=w.getBlockState(at);old.put(at,state);
   if(state.equals(e.getValue()))continue;
   var entity=w.getBlockEntity(at);
   if(entity!=null)tags.put(at,entity.saveWithFullMetadata(w.registryAccess()));
   if(entity instanceof net.minecraft.world.Container container){var items=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();for(int n=0;n<container.getContainerSize();n++)items.add(container.getItem(n).copy());contents.put(at,items);}
  }
  for(var at:tags.keySet()){
   var entity=w.getBlockEntity(at);if(entity instanceof net.minecraft.world.Container container)container.clearContent();w.removeBlockEntity(at);
   // getBlockEntity recreates an entity while its old block state remains present.
   if(!QuietPlacement.setBlock(w,at,Blocks.AIR.defaultBlockState(),QUIET)){restoreBuilding(w,old,tags);return false;}
  }
  boolean success;
  try{success=placeNatural(w,desired);}catch(RuntimeException failure){restoreBuilding(w,old,tags);throw failure;}
  if(!success){restoreBuilding(w,old,tags);return false;}
  for(var e:contents.entrySet())for(var stack:e.getValue())if(!stack.isEmpty())net.minecraft.world.Containers.dropItemStack(w,e.getKey().getX(),e.getKey().getY(),e.getKey().getZ(),stack);
  return true;
 }
 private static void restoreBuilding(ServerLevel w,Map<BlockPos,BlockState> old,Map<BlockPos,net.minecraft.nbt.CompoundTag> tags){
  for(var at:tags.keySet())w.removeBlockEntity(at);
  restore(w,old);
  for(var e:tags.entrySet()){var entity=net.minecraft.world.level.block.entity.BlockEntity.loadStatic(e.getKey(),old.get(e.getKey()),e.getValue(),w.registryAccess());if(entity==null)throw new IllegalStateException("Cannot restore building block entity");w.setBlockEntity(entity);}
 }

 public static boolean placeNatural(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){return place(w,desired,true);}
 public static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){return place(w,desired,false);}
 private static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired,boolean natural){
  if(desired.size()==1){var e=desired.entrySet().iterator().next();if(!multiple(e.getValue().getBlock())&&!(e.getValue().getBlock() instanceof ChestBlock)){
   boolean ok=w.getBlockState(e.getKey()).equals(e.getValue())||QuietPlacement.setBlock(w,e.getKey(),e.getValue(),Block.UPDATE_ALL);
   if(ok&&natural){var current=w.getBlockState(e.getKey());var updated=Block.updateFromNeighbourShapes(current,w,e.getKey());if(!current.canSurvive(w,e.getKey()))updated=Blocks.AIR.defaultBlockState();Block.updateOrDestroy(current,updated,w,e.getKey(),Block.UPDATE_ALL);}
   if(ok){connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));}return ok;
  }}
  var old=new LinkedHashMap<BlockPos,BlockState>();
  for(var e:desired.entrySet()){if(w.getBlockEntity(e.getKey())!=null&&!w.getBlockState(e.getKey()).equals(e.getValue()))return false;old.put(e.getKey(),w.getBlockState(e.getKey()));}
  for(var e:desired.entrySet())if(!w.getBlockState(e.getKey()).equals(e.getValue())&&!QuietPlacement.setBlock(w,e.getKey(),e.getValue(),QUIET)){restore(w,old);return false;}
  for(var e:desired.entrySet())if(!natural&&!e.getValue().isAir()&&!e.getValue().canSurvive(w,e.getKey())){restore(w,old);return false;}
  for(var e:desired.entrySet()){
   var s=w.getBlockState(e.getKey());if(s.getBlock() instanceof ChestBlock&&s.getValue(ChestBlock.TYPE)!=ChestType.SINGLE)QuietPlacement.setBlock(w,e.getKey(),s.setValue(ChestBlock.TYPE,ChestType.SINGLE),QUIET);
   if(natural){var updated=Block.updateFromNeighbourShapes(s,w,e.getKey());if(!s.canSurvive(w,e.getKey()))updated=Blocks.AIR.defaultBlockState();Block.updateOrDestroy(s,updated,w,e.getKey(),Block.UPDATE_ALL);s=w.getBlockState(e.getKey());}
   if(natural&&s.getBlock() instanceof ChestBlock)s.updateNeighbourShapes(w,e.getKey(),Block.UPDATE_ALL);
   w.updateNeighborsAt(e.getKey(),s.getBlock());connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));
  }
  for(var e:desired.entrySet())if(!natural&&w.getBlockState(e.getKey()).getBlock()!=e.getValue().getBlock()){restore(w,old);return false;}
  return true;
 }
 private static void restore(ServerLevel w,Map<BlockPos,BlockState> old){for(var e:old.entrySet())QuietPlacement.setBlock(w,e.getKey(),e.getValue(),QUIET);}
 private static void connect(ServerLevel w,BlockPos p){var s=w.getBlockState(p);var b=s.getBlock();if(b instanceof FenceBlock||b instanceof WallBlock||b instanceof IronBarsBlock){var updated=Block.updateFromNeighbourShapes(s,w,p);if(!updated.equals(s))QuietPlacement.setBlock(w,p,updated,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}}
}
