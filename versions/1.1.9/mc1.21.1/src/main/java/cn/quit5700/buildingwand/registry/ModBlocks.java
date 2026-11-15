package cn.quit5700.buildingwand.registry;

import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.block.WandSettingsStationBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    public static final ResourceLocation WAND_SETTINGS_STATION_ID = BuildingWandMod.id("wand_settings_station");
    public static final Block WAND_SETTINGS_STATION = Registry.register(
            BuiltInRegistries.BLOCK,
            WAND_SETTINGS_STATION_ID,
            new WandSettingsStationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                    )
    );

    private ModBlocks() {
    }

    public static void initialize() {
    }
}
