package cn.quit5700.light.client;

import cn.quit5700.light.network.VariableLightNetworking;
import cn.quit5700.light.logic.DaylightCoverage;
import cn.quit5700.light.logic.DaylightSnapshotMailbox;
import cn.quit5700.light.light.DaylightOcclusion;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.List;

public final class ClientDaylightState {
    private static volatile List<VariableLightNetworking.Entry> entries = List.of();
    private static long occlusionRevision;
    private static final DaylightSnapshotMailbox<VariableLightNetworking.Entry> MAILBOX =
            new DaylightSnapshotMailbox<>();

    private ClientDaylightState() { }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(VariableLightNetworking.SnapshotPayload.ID,
                (payload, context) -> MAILBOX.offer(payload.entries()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MAILBOX.offer(List.of()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> applyPending(client));
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> DaylightOcclusion.chunkChanged(world, chunk.getPos().x(), chunk.getPos().z()));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> DaylightOcclusion.chunkChanged(world, chunk.getPos().x(), chunk.getPos().z()));
    }

    public static boolean isFullBright(BlockPos pos) {
        return isFullBright(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isFullBright(int x, int y, int z) {
        for (VariableLightNetworking.Entry entry : entries) {
            BlockPos center = entry.pos();
            int radius = entry.radius();
            if (DaylightCoverage.contains(center.getX(), center.getY(), center.getZ(), radius,
                    x, y, z)) {
                if (!entry.occluded()) return true;
                var world = Minecraft.getInstance().level;
                if (world != null && DaylightOcclusion.visible(world, center, radius, new BlockPos(x, y, z)))
                    return true;
            }
        }
        return false;
    }

    private static void applyPending(Minecraft minecraft) {
        List<VariableLightNetworking.Entry> updated = MAILBOX.drain();
        if (updated == null) {
            if (minecraft.level != null) {
                long revision = DaylightOcclusion.revision(minecraft.level);
                if (revision != occlusionRevision) {
                    occlusionRevision = revision;
                    markRangesDirty(minecraft, entries.stream().filter(VariableLightNetworking.Entry::occluded).toList());
                }
            }
            return;
        }
        List<VariableLightNetworking.Entry> previous = entries;
        entries = List.copyOf(updated);
        if (minecraft.level == null) return;
        DaylightOcclusion.clear(minecraft.level);
        occlusionRevision = 0;
        markRangesDirty(minecraft, previous);
        markRangesDirty(minecraft, entries);
    }

    private static void markRangesDirty(Minecraft minecraft, List<VariableLightNetworking.Entry> ranges) {
        for (VariableLightNetworking.Entry entry : ranges) {
            BlockPos center = entry.pos();
            int radius = entry.radius();
            minecraft.level.setSectionRangeDirty(
                    (center.getX() - radius) >> 4,
                    (center.getY() - radius) >> 4,
                    (center.getZ() - radius) >> 4,
                    (center.getX() + radius) >> 4,
                    (center.getY() + radius) >> 4,
                    (center.getZ() + radius) >> 4);
        }
    }
}
