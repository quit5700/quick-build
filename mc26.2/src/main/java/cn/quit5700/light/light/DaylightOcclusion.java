package cn.quit5700.light.light;

import cn.quit5700.light.logic.DaylightCoverage;
import cn.quit5700.light.logic.DaylightVisibility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Lazy, bounded visibility caches. Never loads chunks or creates light blocks. */
public final class DaylightOcclusion {
    private static final Map<Level, WorldCache> WORLDS = new WeakHashMap<>();
    private static final int MAX_SECTIONS = 128;

    private DaylightOcclusion() { }

    private static synchronized WorldCache cache(Level level) {
        return WORLDS.computeIfAbsent(level, ignored -> new WorldCache());
    }

    public static boolean visible(Level level, BlockPos source, int radius, BlockPos target) {
        if (!DaylightCoverage.contains(source.getX(), source.getY(), source.getZ(), radius,
                target.getX(), target.getY(), target.getZ())) return false;
        WorldCache world = cache(level);
        synchronized (world) {
            SourceCache sourceCache = world.sources.computeIfAbsent(source.asLong(),
                    ignored -> new SourceCache(source.immutable(), radius));
            if (sourceCache.radius != radius) {
                sourceCache = new SourceCache(source.immutable(), radius);
                world.sources.put(source.asLong(), sourceCache);
            }
            long sectionKey = BlockPos.asLong(target.getX() >> 4, target.getY() >> 4, target.getZ() >> 4);
            byte[] section = sourceCache.sections.get(sectionKey);
            if (section == null) {
                if (sourceCache.sections.size() >= MAX_SECTIONS)
                    sourceCache.sections.remove(sourceCache.sections.keySet().iterator().next());
                section = new byte[4096];
                sourceCache.sections.put(sectionKey, section);
            }
            int index = (target.getX() & 15) | ((target.getZ() & 15) << 4) | ((target.getY() & 15) << 8);
            if (section[index] != 0) return section[index] == 1;
            Vec3 from = Vec3.atCenterOf(source);
            Vec3 to = Vec3.atCenterOf(target);
            boolean[] unloaded = {false};
            boolean result = DaylightVisibility.visible(source.getX(), source.getY(), source.getZ(),
                    target.getX(), target.getY(), target.getZ(), (x, y, z) -> {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!level.hasChunk(x >> 4, z >> 4)) {
                            unloaded[0] = true;
                            return true;
                        }
                        var shape = level.getBlockState(pos).getCollisionShape(level, pos);
                        return !shape.isEmpty() && shape.clip(from, to, pos) != null;
                    });
            // Missing chunks must be reconsidered when they become available.
            if (!unloaded[0]) section[index] = (byte) (result ? 1 : 2);
            return result;
        }
    }

    public static void changed(Level level, BlockPos pos) {
        WorldCache world;
        synchronized (DaylightOcclusion.class) { world = WORLDS.get(level); }
        if (world == null) return;
        synchronized (world) {
            for (SourceCache source : world.sources.values()) {
                if (DaylightCoverage.contains(source.pos.getX(), source.pos.getY(), source.pos.getZ(),
                        source.radius, pos.getX(), pos.getY(), pos.getZ())) {
                    source.sections.clear();
                    world.revision++;
                }
            }
        }
    }

    public static long revision(Level level) {
        WorldCache world = cache(level);
        synchronized (world) { return world.revision; }
    }

    public static void chunkChanged(Level level, int chunkX, int chunkZ) {
        WorldCache world;
        synchronized (DaylightOcclusion.class) { world = WORLDS.get(level); }
        if (world == null) return;
        synchronized (world) {
            for (SourceCache source : world.sources.values()) {
                if (chunkX >= (source.pos.getX() - source.radius) >> 4
                        && chunkX <= (source.pos.getX() + source.radius) >> 4
                        && chunkZ >= (source.pos.getZ() - source.radius) >> 4
                        && chunkZ <= (source.pos.getZ() + source.radius) >> 4) {
                    source.sections.clear();
                    world.revision++;
                }
            }
        }
    }

    public static void clear(Level level) {
        synchronized (DaylightOcclusion.class) { WORLDS.remove(level); }
    }

    private static final class WorldCache {
        final Map<Long, SourceCache> sources = new LinkedHashMap<>();
        long revision;
    }

    private static final class SourceCache {
        final BlockPos pos;
        final int radius;
        final Map<Long, byte[]> sections = new LinkedHashMap<>(16, 0.75f, true);
        SourceCache(BlockPos pos, int radius) { this.pos = pos; this.radius = radius; }
    }
}
