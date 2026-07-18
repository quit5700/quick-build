package cn.quit5700.pathfindingbeacon;

import cn.quit5700.pathfindingbeacon.command.IdCancelCommand;
import cn.quit5700.pathfindingbeacon.event.PlayerEvents;
import cn.quit5700.pathfindingbeacon.network.RouteNetworking;
import cn.quit5700.pathfindingbeacon.registry.ModBlocks;
import cn.quit5700.pathfindingbeacon.registry.ModItemGroup;
import cn.quit5700.pathfindingbeacon.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public final class PathfindingBeaconMod implements ModInitializer {
    public static final String MOD_ID = "pathfinding_beacon";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroup.initialize();
        IdCancelCommand.register();
        PlayerEvents.register();
        RouteNetworking.registerServer();
    }
}

