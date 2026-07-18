package cn.quit5700.buildingwand;

import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.registry.ModBlocks;
import cn.quit5700.buildingwand.network.WandNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import cn.quit5700.buildingwand.server.WandActions;

public final class BuildingWandMod implements ModInitializer {
    public static final String MOD_ID = "building_wand";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModItems.initialize();
        WandNetworking.registerServer();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WandActions.clearMoveSessions());
    }
}
