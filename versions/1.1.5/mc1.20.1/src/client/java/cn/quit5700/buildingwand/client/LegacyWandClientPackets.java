package cn.quit5700.buildingwand.client;
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
