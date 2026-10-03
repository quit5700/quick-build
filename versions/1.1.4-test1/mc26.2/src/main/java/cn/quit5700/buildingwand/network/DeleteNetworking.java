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
    public static final int PICK=0, CONFIRM=1, CANCEL=2, RESET=3;
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(Request.ID,Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(State.ID,State.CODEC);
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
    public record State(int stage,BlockPos first,BlockPos second,long revision,long sequence) implements CustomPacketPayload {
        public static final Type<State> ID=new Type<>(BuildingWandMod.id("delete_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=CustomPacketPayload.codec(State::write,State::read);
        public Type<? extends CustomPacketPayload> type(){return ID;}
        private void write(RegistryFriendlyByteBuf b){b.writeVarInt(stage);b.writeBlockPos(first);b.writeBlockPos(second);b.writeLong(revision);b.writeLong(sequence);}
        private static State read(RegistryFriendlyByteBuf b){return new State(b.readVarInt(),b.readBlockPos(),b.readBlockPos(),b.readLong(),b.readLong());}
    }
}
