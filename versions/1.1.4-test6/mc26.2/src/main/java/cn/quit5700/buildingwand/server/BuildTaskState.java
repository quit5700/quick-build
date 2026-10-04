package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.logic.*;
import cn.quit5700.buildingwand.util.BuildWandData;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.Container;
import net.minecraft.world.level.saveddata.*;
import java.math.BigInteger;
import java.util.*;
public final class BuildTaskState extends SavedData {
 private record Data(DeleteTaskData progress,String block,int mode){static final Codec<Data> CODEC=RecordCodecBuilder.create(i->i.group(DeleteTaskData.CODEC.fieldOf("progress").forGetter(Data::progress),Codec.STRING.fieldOf("block").forGetter(Data::block),Codec.intRange(1,2).fieldOf("mode").forGetter(Data::mode)).apply(i,Data::new));}
 private static final Codec<BuildTaskState> CODEC=RecordCodecBuilder.create(i->i.group(Data.CODEC.listOf().optionalFieldOf("jobs",List.of()).forGetter(s->s.jobs.stream().map(Job::data).toList())).apply(i,BuildTaskState::new));
 private static final SavedDataType<BuildTaskState> TYPE=new SavedDataType<>(BuildingWandMod.id("build_tasks"),BuildTaskState::new,CODEC,null);
 private final List<Job> jobs=new ArrayList<>();public BuildTaskState(){}private BuildTaskState(List<Data> saved){for(var d:saved)jobs.add(new Job(d));}
 public static BuildTaskState get(ServerLevel w){return w.getDataStorage().computeIfAbsent(TYPE);}
 public boolean busy(UUID p){return jobs.stream().anyMatch(j->j.owner.equals(p));}
 public void add(UUID p,String wand,Block block,DeleteSelection.Point a,DeleteSelection.Point b,int mode){var box=DeleteBox.between(a.x(),a.y(),a.z(),b.x(),b.y(),b.z());jobs.add(new Job(new Data(new DeleteTaskData(p.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),List.of(a.x(),a.y(),a.z()),"0","0",List.of(b.x()>=a.x()?1:-1,b.y()>=a.y()?1:-1,b.z()>=a.z()?1:-1),wand),BuiltInRegistries.BLOCK.getKey(block).toString(),mode)));setDirty();}
 public boolean cancel(UUID p,String wand){boolean found=jobs.removeIf(j->{if(j.owner.equals(p)&&j.wand.equals(wand)){j.lease.release();return true;}return false;});if(found)setDirty();return found;}
 public void release(){for(var j:jobs)j.lease.release();}
 public void tick(ServerLevel w){
  int budget=2048;long deadline=System.nanoTime()+4_000_000;
  for(var it=jobs.iterator();it.hasNext()&&budget>0&&System.nanoTime()<deadline;){var j=it.next();var p=w.getServer().getPlayerList().getPlayer(j.owner);
   if(p==null||p.level()!=w||!p.mayBuild()||p.isSpectator()||BuildWandData.find(p,j.wand).isEmpty()){j.lease.release();continue;}
   var c=j.cursor;int cx=c.chunkX(),cz=c.chunkZ();if(!j.lease.ready(w,cx,cz))continue;int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
   while(budget>0&&!c.done()&&c.chunkX()==cx&&c.chunkZ()==cz&&System.nanoTime()<deadline){
    budget--;BlockPos pos=new BlockPos(c.x(),c.y(),c.z());boolean include=BuildPlan.includes(j.box,c.x(),c.y(),c.z(),j.mode);
    if(include){
     var state=w.getBlockState(pos);ItemStack material=ItemStack.EMPTY;
     if(!w.isInWorldBounds(pos)||!w.getWorldBorder().isWithinBounds(pos)||!w.mayInteract(p,pos)){pause(j,p,"message.q5.build_blocked");break;}
     if(!p.isCreative()){
      if(!w.mayInteract(p,pos)||!w.getWorldBorder().isWithinBounds(pos)||(!state.isAir()&&!state.canBeReplaced())||!supported(w,pos)) {pause(j,p,"message.q5.build_blocked");break;}
      for(int n=0;n<p.getInventory().getContainerSize();n++){var s=p.getInventory().getItem(n);if(s.getItem() instanceof BlockItem bi&&bi.getBlock()==j.block&&!s.isEmpty()){material=s;break;}}
      if(material.isEmpty()){pause(j,p,"message.q5.build_materials");break;}
     }
     var entity=w.getBlockEntity(pos);List<ItemStack> contents=new ArrayList<>();Container container=entity instanceof Container co?co:null;
     if(container!=null){for(int n=0;n<container.getContainerSize();n++)contents.add(container.getItem(n).copy());container.clearContent();}
     boolean success=state.equals(j.block.defaultBlockState())||w.setBlock(pos,j.block.defaultBlockState(),Block.UPDATE_ALL);
     if(!success){if(container!=null&&w.getBlockEntity(pos)==entity)for(int n=0;n<contents.size();n++)container.setItem(n,contents.get(n));pause(j,p,"message.q5.build_blocked");break;}
     if(!p.isCreative())material.shrink(1);j.placed=j.placed.add(BigInteger.ONE);min=Math.min(min,pos.getY()>>4);max=Math.max(max,pos.getY()>>4);j.paused="";
    }
    c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();
   }
   p.inventoryMenu.broadcastChanges();
   if(min!=Integer.MAX_VALUE){var packet=new DeleteNetworking.Refresh(w.dimension().identifier().toString(),cx,cz,min,max);for(var observer:w.getPlayers(o->true))net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(observer,packet);}
   if(c.done()){j.lease.release();it.remove();setDirty();BuildWandServer.message(p,"message.q5.build_done",j.placed.toString());}
   else if(w.getGameTime()%40==0){BuildWandServer.message(p,j.paused.isEmpty()?"message.q5.build_progress":j.paused,j.placed.toString(),BuildPlan.total(j.box,j.mode).toString());}
  }
 }
 private static void pause(Job j,ServerPlayer p,String key){if(!key.equals(j.paused))BuildWandServer.message(p,key,j.placed.toString(),BuildPlan.total(j.box,j.mode).toString());j.paused=key;}
 private static boolean supported(ServerLevel w,BlockPos pos){for(var d:Direction.values()){var s=w.getBlockState(pos.relative(d));if(!s.isAir()&&!s.canBeReplaced())return true;}return false;}
 private static final class Job {
  final UUID owner;final String wand;final DeleteBox box;final DirectedDeleteCursor cursor;final Block block;final int mode;final DeleteChunkLease lease=new DeleteChunkLease();BigInteger visited,placed;String paused="";
  Job(Data d){var p=d.progress;owner=UUID.fromString(p.owner());wand=p.wand();box=p.box();cursor=p.restoreDirectedCursor();block=BuiltInRegistries.BLOCK.getValue(Identifier.parse(d.block));if(block==null||block==Blocks.AIR)throw new IllegalArgumentException("Invalid build material");mode=d.mode;visited=p.visitedCount();placed=p.removedCount();}
  Data data(){return new Data(new DeleteTaskData(owner.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),List.of(cursor.x(),cursor.y(),cursor.z()),visited.toString(),placed.toString(),List.of(cursor.dx(),cursor.dy(),cursor.dz()),wand),BuiltInRegistries.BLOCK.getKey(block).toString(),mode);}
 }
}
