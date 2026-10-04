package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.logic.SectionRefreshQueue;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class DeleteMeshRefresh {
    private static final SectionRefreshQueue QUEUE=new SectionRefreshQueue();
    private static long tick;private static String dimension="";
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(DeleteNetworking.Refresh.ID,(p,c)->c.client().execute(()->{
            var world=c.client().level;if(world==null||!world.dimension().location().toString().equals(p.dimension()))return;
            if(!dimension.equals(p.dimension())){QUEUE.clear();dimension=p.dimension();}
            if(p.minY()<=p.maxY())QUEUE.offer(p.x(),p.z(),p.minY(),p.maxY(),tick);
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{QUEUE.clear();dimension="";});
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            tick++;if(client.level==null){QUEUE.clear();dimension="";return;}
            String current=client.level.dimension().location().toString();
            if(!current.equals(dimension)){QUEUE.clear();dimension=current;}
            for(var refresh:QUEUE.drain(tick,2)){
                if(client.level.hasChunk(refresh.x(),refresh.z()))client.level.setSectionRangeDirty(refresh.x()-1,refresh.minY()-1,refresh.z()-1,refresh.x()+1,refresh.maxY()+1,refresh.z()+1);
            }
        });
    }
}
