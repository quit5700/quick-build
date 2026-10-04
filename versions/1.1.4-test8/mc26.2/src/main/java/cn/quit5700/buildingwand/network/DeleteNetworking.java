package cn.quit5700.buildingwand.network;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.DeleteWandServer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class DeleteNetworking {
    public static final int PICK=0, CONFIRM=1, CANCEL=2, QUERY=3;
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(Request.ID,Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(State.ID,State.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Refresh.ID,Refresh.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Request.ID,(p,c)->c.server().execute(()->DeleteWandServer.handle(c.player(),p)));
        DeleteWandServer.register();
    }
    public record Request(int action,BlockPos pos,long revision,long sequence) implements CustomPacketPayload {
        public static final Type<Request> ID=new Type<>(BuildingWandMod.id("delete_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=CustomPacketPayload.codec(Request::write,Request::read);
        public Type<? extends CustomPacketPayload> type(){return ID;}
        private void write(RegistryFriendlyByteBuf b){b.writeVarInt(action);b.writeBlockPos(pos);b.writeLong(revision);b.writeLong(sequence);}
        private static Request read(RegistryFriendlyByteBuf b){return new Request(b.readVarInt(),b.readBlockPos(),b.readLong(),b.readLong());}
    }
    public record State(String dimension,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean checking,boolean insufficient,long capacity) implements CustomPacketPayload {
        public static final Type<State> ID=new Type<>(BuildingWandMod.id("delete_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=CustomPacketPayload.codec(State::write,State::read);
        public Type<? extends CustomPacketPayload> type(){return ID;}
        private void write(RegistryFriendlyByteBuf b){b.writeUtf(dimension,128);b.writeVarInt(stage);b.writeBlockPos(first);b.writeBlockPos(second);b.writeLong(revision);b.writeLong(sequence);b.writeBoolean(checking);b.writeBoolean(insufficient);b.writeLong(capacity);}
        private static State read(RegistryFriendlyByteBuf b){return new State(b.readUtf(128),b.readVarInt(),b.readBlockPos(),b.readBlockPos(),b.readLong(),b.readLong(),b.readBoolean(),b.readBoolean(),b.readLong());}
    }
    public record Refresh(String dimension,int x,int z,int minY,int maxY) implements CustomPacketPayload {
        public static final Type<Refresh> ID=new Type<>(BuildingWandMod.id("delete_refresh"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Refresh> CODEC=CustomPacketPayload.codec(Refresh::write,Refresh::read);
        public Type<? extends CustomPacketPayload> type(){return ID;}
        private void write(RegistryFriendlyByteBuf b){b.writeUtf(dimension,128);b.writeVarInt(x);b.writeVarInt(z);b.writeVarInt(minY);b.writeVarInt(maxY);}
        private static Refresh read(RegistryFriendlyByteBuf b){return new Refresh(b.readUtf(128),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
    }
}
