package cn.quit5700.buildingwand.server;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import java.util.*;
import java.lang.reflect.*;
public final class WandSmokeChecks {
 private static int failures;
 private static ServerLevel world;
 private static final int QUIET=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
 public static void register(){ServerLifecycleEvents.SERVER_STARTED.register(server->{world=server.overworld();
  run("powered_wire_quiet",()->quietWire());run("wire_before_support",()->lateSupport());
  run("power_difference_allowed",()->powerDifference());run("missing_block_rejected",()->missingBlock());
  run("unsupported_wire_rejected",()->unsupported());run("container_fluid_rollback",()->containerRollback());
  run("complete_powered_circuit",()->circuit());
  run("replace_material_selection",()->selection(server));run("replace_protection",()->protection());
  run("replace_entity_and_support",()->entityProtection());run("replace_job_persistence",()->persistence());
  run("replace_survival_pause_resume",()->survival(server));run("replace_filter_counts_and_no_drops",()->filterCounts(server));
  System.out.println(failures==0?"Q5_MOVE_REGRESSION_PASS: 13 cases":"Q5_MOVE_REGRESSION_FAIL: "+failures);server.halt(false);});}
 private interface Case{void run()throws Exception;}
 private static void run(String name,Case action){try{action.run();System.out.println("Q5_CASE_PASS: "+name);}catch(Throwable e){failures++;System.out.println("Q5_CASE_FAIL: "+name);e.printStackTrace();}}
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 private static Method method(String name,Class<?>... args)throws Exception{var m=WandActions.class.getDeclaredMethod(name,args);m.setAccessible(true);return m;}
 @SuppressWarnings({"rawtypes","unchecked"}) private static Map image(LinkedHashMap<BlockPos,BlockState> states)throws Exception{
  var type=Class.forName("cn.quit5700.buildingwand.server.WandActions$MoveBlock");var ctor=type.getDeclaredConstructors()[0];ctor.setAccessible(true);Map result=new LinkedHashMap();for(var e:states.entrySet())result.put(e.getKey(),ctor.newInstance(e.getKey(),e.getValue(),null));return result;
 }
 private static void place(Map<?,?> map)throws Exception{method("placeSnapshot",ServerLevel.class,Map.class).invoke(null,world,map);}
 private static void verify(Map<?,?> map)throws Exception{method("verifySnapshot",ServerLevel.class,Map.class).invoke(null,world,map);}
 private static void reject(Map<?,?> map)throws Exception{try{verify(map);throw new AssertionError("invalid move accepted");}catch(InvocationTargetException e){check(e.getCause() instanceof IllegalStateException,"unexpected failure");}}
 private static BlockState wire(int power){return Blocks.REDSTONE_WIRE.defaultBlockState().setValue(BlockStateProperties.POWER,power);}
 private static void reset(BlockPos p){world.setBlock(p,Blocks.AIR.defaultBlockState(),QUIET);world.setBlock(p.below(),Blocks.STONE.defaultBlockState(),QUIET);}
 private static void quietWire()throws Exception{var p=new BlockPos(64,180,64);reset(p);var states=new LinkedHashMap<BlockPos,BlockState>();states.put(p,wire(15));var map=image(states);place(map);check(world.getBlockState(p).getValue(BlockStateProperties.POWER)==15,"wire power changed before complete placement");verify(map);}
 private static void lateSupport()throws Exception{var p=new BlockPos(68,180,64);world.setBlock(p.below(),Blocks.AIR.defaultBlockState(),QUIET);world.setBlock(p,Blocks.AIR.defaultBlockState(),QUIET);var states=new LinkedHashMap<BlockPos,BlockState>();states.put(p,wire(15));states.put(p.below(),Blocks.STONE.defaultBlockState());var map=image(states);place(map);check(world.getBlockState(p).is(Blocks.REDSTONE_WIRE),"wire lost while support not yet placed");verify(map);}
 private static void powerDifference()throws Exception{var p=new BlockPos(72,180,64);reset(p);world.setBlock(p,wire(0),QUIET);var states=new LinkedHashMap<BlockPos,BlockState>();states.put(p,wire(15));verify(image(states));}
 private static void missingBlock()throws Exception{var p=new BlockPos(76,180,64);reset(p);var states=new LinkedHashMap<BlockPos,BlockState>();states.put(p,wire(15));reject(image(states));}
 private static void unsupported()throws Exception{var p=new BlockPos(80,180,64);world.setBlock(p.below(),Blocks.AIR.defaultBlockState(),QUIET);world.setBlock(p,wire(15),QUIET);var states=new LinkedHashMap<BlockPos,BlockState>();states.put(p,wire(15));reject(image(states));}
 @SuppressWarnings({"rawtypes","unchecked"}) private static void containerRollback()throws Exception{
  var chest=new BlockPos(84,180,64);var water=chest.offset(1,0,0);var observer=chest.offset(2,0,0);reset(chest);reset(water);reset(observer);
  world.setBlock(chest,Blocks.CHEST.defaultBlockState(),QUIET);((Container)world.getBlockEntity(chest)).setItem(0,new ItemStack(Items.DIAMOND,31));world.setBlock(water,Blocks.WATER.defaultBlockState(),QUIET);world.setBlock(observer,Blocks.OBSERVER.defaultBlockState(),QUIET);
  Set<BlockPos> source=new LinkedHashSet<>(List.of(chest,water,observer));Map original=(Map)method("snapshot",ServerLevel.class,Set.class).invoke(null,world,source);
  var type=Class.forName("cn.quit5700.buildingwand.server.WandActions$MoveBlock");var ctor=type.getDeclaredConstructors()[0];ctor.setAccessible(true);var state=type.getDeclaredMethod("state");var tag=type.getDeclaredMethod("blockEntityTag");state.setAccessible(true);tag.setAccessible(true);Map target=new LinkedHashMap();Set<BlockPos> affected=new LinkedHashSet<>(source);
  for(Object key:original.keySet()){var p=(BlockPos)key;var dest=p.offset(16,0,0);reset(dest);affected.add(dest);Object block=original.get(key);target.put(dest,ctor.newInstance(p,state.invoke(block),tag.invoke(block)));}
  Map rollback=(Map)method("snapshot",ServerLevel.class,Set.class).invoke(null,world,affected);method("clearPositions",ServerLevel.class,Set.class).invoke(null,world,source);place(target);verify(target);
  check(((Container)world.getBlockEntity(chest.offset(16,0,0))).getItem(0).getCount()==31,"container inventory lost");check(world.getFluidState(water.offset(16,0,0)).getType()==Fluids.WATER,"water lost");check(!world.getBlockState(observer.offset(16,0,0)).getValue(BlockStateProperties.POWERED),"observer triggered during assembly");
  method("restoreSnapshot",ServerLevel.class,Set.class,Map.class).invoke(null,world,affected,rollback);check(((Container)world.getBlockEntity(chest)).getItem(0).getCount()==31,"rollback inventory lost");check(world.getBlockState(chest.offset(16,0,0)).isAir(),"rollback target not restored");
 }
 private static void circuit()throws Exception{var p=new BlockPos(112,180,64);reset(p);reset(p.east());var states=new LinkedHashMap<BlockPos,BlockState>();states.put(p,wire(15));states.put(p.east(),Blocks.REDSTONE_BLOCK.defaultBlockState());var map=image(states);place(map);verify(map);for(var pos:states.keySet())world.updateNeighborsAt(pos,world.getBlockState(pos).getBlock());check(world.getBlockState(p).is(Blocks.REDSTONE_WIRE)&&world.getBlockState(p).getValue(BlockStateProperties.POWER)==15,"complete circuit did not power wire");}
 private static final class TestPlayer extends net.minecraft.server.level.ServerPlayer {
  TestPlayer(net.minecraft.server.MinecraftServer server){super(server,world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Q5ReplaceTest"),net.minecraft.server.level.ClientInformation.createDefault());}
  net.minecraft.network.chat.Component lastMessage;
  @Override public void sendOverlayMessage(net.minecraft.network.chat.Component message){lastMessage=message;}
 }
 private static void selection(net.minecraft.server.MinecraftServer server)throws Exception{
  var p=new TestPlayer(server);var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);p.getInventory().setItem(0,wand);var data=cn.quit5700.buildingwand.util.BuildWandData.class;
  cn.quit5700.buildingwand.util.BuildWandData.mode(wand,3);var a=new BlockPos(128,180,64);var b=a.east();world.setBlock(a,Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(b,Blocks.DIRT.defaultBlockState(),QUIET);
  BuildWandServer.pickMaterial(p,a);BuildWandServer.pickMaterial(p,b);check(cn.quit5700.buildingwand.util.WandStackData.selectedBlock(wand)==Blocks.STONE,"global material overwritten");check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.DIRT,"target not remembered");
  p.getInventory().setItem(0,ItemStack.EMPTY);p.getInventory().setItem(0,wand);check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.DIRT,"item switch erased target");
  cn.quit5700.buildingwand.util.BuildWandData.mode(wand,1);BuildWandServer.pickMaterial(p,b);check(cn.quit5700.buildingwand.util.WandStackData.selectedBlock(wand)==Blocks.DIRT,"other mode material pick failed");
  BuildWandServer.clearMaterials(p);check(cn.quit5700.buildingwand.util.WandStackData.selectedBlock(wand)==Blocks.AIR,"clear material failed");check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.DIRT,"other mode cleared exclusive target");
  cn.quit5700.buildingwand.util.BuildWandData.mode(wand,3);BuildWandServer.clearMaterials(p);check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.AIR,"replacement did not clear both");
 }
 private static void protection(){var p=new BlockPos(132,180,64);for(Block block:List.of(Blocks.REDSTONE_WIRE,Blocks.PISTON,Blocks.WATER,Blocks.CHEST,Blocks.FURNACE,Blocks.CRAFTING_TABLE,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:red_bed")),Blocks.WHEAT,Blocks.OAK_DOOR,Blocks.OAK_SIGN,Blocks.SPAWNER,Blocks.BEACON,Blocks.TORCH,Blocks.LADDER,Blocks.RAIL)){
  world.setBlock(p,block.defaultBlockState(),QUIET);check(ReplacementRules.protectedBlock(world,p,world.getBlockState(p),true),"protected block missed: "+block);
 }world.setBlock(p,Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,true),QUIET);check(ReplacementRules.protectedBlock(world,p,world.getBlockState(p),true),"waterlogged block missed");world.setBlock(p,Blocks.STONE.defaultBlockState(),QUIET);check(!ReplacementRules.protectedBlock(world,p,world.getBlockState(p),false),"ordinary stone protected");world.setBlock(p,Blocks.BEDROCK.defaultBlockState(),QUIET);check(ReplacementRules.protectedBlock(world,p,world.getBlockState(p),false),"survival bedrock unprotected");check(!ReplacementRules.protectedBlock(world,p,world.getBlockState(p),true),"creative bedrock unexpectedly protected");}
 private static void entityProtection(){var p=new BlockPos(136,180,64);world.setBlock(p,Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(p.above(),Blocks.TORCH.defaultBlockState(),QUIET);check(ReplacementRules.protectedPosition(world,p,Blocks.AIR.defaultBlockState(),true),"torch support lost");world.setBlock(p.above(),Blocks.AIR.defaultBlockState(),QUIET);var stand=new net.minecraft.world.entity.decoration.ArmorStand(world,p.getX()+.5,p.getY()+.5,p.getZ()+.5);world.addFreshEntity(stand);check(ReplacementRules.protectedPosition(world,p,Blocks.DIRT.defaultBlockState(),true),"entity collision missed");stand.discard();var frame=new net.minecraft.world.entity.decoration.ItemFrame(world,p.south(),net.minecraft.core.Direction.SOUTH);world.addFreshEntity(frame);check(ReplacementRules.protectedPosition(world,p,Blocks.DIRT.defaultBlockState(),true),"frame support missed");frame.discard();}
 @SuppressWarnings("unchecked") private static void persistence()throws Exception{var tasks=new BuildTaskState();var owner=UUID.randomUUID();String wand=UUID.randomUUID().toString();tasks.add(owner,wand,Blocks.STONE,new cn.quit5700.buildingwand.logic.DeleteSelection.Point(0,0,0),new cn.quit5700.buildingwand.logic.DeleteSelection.Point(2,2,2),3,Blocks.DIRT);var f=BuildTaskState.class.getDeclaredField("CODEC");f.setAccessible(true);var codec=(com.mojang.serialization.Codec<BuildTaskState>)f.get(null);var json=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,tasks).getOrThrow();check(json.toString().contains("minecraft:dirt"),"filter not persisted");var restored=codec.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow();check(restored.busy(owner)&&restored.cancel(owner,wand),"replacement did not restore");tasks=new BuildTaskState();tasks.add(owner,wand,Blocks.STONE,new cn.quit5700.buildingwand.logic.DeleteSelection.Point(0,0,0),new cn.quit5700.buildingwand.logic.DeleteSelection.Point(2,2,2),1);json=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,tasks).getOrThrow();var job=json.getAsJsonObject().getAsJsonArray("jobs").get(0).getAsJsonObject();job.remove("filter");job.remove("skipped");check(codec.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow().busy(owner),"old task compatibility lost");}
 private static void survival(net.minecraft.server.MinecraftServer server)throws Exception{
  var p=new TestPlayer(server);server.getPlayerList().getPlayersByUUID().put(p.getUUID(),p);try{
   var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);p.getInventory().setItem(0,wand);String id=cn.quit5700.buildingwand.util.BuildWandData.ensure(wand);var first=new BlockPos(144,180,64);world.getChunk(first);world.setBlock(first.below(),Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(first,Blocks.DIRT.defaultBlockState(),QUIET);world.setBlock(first.east().below(),Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(first.east(),Blocks.DIRT.defaultBlockState(),QUIET);var tasks=new BuildTaskState();var a=new cn.quit5700.buildingwand.logic.DeleteSelection.Point(first.getX(),first.getY(),first.getZ());var b=new cn.quit5700.buildingwand.logic.DeleteSelection.Point(first.getX()+1,first.getY(),first.getZ());tasks.add(p.getUUID(),id,Blocks.STONE,a,b,3,Blocks.DIRT);
   for(int n=0;n<20;n++)tasks.tick(world);check(tasks.busy(p.getUUID())&&world.getBlockState(first).is(Blocks.DIRT),"missing materials did not pause");p.getInventory().setItem(1,new ItemStack(Blocks.STONE,1));for(int n=0;n<20;n++)tasks.tick(world);check(world.getBlockState(first).is(Blocks.STONE)&&world.getBlockState(first.east()).is(Blocks.DIRT)&&tasks.busy(p.getUUID()),"survival material ratio wrong");p.getInventory().setItem(1,new ItemStack(Blocks.STONE,1));for(int n=0;n<20;n++)tasks.tick(world);check(!tasks.busy(p.getUUID())&&world.getBlockState(first.east()).is(Blocks.STONE)&&p.getInventory().getItem(1).isEmpty(),"refill did not resume");
  }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}
 }

 private static void filterCounts(net.minecraft.server.MinecraftServer server)throws Exception{
  var p=new TestPlayer(server);server.getPlayerList().getPlayersByUUID().put(p.getUUID(),p);try{
   var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);p.getInventory().setItem(0,wand);String id=cn.quit5700.buildingwand.util.BuildWandData.ensure(wand);p.getInventory().setItem(1,new ItemStack(Blocks.STONE,5));var first=new BlockPos(160,180,64);world.getChunk(first);
   var states=List.of(Blocks.DIRT.defaultBlockState(),Blocks.GRAVEL.defaultBlockState(),Blocks.CHEST.defaultBlockState(),wire(0),Blocks.AIR.defaultBlockState(),Blocks.STONE.defaultBlockState());
   for(int n=0;n<states.size();n++){world.setBlock(first.offset(n,-1,0),Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(first.offset(n,0,0),states.get(n),QUIET);}
   ((net.minecraft.world.Container)world.getBlockEntity(first.offset(2,0,0))).setItem(0,new ItemStack(Items.DIAMOND,7));
   var a=new cn.quit5700.buildingwand.logic.DeleteSelection.Point(first.getX(),first.getY(),first.getZ());var b=new cn.quit5700.buildingwand.logic.DeleteSelection.Point(first.getX()+5,first.getY(),first.getZ());var tasks=new BuildTaskState();tasks.add(p.getUUID(),id,Blocks.STONE,a,b,3,Blocks.DIRT);for(int n=0;n<20;n++)tasks.tick(world);
   check(!tasks.busy(p.getUUID())&&world.getBlockState(first).is(Blocks.STONE)&&world.getBlockState(first.east()).is(Blocks.GRAVEL)&&p.getInventory().getItem(1).getCount()==4,"target filter or material count wrong");
   tasks.add(p.getUUID(),id,Blocks.STONE,a,b,3);for(int n=0;n<20;n++)tasks.tick(world);check(!tasks.busy(p.getUUID())&&world.getBlockState(first.east()).is(Blocks.STONE)&&world.getBlockState(first.offset(4,0,0)).isAir(),"unfiltered replacement filled air or failed");
   check(world.getBlockState(first.offset(3,0,0)).is(Blocks.REDSTONE_WIRE)&&((net.minecraft.world.Container)world.getBlockEntity(first.offset(2,0,0))).getItem(0).getCount()==7,"protected block or inventory changed");
   var args=((net.minecraft.network.chat.contents.TranslatableContents)p.lastMessage.getContents()).getArgs();check(args[0].equals("1")&&args[1].equals("2"),"replacement/protection counts wrong");check(p.getInventory().getItem(1).getCount()==3,"skipped blocks consumed material");check(world.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(first).inflate(8)).isEmpty(),"old blocks dropped items");
  }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}
 }

}
