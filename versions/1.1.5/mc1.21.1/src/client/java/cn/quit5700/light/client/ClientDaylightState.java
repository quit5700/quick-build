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
    private static final java.util.ArrayDeque<SectionRefresh> REFRESHES=new java.util.ArrayDeque<>();
    private static final DaylightSnapshotMailbox<VariableLightNetworking.Entry> MAILBOX =
            new DaylightSnapshotMailbox<>();

    private ClientDaylightState() { }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(VariableLightNetworking.SnapshotPayload.ID,
                (payload, context) -> MAILBOX.offer(payload.entries()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MAILBOX.offer(List.of()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> applyPending(client));
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> DaylightOcclusion.chunkChanged(world, chunk.getPos().x, chunk.getPos().z));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> DaylightOcclusion.chunkChanged(world, chunk.getPos().x, chunk.getPos().z));
    }

    public static boolean isFullBright(BlockPos pos) {
        return isFullBright(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isFullBright(int x, int y, int z) {
        for (VariableLightNetworking.Entry entry : entries) {
            if(entry.box()!=null) {
                if(entry.box().contains(x,y,z)) return true;
                continue;
            }
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
        if(minecraft.level==null) REFRESHES.clear();
        else for(int budget=0,scanned=0;budget<128 && scanned<2048 && !REFRESHES.isEmpty();scanned++) {
            SectionRefresh next=REFRESHES.peek();
            if(!minecraft.level.hasChunk(next.x,next.z)) {
                next.y=next.maxY;
            } else {
                minecraft.levelRenderer.setSectionDirty(next.x,next.y,next.z); budget++;
            }
            if(!next.advance()) REFRESHES.remove();
        }
        if (minecraft.level != null) DaylightOcclusion.tick(minecraft.level);
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
        if(!previous.stream().filter(VariableLightNetworking.Entry::occluded).toList().equals(entries.stream().filter(VariableLightNetworking.Entry::occluded).toList())) {
            DaylightOcclusion.clear(minecraft.level); occlusionRevision=0;
        }
        markRangesDirty(minecraft, previous.stream().filter(e->!entries.contains(e)).toList());
        markRangesDirty(minecraft, entries.stream().filter(e->!previous.contains(e)).toList());
    }

    private static void markRangesDirty(Minecraft minecraft, List<VariableLightNetworking.Entry> ranges) {
        for (VariableLightNetworking.Entry entry : ranges) {
            BlockPos center = entry.pos();
            int radius = entry.radius();
            var box=entry.box();
            REFRESHES.add(box==null ? new SectionRefresh(center.getX()-radius,center.getY()-radius,center.getZ()-radius,
                    center.getX()+radius,center.getY()+radius,center.getZ()+radius)
                    : new SectionRefresh(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()));
        }
    }
    private static final class SectionRefresh {
        final int minX,minY,minZ,maxX,maxY,maxZ; int x,y,z;
        SectionRefresh(int a,int b,int c,int d,int e,int f){minX=(a-1)>>4;minY=(b-1)>>4;minZ=(c-1)>>4;maxX=(d+1)>>4;maxY=(e+1)>>4;maxZ=(f+1)>>4;x=minX;y=minY;z=minZ;}
        boolean advance(){if(++y<=maxY)return true;y=minY;if(++x<=maxX)return true;x=minX;return ++z<=maxZ;}
    }
}
