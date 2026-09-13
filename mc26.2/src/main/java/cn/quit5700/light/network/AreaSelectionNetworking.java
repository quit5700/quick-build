package cn.quit5700.light.network;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import cn.quit5700.light.logic.LightBox;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AreaSelectionNetworking {
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private AreaSelectionNetworking() { }
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(Pick.ID,Pick.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Preview.ID,Preview.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Pick.ID,(payload,context)->context.server().execute(()->pick(context.player(),payload)));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->SESSIONS.remove(handler.player.getUUID()));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player,origin,destination)->{
            SESSIONS.remove(player.getUUID()); ServerPlayNetworking.send(player,new Preview(false,null,null));
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server->SESSIONS.clear());
    }
    public static void begin(ServerPlayer player,VariableConstantLightBlockEntity lamp) {
        if(lamp.isLegacy() || !player.mayBuild()) return;
        SESSIONS.put(player.getUUID(),new Session(lamp.getBlockPos(),player.level().dimension().identifier(),null,
                player.level().getGameTime()+12000));
        LightBox old=lamp.area();
        ServerPlayNetworking.send(player,new Preview(true,old==null?null:new BlockPos(old.minX(),old.minY(),old.minZ()),
                old==null?null:new BlockPos(old.maxX(),old.maxY(),old.maxZ())));
        player.sendSystemMessage(Component.translatable("screen.quick_build.area_start"));
    }
    public static void showRange(ServerPlayer player,VariableConstantLightBlockEntity lamp,boolean show) {
        if(lamp.isLegacy()) return;
        LightBox box=show ? lamp.area() : null;
        ServerPlayNetworking.send(player,new Preview(false,
                box==null?null:new BlockPos(box.minX(),box.minY(),box.minZ()),
                box==null?null:new BlockPos(box.maxX(),box.maxY(),box.maxZ()),box!=null));
    }
    private static void pick(ServerPlayer player,Pick packet) {
        Session session=SESSIONS.get(player.getUUID());
        if(session==null) return;
        if(packet.cancel() || !player.mayBuild() || player.level().getGameTime()>session.expires()
                || !player.level().dimension().identifier().equals(session.dimension())) {
            SESSIONS.remove(player.getUUID()); ServerPlayNetworking.send(player,new Preview(false,null,null)); return;
        }
        BlockPos point=packet.pos();
        if(player.distanceToSqr(point.getX()+.5,point.getY()+.5,point.getZ()+.5)>64
                || player.level().isOutsideBuildHeight(point) || !player.level().hasChunk(point.getX()>>4,point.getZ()>>4)) {
            ServerPlayNetworking.send(player,new Preview(true,session.first(),null)); return;
        }
        if(!player.level().hasChunk(session.lamp().getX()>>4,session.lamp().getZ()>>4)
                || !(player.level().getBlockEntity(session.lamp()) instanceof VariableConstantLightBlockEntity lamp) || lamp.isLegacy()) {
            SESSIONS.remove(player.getUUID()); ServerPlayNetworking.send(player,new Preview(false,null,null));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_missing_lamp")); return;
        }
        if(session.first()==null) {
            SESSIONS.put(player.getUUID(),new Session(session.lamp(),session.dimension(),point.immutable(),session.expires()));
            ServerPlayNetworking.send(player,new Preview(true,point,null));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_second")); return;
        }
        try {
            BlockPos first=session.first();
            LightBox box=LightBox.of(first.getX(),first.getY(),first.getZ(),point.getX(),point.getY(),point.getZ());
            lamp.selectArea(box); SESSIONS.remove(player.getUUID());
            ServerPlayNetworking.send(player,new Preview(false,new BlockPos(box.minX(),box.minY(),box.minZ()),new BlockPos(box.maxX(),box.maxY(),box.maxZ())));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_saved",box.maxX()-box.minX()+1,box.maxY()-box.minY()+1,box.maxZ()-box.minZ()+1));
        } catch(IllegalArgumentException oversized) {
            ServerPlayNetworking.send(player,new Preview(true,session.first(),null));
            player.sendSystemMessage(Component.translatable("screen.quick_build.area_too_large",LightBox.MAX_SIDE));
        }
    }
    private record Session(BlockPos lamp,Identifier dimension,BlockPos first,long expires) { }
    public record Pick(BlockPos pos,boolean cancel) implements CustomPacketPayload {
        public static final Type<Pick> ID=new Type<>(LightMod.id("area_light_pick"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Pick> CODEC=CustomPacketPayload.codec(Pick::write,Pick::read);
        private void write(RegistryFriendlyByteBuf b){b.writeBlockPos(pos);b.writeBoolean(cancel);}
        private static Pick read(RegistryFriendlyByteBuf b){return new Pick(b.readBlockPos(),b.readBoolean());}
        public Type<? extends CustomPacketPayload> type(){return ID;}
    }
    public record Preview(boolean selecting,BlockPos first,BlockPos second,boolean pinned) implements CustomPacketPayload {
        public Preview(boolean selecting,BlockPos first,BlockPos second){this(selecting,first,second,false);}
        public static final Type<Preview> ID=new Type<>(LightMod.id("area_light_preview"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Preview> CODEC=CustomPacketPayload.codec(Preview::write,Preview::read);
        private void write(RegistryFriendlyByteBuf b){b.writeBoolean(selecting);b.writeBoolean(first!=null);if(first!=null)b.writeBlockPos(first);b.writeBoolean(second!=null);if(second!=null)b.writeBlockPos(second);b.writeBoolean(pinned);}
        private static Preview read(RegistryFriendlyByteBuf b){boolean active=b.readBoolean();BlockPos first=b.readBoolean()?b.readBlockPos():null;BlockPos second=b.readBoolean()?b.readBlockPos():null;return new Preview(active,first,second,b.readBoolean());}
        public Type<? extends CustomPacketPayload> type(){return ID;}
    }
}
