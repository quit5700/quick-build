package cn.quit5700.buildingwand.network;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.BuildWandServer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


public final class BuildNetworking {
 public static final int LEFT=0,RIGHT=1,SELECT=2,QUERY=3,CLEAR=4;
 public static void register(){LegacyWandPackets.serverReceiver(Request.ID,Request::read,(p,c)->c.server().execute(()->BuildWandServer.handle(c.player(),p)));LegacyWandPackets.serverReceiver(CancelAll.ID,CancelAll::read,(p,c)->c.server().execute(()->LegacyWandPackets.send(c.player(),new Cancelled(BuildWandServer.cancelAll(c.player())))));BuildWandServer.register();}
 public record Request(int action,BlockPos pos,long revision,long sequence,String wand,int slot) implements LegacyWandPayload{
  public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("build_request");
  public net.minecraft.resources.ResourceLocation id(){return ID;}public void write(FriendlyByteBuf b){b.writeVarInt(action);b.writeBlockPos(pos);b.writeLong(revision);b.writeLong(sequence);b.writeUtf(wand,64);b.writeVarInt(slot);}public static Request read(FriendlyByteBuf b){return new Request(b.readVarInt(),b.readBlockPos(),b.readLong(),b.readLong(),b.readUtf(64),b.readVarInt());}
 }
 public record State(String dimension,String wand,int mode,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean busy,String material,String filter,String warning) implements LegacyWandPayload{
  public State(String dimension,String wand,int mode,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean busy,String material,String filter){this(dimension,wand,mode,stage,first,second,revision,sequence,busy,material,filter,"");}
  public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("build_state");
  public net.minecraft.resources.ResourceLocation id(){return ID;}public void write(FriendlyByteBuf b){b.writeUtf(dimension,128);b.writeUtf(wand,64);b.writeVarInt(mode);b.writeVarInt(stage);b.writeBlockPos(first);b.writeBlockPos(second);b.writeLong(revision);b.writeLong(sequence);b.writeBoolean(busy);b.writeUtf(material,256);b.writeUtf(filter,256);b.writeUtf(warning,128);}public static State read(FriendlyByteBuf b){return new State(b.readUtf(128),b.readUtf(64),b.readVarInt(),b.readVarInt(),b.readBlockPos(),b.readBlockPos(),b.readLong(),b.readLong(),b.readBoolean(),b.readUtf(256),b.readUtf(256),b.readUtf(128));}
 }
 public record CancelAll() implements LegacyWandPayload{
  public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("cancel_all_builds");public net.minecraft.resources.ResourceLocation id(){return ID;}public void write(FriendlyByteBuf b){}public static CancelAll read(FriendlyByteBuf b){return new CancelAll();}
 }
 public record Cancelled(int count) implements LegacyWandPayload {
  public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("builds_cancelled");
  public net.minecraft.resources.ResourceLocation id(){return ID;}
  public void write(FriendlyByteBuf b){b.writeVarInt(count);}
  public static Cancelled read(FriendlyByteBuf b){return new Cancelled(b.readVarInt());}
 }
}
