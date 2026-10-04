package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.logic.*;
import cn.quit5700.buildingwand.util.BuildWandData;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.Container;
import net.minecraft.world.level.saveddata.SavedData;
import java.math.BigInteger;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
public final class BuildTaskState extends SavedData {
 private record Data(DeleteTaskData progress,String block,int mode,String filter,String skipped){static final Codec<Data> CODEC=RecordCodecBuilder.create(i->i.group(DeleteTaskData.CODEC.fieldOf("progress").forGetter(Data::progress),Codec.STRING.fieldOf("block").forGetter(Data::block),Codec.intRange(1,3).fieldOf("mode").forGetter(Data::mode),Codec.STRING.optionalFieldOf("filter","").forGetter(Data::filter),Codec.STRING.optionalFieldOf("skipped","0").forGetter(Data::skipped)).apply(i,Data::new));}
 private static final Codec<BuildTaskState> CODEC=RecordCodecBuilder.create(i->i.group(Data.CODEC.listOf().optionalFieldOf("jobs",List.of()).forGetter(s->s.jobs.stream().map(Job::data).toList())).apply(i,BuildTaskState::new));
 private static final String SAVE_ID="building_wand_build_tasks";
 private static final SavedData.Factory<BuildTaskState> TYPE=new SavedData.Factory<>(()->new BuildTaskState(),tag->CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet(()->new BuildTaskState()),DataFixTypes.LEVEL);
 @Override public CompoundTag save(CompoundTag tag){CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);return tag;}
 private final List<Job> jobs=new ArrayList<>();public BuildTaskState(){}private BuildTaskState(List<Data> saved){for(var d:saved)jobs.add(new Job(d));}
 public static BuildTaskState get(ServerLevel w){return w.getDataStorage().computeIfAbsent(TYPE,SAVE_ID);}
 public boolean busy(UUID p){return jobs.stream().anyMatch(j->j.owner.equals(p));}
 public void add(UUID p,String wand,Block block,DeleteSelection.Point a,DeleteSelection.Point b,int mode){add(p,wand,block,a,b,mode,Blocks.AIR);}
 public void add(UUID p,String wand,Block block,DeleteSelection.Point a,DeleteSelection.Point b,int mode,Block filter){var box=DeleteBox.between(a.x(),a.y(),a.z(),b.x(),b.y(),b.z());jobs.add(new Job(new Data(new DeleteTaskData(p.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),List.of(a.x(),a.y(),a.z()),"0","0",List.of(b.x()>=a.x()?1:-1,b.y()>=a.y()?1:-1,b.z()>=a.z()?1:-1),wand),BuiltInRegistries.BLOCK.getKey(block).toString(),mode,filter==Blocks.AIR?"":BuiltInRegistries.BLOCK.getKey(filter).toString(),"0")));setDirty();}
 public boolean cancel(UUID p,String wand){boolean found=jobs.removeIf(j->{if(j.owner.equals(p)&&j.wand.equals(wand)){j.lease.release();return true;}return false;});if(found)setDirty();return found;}
 public int cancelAll(UUID owner){int count=0;for(var it=jobs.iterator();it.hasNext();){var job=it.next();if(job.owner.equals(owner)){job.lease.release();it.remove();count++;}}if(count>0)setDirty();return count;}
 public void release(){for(var j:jobs)j.lease.release();}
 public void tick(ServerLevel w){
  int budget=2048;long deadline=System.nanoTime()+4_000_000;
  for(var it=jobs.iterator();it.hasNext()&&budget>0&&System.nanoTime()<deadline;){var j=it.next();var p=w.getServer().getPlayerList().getPlayer(j.owner);
   if(p==null||p.level()!=w||!p.mayBuild()||p.isSpectator()||BuildWandData.find(p,j.wand).isEmpty()){j.lease.release();continue;}
   var c=j.cursor;int cx=c.chunkX(),cz=c.chunkZ();if(!j.lease.ready(w,cx,cz))continue;int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
   while(budget>0&&!c.done()&&c.chunkX()==cx&&c.chunkZ()==cz&&System.nanoTime()<deadline){
    budget--;BlockPos pos=new BlockPos(c.x(),c.y(),c.z());
    if(StructurePlacement.multiple(j.block)){
     var group=StructurePlacement.plan(j.block,j.box,pos,j.mode,c.dx(),c.dy(),c.dz());
     if(!group.isEmpty()){
      boolean clearing=group.values().stream().allMatch(net.minecraft.world.level.block.state.BlockState::isAir);
      boolean blocked=false,protectedGroup=false,match=true,noop=true;
      for(var e:group.entrySet()){
       var at=e.getKey();w.getChunk(at);var old=w.getBlockState(at);
       blocked|=!w.isInWorldBounds(at)||!w.getWorldBorder().isWithinBounds(at)||!w.mayInteract(p,at);
       if(j.mode==3){match&=!old.isAir()&&(j.filter==Blocks.AIR||old.is(j.filter));protectedGroup|=ReplacementRules.protectedPosition(w,at,e.getValue(),p.isCreative());}
       else if(!p.isCreative()&&!clearing)blocked|=!old.isAir()&&!old.canBeReplaced();
       noop&=old.equals(e.getValue());
      }
      if(j.mode==3&&(!match||protectedGroup)){if(protectedGroup)j.skipped=j.skipped.add(BigInteger.valueOf(group.size()));}
      else if(!noop){
       if(blocked){pause(j,p,"message.q5.build_blocked");break;}
       ItemStack material=ItemStack.EMPTY;
       if(!p.isCreative()&&!clearing){for(int n=0;n<p.getInventory().getContainerSize();n++){var st=p.getInventory().getItem(n);if(st.getItem() instanceof BlockItem bi&&bi.getBlock()==j.block&&!st.isEmpty()){material=st;break;}}if(material.isEmpty()){pause(j,p,"message.q5.build_materials");break;}}
       if(!(clearing&&j.mode!=3?StructurePlacement.clearRemainder(w,group):StructurePlacement.placeNatural(w,group))){pause(j,p,"message.q5.build_blocked");break;}
       if(!p.isCreative()&&!clearing)material.shrink(1);
       if(!clearing)j.placed=j.placed.add(BigInteger.valueOf(group.size()));j.paused="";
       for(var at:group.keySet()){min=Math.min(min,at.getY()>>4);max=Math.max(max,at.getY()>>4);}
      }
     }
     c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();continue;
    }
    boolean include=BuildPlan.includes(j.box,c.x(),c.y(),c.z(),j.mode);
    if(include){
     var state=w.getBlockState(pos);ItemStack material=ItemStack.EMPTY;
     if(j.mode==3){
      boolean match=j.filter==Blocks.AIR||state.is(j.filter);
      if(state.isAir()||!match||state.is(j.block)){c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();continue;}
      if(ReplacementRules.protectedPosition(w,pos,j.block.defaultBlockState(),p.isCreative())){j.skipped=j.skipped.add(BigInteger.ONE);c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();continue;}
     }
     if(!w.isInWorldBounds(pos)||!w.getWorldBorder().isWithinBounds(pos)||!w.mayInteract(p,pos)){pause(j,p,"message.q5.build_blocked");break;}
     if(!p.isCreative()){
      if(!w.mayInteract(p,pos)||!w.getWorldBorder().isWithinBounds(pos)||(j.mode!=3&&!state.isAir()&&!state.canBeReplaced())) {pause(j,p,"message.q5.build_blocked");break;}
      for(int n=0;n<p.getInventory().getContainerSize();n++){var s=p.getInventory().getItem(n);if(s.getItem() instanceof BlockItem bi&&bi.getBlock()==j.block&&!s.isEmpty()){material=s;break;}}
      if(material.isEmpty()){pause(j,p,"message.q5.build_materials");break;}
     }
     var entity=w.getBlockEntity(pos);List<ItemStack> contents=new ArrayList<>();Container container=entity instanceof Container co?co:null;
     if(container!=null){for(int n=0;n<container.getContainerSize();n++)contents.add(container.getItem(n).copy());container.clearContent();}
     boolean success=state.equals(j.block.defaultBlockState())||StructurePlacement.placeNatural(w,StructurePlacement.single(j.block,pos));
     if(!success){if(container!=null&&w.getBlockEntity(pos)==entity)for(int n=0;n<contents.size();n++)container.setItem(n,contents.get(n));pause(j,p,"message.q5.build_blocked");break;}
     if(!p.isCreative())material.shrink(1);j.placed=j.placed.add(BigInteger.ONE);min=Math.min(min,pos.getY()>>4);max=Math.max(max,pos.getY()>>4);j.paused="";
    }
    c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();
   }
   p.inventoryMenu.broadcastChanges();
   if(min!=Integer.MAX_VALUE){var packet=new DeleteNetworking.Refresh(w.dimension().location().toString(),cx,cz,min,max);for(var observer:w.getPlayers(o->true))cn.quit5700.buildingwand.network.LegacyWandPackets.send(observer,packet);}
   if(c.done()){j.lease.release();it.remove();setDirty();BuildWandServer.message(p,j.mode==3?"message.q5.replace_done":"message.q5.build_done",j.placed.toString(),j.skipped.toString());}
   else if(w.getGameTime()%40==0){if(j.mode==3&&j.paused.isEmpty())BuildWandServer.message(p,"message.q5.replace_progress",j.visited.toString(),j.placed.toString(),j.skipped.toString());else BuildWandServer.message(p,j.paused.isEmpty()?"message.q5.build_progress":j.paused,j.placed.toString(),StructurePlacement.total(j.block,j.box,j.mode).toString());}
  }
 }
 private static void pause(Job j,ServerPlayer p,String key){if(!key.equals(j.paused))BuildWandServer.message(p,key,j.placed.toString(),StructurePlacement.total(j.block,j.box,j.mode).toString());j.paused=key;}
 private static boolean supported(ServerLevel w,BlockPos pos){for(var d:Direction.values()){var s=w.getBlockState(pos.relative(d));if(!s.isAir()&&!s.canBeReplaced())return true;}return false;}
 private static final class Job {
  final UUID owner;final String wand;final DeleteBox box;final DirectedDeleteCursor cursor;final Block block,filter;final int mode;final DeleteChunkLease lease=new DeleteChunkLease();BigInteger visited,placed,skipped;String paused="";
  Job(Data d){var p=d.progress;owner=UUID.fromString(p.owner());wand=p.wand();box=p.box();cursor=p.restoreDirectedCursor();block=BuiltInRegistries.BLOCK.get(new ResourceLocation(d.block));if(block==null||block==Blocks.AIR)throw new IllegalArgumentException("Invalid build material");mode=d.mode;filter=d.filter.isEmpty()?Blocks.AIR:BuiltInRegistries.BLOCK.get(new ResourceLocation(d.filter));if(filter==null)throw new IllegalArgumentException("Invalid replacement filter");skipped=new BigInteger(d.skipped);if(skipped.signum()<0)throw new IllegalArgumentException("Invalid skipped count");visited=p.visitedCount();placed=p.removedCount();}
  Data data(){return new Data(new DeleteTaskData(owner.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),List.of(cursor.x(),cursor.y(),cursor.z()),visited.toString(),placed.toString(),List.of(cursor.dx(),cursor.dy(),cursor.dz()),wand),BuiltInRegistries.BLOCK.getKey(block).toString(),mode,filter==Blocks.AIR?"":BuiltInRegistries.BLOCK.getKey(filter).toString(),skipped.toString());}
 }
}
