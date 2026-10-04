from pathlib import Path
import re
r=Path.cwd();root=r/'versions/1.1.4'
wand_types={'SelectBlockPayload':'SELECT_BLOCK','PlaceSinglePayload':'PLACE_SINGLE','PlaceShapePayload':'PLACE_SHAPE','CaptureMoveSelectionPayload':'CAPTURE_MOVE_SELECTION','ExecuteMovePayload':'EXECUTE_MOVE','CancelMoveSelectionPayload':'CANCEL_MOVE_SELECTION','QueryMoveMemoryPayload':'QUERY_MOVE_MEMORY','MoveSelectionResultPayload':'MOVE_SELECTION_RESULT','MoveExecutionResultPayload':'MOVE_EXECUTION_RESULT','MoveMemoryPayload':'MOVE_MEMORY','OpenSettingsPayload':'OPEN_SETTINGS','UpdateSettingsPayload':'UPDATE_SETTINGS','SettingsSavedPayload':'SETTINGS_SAVED'}
for v in ['1.20.4','1.20.1']:
 d=root/('mc'+v);net=d/'src/main/java/cn/quit5700/buildingwand/network';client=d/'src/client/java/cn/quit5700/buildingwand/client'
 (net/'LegacyWandPayload.java').write_text('''package cn.quit5700.buildingwand.network;
public interface LegacyWandPayload {net.minecraft.resources.ResourceLocation id();void write(net.minecraft.network.FriendlyByteBuf buffer);}
''',encoding='utf-8')
 for name in ['BuildNetworking','DeleteNetworking']:
  f=net/(name+'.java');s=f.read_text(encoding='utf-8').replace('import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;','').replace('import net.minecraft.network.codec.StreamCodec;','').replace('import net.minecraft.network.protocol.common.custom.CustomPacketPayload;','').replace('RegistryFriendlyByteBuf','FriendlyByteBuf').replace('implements CustomPacketPayload','implements LegacyWandPayload')
  s=re.sub(r'PayloadTypeRegistry\.play[CS]2[CS]\(\).register\([^;]+;','',s)
  s=re.sub(r'public static final Type<\w+> ID\s*=\s*new Type<>\((.*?)\);',r'public static final net.minecraft.resources.ResourceLocation ID=\1;',s)
  s=re.sub(r'public static final StreamCodec<[^;]+;','',s)
  s=re.sub(r'public Type<\? extends CustomPacketPayload> type\(\)\{return ID;\}', 'public net.minecraft.resources.ResourceLocation id(){return ID;}',s)
  s=s.replace('private void write(FriendlyByteBuf','public void write(FriendlyByteBuf');s=re.sub(r'private static (\w+) read\(',r'public static \1 read(',s)
  s=s.replace('public net.minecraft.resources.ResourceLocation id(){return ID;}\n }','public net.minecraft.resources.ResourceLocation id(){return ID;}public void write(FriendlyByteBuf b){}public static CancelAll read(FriendlyByteBuf b){return new CancelAll();}\n }') if name=='BuildNetworking' else s
  if name=='BuildNetworking':
   s=s.replace('public record Cancelled(int count) implements LegacyWandPayload{','public record Cancelled(int count) implements LegacyWandPayload{public void write(FriendlyByteBuf b){b.writeVarInt(count);}public static Cancelled read(FriendlyByteBuf b){return new Cancelled(b.readVarInt());}')
  s=re.sub(r'ServerPlayNetworking.registerGlobalReceiver\((\w+)\.ID,',r'LegacyWandPackets.serverReceiver(\1.ID,\1::read,',s).replace('ServerPlayNetworking.send(', 'LegacyWandPackets.send(')
  f.write_text(s,encoding='utf-8')
 server='''package cn.quit5700.buildingwand.network;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.*;
public final class LegacyWandPackets {
 public record Context(MinecraftServer server,ServerPlayer player){}
 public static <T> void serverReceiver(ResourceLocation id,Function<FriendlyByteBuf,T> read,BiConsumer<T,Context> receiver){ServerPlayNetworking.registerGlobalReceiver(id,(server,player,handler,buffer,response)->{T value=read.apply(buffer);receiver.accept(value,new Context(server,player));});}
 public static void send(ServerPlayer player,LegacyWandPayload value){var buffer=PacketByteBufs.create();value.write(buffer);ServerPlayNetworking.send(player,value.id(),buffer);}
 public static void send(ServerPlayer player,WandNetworking.PacketData value){ResourceLocation id=wandId(value);WandNetworking.send(player,id,value);}
 public static ResourceLocation wandId(WandNetworking.PacketData value){
'''
 for t,key in wand_types.items():server+=f'  if(value instanceof WandNetworking.{t})return WandNetworking.{key};\n'
 server+='  throw new IllegalArgumentException("Unknown wand payload");\n }\n}\n';(net/'LegacyWandPackets.java').write_text(server,encoding='utf-8')
 (client/'LegacyWandClientPackets.java').write_text('''package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import java.util.function.*;
public final class LegacyWandClientPackets {
 public record Context(Minecraft client){}
 public static <T> void receiver(ResourceLocation id,Function<FriendlyByteBuf,T> read,BiConsumer<T,Context> receiver){ClientPlayNetworking.registerGlobalReceiver(id,(client,handler,buffer,response)->{T value=read.apply(buffer);receiver.accept(value,new Context(client));});}
 public static void send(LegacyWandPayload value){var buffer=PacketByteBufs.create();value.write(buffer);ClientPlayNetworking.send(value.id(),buffer);}
 public static void send(WandNetworking.PacketData value){ClientPlayNetworking.send(LegacyWandPackets.wandId(value),WandNetworking.buffer(value));}
}
''',encoding='utf-8')
 for f in list((d/'src/main/java/cn/quit5700/buildingwand/server').rglob('*.java')):
  s=f.read_text(encoding='utf-8').replace('ServerPlayNetworking.send(', 'cn.quit5700.buildingwand.network.LegacyWandPackets.send(');f.write_text(s,encoding='utf-8')
 for f in (d/'src/client/java/cn/quit5700/buildingwand').rglob('*.java'):
  if f.name=='LegacyWandClientPackets.java':continue
  s=f.read_text(encoding='utf-8').replace('ClientPlayNetworking.send(', 'cn.quit5700.buildingwand.client.LegacyWandClientPackets.send(')
  s=re.sub(r'ClientPlayNetworking.registerGlobalReceiver\(([^,\s]+)\.ID,',r'cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(\1.ID,\1::read,',s)
  for t,key in wand_types.items():s=s.replace(f'WandNetworking.{t}.ID',f'WandNetworking.{key}')
  f.write_text(s,encoding='utf-8')
 f=d/'src/client/java/cn/quit5700/light/client/AreaSelectionClient.java';s=f.read_text(encoding='utf-8').replace('ClientPlayNetworking.send(new AreaSelectionNetworking.Pick(BlockPos.ZERO,true));','sendPick(new AreaSelectionNetworking.Pick(BlockPos.ZERO,true));');f.write_text(s,encoding='utf-8')
print('1.20.x保留旧频道协议，新增法杖通信数据完成同等适配。')
