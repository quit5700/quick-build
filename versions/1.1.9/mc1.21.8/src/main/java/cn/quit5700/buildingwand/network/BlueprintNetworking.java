package cn.quit5700.buildingwand.network;

import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.WandActions;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.*;

public final class BlueprintNetworking {
    public static final int CHUNK_BYTES = 30000;
    public static final int MAX_BYTES = 256 * 1024 * 1024;
    private record Transfer(UUID token, byte[] bytes, int offset) {}
    private static final Map<UUID, UUID> requests = new HashMap<>();
    private static final Map<UUID, Transfer> transfers = new HashMap<>();
    private BlueprintNetworking() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(Request.ID, Request.CODEC);
        PayloadTypeRegistry.playS2C().register(Chunk.ID, Chunk.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Request.ID, (payload, context) -> context.server().execute(() -> {
            begin(context.player(), payload.token, () -> WandActions.exportMoveSelection(context.player()));
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            requests.remove(handler.player.getUUID()); transfers.remove(handler.player.getUUID());
            cn.quit5700.buildingwand.server.GenerationBoxBlueprint.cancel(handler.player.getUUID());
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            cn.quit5700.buildingwand.server.GenerationBoxBlueprint.tick();
            for (var iterator = transfers.entrySet().iterator(); iterator.hasNext();) {
                var entry = iterator.next(); var t = entry.getValue();
                var player = server.getPlayerList().getPlayer(entry.getKey());
                if (player == null) { iterator.remove(); requests.remove(entry.getKey()); continue; }
                int end = Math.min(t.offset + CHUNK_BYTES, t.bytes.length);
                ServerPlayNetworking.send(player, new Chunk(t.token, t.offset, t.bytes.length,
                        Arrays.copyOfRange(t.bytes, t.offset, end)));
                if (end == t.bytes.length) { iterator.remove(); requests.remove(entry.getKey()); }
                else entry.setValue(new Transfer(t.token, t.bytes, end));
            }
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            requests.clear(); transfers.clear();
            cn.quit5700.buildingwand.server.GenerationBoxBlueprint.clear();
        });
    }

    public static void begin(net.minecraft.server.level.ServerPlayer player, UUID token,
            java.util.function.Supplier<java.util.concurrent.CompletableFuture<byte[]>> export) {
        UUID playerId = player.getUUID();
        if (requests.containsKey(playerId)) {
            ServerPlayNetworking.send(player, new Chunk(token, 0, -1, new byte[0]));
            return;
        }
        var server = player.level().getServer();
        requests.put(playerId, token);
        java.util.concurrent.CompletableFuture<byte[]> result;
        try {
            result = export.get();
        } catch (RuntimeException error) {
            result = java.util.concurrent.CompletableFuture.failedFuture(error);
        }
        result.whenComplete((bytes, error) -> server.execute(() -> {
            if (!token.equals(requests.get(playerId))) return;
            if (error != null || bytes.length == 0 || bytes.length > MAX_BYTES) {
                requests.remove(playerId);
                var cause = error instanceof java.util.concurrent.CompletionException ? error.getCause() : error;
                if (cause instanceof cn.quit5700.buildingwand.server.GenerationBoxBlueprint.TooLarge)
                    cn.quit5700.buildingwand.aux.AuxServer.message(player, "message.quick_build.structure_too_large",
                            cn.quit5700.buildingwand.server.GenerationBoxBlueprint.MAX_CELLS);
                ServerPlayNetworking.send(player, new Chunk(token, 0, -1, new byte[0]));
            } else transfers.put(playerId, new Transfer(token, bytes, 0));
        }));
    }

    public record Request(UUID token) implements CustomPacketPayload {
        public static final Type<Request> ID = new Type<>(BuildingWandMod.id("blueprint_export"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = CustomPacketPayload.codec(
                (p, b) -> b.writeUUID(p.token), b -> new Request(b.readUUID()));
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record Chunk(UUID token, int offset, int total, byte[] bytes) implements CustomPacketPayload {
        public static final Type<Chunk> ID = new Type<>(BuildingWandMod.id("blueprint_chunk"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Chunk> CODEC = CustomPacketPayload.codec(
                (p, b) -> { b.writeUUID(p.token); b.writeInt(p.offset); b.writeInt(p.total); b.writeByteArray(p.bytes); },
                b -> new Chunk(b.readUUID(), b.readInt(), b.readInt(), b.readByteArray(CHUNK_BYTES)));
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }
}
