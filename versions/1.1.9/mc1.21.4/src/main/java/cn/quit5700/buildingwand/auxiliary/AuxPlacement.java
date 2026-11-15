package cn.quit5700.buildingwand.aux;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Install the complete structure before normal support, redstone and fluid updates. */
public final class AuxPlacement {
 private static final int QUIET=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|cn.quit5700.buildingwand.server.QuietPlacement.FLAG;
 public static boolean place(ServerLevel world,LinkedHashMap<BlockPos,BlockState> cells){
  var old=new LinkedHashMap<BlockPos,BlockState>();var tags=new HashMap<BlockPos,net.minecraft.nbt.CompoundTag>();var inventories=new ArrayList<net.minecraft.world.item.ItemStack>();
  for(var entry:cells.entrySet()){var pos=entry.getKey();old.put(pos,world.getBlockState(pos));if(world.getBlockState(pos).equals(entry.getValue()))continue;var entity=world.getBlockEntity(pos);if(entity!=null)tags.put(pos,entity.saveWithFullMetadata(world.registryAccess()));if(entity instanceof net.minecraft.world.Container container)for(int i=0;i<container.getContainerSize();i++)if(!container.getItem(i).isEmpty())inventories.add(container.getItem(i).copy());}
  try{
   for(var pos:cn.quit5700.buildingwand.aux.LegacyNbt.keys(tags)){var entity=world.getBlockEntity(pos);if(entity instanceof net.minecraft.world.Container c)c.clearContent();world.removeBlockEntity(pos);if(!cn.quit5700.buildingwand.server.QuietPlacement.setBlock(world,pos,Blocks.AIR.defaultBlockState(),QUIET))throw new IllegalStateException("Cannot remove old block entity");}
   for(var entry:cells.entrySet())if(!world.getBlockState(entry.getKey()).equals(entry.getValue())&&!cn.quit5700.buildingwand.server.QuietPlacement.setBlock(world,entry.getKey(),entry.getValue(),QUIET))throw new IllegalStateException("Cannot place auxiliary block");
   for(var entry:cells.entrySet()){var state=world.getBlockState(entry.getKey());if(state.getBlock() instanceof ChestBlock&&state.hasProperty(ChestBlock.TYPE))cn.quit5700.buildingwand.server.QuietPlacement.setBlock(world,entry.getKey(),state.setValue(ChestBlock.TYPE,net.minecraft.world.level.block.state.properties.ChestType.SINGLE),QUIET);}
  }catch(RuntimeException error){for(var pos:cn.quit5700.buildingwand.aux.LegacyNbt.keys(tags))world.removeBlockEntity(pos);for(var entry:old.entrySet())cn.quit5700.buildingwand.server.QuietPlacement.setBlock(world,entry.getKey(),entry.getValue(),QUIET);for(var entry:tags.entrySet()){var restored=net.minecraft.world.level.block.entity.BlockEntity.loadStatic(entry.getKey(),old.get(entry.getKey()),entry.getValue(),world.registryAccess());if(restored==null)throw new IllegalStateException("Failed restoring block entity",error);world.setBlockEntity(restored);}return false;}
  var at=cn.quit5700.buildingwand.aux.LegacyNbt.keys(cells).iterator().next();for(var item:inventories)net.minecraft.world.Containers.dropItemStack(world,at.getX()+.5,at.getY()+.5,at.getZ()+.5,item);return true;
 }
 public static void update(ServerLevel world,BlockPos pos){
  var state=world.getBlockState(pos);if(state.isAir())return;
  var updated=Block.updateFromNeighbourShapes(state,world,pos);if(!state.canSurvive(world,pos))updated=Blocks.AIR.defaultBlockState();
  if(!state.equals(updated))Block.updateOrDestroy(state,updated,world,pos,Block.UPDATE_ALL);
  state=world.getBlockState(pos);state.updateNeighbourShapes(world,pos,Block.UPDATE_ALL);world.updateNeighborsAt(pos,state.getBlock());
  if(!state.getFluidState().isEmpty())world.scheduleTick(pos,state.getFluidState().getType(),state.getFluidState().getType().getTickDelay(world));
  if(state.getBlock() instanceof FallingBlock)world.scheduleTick(pos,state.getBlock(),2);
 }
}
