package cn.quit5700.light.registry;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.*;
import cn.quit5700.light.redstone.EnergyColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public final class LightBlocks {
    public static final ConstantLightBlock CONSTANT_LIGHT_BLOCK = register("constant_light_block", ConstantLightBlock::new);
    public static final RedstoneEnergyEmitterBlock REDSTONE_ENERGY_EMITTER = register("redstone_energy_emitter", RedstoneEnergyEmitterBlock::new);
    public static final Map<EnergyColor, RedstoneEnergyRemoteSwitchBlock> REMOTE_SWITCHES = new EnumMap<>(EnergyColor.class);
    public static final RedstoneEnergyRemoteSwitchBlock REDSTONE_ENERGY_REMOTE_SWITCH;
    public static final RedstoneEnergySensorBlock REDSTONE_ENERGY_SENSOR = register("redstone_energy_sensor", RedstoneEnergySensorBlock::new);
    public static final RedstoneSignalDelayBlock REDSTONE_SIGNAL_DELAY = register("redstone_signal_delay", RedstoneSignalDelayBlock::new);

    static {
        for (EnergyColor color : EnergyColor.values()) {
            if (color == EnergyColor.NONE) continue;
            String id = color == EnergyColor.WHITE ? "redstone_energy_remote_switch" : "redstone_energy_remote_switch_" + color.getSerializedName();
            REMOTE_SWITCHES.put(color, register(id, key -> new RedstoneEnergyRemoteSwitchBlock(key, color)));
        }
        REDSTONE_ENERGY_REMOTE_SWITCH = REMOTE_SWITCHES.get(EnergyColor.WHITE);
    }

    private static <T extends Block> T register(String path, java.util.function.Function<ResourceKey<Block>, T> factory) {
        ResourceLocation id = LightMod.id(path);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(key));
    }

    private LightBlocks() {}
    public static void initialize() {}
}
