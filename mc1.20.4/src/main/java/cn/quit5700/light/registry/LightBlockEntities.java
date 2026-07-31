package cn.quit5700.light.registry;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.RedstoneEnergyEmitterBlockEntity;
import cn.quit5700.light.block.RedstoneEnergySensorBlockEntity;
import cn.quit5700.light.block.RedstoneSignalDelayBlockEntity;
import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public final class LightBlockEntities {
    public static final BlockEntityType<RedstoneEnergyEmitterBlockEntity> REDSTONE_ENERGY_EMITTER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            LightMod.id("redstone_energy_emitter"),
            FabricBlockEntityTypeBuilder.create(RedstoneEnergyEmitterBlockEntity::new, LightBlocks.REDSTONE_ENERGY_EMITTER).build()
    );
    public static final BlockEntityType<RedstoneEnergySensorBlockEntity> REDSTONE_ENERGY_SENSOR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, LightMod.id("redstone_energy_sensor"),
            FabricBlockEntityTypeBuilder.create(RedstoneEnergySensorBlockEntity::new, LightBlocks.REDSTONE_ENERGY_SENSOR).build());
    public static final BlockEntityType<RedstoneSignalDelayBlockEntity> REDSTONE_SIGNAL_DELAY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, LightMod.id("redstone_signal_delay"),
            FabricBlockEntityTypeBuilder.create(RedstoneSignalDelayBlockEntity::new, LightBlocks.REDSTONE_SIGNAL_DELAY).build());
    public static final BlockEntityType<VariableConstantLightBlockEntity> VARIABLE_CONSTANT_LIGHT = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, LightMod.id("variable_constant_light"),
            FabricBlockEntityTypeBuilder.create(VariableConstantLightBlockEntity::new, LightBlocks.VARIABLE_CONSTANT_LIGHT).build());

    private LightBlockEntities() {
    }

    public static void initialize() {
    }
}
