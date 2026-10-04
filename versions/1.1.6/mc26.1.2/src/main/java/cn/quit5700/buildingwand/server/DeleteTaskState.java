package cn.quit5700.buildingwand.server;

import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.logic.*;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.util.DeleteWandData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DeleteTaskState extends SavedData {
    private static final Codec<DeleteTaskState> CODEC=RecordCodecBuilder.create(i->i.group(
        DeleteTaskData.CODEC.listOf().optionalFieldOf("jobs",List.of()).forGetter(s->s.jobs.stream().map(Job::data).toList())
    ).apply(i,DeleteTaskState::new));
    private static final SavedDataType<DeleteTaskState> TYPE=new SavedDataType<>(BuildingWandMod.id("delete_wand_tasks"),DeleteTaskState::new,CODEC,DataFixTypes.LEVEL);
    private final List<Job> jobs=new ArrayList<>();
    public DeleteTaskState(){}
    private DeleteTaskState(List<DeleteTaskData> saved){for(DeleteTaskData d:saved)jobs.add(new Job(d));}
    public static void initialize(){DeleteChunkLease.initialize();}
    public static DeleteTaskState get(ServerLevel world){return world.getDataStorage().computeIfAbsent(TYPE);}
    public boolean busy(UUID owner){return jobs.stream().anyMatch(j->j.owner.equals(owner));}
    public void add(UUID owner,DeleteSelection.Point first,DeleteSelection.Point second,String wand){
        jobs.add(new Job(owner,first,second,wand));setDirty();
    }
    public void release(ServerLevel world){for(Job job:jobs)job.lease.release();}
    public static boolean eligible(ServerLevel world,ServerPlayer player,BlockPos pos){
        return !world.isOutsideBuildHeight(pos)&&world.getWorldBorder().isWithinBounds(pos)&&world.mayInteract(player,pos)
                && DeleteWear.removable(world.getBlockState(pos).isAir(),world.getBlockState(pos).is(Blocks.BEDROCK),player.isCreative());
    }
    public void tick(ServerLevel world){
        if(jobs.isEmpty())return;
        long deadline=System.nanoTime()+4_000_000L;int budget=2048;
        for(var iterator=jobs.iterator();iterator.hasNext()&&budget>0&&System.nanoTime()<deadline;){
            Job job=iterator.next();ServerPlayer owner=world.getServer().getPlayerList().getPlayer(job.owner);
            if(owner==null||owner.level()!=world||!owner.mayBuild()||owner.isSpectator()){job.lease.release();continue;}
            if(job.wand.isEmpty()){
                ItemStack held=owner.getMainHandItem();if(!held.is(ModItems.DELETE_WAND)){job.lease.release();continue;}
                job.wand=DeleteWandData.ensureId(held);setDirty();
            }
            ItemStack tool=DeleteWandData.find(owner,job.wand);
            if(tool.isEmpty()){
                job.lease.release();
                if(!job.paused){owner.sendSystemMessage(Component.translatable("message.quick_build.delete_tool_missing"));job.paused=true;}
                continue;
            }
            job.paused=false;
            if(!owner.isCreative()&&DeleteWandData.capacity(tool)<=0){
                job.lease.release();owner.sendSystemMessage(Component.translatable("message.quick_build.delete_no_durability").withStyle(ChatFormatting.RED));
                iterator.remove();setDirty();continue;
            }
            DirectedDeleteCursor cursor=job.cursor;int cx=cursor.chunkX(),cz=cursor.chunkZ();
            if(!job.lease.ready(world,cx,cz))continue;
            int minSection=Integer.MAX_VALUE,maxSection=Integer.MIN_VALUE;boolean broken=false;
            while(budget>0&&!cursor.done()&&cursor.chunkX()==cx&&cursor.chunkZ()==cz&&System.nanoTime()<deadline){
                BlockPos pos=new BlockPos(cursor.x(),cursor.y(),cursor.z());
                if(eligible(world,owner,pos)){
                    var entity=world.getBlockEntity(pos);
                    Container container=entity instanceof Container c?c:null;
                    List<ItemStack> contents=new ArrayList<>();
                    if(container!=null){for(int n=0;n<container.getContainerSize();n++)contents.add(container.getItem(n).copy());container.clearContent();}
                    if(world.setBlock(pos,Blocks.AIR.defaultBlockState(),3)){
                        job.removed=job.removed.add(BigInteger.ONE);
                        minSection=Math.min(minSection,pos.getY()>>4);maxSection=Math.max(maxSection,pos.getY()>>4);
                        broken=DeleteWandData.charge(tool,owner);
                    }else if(container!=null&&world.getBlockEntity(pos)==entity){
                        for(int n=0;n<contents.size();n++)container.setItem(n,contents.get(n));
                    }
                }
                cursor.advance();job.visited=job.visited.add(BigInteger.ONE);budget--;setDirty();
                if(broken)break;
            }
            if(minSection!=Integer.MAX_VALUE){
                var packet=new DeleteNetworking.Refresh(world.dimension().identifier().toString(),cx,cz,minSection,maxSection);
                for(ServerPlayer observer:world.getPlayers(p->true))net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(observer,packet);
            }
            if(broken||cursor.done()){
                String key=broken?"message.quick_build.delete_broken":"message.quick_build.delete_done";
                var message=Component.translatable(key,job.removed.toString());
                owner.sendSystemMessage(broken?message.withStyle(ChatFormatting.RED):message);
                job.lease.release();iterator.remove();setDirty();
            }else if(world.getGameTime()%40==0){
                owner.sendOverlayMessage(Component.translatable("message.quick_build.delete_progress",job.visited.toString(),job.box.volume().toString(),job.removed.toString()));
            }
        }
    }
    private static final class Job {
        private final UUID owner;private final DeleteBox box;private final DirectedDeleteCursor cursor;
        private String wand;private boolean paused;
        private BigInteger visited=BigInteger.ZERO,removed=BigInteger.ZERO;
        private final DeleteChunkLease lease=new DeleteChunkLease();
        Job(UUID owner,DeleteSelection.Point first,DeleteSelection.Point second,String wand){
            this.owner=owner;this.wand=wand;
            box=DeleteBox.between(first.x(),first.y(),first.z(),second.x(),second.y(),second.z());
            cursor=new DirectedDeleteCursor(box,second.x()>=first.x()?1:-1,second.y()>=first.y()?1:-1,second.z()>=first.z()?1:-1);
        }
        Job(DeleteTaskData data){
            owner=UUID.fromString(data.owner());box=data.box();cursor=data.restoreDirectedCursor();wand=data.wand();
            visited=data.visitedCount();removed=data.removedCount();
        }
        DeleteTaskData data(){return new DeleteTaskData(owner.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),
                List.of(cursor.x(),cursor.y(),cursor.z()),visited.toString(),removed.toString(),List.of(cursor.dx(),cursor.dy(),cursor.dz()),wand);}
    }
}
