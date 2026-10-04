package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.BuildingWandMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import java.util.HashMap;
import java.util.Map;

/** Reference counts prevent one task releasing a ticket used by another. */
public final class DeleteChunkLease {
    private static final TicketType<ChunkPos> TYPE=TicketType.create("q5_delete_wand_chunk",java.util.Comparator.comparingLong(ChunkPos::toLong),40);
    private static final Map<ServerLevel,Map<ChunkPos,Integer>> COUNTS=new HashMap<>();
    private ServerLevel world;private ChunkPos pos;private java.util.concurrent.CompletableFuture<?> future;
    public static void initialize(){}
    public boolean ready(ServerLevel world,int x,int z){
        ChunkPos next=new ChunkPos(x,z);
        if(this.world!=world||!next.equals(pos)){
            release();this.world=world;pos=next;
            COUNTS.computeIfAbsent(world,w->new HashMap<>()).merge(pos,1,Integer::sum);
            world.getChunkSource().addRegionTicket(TYPE,pos,2,pos);
            future=world.getChunkSource().getChunkFuture(x,z,ChunkStatus.FULL,true);
        }
        return future.isDone()&&!future.isCompletedExceptionally()&&world.getChunkSource().getChunkNow(x,z)!=null;
    }
    public void release(){
        if(world==null)return;Map<ChunkPos,Integer> counts=COUNTS.get(world);
        if(counts!=null){int count=counts.getOrDefault(pos,1)-1;if(count==0){counts.remove(pos);world.getChunkSource().removeRegionTicket(TYPE,pos,2,pos);}else counts.put(pos,count);if(counts.isEmpty())COUNTS.remove(world);}
        world=null;pos=null;future=null;
    }
    public static void refreshAll(){for(var entry:COUNTS.entrySet())for(ChunkPos pos:entry.getValue().keySet())entry.getKey().getChunkSource().addRegionTicket(TYPE,pos,2,pos);}
}
