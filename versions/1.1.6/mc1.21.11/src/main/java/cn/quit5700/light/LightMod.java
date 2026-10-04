package cn.quit5700.light;

import cn.quit5700.light.light.ConstantLightTaskQueue;
import cn.quit5700.light.light.VariableLightTaskState;
import cn.quit5700.light.registry.*;
import cn.quit5700.light.network.VariableLightNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import cn.quit5700.light.redstone.RedstoneEnergyNotifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

public final class LightMod implements ModInitializer {
    public static final String MOD_ID = "light";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    @Override public void onInitialize() {
        if (Boolean.getBoolean("q5.lightLoadSmoke")) {
            try { Class.forName("cn.quit5700.light.light.LightLoadSmokeChecks").getMethod("register").invoke(null); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException("Light load harness unavailable", e); }
        }
        LightBlocks.initialize();
        LightBlockEntities.initialize();
        LightItems.initialize();
        LightMenus.initialize();
        VariableLightNetworking.initialize();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ConstantLightTaskQueue.tick();
            server.getAllLevels().forEach(cn.quit5700.light.light.DaylightOcclusion::tick);
            server.getAllLevels().forEach(world -> VariableLightTaskState.get(world).tick(world));
            server.getAllLevels().forEach(world -> cn.quit5700.light.light.RealAreaLightState.get(world).tick(world));
            RedstoneEnergyNotifier.tickQueuedChunkSyncs();
        });
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            for (var entity : chunk.getBlockEntities().values()) {
                if (entity instanceof cn.quit5700.light.block.VariableConstantLightBlockEntity lamp)
                    lamp.finishMigrationSave(chunk);
            }
        });
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> RedstoneEnergyNotifier.queueChunkSync(world, chunk.getPos()));
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> cn.quit5700.light.light.DaylightOcclusion.chunkChanged(world, chunk.getPos().x, chunk.getPos().z));
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> cn.quit5700.light.light.DaylightOcclusion.chunkChanged(world, chunk.getPos().x, chunk.getPos().z));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            RedstoneEnergyNotifier.clear();
        });
    }

}
