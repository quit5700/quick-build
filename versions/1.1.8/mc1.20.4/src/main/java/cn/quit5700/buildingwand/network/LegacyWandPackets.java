package cn.quit5700.buildingwand.network;
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
  if(value instanceof WandNetworking.SelectBlockPayload)return WandNetworking.SELECT_BLOCK;
  if(value instanceof WandNetworking.PlaceSinglePayload)return WandNetworking.PLACE_SINGLE;
  if(value instanceof WandNetworking.PlaceShapePayload)return WandNetworking.PLACE_SHAPE;
  if(value instanceof WandNetworking.CaptureMoveSelectionPayload)return WandNetworking.CAPTURE_MOVE_SELECTION;
  if(value instanceof WandNetworking.ExecuteMovePayload)return WandNetworking.EXECUTE_MOVE;
  if(value instanceof WandNetworking.CancelMoveSelectionPayload)return WandNetworking.CANCEL_MOVE_SELECTION;
  if(value instanceof WandNetworking.QueryMoveMemoryPayload)return WandNetworking.QUERY_MOVE_MEMORY;
  if(value instanceof WandNetworking.MoveSelectionResultPayload)return WandNetworking.MOVE_SELECTION_RESULT;
  if(value instanceof WandNetworking.MoveExecutionResultPayload)return WandNetworking.MOVE_EXECUTION_RESULT;
  if(value instanceof WandNetworking.MoveMemoryPayload)return WandNetworking.MOVE_MEMORY;
  if(value instanceof WandNetworking.OpenSettingsPayload)return WandNetworking.OPEN_SETTINGS;
  if(value instanceof WandNetworking.UpdateSettingsPayload)return WandNetworking.UPDATE_SETTINGS;
  if(value instanceof WandNetworking.SettingsSavedPayload)return WandNetworking.SETTINGS_SAVED;
  throw new IllegalArgumentException("Unknown wand payload");
 }
}
