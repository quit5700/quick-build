package cn.quit5700.light.network;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.light.VariableLightTaskState;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class VariableLightNetworking {
    public static final ResourceLocation SNAPSHOT = LightMod.id("variable_daylight_snapshot");

    private VariableLightNetworking() { }

    public static void initialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncPlayer(handler.player));
    }

    public static void syncWorld(ServerLevel world) {
        List<Entry> entries = snapshot(world);
        for (ServerPlayer player : world.players()) send(player, entries);
    }

    public static void syncPlayer(ServerPlayer player) {
        send(player, snapshot((ServerLevel) player.level()));
    }

    private static List<Entry> snapshot(ServerLevel world) {
        return VariableLightTaskState.get(world).daylightSources().stream()
                .map(source -> new Entry(source.pos(), source.radius())).toList();
    }

    private static void send(ServerPlayer player, List<Entry> entries) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            buf.writeBlockPos(entry.pos());
            buf.writeVarInt(entry.radius());
        }
        ServerPlayNetworking.send(player, SNAPSHOT, buf);
    }

    public static List<Entry> read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) entries.add(new Entry(buf.readBlockPos(), buf.readVarInt()));
        return List.copyOf(entries);
    }

    public record Entry(BlockPos pos, int radius) { }
}
