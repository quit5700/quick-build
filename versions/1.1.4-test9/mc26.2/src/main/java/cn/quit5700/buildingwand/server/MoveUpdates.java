package cn.quit5700.buildingwand.server;
import cn.quit5700.light.mixin.MoveBlockEventsAccessor;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.*;
import java.util.*;
/** Captures scheduled effects before mutation; server-thread move is a single transaction. */
final class MoveUpdates {
 private final ServerLevel world;private final Set<BlockPos> affected;private final BoundingBox sourceBox,targetBox;
 private final List<ScheduledTick<Block>> blocks=new ArrayList<>();private final List<ScheduledTick<Fluid>> fluids=new ArrayList<>();private final List<BlockEventData> events=new ArrayList<>(),deferred=new ArrayList<>();
 MoveUpdates(ServerLevel world,Set<BlockPos> source,Set<BlockPos> target){
  this.world=world;affected=new HashSet<>(source);affected.addAll(target);sourceBox=box(source);targetBox=box(target);
  Set<Long> chunks=new HashSet<>();for(var p:affected)chunks.add(ChunkPos.pack(p.getX()>>4,p.getZ()>>4));
  for(long key:chunks){var chunk=world.getChunk(ChunkPos.getX(key),ChunkPos.getZ(key));chunk.unpackTicks(world.getGameTime());
   if(chunk.getBlockTicks() instanceof LevelChunkTicks<?> t)t.getAll().filter(a->affected.contains(a.pos())).forEach(a->{@SuppressWarnings("unchecked")var tick=(ScheduledTick<Block>)a;blocks.add(tick);});
   if(chunk.getFluidTicks() instanceof LevelChunkTicks<?> t)t.getAll().filter(a->affected.contains(a.pos())).forEach(a->{@SuppressWarnings("unchecked")var tick=(ScheduledTick<Fluid>)a;fluids.add(tick);});
  }
  var access=(MoveBlockEventsAccessor)world;for(var event:access.q5$blockEvents())if(affected.contains(event.pos()))events.add(event);for(var event:access.q5$deferredEvents())if(affected.contains(event.pos()))deferred.add(event);
 }
 private static BoundingBox box(Set<BlockPos> positions){int x1=Integer.MAX_VALUE,y1=x1,z1=x1,x2=Integer.MIN_VALUE,y2=x2,z2=x2;for(var p:positions){x1=Math.min(x1,p.getX());y1=Math.min(y1,p.getY());z1=Math.min(z1,p.getZ());x2=Math.max(x2,p.getX());y2=Math.max(y2,p.getY());z2=Math.max(z2,p.getZ());}return new BoundingBox(x1,y1,z1,x2,y2,z2);}
 private void clear(){world.getBlockTicks().clearArea(sourceBox);world.getBlockTicks().clearArea(targetBox);world.getFluidTicks().clearArea(sourceBox);world.getFluidTicks().clearArea(targetBox);var a=(MoveBlockEventsAccessor)world;a.q5$blockEvents().removeIf(e->affected.contains(e.pos()));a.q5$deferredEvents().removeIf(e->affected.contains(e.pos()));}
 void commit(Map<BlockPos,BlockPos> destinations,int rotation){
  clear();for(var t:blocks){var p=destinations.get(t.pos());if(p!=null)world.getBlockTicks().schedule(new ScheduledTick<>(t.type(),p,t.triggerTick(),t.priority(),t.subTickOrder()));}
  for(var t:fluids){var p=destinations.get(t.pos());if(p!=null)world.getFluidTicks().schedule(new ScheduledTick<>(t.type(),p,t.triggerTick(),t.priority(),t.subTickOrder()));}
  for(var e:events)moveEvent(e,destinations,rotation);for(var e:deferred)moveEvent(e,destinations,rotation);
 }
 private void moveEvent(BlockEventData e,Map<BlockPos,BlockPos> destinations,int rotation){var p=destinations.get(e.pos());if(p==null)return;int param=e.paramB();if(e.block() instanceof PistonBaseBlock&&(param&7)<6){Direction d=Direction.from3DDataValue(param&7);for(int i=0;i<(rotation&3);i++)if(d.getAxis().isHorizontal())d=d.getClockWise();param=(param&~7)|d.get3DDataValue();}world.blockEvent(p,e.block(),e.paramA(),param);}
 void restore(){clear();for(var t:blocks)world.getBlockTicks().schedule(t);for(var t:fluids)world.getFluidTicks().schedule(t);var a=(MoveBlockEventsAccessor)world;a.q5$blockEvents().addAll(events);a.q5$deferredEvents().addAll(deferred);}
 static int flags(){return Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SKIP_ALL_SIDEEFFECTS;}
}
