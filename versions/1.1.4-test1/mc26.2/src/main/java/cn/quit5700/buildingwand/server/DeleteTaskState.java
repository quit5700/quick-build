package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.logic.DeleteCursor;
import cn.quit5700.buildingwand.logic.DeleteTaskData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Container;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
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
    // A distinct registered ticket prevents interference with vanilla or other mods' tickets.
    private static final TicketType TICKET=Registry.register(BuiltInRegistries.TICKET_TYPE,BuildingWandMod.id("delete_wand_chunk"),new TicketType(40L,0));
    private final List<Job> jobs=new ArrayList<>();
    public DeleteTaskState(){}
    private DeleteTaskState(List<DeleteTaskData> saved){for(DeleteTaskData d:saved)jobs.add(new Job(d));}
    public static void initialize(){} // Force ticket registration before registries freeze.
    public static DeleteTaskState get(ServerLevel w){return w.getDataStorage().computeIfAbsent(TYPE);}
    public boolean busy(UUID owner){return jobs.stream().anyMatch(j->j.owner.equals(owner));}
    public void add(UUID owner,DeleteBox box){jobs.add(new Job(owner,box));setDirty();}
    public void release(ServerLevel world){for(Job j:jobs)j.release(world);}
    public void tick(ServerLevel world) {
        if(jobs.isEmpty())return;
        long deadline=System.nanoTime()+4_000_000L;
        int budget=2048;
        for(var iterator=jobs.iterator();iterator.hasNext() && budget>0 && System.nanoTime()<deadline;){
            Job job=iterator.next();
            ServerPlayer owner=world.getServer().getPlayerList().getPlayer(job.owner);
            if(owner==null || owner.level()!=world || !owner.mayBuild() || owner.isSpectator()){job.release(world);continue;}
            DeleteCursor c=job.cursor;
            ChunkPos needed=new ChunkPos(c.chunkX(),c.chunkZ());
            if(!needed.equals(job.loaded)) {
                job.release(world);job.loaded=needed;
                world.getChunkSource().addTicketWithRadius(TICKET,needed,0);
                job.loading=world.getChunkSource().getChunkFuture(needed.x(),needed.z(),ChunkStatus.FULL,true);
            } else world.getChunkSource().addTicketWithRadius(TICKET,needed,0);
            // Do not join incomplete chunk futures on the server thread.
            if(job.loading!=null && (!job.loading.isDone() || job.loading.isCompletedExceptionally()))continue;
            if(world.getChunkSource().getChunkNow(needed.x(),needed.z())==null)continue;
            while(budget>0 && !c.done() && c.chunkX()==needed.x() && c.chunkZ()==needed.z() && System.nanoTime()<deadline){
                BlockPos pos=new BlockPos(c.x(),c.y(),c.z());
                if(!world.isOutsideBuildHeight(pos) && world.getWorldBorder().isWithinBounds(pos) && world.mayInteract(owner,pos)) {
                    if(!world.getBlockState(pos).isAir()) {
                        if(world.getBlockEntity(pos) instanceof Container container)container.clearContent();
                        if(world.setBlock(pos,Blocks.AIR.defaultBlockState(),3))job.removed=job.removed.add(BigInteger.ONE);
                    }
                }
                c.advance();job.visited=job.visited.add(BigInteger.ONE);budget--;setDirty();
            }
            if(c.done()) {
                owner.sendSystemMessage(Component.translatable("message.quick_build.delete_done",job.removed.toString()));
                job.release(world);iterator.remove();setDirty();
            } else if(world.getGameTime()%40==0) {
                owner.sendOverlayMessage(Component.translatable("message.quick_build.delete_progress",job.visited.toString(),job.box.volume().toString(),job.removed.toString()));
            }
        }
    }

    private static final class Job {
        private final UUID owner;
        private final DeleteBox box;
        private final DeleteCursor cursor;
        private BigInteger visited=BigInteger.ZERO,removed=BigInteger.ZERO;
        private ChunkPos loaded;
        private java.util.concurrent.CompletableFuture<?> loading;
        Job(UUID owner,DeleteBox box){this.owner=owner;this.box=box;this.cursor=new DeleteCursor(box);}
        Job(DeleteTaskData data) {
            owner=UUID.fromString(data.owner());
            List<Integer>b=data.bounds(),c=data.cursor();
            if(b.size()!=6 || c.size()!=3)throw new IllegalArgumentException("Invalid deletion data");
            box=new DeleteBox(b.get(0),b.get(1),b.get(2),b.get(3),b.get(4),b.get(5));
            cursor=new DeleteCursor(box,c.get(0),c.get(1),c.get(2),false);
            visited=new BigInteger(data.visited());removed=new BigInteger(data.removed());
            if(visited.signum()<0 || visited.compareTo(box.volume())>=0 || removed.signum()<0 || removed.compareTo(visited)>0)
                throw new IllegalArgumentException("Invalid deletion progress");
        }
        DeleteTaskData data(){return new DeleteTaskData(owner.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),
            List.of(cursor.x(),cursor.y(),cursor.z()),visited.toString(),removed.toString());}
        void release(ServerLevel world){if(loaded!=null)world.getChunkSource().removeTicketWithRadius(TICKET,loaded,0);loaded=null;loading=null;}
    }
}
