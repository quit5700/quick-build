package cn.quit5700.buildingwand.network;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.BuildWandServer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class BuildNetworking {
 public static final int LEFT=0,RIGHT=1,SELECT=2,QUERY=3;
 public static void register(){PayloadTypeRegistry.serverboundPlay().register(Request.ID,Request.CODEC);PayloadTypeRegistry.clientboundPlay().register(State.ID,State.CODEC);ServerPlayNetworking.registerGlobalReceiver(Request.ID,(p,c)->c.server().execute(()->BuildWandServer.handle(c.player(),p)));BuildWandServer.register();}
 public record Request(int action,BlockPos pos,long revision,long sequence) implements CustomPacketPayload{
  public static final Type<Request> ID=new Type<>(BuildingWandMod.id("build_request"));public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=CustomPacketPayload.codec(Request::write,Request::read);
  public Type<? extends CustomPacketPayload> type(){return ID;}private void write(RegistryFriendlyByteBuf b){b.writeVarInt(action);b.writeBlockPos(pos);b.writeLong(revision);b.writeLong(sequence);}private static Request read(RegistryFriendlyByteBuf b){return new Request(b.readVarInt(),b.readBlockPos(),b.readLong(),b.readLong());}
 }
 public record State(String dimension,String wand,int mode,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean busy) implements CustomPacketPayload{
  public static final Type<State> ID=new Type<>(BuildingWandMod.id("build_state"));public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=CustomPacketPayload.codec(State::write,State::read);
  public Type<? extends CustomPacketPayload> type(){return ID;}private void write(RegistryFriendlyByteBuf b){b.writeUtf(dimension,128);b.writeUtf(wand,64);b.writeVarInt(mode);b.writeVarInt(stage);b.writeBlockPos(first);b.writeBlockPos(second);b.writeLong(revision);b.writeLong(sequence);b.writeBoolean(busy);}private static State read(RegistryFriendlyByteBuf b){return new State(b.readUtf(128),b.readUtf(64),b.readVarInt(),b.readVarInt(),b.readBlockPos(),b.readBlockPos(),b.readLong(),b.readLong(),b.readBoolean());}
 }
}
