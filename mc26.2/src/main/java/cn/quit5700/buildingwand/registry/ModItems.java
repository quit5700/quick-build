package cn.quit5700.buildingwand.registry;

import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.item.BuildingWandItem;
import cn.quit5700.buildingwand.item.MoveWandItem;
import cn.quit5700.common.item.DeliberateBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    private static final Identifier BUILDING_WAND_ID = BuildingWandMod.id("building_wand");
    private static final Identifier MOVE_WAND_ID = BuildingWandMod.id("move_wand");
    private static final Identifier WAND_SETTINGS_STATION_ID = BuildingWandMod.id("wand_settings_station");

    public static final Item BUILDING_WAND = Registry.register(
            BuiltInRegistries.ITEM,
            BUILDING_WAND_ID,
            new BuildingWandItem(itemProperties(BUILDING_WAND_ID).stacksTo(1))
    );
    public static final Item MOVE_WAND = Registry.register(
            BuiltInRegistries.ITEM,
            MOVE_WAND_ID,
            new MoveWandItem(itemProperties(MOVE_WAND_ID).stacksTo(1))
    );
    public static final Item WAND_SETTINGS_STATION = Registry.register(
            BuiltInRegistries.ITEM,
            WAND_SETTINGS_STATION_ID,
            new DeliberateBlockItem(ModBlocks.WAND_SETTINGS_STATION, itemProperties(WAND_SETTINGS_STATION_ID))
    );

    private ModItems() {
    }

    private static Item.Properties itemProperties(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    public static void initialize() {
    }
}
