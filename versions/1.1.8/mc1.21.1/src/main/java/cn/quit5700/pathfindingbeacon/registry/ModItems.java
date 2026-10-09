package cn.quit5700.pathfindingbeacon.registry;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import cn.quit5700.pathfindingbeacon.item.SequenceReordererItem;
import cn.quit5700.common.item.DeliberateBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    public static final List<BlockItem> ROUTE_BLOCK_ITEMS = registerBlockItems();
    private static final ResourceLocation SEQUENCE_REORDERER_ID = PathfindingBeaconMod.id("id_sequence_reorderer");
    public static final Item SEQUENCE_REORDERER = Registry.register(
            BuiltInRegistries.ITEM,
            SEQUENCE_REORDERER_ID,
            new SequenceReordererItem(itemProperties(SEQUENCE_REORDERER_ID).stacksTo(1))
    );

    private ModItems() {
    }

    private static List<BlockItem> registerBlockItems() {
        List<BlockItem> result = new ArrayList<>(30);
        for (int i = 0; i < ModBlocks.ROUTE_BLOCKS.size(); i++) {
            ResourceLocation id = PathfindingBeaconMod.id("route_block_" + (i + 1));
            BlockItem item = new DeliberateBlockItem(ModBlocks.ROUTE_BLOCKS.get(i), itemProperties(id));
            Registry.register(BuiltInRegistries.ITEM, id, item);
            result.add(item);
        }
        return List.copyOf(result);
    }

    private static Item.Properties itemProperties(ResourceLocation id) {
        return new Item.Properties();
    }

    public static void initialize() {
    }
}

