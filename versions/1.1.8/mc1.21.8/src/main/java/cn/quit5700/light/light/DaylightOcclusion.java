package cn.quit5700.light.light;

import cn.quit5700.light.logic.ConnectedLightVolume;
import cn.quit5700.light.logic.DaylightCoverage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** World reads occur only in tick(), never in renderer worker queries. */
public final class DaylightOcclusion {
    private static final Map<Level, WorldCache> WORLDS = new WeakHashMap<>();
    private DaylightOcclusion() { }
    private static synchronized WorldCache cache(Level level) {
        return WORLDS.computeIfAbsent(level, ignored -> new WorldCache());
    }
    private static SourceCache ensure(WorldCache world, BlockPos pos, int radius) {
        SourceCache result = world.sources.get(pos.asLong());
        if (result == null || result.radius != radius) {
            result = new SourceCache(pos.immutable(), radius);
            world.sources.put(pos.asLong(), result);
        }
        return result;
    }
    public static boolean visible(Level level, BlockPos source, int radius, BlockPos target) {
        if (!DaylightCoverage.contains(source.getX(),source.getY(),source.getZ(),radius,
                target.getX(),target.getY(),target.getZ())) return false;
        WorldCache world = cache(level);
        synchronized (world) {
            SourceCache entry = ensure(world, source, radius);
            return entry.volume != null && entry.volume.contains(target.getX()-source.getX(),
                    target.getY()-source.getY(), target.getZ()-source.getZ());
        }
    }
    public static boolean ready(Level level, BlockPos source, int radius) {
        WorldCache world = cache(level);
        synchronized (world) {
            SourceCache entry = ensure(world, source, radius);
            return entry.volume != null && entry.volume.complete();
        }
    }
    public static void tick(Level level) {
        WorldCache world = cache(level);
        synchronized (world) {
            int budget = 8192;
            for (SourceCache source : world.sources.values()) {
                if (budget <= 0) break;
                budget -= advanceSource(world, source, budget, (x,y,z,nx,ny,nz) ->
                        passage(level, source.pos, source.pos.offset(x,y,z), source.pos.offset(nx,ny,nz)));
            }
        }
    }
    // Shared production seam for invalidation during world-read callbacks.
    private static int advanceSource(WorldCache world, SourceCache source, int budget,
                                     ConnectedLightVolume.Passage passage) {
        if (source.volume == null) source.volume = new ConnectedLightVolume(source.radius);
        ConnectedLightVolume calculating = source.volume;
        if (calculating.complete()) return 0;
        int used = calculating.advance(budget, passage);
        // World reads can synchronously invalidate this cache on the same thread.
        // Never dereference or publish a replacement/invalidated volume.
        if (source.volume == calculating && calculating.complete()) world.revision++;
        return used;
    }
    private static boolean passage(Level level, BlockPos lamp, BlockPos from, BlockPos to) {
        if (level.isOutsideBuildHeight(to) || !level.hasChunk(to.getX() >> 4,to.getZ() >> 4)
                || !level.hasChunk(from.getX() >> 4,from.getZ() >> 4)) return false;
        BlockState b = level.getBlockState(to);
        if (b.isSolidRender() || closedDoor(b)) return false;
        if (from.equals(lamp)) return true;
        BlockState a = level.getBlockState(from);
        if (closedDoor(a)) return false;
        Direction direction = to.getX() > from.getX() ? Direction.EAST : to.getX() < from.getX() ? Direction.WEST
                : to.getY() > from.getY() ? Direction.UP : to.getY() < from.getY() ? Direction.DOWN
                : to.getZ() > from.getZ() ? Direction.SOUTH : Direction.NORTH;
        var shapeA = a.canOcclude() ? a.getOcclusionShape() : Shapes.empty();
        var shapeB = b.canOcclude() ? b.getOcclusionShape() : Shapes.empty();
        if (a.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock) shapeA = a.getCollisionShape(level, from);
        if (b.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock) shapeB = b.getCollisionShape(level, to);
        return !Shapes.faceShapeOccludes(shapeA.getFaceShape(direction), shapeB.getFaceShape(direction.getOpposite()));
    }
    private static boolean closedDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock && !state.getValue(BlockStateProperties.OPEN);
    }
    public static void changed(Level level, BlockPos pos) {
        WorldCache world;
        synchronized (DaylightOcclusion.class) { world = WORLDS.get(level); }
        if (world == null) return;
        synchronized (world) {
            for (SourceCache source : world.sources.values()) {
                if (DaylightCoverage.contains(source.pos.getX(),source.pos.getY(),source.pos.getZ(),
                        source.radius+1,pos.getX(),pos.getY(),pos.getZ())) {
                    source.volume = null;
                    world.revision++;
                }
            }
        }
    }
    public static void chunkChanged(Level level, int x, int z) {
        WorldCache world;
        synchronized (DaylightOcclusion.class) { world = WORLDS.get(level); }
        if (world == null) return;
        synchronized (world) {
            for (SourceCache source : world.sources.values()) {
                if (x >= ((source.pos.getX()-source.radius)>>4) && x <= ((source.pos.getX()+source.radius)>>4)
                        && z >= ((source.pos.getZ()-source.radius)>>4) && z <= ((source.pos.getZ()+source.radius)>>4)) {
                    source.volume = null; world.revision++;
                }
            }
        }
    }
    public static long revision(Level level) {
        WorldCache world = cache(level);
        synchronized (world) { return world.revision; }
    }
    public static void clear(Level level) {
        synchronized (DaylightOcclusion.class) { WORLDS.remove(level); }
    }
    public static void remove(Level level, BlockPos pos) {
        WorldCache world;
        synchronized (DaylightOcclusion.class) { world = WORLDS.get(level); }
        if (world != null) synchronized (world) {
            world.sources.remove(pos.asLong()); world.revision++;
        }
    }
    private static final class WorldCache {
        final Map<Long,SourceCache> sources = new LinkedHashMap<>();
        long revision;
    }
    private static final class SourceCache {
        final BlockPos pos;
        final int radius;
        ConnectedLightVolume volume;
        SourceCache(BlockPos pos,int radius) { this.pos=pos; this.radius=radius; }
    }
}
