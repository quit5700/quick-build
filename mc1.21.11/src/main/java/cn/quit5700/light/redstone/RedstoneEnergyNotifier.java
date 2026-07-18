package cn.quit5700.light.redstone;

import cn.quit5700.light.block.RedstoneEnergyEmitterBlock;
import cn.quit5700.light.registry.LightBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import cn.quit5700.light.block.RedstoneEnergyRemoteSwitchBlock;

public final class RedstoneEnergyNotifier {
    private static final Set<TrackedEmitter> EMITTERS = new HashSet<>();
    private static final Set<QueuedChunk> QUEUED_CHUNKS = new HashSet<>();

    private RedstoneEnergyNotifier() {
    }

    public static void clear() {
        EMITTERS.clear();
        QUEUED_CHUNKS.clear();
    }

    public static void queueChunkSync(ServerLevel world, ChunkPos pos) {
        QUEUED_CHUNKS.add(new QueuedChunk(world, pos));
    }

    public static void tickQueuedChunkSyncs() {
        if (QUEUED_CHUNKS.isEmpty()) return;
        Set<QueuedChunk> queued = new HashSet<>(QUEUED_CHUNKS);
        QUEUED_CHUNKS.clear();
        for (QueuedChunk entry : queued) {
            if (entry.world.isLoaded(entry.pos.getWorldPosition())) {
                RedstoneEnergyState.get(entry.world).syncChunk(entry.world, entry.pos);
            }
        }
    }

    public static void register(ServerLevel world, BlockPos pos) {
        EMITTERS.add(new TrackedEmitter(world, pos.immutable()));
    }

    public static void unregister(ServerLevel world, BlockPos pos) {
        EMITTERS.remove(new TrackedEmitter(world, pos.immutable()));
    }

    public static void syncEmitter(ServerLevel world, BlockPos pos) {
        if (!world.isLoaded(pos)) return;
        BlockState state = world.getBlockState(pos);
        if (!state.is(LightBlocks.REDSTONE_ENERGY_EMITTER)) {
            return;
        }
        RedstoneEnergyState.NetworkInfo info = RedstoneEnergyState.get(world).info(world, pos);
        boolean enabled = info.powered();
        EnergyColor color = info.switches() > 0 ? info.color() : EnergyColor.NONE;
        if (state.getValue(RedstoneEnergyEmitterBlock.ENABLED) != enabled
                || state.getValue(RedstoneEnergyEmitterBlock.COLOR) != color) {
            world.setBlock(pos, state.setValue(RedstoneEnergyEmitterBlock.ENABLED, enabled)
                    .setValue(RedstoneEnergyEmitterBlock.COLOR, color), 3);
        }
    }

    public static void syncLoadedNetworkNodes(ServerLevel sourceWorld, RedstoneEnergyState state, Set<String> members) {
        for (ServerLevel world : sourceWorld.getServer().getAllLevels()) {
            String worldId = RedstoneEnergyState.worldId(world);
            for (String key : members) {
                EnergyNode node = state.node(key);
                if (node == null || !node.world().equals(worldId) || !world.isLoaded(node.pos())) continue;
                if (node.type() == EnergyNodeType.EMITTER) {
                    syncEmitter(world, node.pos());
                } else if (node.type() == EnergyNodeType.SWITCH) {
                    BlockState blockState = world.getBlockState(node.pos());
                    if (blockState.getBlock() instanceof RedstoneEnergyRemoteSwitchBlock
                            && blockState.getValue(RedstoneEnergyRemoteSwitchBlock.POWERED) != node.powered()) {
                        world.setBlock(node.pos(), blockState.setValue(RedstoneEnergyRemoteSwitchBlock.POWERED, node.powered()), 3);
                    }
                }
            }
        }
    }

    public static void syncLoadedEmitters() {
        Iterator<TrackedEmitter> iterator = EMITTERS.iterator();
        while (iterator.hasNext()) {
            TrackedEmitter emitter = iterator.next();
            if (!emitter.world.isLoaded(emitter.pos)) {
                iterator.remove();
                continue;
            }
            syncEmitter(emitter.world, emitter.pos);
        }
    }

    private record TrackedEmitter(ServerLevel world, BlockPos pos) {
    }
    private record QueuedChunk(ServerLevel world, ChunkPos pos) {
    }
}
