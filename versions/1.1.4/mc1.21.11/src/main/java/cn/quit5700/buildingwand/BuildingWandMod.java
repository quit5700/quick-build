package cn.quit5700.buildingwand;

import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.registry.ModBlocks;
import cn.quit5700.buildingwand.network.WandNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import cn.quit5700.buildingwand.server.WandActions;

public final class BuildingWandMod implements ModInitializer {
    public static final String MOD_ID = "building_wand";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        if(Boolean.getBoolean("q5.wandSmoke")){
            try{Class.forName("cn.quit5700.buildingwand.server.WandSmokeChecks").getMethod("register").invoke(null);}
            catch(ReflectiveOperationException e){throw new IllegalStateException("Smoke harness unavailable",e);}
        }

        ModBlocks.initialize();
        ModItems.initialize();
        cn.quit5700.buildingwand.item.DeletionWandInteractions.register();
        WandNetworking.registerServer();
        cn.quit5700.buildingwand.network.BuildNetworking.register();
        cn.quit5700.buildingwand.network.DeleteNetworking.register();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WandActions.clearMoveSessions());
    }
}
