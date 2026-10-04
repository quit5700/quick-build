package cn.quit5700.light.light;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import cn.quit5700.light.logic.LightBox;
import cn.quit5700.light.logic.VariableLightMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import java.util.ArrayList;
import java.util.List;

public final class AreaLightState extends SavedData {
    public static final Codec<LightBox> BOX_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("min_x").forGetter(LightBox::minX), Codec.INT.fieldOf("min_y").forGetter(LightBox::minY),
            Codec.INT.fieldOf("min_z").forGetter(LightBox::minZ), Codec.INT.fieldOf("max_x").forGetter(LightBox::maxX),
            Codec.INT.fieldOf("max_y").forGetter(LightBox::maxY), Codec.INT.fieldOf("max_z").forGetter(LightBox::maxZ)
    ).apply(i,LightBox::new));
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.fieldOf("source").forGetter(Entry::source), BOX_CODEC.fieldOf("box").forGetter(Entry::box)
    ).apply(i,Entry::new));
    public static final Codec<AreaLightState> CODEC = RecordCodecBuilder.create(i -> i.group(
            ENTRY_CODEC.listOf().optionalFieldOf("areas",List.of()).forGetter(s -> s.entries)
    ).apply(i,AreaLightState::new));
    private static final String SAVE_ID="light_area_light_ranges";
    public static final SavedData.Factory<AreaLightState> TYPE=new SavedData.Factory<>(
            ()->new AreaLightState(List.of()),tag->CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet(()->new AreaLightState(List.of())),DataFixTypes.LEVEL);
    @Override public CompoundTag save(CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);
        return tag;
    }
    private final List<Entry> entries;
    private AreaLightState(List<Entry> entries) { this.entries = new ArrayList<>(entries); }
    public static AreaLightState get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE,SAVE_ID); }
    public List<Entry> entries() { return List.copyOf(entries); }
    public void remove(BlockPos source) { if(entries.removeIf(e->e.source()==source.asLong())) setDirty(); }
    public void update(ServerLevel level,BlockPos source) {
        if(level.getBlockEntity(source) instanceof VariableConstantLightBlockEntity lamp) update(lamp);
        else remove(source);
    }
    /** Safe during setLevel: the entity has not yet been inserted into its chunk. */
    public void update(VariableConstantLightBlockEntity lamp) {
        BlockPos source = lamp.getBlockPos();
        remove(source);
        if(!lamp.isLegacy()
                && lamp.mode()==VariableLightMode.AREA_FULL_BRIGHT && lamp.area()!=null
                && lamp.getBlockState().getValue(cn.quit5700.light.block.VariableConstantLightBlock.POWERED)) {
            entries.add(new Entry(source.asLong(),lamp.area())); setDirty();
        }
    }
    public boolean contains(BlockPos pos) {
        for(Entry entry:entries) if(entry.box().contains(pos.getX(),pos.getY(),pos.getZ())) return true;
        return false;
    }
    public record Entry(long source,LightBox box) { }
}
