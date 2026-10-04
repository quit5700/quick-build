package cn.quit5700.light.network;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import cn.quit5700.light.logic.LightBox;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.logic.RealAreaPolicy;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AreaSelectionNetworking {
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private AreaSelectionNetworking() { }
    public static void initialize() {
        ServerPlayNetworking.registerGlobalReceiver(Pick.ID,(server,player,handler,buffer,response)->{Pick payload=Pick.read(buffer);server.execute(()->pick(player,payload));});
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->SESSIONS.remove(handler.player.getUUID()));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player,origin,destination)->{
            SESSIONS.remove(player.getUUID()); send(player,new Preview(false,null,null));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server->SESSIONS.clear());
    }
    public static void begin(ServerPlayer player,VariableConstantLightBlockEntity lamp,VariableLightMode mode) {
        if(lamp.isLegacy() || !player.mayBuild()) return;
        SESSIONS.put(player.getUUID(),new Session(lamp.getBlockPos(),player.level().dimension().location(),null,
                player.level().getGameTime()+12000,mode));
        LightBox old=lamp.area();
        send(player,new Preview(true,old==null?null:new BlockPos(old.minX(),old.minY(),old.minZ()),
                old==null?null:new BlockPos(old.maxX(),old.maxY(),old.maxZ())));
        player.sendSystemMessage(Component.translatable("screen.quick_build.area_wand_start"));
    }
    public static void showRange(ServerPlayer player,VariableConstantLightBlockEntity lamp,boolean show) {
        if(lamp.isLegacy()) return;
        LightBox box=show ? lamp.area() : null;
        send(player,new Preview(false,
                box==null?null:new BlockPos(box.minX(),box.minY(),box.minZ()),
                box==null?null:new BlockPos(box.maxX(),box.maxY(),box.maxZ()),box!=null));
    }
    private static void pick(ServerPlayer player,Pick packet) {
        Session session=SESSIONS.get(player.getUUID());
        if(session==null) return;
        if(packet.cancel() || !player.mayBuild() || player.level().getGameTime()>session.expires()
                || !player.level().dimension().location().equals(session.dimension())) {
            SESSIONS.remove(player.getUUID()); send(player,new Preview(false,null,null)); return;
        }
        BlockPos point=packet.pos();
        if(player.distanceToSqr(point.getX()+.5,point.getY()+.5,point.getZ()+.5)>64
                || player.level().isOutsideBuildHeight(point) || !player.level().hasChunk(point.getX()>>4,point.getZ()>>4)) {
            send(player,new Preview(true,session.first(),null)); return;
        }
        if(!player.level().hasChunk(session.lamp().getX()>>4,session.lamp().getZ()>>4)
                || !(player.level().getBlockEntity(session.lamp()) instanceof VariableConstantLightBlockEntity lamp) || lamp.isLegacy()) {
            SESSIONS.remove(player.getUUID()); send(player,new Preview(false,null,null));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_missing_lamp")); return;
        }
        if(session.first()==null) {
            SESSIONS.put(player.getUUID(),new Session(session.lamp(),session.dimension(),point.immutable(),session.expires(),session.mode()));
            send(player,new Preview(true,point,null));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_second")); return;
        }
        try {
            BlockPos first=session.first();
            LightBox box=LightBox.of(first.getX(),first.getY(),first.getZ(),point.getX(),point.getY(),point.getZ());
            if(session.mode()==VariableLightMode.AREA_REAL_LIGHT && !RealAreaPolicy.accepts(box)) {
                send(player,new Preview(true,session.first(),null));
                player.sendSystemMessage(Component.translatable("screen.quick_build.real_area_limit",RealAreaPolicy.MAX_VOLUME));
                return;
            }
            lamp.selectArea(box,session.mode()); SESSIONS.remove(player.getUUID());
            send(player,new Preview(false,new BlockPos(box.minX(),box.minY(),box.minZ()),new BlockPos(box.maxX(),box.maxY(),box.maxZ())));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_saved",box.maxX()-box.minX()+1,box.maxY()-box.minY()+1,box.maxZ()-box.minZ()+1));
        } catch(IllegalArgumentException oversized) {
            send(player,new Preview(true,session.first(),null));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_too_large",LightBox.MAX_SIDE));
        }
    }
    private static void send(ServerPlayer player,Preview preview){var buffer=PacketByteBufs.create();preview.write(buffer);ServerPlayNetworking.send(player,Preview.ID,buffer);}
    private record Session(BlockPos lamp,ResourceLocation dimension,BlockPos first,long expires,VariableLightMode mode) { }
    public record Pick(BlockPos pos,boolean cancel) {
        public static final ResourceLocation ID=LightMod.id("area_light_pick");
        public void write(FriendlyByteBuf b){b.writeBlockPos(pos);b.writeBoolean(cancel);}
        public static Pick read(FriendlyByteBuf b){return new Pick(b.readBlockPos(),b.readBoolean());}
    }
    public record Preview(boolean selecting,BlockPos first,BlockPos second,boolean pinned) {
        public Preview(boolean selecting,BlockPos first,BlockPos second){this(selecting,first,second,false);}
        public static final ResourceLocation ID=LightMod.id("area_light_preview");
        public void write(FriendlyByteBuf b){b.writeBoolean(selecting);b.writeBoolean(first!=null);if(first!=null)b.writeBlockPos(first);b.writeBoolean(second!=null);if(second!=null)b.writeBlockPos(second);b.writeBoolean(pinned);}
        public static Preview read(FriendlyByteBuf b){boolean active=b.readBoolean();BlockPos first=b.readBoolean()?b.readBlockPos():null;BlockPos second=b.readBoolean()?b.readBlockPos():null;return new Preview(active,first,second,b.readBoolean());}
    }
}
