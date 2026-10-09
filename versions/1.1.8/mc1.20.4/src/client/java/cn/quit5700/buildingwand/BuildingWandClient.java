package cn.quit5700.buildingwand;

import cn.quit5700.buildingwand.client.ClientWandController;
import cn.quit5700.buildingwand.client.WandSettingsScreen;
import cn.quit5700.buildingwand.client.WandPreviewRenderer;
import cn.quit5700.buildingwand.network.WandNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class BuildingWandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientWandController.register();
        cn.quit5700.buildingwand.client.LegacyWandFlash.initialize();
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(cn.quit5700.buildingwand.network.BuildNetworking.Cancelled.ID,cn.quit5700.buildingwand.network.BuildNetworking.Cancelled::read,(payload,context)->Minecraft.getInstance().execute(()->{cn.quit5700.buildingwand.client.BuildWandClient.cancelAllState();WandSettingsScreen.applyCancelledBuilds(payload.count());}));
        cn.quit5700.buildingwand.client.BuildWandClient.register();
        cn.quit5700.buildingwand.client.BuildBoxRenderer.register();
        cn.quit5700.buildingwand.client.DeleteWandClient.register();
        cn.quit5700.buildingwand.client.DeleteMeshRefresh.register();
        cn.quit5700.buildingwand.client.DeleteBoxRenderer.register();
        WandPreviewRenderer.register();
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(WandNetworking.MOVE_SELECTION_RESULT,WandNetworking.MoveSelectionResultPayload::read,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        ClientWandController.onMoveSelectionResult(payload.accepted())));
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(WandNetworking.MOVE_EXECUTION_RESULT,WandNetworking.MoveExecutionResultPayload::read,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        ClientWandController.onMoveExecutionResult(payload.completed())));
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(WandNetworking.MOVE_MEMORY,WandNetworking.MoveMemoryPayload::read,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        ClientWandController.onMoveMemory(payload.hasMemory(), payload.first(), payload.second())));
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(WandNetworking.OPEN_SETTINGS,WandNetworking.OpenSettingsPayload::read,
                (payload, context) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(
                        new WandSettingsScreen(payload.planeLimit(), payload.cubeLimit(), payload.moveLimit(), payload.editable())
                )));
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(WandNetworking.SETTINGS_SAVED,WandNetworking.SettingsSavedPayload::read,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        WandSettingsScreen.applySavedSettings(payload.accepted(), payload.planeLimit(),
                                payload.cubeLimit(), payload.moveLimit())));
    }
}
