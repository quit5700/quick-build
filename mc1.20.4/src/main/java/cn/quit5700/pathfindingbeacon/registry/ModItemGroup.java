package cn.quit5700.pathfindingbeacon.registry;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import cn.quit5700.light.registry.LightItems;

public final class ModItemGroup {
    public static final CreativeModeTab PATHFINDING = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            PathfindingBeaconMod.id("pathfinding"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.pathfinding_beacon.pathfinding"))
                    .icon(() -> new ItemStack(Blocks.CRAFTING_TABLE))
                    .displayItems((context, entries) -> {
                        entries.accept(cn.quit5700.buildingwand.registry.ModItems.WAND_SETTINGS_STATION);
                        entries.accept(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);
                        entries.accept(cn.quit5700.buildingwand.registry.ModItems.MOVE_WAND);
                        entries.accept(LightItems.REDSTONE_SIGNAL_DELAY_ITEM);
                        cn.quit5700.pathfindingbeacon.registry.ModItems.ROUTE_BLOCK_ITEMS.forEach(entries::accept);
                        entries.accept(cn.quit5700.pathfindingbeacon.registry.ModItems.SEQUENCE_REORDERER);
                        entries.accept(LightItems.CONSTANT_LIGHT_BLOCK_ITEM);
                        entries.accept(LightItems.REDSTONE_ENERGY_SENSOR_ITEM);
                        entries.accept(LightItems.REDSTONE_ENERGY_CONNECTOR_ITEM);
                        entries.accept(LightItems.REDSTONE_ENERGY_EMITTER_ITEM);
                        LightItems.REMOTE_SWITCH_ITEMS.values().forEach(entries::accept);
                    })
                    .build()
    );

    private ModItemGroup() {
    }

    public static void initialize() {
    }
}
