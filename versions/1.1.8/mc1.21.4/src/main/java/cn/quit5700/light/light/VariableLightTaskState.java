package cn.quit5700.light.light;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.logic.VariableConstantLightPositions;
import cn.quit5700.light.logic.VariableConstantLightSettings;
import cn.quit5700.light.logic.VariableLightTaskPlan;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.network.VariableLightNetworking;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

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
            Codec.INT.optionalFieldOf("mode", 0).forGetter(SourceData::mode),
            Codec.BOOL.optionalFieldOf("legacy", false).forGetter(SourceData::legacy)
    ).apply(instance, SourceData::new));

    public static final Codec<VariableLightTaskState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TASK_CODEC.listOf().optionalFieldOf("tasks", List.of()).forGetter(state -> state.tasks),
            SOURCE_CODEC.listOf().optionalFieldOf("sources", List.of()).forGetter(state -> state.sources)
    ).apply(instance, VariableLightTaskState::new));

    private static final String SAVE_ID="light_variable_light_tasks";
    public static final SavedData.Factory<VariableLightTaskState> TYPE=new SavedData.Factory<>(
            ()->new VariableLightTaskState(),(tag,registries)->CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet(()->new VariableLightTaskState()),DataFixTypes.LEVEL);
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries) {
        CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);
        return tag;
    }

    private final List<TaskData> tasks;
    private final List<SourceData> sources;
    private long connectedRevision = -1;

    public void setLegacy(BlockPos pos, boolean legacy) {
        for (int i = 0; i < sources.size(); i++) {
            SourceData source = sources.get(i);
            if (source.source() == pos.asLong() && source.legacy() != legacy) {
                sources.set(i, new SourceData(source.source(), source.radius(), source.spacing(), source.mode(), legacy));
                setDirty();
            }
        }
    }

    public VariableLightTaskState() {
        this(List.of(), List.of());
    }

    private VariableLightTaskState(List<TaskData> tasks, List<SourceData> sources) {
        this.sources = new ArrayList<>(sources);
        this.tasks = normalize(tasks, this.sources);
    }

    public static VariableLightTaskState get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(TYPE,SAVE_ID);
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
                sourceKey, mode.usesRadiusLight() ? new VariableLightTaskPlan.Configuration(radius, spacing) : null);
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
                historicalConfigurations(sourceKey, oldMode.usesRadiusLight() ? oldConfiguration : null);
        tasks.removeIf(task -> task.source() == sourceKey);
        replaceSource(source, desired.radius(), desired.spacing(), newMode);
        appendPlan(sourceKey, newMode.usesRadiusLight()
                ? VariableLightTaskPlan.reconfigure(historical, desired)
                : VariableLightTaskPlan.remove(historical));
        setDirty();
    }

    private List<VariableLightTaskPlan.Configuration> historicalConfigurations(
            long source, VariableLightTaskPlan.Configuration fallback) {
        List<VariableLightTaskPlan.Configuration> configurations = new ArrayList<>();
        if(fallback!=null)configurations.add(fallback);
        for (TaskData task : tasks) {
            if (task.source() == source) {
                configurations.add(new VariableLightTaskPlan.Configuration(task.radius(), task.spacing()));
            }
        }
        return configurations;
    }

    private void appendPlan(long source, VariableLightTaskPlan.Plan plan) {
        if (plan.install() != null) {
            tasks.add(new TaskData(source, plan.install().radius(), plan.install().spacing(), true, 0));
        }
        for (VariableLightTaskPlan.Configuration configuration : plan.cleanup()) {
            tasks.add(new TaskData(source, configuration.radius(), configuration.spacing(), false, 0));
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
                    : (VariableLightMode.fromOrdinal(desiredSource.mode()).usesRadiusLight()
                    ? VariableLightTaskPlan.reconfigure(historical,
                    new VariableLightTaskPlan.Configuration(desiredSource.radius(), desiredSource.spacing()))
                    : VariableLightTaskPlan.remove(historical));
            if (plan.install() != null) {
                normalized.add(new TaskData(sourceKey, plan.install().radius(), plan.install().spacing(), true, 0));
            }
            for (VariableLightTaskPlan.Configuration configuration : plan.cleanup()) {
                normalized.add(new TaskData(sourceKey, configuration.radius(), configuration.spacing(), false, 0));
            }
        }
        return normalized;
    }

    private void replaceSource(BlockPos source, int radius, int spacing, VariableLightMode mode) {
        boolean legacy = sources.stream().anyMatch(entry -> entry.source() == source.asLong() && entry.legacy());
        sources.removeIf(entry -> entry.source() == source.asLong());
        sources.add(new SourceData(source.asLong(),
                VariableConstantLightSettings.clampRadius(radius),
                VariableConstantLightSettings.clampSpacing(spacing), mode.ordinal(), legacy));
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
        boolean ready = true;
        for (SourceData source : sources) {
            if (!source.legacy() && !VariableLightMode.fromOrdinal(source.mode()).isAreaMode())
                ready &= DaylightOcclusion.ready(world, BlockPos.of(source.source()), source.radius());
        }
        long revision = DaylightOcclusion.revision(world);
        if (ready && revision != connectedRevision) {
            connectedRevision = revision;
            for (SourceData source : List.copyOf(sources)) {
                if (source.legacy() || !VariableLightMode.fromOrdinal(source.mode()).usesRadiusLight()) continue;
                tasks.removeIf(task -> task.source() == source.source() && task.install());
                tasks.addFirst(new TaskData(source.source(), source.radius(), 1, true, 0));
                if (tasks.stream().noneMatch(task -> task.source() == source.source() && !task.install()
                        && task.radius() == source.radius() && task.spacing() == 1))
                    tasks.add(new TaskData(source.source(), source.radius(), 1, false, 0));
            }
        }
        int operations = 0;
        while (operations < MAX_OPERATIONS_PER_TICK && !tasks.isEmpty()) {
            TaskData task = tasks.getFirst();
            SourceData owner = sources.stream().filter(source -> source.source() == task.source()).findFirst().orElse(null);
            boolean connected = owner != null && !owner.legacy();
            if (connected && task.install() && !DaylightOcclusion.ready(world, BlockPos.of(owner.source()), owner.radius())) break;
            List<VariableConstantLightPositions.Offset> offsets =
                    VariableConstantLightPositions.relativeOffsets(task.radius(), task.spacing());
            int nextIndex = task.nextIndex();
            while (operations < MAX_OPERATIONS_PER_TICK && nextIndex < offsets.size()) {
                VariableConstantLightPositions.Offset offset = offsets.get(nextIndex++);
                BlockPos lightPos = BlockPos.of(task.source()).offset(offset.dx(), offset.dy(), offset.dz());
                if (!world.isOutsideBuildHeight(lightPos)) {
                    world.getChunkAt(lightPos);
                    if (task.install()) {
                        if (!connected || DaylightOcclusion.visible(world, BlockPos.of(owner.source()), owner.radius(), lightPos))
                            ConstantLightTaskQueue.installVariableLight(world, lightPos);
                    } else {
                        ConstantLightTaskQueue.cleanupVariableLight(
                                world, lightPos, isCoveredByActiveSource(world, lightPos));
                    }
                }
                operations++;
            }
            if (nextIndex >= offsets.size()) {
                tasks.removeFirst();
            } else {
                tasks.set(0, task.withNextIndex(nextIndex));
            }
            setDirty();
        }
    }

    private boolean isCoveredByActiveSource(ServerLevel world, BlockPos lightPos) {
        return RealAreaLightState.get(world).contains(lightPos) || ConstantLightTaskQueue.isConstantLightTracked(world,lightPos)
                || isCoveredByRadiusSource(world,lightPos);
    }

    public boolean isCoveredByRadiusSource(ServerLevel world, BlockPos lightPos) {
        for (SourceData source : sources) {
            if (!VariableLightMode.fromOrdinal(source.mode()).usesRadiusLight()) continue;
            BlockPos center = BlockPos.of(source.source());
            if (!source.legacy()) {
                if (DaylightOcclusion.visible(world, center, source.radius(), lightPos)) return true;
                continue;
            }
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
                .filter(source -> VariableLightMode.fromOrdinal(source.mode()).usesDaylightRendering()
                        && VariableLightMode.fromOrdinal(source.mode())!=VariableLightMode.AREA_FULL_BRIGHT)
                .map(source -> new DaylightSource(BlockPos.of(source.source()), source.radius(),
                        !source.legacy()))
                .toList();
    }

    public void syncClients(ServerLevel world) {
        VariableLightNetworking.syncWorld(world);
    }

    public boolean preventsNaturalMonsterSpawn(ServerLevel world, BlockPos pos) {
        if (AreaLightState.get(world).contains(pos)) return true;
        for (SourceData source : sources) {
            if (!source.legacy() && VariableLightMode.fromOrdinal(source.mode()) == VariableLightMode.LOCAL_DAYLIGHT
                    && DaylightOcclusion.visible(world, BlockPos.of(source.source()), source.radius(), pos))
                return true;
        }
        return false;
    }

    public record DaylightSource(BlockPos pos, int radius, boolean occluded) { }

    private record SourceData(long source, int radius, int spacing, int mode, boolean legacy) {
    }
}
