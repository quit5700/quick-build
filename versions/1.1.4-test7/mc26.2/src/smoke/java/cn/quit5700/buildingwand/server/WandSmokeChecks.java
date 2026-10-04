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
  System.out.println(failures==0?"Q5_MOVE_REGRESSION_PASS: 7 cases":"Q5_MOVE_REGRESSION_FAIL: "+failures);server.halt(false);});}
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
}
