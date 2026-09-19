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
        WandPreviewRenderer.register();
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.MoveSelectionResultPayload.ID,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        ClientWandController.onMoveSelectionResult(payload.accepted())));
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.MoveExecutionResultPayload.ID,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        ClientWandController.onMoveExecutionResult(payload.completed())));
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.MoveMemoryPayload.ID,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        ClientWandController.onMoveMemory(payload.hasMemory(), payload.first(), payload.second())));
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.OpenSettingsPayload.ID,
                (payload, context) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreenAndShow(
                        new WandSettingsScreen(payload.planeLimit(), payload.cubeLimit(), payload.moveLimit(), payload.editable())
                )));
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.SettingsSavedPayload.ID,
                (payload, context) -> Minecraft.getInstance().execute(() ->
                        WandSettingsScreen.applySavedSettings(payload.accepted(), payload.planeLimit(),
                                payload.cubeLimit(), payload.moveLimit())));
    }
}
