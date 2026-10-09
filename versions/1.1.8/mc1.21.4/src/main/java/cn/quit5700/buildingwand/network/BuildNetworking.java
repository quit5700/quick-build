package cn.quit5700.buildingwand.network;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.BuildWandServer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public final class BuildNetworking {
 public static final int LEFT=0,RIGHT=1,SELECT=2,QUERY=3,CLEAR=4;
 public static void register(){PayloadTypeRegistry.playC2S().register(Request.ID,Request.CODEC);PayloadTypeRegistry.playS2C().register(State.ID,State.CODEC);ServerPlayNetworking.registerGlobalReceiver(Request.ID,(p,c)->c.server().execute(()->BuildWandServer.handle(c.player(),p)));PayloadTypeRegistry.playC2S().register(CancelAll.ID,CancelAll.CODEC);PayloadTypeRegistry.playS2C().register(Cancelled.ID,Cancelled.CODEC);ServerPlayNetworking.registerGlobalReceiver(CancelAll.ID,(p,c)->c.server().execute(()->ServerPlayNetworking.send(c.player(),new Cancelled(BuildWandServer.cancelAll(c.player())))));BuildWandServer.register();}
 public record Request(int action,BlockPos pos,long revision,long sequence,String wand,int slot) implements CustomPacketPayload{
  public static final Type<Request> ID=new Type<>(BuildingWandMod.id("build_request"));public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=CustomPacketPayload.codec(Request::write,Request::read);
  public Type<? extends CustomPacketPayload> type(){return ID;}private void write(RegistryFriendlyByteBuf b){b.writeVarInt(action);b.writeBlockPos(pos);b.writeLong(revision);b.writeLong(sequence);b.writeUtf(wand,64);b.writeVarInt(slot);}private static Request read(RegistryFriendlyByteBuf b){return new Request(b.readVarInt(),b.readBlockPos(),b.readLong(),b.readLong(),b.readUtf(64),b.readVarInt());}
 }
 public record State(String dimension,String wand,int mode,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean busy,String material,String filter,String warning) implements CustomPacketPayload{
  public State(String dimension,String wand,int mode,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean busy,String material,String filter){this(dimension,wand,mode,stage,first,second,revision,sequence,busy,material,filter,"");}
  public static final Type<State> ID=new Type<>(BuildingWandMod.id("build_state"));public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=CustomPacketPayload.codec(State::write,State::read);
  public Type<? extends CustomPacketPayload> type(){return ID;}private void write(RegistryFriendlyByteBuf b){b.writeUtf(dimension,128);b.writeUtf(wand,64);b.writeVarInt(mode);b.writeVarInt(stage);b.writeBlockPos(first);b.writeBlockPos(second);b.writeLong(revision);b.writeLong(sequence);b.writeBoolean(busy);b.writeUtf(material,256);b.writeUtf(filter,256);b.writeUtf(warning,128);}private static State read(RegistryFriendlyByteBuf b){return new State(b.readUtf(128),b.readUtf(64),b.readVarInt(),b.readVarInt(),b.readBlockPos(),b.readBlockPos(),b.readLong(),b.readLong(),b.readBoolean(),b.readUtf(256),b.readUtf(256),b.readUtf(128));}
 }
 public record CancelAll() implements CustomPacketPayload{
  public static final Type<CancelAll> ID=new Type<>(BuildingWandMod.id("cancel_all_builds"));public static final StreamCodec<RegistryFriendlyByteBuf,CancelAll> CODEC=CustomPacketPayload.codec((p,b)->{},b->new CancelAll());public Type<? extends CustomPacketPayload> type(){return ID;}
 }
 public record Cancelled(int count) implements CustomPacketPayload{
  public static final Type<Cancelled> ID=new Type<>(BuildingWandMod.id("builds_cancelled"));public static final StreamCodec<RegistryFriendlyByteBuf,Cancelled> CODEC=CustomPacketPayload.codec((p,b)->b.writeVarInt(p.count),b->new Cancelled(b.readVarInt()));public Type<? extends CustomPacketPayload> type(){return ID;}
 }
}
