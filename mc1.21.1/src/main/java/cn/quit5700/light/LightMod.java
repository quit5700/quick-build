package cn.quit5700.light;

import cn.quit5700.light.light.ConstantLightTaskQueue;
import cn.quit5700.light.network.VariableLightNetworking;
import cn.quit5700.light.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import cn.quit5700.light.redstone.RedstoneEnergyNotifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.ResourceLocation;

public final class LightMod implements ModInitializer {
    public static final String MOD_ID = "light";
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }

    @Override public void onInitialize() {
        LightBlocks.initialize();
        LightBlockEntities.initialize();
        LightItems.initialize();
        LightMenus.initialize();
        VariableLightNetworking.initialize();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ConstantLightTaskQueue.tick();
            RedstoneEnergyNotifier.tickQueuedChunkSyncs();
        });
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> RedstoneEnergyNotifier.queueChunkSync(world, chunk.getPos()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            RedstoneEnergyNotifier.clear();
        });
    }

}
