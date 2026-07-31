package cn.quit5700.light.network;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.light.VariableLightTaskState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class VariableLightNetworking {
    private static final ResourceLocation SNAPSHOT = LightMod.id("variable_daylight_snapshot");

    private VariableLightNetworking() { }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(SnapshotPayload.ID, SnapshotPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncPlayer(handler.player));
    }

    public static void syncWorld(ServerLevel world) {
        SnapshotPayload payload = snapshot(world);
        for (ServerPlayer player : world.players()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, snapshot(player.level()));
    }

    private static SnapshotPayload snapshot(ServerLevel world) {
        List<Entry> entries = VariableLightTaskState.get(world).daylightSources().stream()
                .map(source -> new Entry(source.pos(), source.radius()))
                .toList();
        return new SnapshotPayload(entries);
    }

    public record Entry(BlockPos pos, int radius) { }

    public record SnapshotPayload(List<Entry> entries) implements CustomPacketPayload {
        public static final Type<SnapshotPayload> ID = new Type<>(SNAPSHOT);
        public static final StreamCodec<RegistryFriendlyByteBuf, SnapshotPayload> CODEC =
                CustomPacketPayload.codec(SnapshotPayload::write, SnapshotPayload::read);

        @Override public Type<? extends CustomPacketPayload> type() { return ID; }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(entries.size());
            for (Entry entry : entries) {
                buf.writeBlockPos(entry.pos());
                buf.writeVarInt(entry.radius());
            }
        }

        private static SnapshotPayload read(RegistryFriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<Entry> entries = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                entries.add(new Entry(buf.readBlockPos(), buf.readVarInt()));
            }
            return new SnapshotPayload(List.copyOf(entries));
        }
    }
}
