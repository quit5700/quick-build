package cn.quit5700.pathfindingbeacon.registry;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import cn.quit5700.pathfindingbeacon.block.PathfindingBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {
    public static final List<PathfindingBlock> ROUTE_BLOCKS = registerBlocks();

    private ModBlocks() {
    }

    private static List<PathfindingBlock> registerBlocks() {
        List<PathfindingBlock> result = new ArrayList<>(30);
        for (int number = 1; number <= 30; number++) {
            Identifier id = PathfindingBeaconMod.id("route_block_" + number);
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
            PathfindingBlock block = new PathfindingBlock(number, key);
            Registry.register(
                    BuiltInRegistries.BLOCK,
                    id,
                    block
            );
            result.add(block);
        }
        return List.copyOf(result);
    }

    public static int numberOf(Block block) {
        return block instanceof PathfindingBlock pathfindingBlock ? pathfindingBlock.number() : 0;
    }

    public static void initialize() {
    }
}

