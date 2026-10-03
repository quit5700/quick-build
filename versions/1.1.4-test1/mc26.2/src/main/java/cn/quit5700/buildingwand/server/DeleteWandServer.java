package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.logic.DeleteSelection;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import cn.quit5700.buildingwand.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeleteWandServer {
    private record Session(ServerLevel world,DeleteSelection selection){}
    private static final Map<UUID,Session> selections=new HashMap<>();
    private static final Map<UUID,Long> lastSequence=new HashMap<>();
    public static void register() {
        DeleteTaskState.initialize();
        ServerTickEvents.END_SERVER_TICK.register(server->{
            selections.entrySet().removeIf(e->{
                ServerPlayer p=server.getPlayerList().getPlayer(e.getKey());
                return p==null || p.level()!=e.getValue().world() || !p.getMainHandItem().is(ModItems.DELETE_WAND);
            });
            for(ServerLevel world:server.getAllLevels())DeleteTaskState.get(world).tick(world);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
            selections.remove(handler.player.getUUID());lastSequence.remove(handler.player.getUUID());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{
            for(ServerLevel world:server.getAllLevels())DeleteTaskState.get(world).release(world);
            selections.clear();lastSequence.clear();
        });
    }
    public static void handle(ServerPlayer player,DeleteNetworking.Request request) {
        if(request.sequence()<=lastSequence.getOrDefault(player.getUUID(),0L))return;
        lastSequence.put(player.getUUID(),request.sequence());
        ServerLevel world=(ServerLevel)player.level();
        Session session=selections.get(player.getUUID());
        if(session==null || session.world()!=world){
            session=new Session(world,new DeleteSelection(java.util.concurrent.ThreadLocalRandom.current().nextLong(1,Long.MAX_VALUE/2)));
            selections.put(player.getUUID(),session);
        }
        DeleteSelection selection=session.selection();
        int action=request.action();
        if(action==DeleteNetworking.CANCEL || action==DeleteNetworking.RESET) {
            int old=selection.cancel();
            if(action==DeleteNetworking.CANCEL && old>0)message(player,old==1?"message.quick_build.delete_cancel_first":"message.quick_build.delete_cancel");
        } else if(player.getMainHandItem().is(ModItems.DELETE_WAND) && player.mayBuild() && !player.isSpectator()) {
            if(action==DeleteNetworking.PICK) {
                BlockPos pos=request.pos();
                if(!world.isOutsideBuildHeight(pos) && world.getWorldBorder().isWithinBounds(pos)
                        && player.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<=36D
                        && world.mayInteract(player,pos)) {
                    if(selection.pick(new DeleteSelection.Point(pos.getX(),pos.getY(),pos.getZ()),request.revision()))
                        message(player,selection.stage()==1?"message.quick_build.delete_first":"message.quick_build.delete_confirm");
                } else message(player,"message.quick_build.delete_invalid");
            } else if(action==DeleteNetworking.CONFIRM && selection.stage()==2 && selection.revision()==request.revision()) {
                if(DeleteTaskState.get(world).busy(player.getUUID()))message(player,"message.quick_build.delete_busy");
                else {
                    DeleteBox box=selection.confirm(request.revision());
                    if(box!=null){DeleteTaskState.get(world).add(player.getUUID(),box);player.sendSystemMessage(Component.translatable("message.quick_build.delete_start",box.volume().toString()));}
                }
            }
        } else {selection.cancel();message(player,"message.quick_build.delete_invalid");}
        ServerPlayNetworking.send(player,new DeleteNetworking.State(selection.stage(),pos(selection.first()),pos(selection.second()),selection.revision(),request.sequence()));
    }
    private static BlockPos pos(DeleteSelection.Point p){return p==null?BlockPos.ZERO:new BlockPos(p.x(),p.y(),p.z());}
    private static void message(ServerPlayer player,String key){player.sendOverlayMessage(Component.translatable(key));}
}
