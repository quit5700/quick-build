package cn.quit5700.buildingwand.config;

import cn.quit5700.buildingwand.BuildingWandMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class WandSettingsState extends SavedData {
    public static final int DEFAULT_PLANE_LIMIT = 256;
    public static final int DEFAULT_CUBE_LIMIT = 1024;
    public static final int DEFAULT_MOVE_LIMIT = 4096;

    public static final Codec<WandSettingsState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("plane_limit", DEFAULT_PLANE_LIMIT).forGetter(WandSettingsState::planeLimit),
            Codec.INT.optionalFieldOf("cube_limit", DEFAULT_CUBE_LIMIT).forGetter(WandSettingsState::cubeLimit),
            Codec.INT.optionalFieldOf("move_limit", DEFAULT_MOVE_LIMIT).forGetter(WandSettingsState::moveLimit)
    ).apply(instance, WandSettingsState::new));

    public static final String SAVE_ID = "building_wand_wand_settings";

    private int planeLimit;
    private int cubeLimit;
    private int moveLimit;

    public WandSettingsState() {
        this(DEFAULT_PLANE_LIMIT, DEFAULT_CUBE_LIMIT, DEFAULT_MOVE_LIMIT);
    }

    public WandSettingsState(int planeLimit, int cubeLimit, int moveLimit) {
        this.planeLimit = Math.max(1, planeLimit);
        this.cubeLimit = Math.max(1, cubeLimit);
        this.moveLimit = Math.max(1, moveLimit);
    }

    public static WandSettingsState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                tag -> CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(WandSettingsState::new),
                WandSettingsState::new,
                SAVE_ID);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        DataResult<net.minecraft.nbt.Tag> encoded = CODEC.encodeStart(NbtOps.INSTANCE, this);
        encoded.result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast)
                .ifPresent(tag::merge);
        return tag;
    }

    public int planeLimit() {
        return planeLimit;
    }

    public int cubeLimit() {
        return cubeLimit;
    }

    public int moveLimit() {
        return moveLimit;
    }

    public void setLimits(int planeLimit, int cubeLimit, int moveLimit) {
        this.planeLimit = Math.max(1, planeLimit);
        this.cubeLimit = Math.max(1, cubeLimit);
        this.moveLimit = Math.max(1, moveLimit);
        setDirty();
    }
}
