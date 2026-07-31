package cn.quit5700.light.light;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.logic.VariableConstantLightPositions;
import cn.quit5700.light.logic.VariableConstantLightSettings;
import cn.quit5700.light.logic.VariableLightTaskPlan;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.network.VariableLightNetworking;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public final class VariableLightTaskState extends SavedData {
    private static final int MAX_OPERATIONS_PER_TICK = 4_096;

    private static final Codec<TaskData> TASK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("source").forGetter(TaskData::source),
            Codec.INT.fieldOf("radius").forGetter(TaskData::radius),
            Codec.INT.fieldOf("spacing").forGetter(TaskData::spacing),
            Codec.BOOL.fieldOf("install").forGetter(TaskData::install),
            Codec.INT.optionalFieldOf("next_index", 0).forGetter(TaskData::nextIndex)
    ).apply(instance, TaskData::new));
    private static final Codec<SourceData> SOURCE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("source").forGetter(SourceData::source),
            Codec.INT.fieldOf("radius").forGetter(SourceData::radius),
            Codec.INT.fieldOf("spacing").forGetter(SourceData::spacing),
            Codec.INT.optionalFieldOf("mode", 0).forGetter(SourceData::mode)
    ).apply(instance, SourceData::new));

    public static final Codec<VariableLightTaskState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TASK_CODEC.listOf().optionalFieldOf("tasks", List.of()).forGetter(state -> state.tasks),
            SOURCE_CODEC.listOf().optionalFieldOf("sources", List.of()).forGetter(state -> state.sources)
    ).apply(instance, VariableLightTaskState::new));

    private static final String SAVE_ID = "light_variable_light_tasks";
    public static final SavedData.Factory<VariableLightTaskState> TYPE = new SavedData.Factory<>(
            VariableLightTaskState::new,
            tag -> CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(VariableLightTaskState::new),
            DataFixTypes.LEVEL
    );

    private final List<TaskData> tasks;
    private final List<SourceData> sources;

    public VariableLightTaskState() {
        this(List.of(), List.of());
    }

    private VariableLightTaskState(List<TaskData> tasks, List<SourceData> sources) {
        this.sources = new ArrayList<>(sources);
        this.tasks = normalize(tasks, this.sources);
    }

    public static VariableLightTaskState get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(TYPE, SAVE_ID);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        DataResult<net.minecraft.nbt.Tag> encoded = CODEC.encodeStart(NbtOps.INSTANCE, this);
        encoded.result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);
        return tag;
    }

    public void install(BlockPos source, int radius, int spacing, VariableLightMode mode) {
        reconcile(source, radius, spacing, mode, true);
    }

    public void registerExisting(BlockPos source, int radius, int spacing, VariableLightMode mode) {
        replaceSource(source, radius, spacing, mode);
    }

    public void cleanup(BlockPos source, int radius, int spacing, VariableLightMode mode) {
        reconcile(source, radius, spacing, mode, false);
    }

    public void reconfigure(BlockPos source, int oldRadius, int oldSpacing, VariableLightMode oldMode,
                            int newRadius, int newSpacing, VariableLightMode newMode) {
        reconcile(source, oldRadius, oldSpacing, oldMode, newRadius, newSpacing, newMode);
    }

    private void reconcile(BlockPos source, int radius, int spacing, VariableLightMode mode, boolean install) {
        if (install) {
            reconcile(source, radius, spacing, mode, radius, spacing, mode);
            return;
        }
        long sourceKey = source.asLong();
        List<VariableLightTaskPlan.Configuration> historical = historicalConfigurations(
                sourceKey, new VariableLightTaskPlan.Configuration(radius, spacing));
        tasks.removeIf(task -> task.source() == sourceKey);
        sources.removeIf(entry -> entry.source() == sourceKey);
        appendPlan(sourceKey, VariableLightTaskPlan.remove(historical));
        setDirty();
    }

    private void reconcile(BlockPos source, int oldRadius, int oldSpacing, VariableLightMode oldMode,
                           int newRadius, int newSpacing, VariableLightMode newMode) {
        long sourceKey = source.asLong();
        VariableLightTaskPlan.Configuration oldConfiguration =
                new VariableLightTaskPlan.Configuration(oldRadius, oldSpacing);
        VariableLightTaskPlan.Configuration desired =
                new VariableLightTaskPlan.Configuration(newRadius, newSpacing);
        List<VariableLightTaskPlan.Configuration> historical =
                historicalConfigurations(sourceKey, oldConfiguration);
        tasks.removeIf(task -> task.source() == sourceKey);
        replaceSource(source, desired.radius(), desired.spacing(), newMode);
        appendPlan(sourceKey, newMode.usesInvisibleLight()
                ? VariableLightTaskPlan.reconfigure(historical, desired)
                : VariableLightTaskPlan.remove(historical));
        setDirty();
    }

    private List<VariableLightTaskPlan.Configuration> historicalConfigurations(
            long source, VariableLightTaskPlan.Configuration fallback) {
        List<VariableLightTaskPlan.Configuration> configurations = new ArrayList<>();
        configurations.add(fallback);
        for (TaskData task : tasks) {
            if (task.source() == source) {
                configurations.add(new VariableLightTaskPlan.Configuration(task.radius(), task.spacing()));
            }
        }
        return configurations;
    }

    private void appendPlan(long source, VariableLightTaskPlan.Plan plan) {
        for (VariableLightTaskPlan.Configuration configuration : plan.cleanup()) {
            tasks.add(new TaskData(source, configuration.radius(), configuration.spacing(), false, 0));
        }
        if (plan.install() != null) {
            tasks.add(new TaskData(source, plan.install().radius(), plan.install().spacing(), true, 0));
        }
    }

    private static ArrayList<TaskData> normalize(List<TaskData> loadedTasks, List<SourceData> sources) {
        ArrayList<TaskData> normalized = new ArrayList<>();
        List<Long> sourceKeys = loadedTasks.stream().map(TaskData::source).distinct().toList();
        for (long sourceKey : sourceKeys) {
            List<VariableLightTaskPlan.Configuration> historical = loadedTasks.stream()
                    .filter(task -> task.source() == sourceKey)
                    .map(task -> new VariableLightTaskPlan.Configuration(task.radius(), task.spacing()))
                    .toList();
            SourceData desiredSource = sources.stream()
                    .filter(source -> source.source() == sourceKey)
                    .findFirst()
                    .orElse(null);
            VariableLightTaskPlan.Plan plan = desiredSource == null
                    ? VariableLightTaskPlan.remove(historical)
                    : (VariableLightMode.fromOrdinal(desiredSource.mode()).usesInvisibleLight()
                    ? VariableLightTaskPlan.reconfigure(historical,
                    new VariableLightTaskPlan.Configuration(desiredSource.radius(), desiredSource.spacing()))
                    : VariableLightTaskPlan.remove(historical));
            for (VariableLightTaskPlan.Configuration configuration : plan.cleanup()) {
                normalized.add(new TaskData(sourceKey, configuration.radius(), configuration.spacing(), false, 0));
            }
            if (plan.install() != null) {
                normalized.add(new TaskData(sourceKey, plan.install().radius(), plan.install().spacing(), true, 0));
            }
        }
        return normalized;
    }

    private void replaceSource(BlockPos source, int radius, int spacing, VariableLightMode mode) {
        sources.removeIf(entry -> entry.source() == source.asLong());
        sources.add(new SourceData(source.asLong(),
                VariableConstantLightSettings.clampRadius(radius),
                VariableConstantLightSettings.clampSpacing(spacing), mode.ordinal()));
        setDirty();
    }

    private void enqueue(BlockPos source, int radius, int spacing, boolean install) {
        tasks.add(new TaskData(
                source.asLong(),
                VariableConstantLightSettings.clampRadius(radius),
                VariableConstantLightSettings.clampSpacing(spacing),
                install,
                0));
        setDirty();
    }

    public void tick(ServerLevel world) {
        int operations = 0;
        while (operations < MAX_OPERATIONS_PER_TICK && !tasks.isEmpty()) {
            TaskData task = tasks.get(0);
            List<VariableConstantLightPositions.Offset> offsets =
                    VariableConstantLightPositions.relativeOffsets(task.radius(), task.spacing());
            int nextIndex = task.nextIndex();
            while (operations < MAX_OPERATIONS_PER_TICK && nextIndex < offsets.size()) {
                VariableConstantLightPositions.Offset offset = offsets.get(nextIndex++);
                BlockPos lightPos = BlockPos.of(task.source()).offset(offset.dx(), offset.dy(), offset.dz());
                if (!world.isOutsideBuildHeight(lightPos)) {
                    world.getChunkAt(lightPos);
                    if (task.install()) {
                        ConstantLightTaskQueue.installVariableLight(world, lightPos);
                    } else {
                        ConstantLightTaskQueue.cleanupVariableLight(
                                world, lightPos, isCoveredByActiveSource(lightPos));
                    }
                }
                operations++;
            }
            if (nextIndex >= offsets.size()) {
                tasks.remove(0);
            } else {
                tasks.set(0, task.withNextIndex(nextIndex));
            }
            setDirty();
        }
    }

    private boolean isCoveredByActiveSource(BlockPos lightPos) {
        for (SourceData source : sources) {
            if (!VariableLightMode.fromOrdinal(source.mode()).usesInvisibleLight()) continue;
            BlockPos center = BlockPos.of(source.source());
            if (VariableConstantLightPositions.containsOffset(
                    source.radius(), source.spacing(),
                    lightPos.getX() - center.getX(),
                    lightPos.getY() - center.getY(),
                    lightPos.getZ() - center.getZ())) {
                return true;
            }
        }
        return false;
    }

    private record TaskData(long source, int radius, int spacing, boolean install, int nextIndex) {
        private TaskData withNextIndex(int updatedIndex) {
            return new TaskData(source, radius, spacing, install, updatedIndex);
        }
    }

    public List<DaylightSource> daylightSources() {
        return sources.stream()
                .filter(source -> VariableLightMode.fromOrdinal(source.mode()).usesDaylightRendering())
                .map(source -> new DaylightSource(BlockPos.of(source.source()), source.radius()))
                .toList();
    }

    public void syncClients(ServerLevel world) {
        VariableLightNetworking.syncWorld(world);
    }

    public record DaylightSource(BlockPos pos, int radius) { }

    private record SourceData(long source, int radius, int spacing, int mode) {
    }
}
