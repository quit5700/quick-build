package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.logic.*;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.util.DeleteWandData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeleteWandServer {
    private static final class Session {
        final ServerLevel world;final DeleteSelection selection;long sequence;Scan scan;
        Session(ServerLevel world){this.world=world;selection=new DeleteSelection(java.util.concurrent.ThreadLocalRandom.current().nextLong(1,Long.MAX_VALUE/2));}
        void abortScan(){if(scan!=null)scan.lease.release();scan=null;}
    }
    private static final Map<UUID,Session> selections=new HashMap<>();
    private static final Map<UUID,Long> lastSequence=new HashMap<>();
    public static void register(){
        DeleteTaskState.initialize();
        ServerTickEvents.END_SERVER_TICK.register(server->{
            DeleteChunkLease.refreshAll();
            for(var iterator=selections.entrySet().iterator();iterator.hasNext();){
                var entry=iterator.next();Session session=entry.getValue();ServerPlayer player=server.getPlayerList().getPlayer(entry.getKey());
                if(player==null||player.level()!=session.world){session.abortScan();iterator.remove();continue;}
                if(session.scan!=null&&!session.scan.done)session.scan.tick(player,session);
            }
            for(ServerLevel world:server.getAllLevels())DeleteTaskState.get(world).tick(world);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
            Session old=selections.remove(handler.player.getUUID());if(old!=null)old.abortScan();lastSequence.remove(handler.player.getUUID());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{
            for(Session session:selections.values())session.abortScan();
            for(ServerLevel world:server.getAllLevels())DeleteTaskState.get(world).release(world);
            selections.clear();lastSequence.clear();
        });
    }
    public static void handle(ServerPlayer player,DeleteNetworking.Request request){
        if(request.sequence()<=lastSequence.getOrDefault(player.getUUID(),0L))return;
        lastSequence.put(player.getUUID(),request.sequence());
        ServerLevel world=(ServerLevel)player.level();Session session=selections.get(player.getUUID());
        if(session==null||session.world!=world){if(session!=null)session.abortScan();session=new Session(world);selections.put(player.getUUID(),session);}
        session.sequence=request.sequence();DeleteSelection selection=session.selection;int action=request.action();
        if(action==DeleteNetworking.CANCEL){
            session.abortScan();int old=selection.cancel();
            if(old>0)message(player,old==1?"message.quick_build.delete_cancel_first":"message.quick_build.delete_cancel",false);
        }else if(action==DeleteNetworking.QUERY){
        }else if(player.getMainHandItem().is(ModItems.DELETE_WAND)&&player.mayBuild()&&!player.isSpectator()){
            if(action==DeleteNetworking.PICK){
                BlockPos pos=request.pos();
                if(!world.isOutsideBuildHeight(pos)&&world.getWorldBorder().isWithinBounds(pos)
                        && player.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<=36D&&world.mayInteract(player,pos)){
                    if(selection.pick(new DeleteSelection.Point(pos.getX(),pos.getY(),pos.getZ()),request.revision())){
                        if(selection.stage()==1)message(player,"message.quick_build.delete_first",false);
                        else beginScan(player,session);
                    }
                }else message(player,"message.quick_build.delete_invalid",true);
            }else if(action==DeleteNetworking.CONFIRM&&selection.stage()==2&&selection.revision()==request.revision()){
                ItemStack tool=player.getMainHandItem();
                if(DeleteTaskState.get(world).busy(player.getUUID()))message(player,"message.quick_build.delete_busy",false);
                else if(session.scan==null||!session.scan.matches(player,tool)){beginScan(player,session);}
                else if(!session.scan.done)message(player,"message.quick_build.delete_checking",false);
                else if(!player.isCreative()&&DeleteWandData.capacity(tool)<=0)message(player,"message.quick_build.delete_no_durability",true);
                else{
                    var first=selection.first();var second=selection.second();String wand=DeleteWandData.ensureId(tool);
                    DeleteBox box=selection.confirm(request.revision());session.abortScan();
                    if(box!=null){DeleteTaskState.get(world).add(player.getUUID(),first,second,wand);player.sendSystemMessage(Component.translatable("message.quick_build.delete_start",box.volume().toString()));}
                }
            }
        }else message(player,"message.quick_build.delete_invalid",true);
        send(player,session);
    }
    private static void beginScan(ServerPlayer player,Session session){
        session.abortScan();session.scan=new Scan(player,session);
        if(session.scan.done)message(player,"message.quick_build.delete_confirm",false);
        else message(player,"message.quick_build.delete_checking",false);
    }
    private static void send(ServerPlayer player,Session session){
        Scan scan=session.scan;DeleteSelection selection=session.selection;
        ServerPlayNetworking.send(player,new DeleteNetworking.State(session.world.dimension().identifier().toString(),selection.stage(),pos(selection.first()),pos(selection.second()),selection.revision(),session.sequence,
                scan!=null&&!scan.done,scan!=null&&scan.insufficient,scan==null?0:scan.capacity));
    }
    private static BlockPos pos(DeleteSelection.Point p){return p==null?BlockPos.ZERO:new BlockPos(p.x(),p.y(),p.z());}
    private static void message(ServerPlayer player,String key,boolean red){var message=Component.translatable(key);player.sendOverlayMessage(red?message.withStyle(ChatFormatting.RED):message);}
    private static final class Scan {
        final DirectedDeleteCursor cursor;final String wand;final long capacity;final boolean creative;
        final DeleteChunkLease lease=new DeleteChunkLease();long count;boolean done,insufficient;
        Scan(ServerPlayer player,Session session){
            ItemStack tool=player.getMainHandItem();wand=DeleteWandData.ensureId(tool);capacity=DeleteWandData.capacity(tool);creative=player.isCreative();done=creative;
            var a=session.selection.first();var b=session.selection.second();
            DeleteBox box=DeleteBox.between(a.x(),a.y(),a.z(),b.x(),b.y(),b.z());
            cursor=new DirectedDeleteCursor(box,b.x()>=a.x()?1:-1,b.y()>=a.y()?1:-1,b.z()>=a.z()?1:-1);
        }
        boolean matches(ServerPlayer player,ItemStack tool){return wand.equals(DeleteWandData.id(tool))&&creative==player.isCreative()&&(creative||capacity==DeleteWandData.capacity(tool));}
        void tick(ServerPlayer player,Session session){
            if(!player.mayBuild()||player.isSpectator()||DeleteWandData.find(player,wand).isEmpty()){lease.release();return;}
            long deadline=System.nanoTime()+2_000_000L;int budget=2048;ServerLevel world=session.world;
            int cx=cursor.chunkX(),cz=cursor.chunkZ();if(!lease.ready(world,cx,cz))return;
            while(budget-->0&&!cursor.done()&&cursor.chunkX()==cx&&cursor.chunkZ()==cz&&System.nanoTime()<deadline){
                if(DeleteTaskState.eligible(world,player,new BlockPos(cursor.x(),cursor.y(),cursor.z())))count++;
                cursor.advance();
                if(count>=Math.max(1,capacity)){insufficient=true;done=true;break;}
            }
            if(cursor.done())done=true;
            if(done){
                lease.release();
                if(insufficient)player.sendSystemMessage(Component.translatable("message.quick_build.delete_warning",capacity).withStyle(ChatFormatting.RED));
                else message(player,"message.quick_build.delete_confirm",false);
                send(player,session);
            }
        }
    }
}
