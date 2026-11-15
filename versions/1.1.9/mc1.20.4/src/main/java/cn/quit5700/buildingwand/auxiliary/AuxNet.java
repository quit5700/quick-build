package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.BuildingWandMod;
import net.fabricmc.fabric.api.networking.v1.*;
import cn.quit5700.buildingwand.network.LegacyWandPayload;
import cn.quit5700.buildingwand.network.LegacyWandPackets;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;


import net.minecraft.server.level.ServerPlayer;
import java.io.*;
import java.util.*;

public final class AuxNet {
 public static final int CHUNK=30000,MAX=256*1024*1024;
 public record Part(UUID token,int offset,int total,byte[] bytes) implements LegacyWandPayload{
  public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("aux_data");
  public void write(FriendlyByteBuf b){b.writeUUID(token);b.writeInt(offset);b.writeInt(total);b.writeByteArray(bytes);}
  public static Part read(FriendlyByteBuf b){return new Part(b.readUUID(),b.readInt(),b.readInt(),b.readByteArray(CHUNK));}
  @Override public net.minecraft.resources.ResourceLocation id(){return ID;}
 }
 public static final class Inbox{
  private UUID token;private int total;private long started;private ByteArrayOutputStream buffer=new ByteArrayOutputStream();
  public CompoundTag accept(Part part)throws IOException{
   if(part.total<1||part.total>MAX||part.bytes.length<1||part.offset<0||(long)part.offset+part.bytes.length>part.total)throw new IOException("Invalid transfer");
   if(part.offset==0){token=part.token;total=part.total;buffer.reset();started=System.currentTimeMillis();}
   if(!part.token.equals(token)||part.total!=total||part.offset!=buffer.size()||System.currentTimeMillis()-started>120000)throw new IOException("Expired or out of order transfer");
   buffer.writeBytes(part.bytes);if(buffer.size()!=total)return null;
   byte[] data=buffer.toByteArray();buffer=new ByteArrayOutputStream();token=null;return NbtIo.readCompressed(new ByteArrayInputStream(data),NbtAccounter.create(512L*1024*1024));
  }
 }
 private static final Map<UUID,Inbox> incoming=new HashMap<>();
 public static void register(){
  
  LegacyWandPackets.serverReceiver(Part.ID,Part::read,(packet,ctx)->ctx.server().execute(()->{try{var data=incoming.computeIfAbsent(ctx.player().getUUID(),id->new Inbox()).accept(packet);if(data!=null)AuxServer.command(ctx.player(),cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(data,"Action",""),cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(data,"Data"));}catch(Exception error){incoming.remove(ctx.player().getUUID());AuxServer.message(ctx.player(),"aux.q5.invalid");}}));
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->incoming.remove(handler.player.getUUID()));
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->incoming.clear());
 }
 public static List<Part> packets(String action,CompoundTag data){try{var root=new CompoundTag();root.putString("Action",action);root.put("Data",data);byte[] bytes=cn.quit5700.buildingwand.blueprint.BlueprintNbt.compress(root);if(bytes.length>MAX)throw new IllegalArgumentException("Transfer too large");UUID token=UUID.randomUUID();var packets=new ArrayList<Part>();for(int n=0;n<bytes.length;n+=CHUNK)packets.add(new Part(token,n,bytes.length,Arrays.copyOfRange(bytes,n,Math.min(n+CHUNK,bytes.length))));return packets;}catch(IOException e){throw new UncheckedIOException(e);}}
 public static void send(ServerPlayer player,String action,CompoundTag data){for(var p:packets(action,data))LegacyWandPackets.send(player,p);}
}
