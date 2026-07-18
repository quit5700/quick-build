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
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.MOVE_SELECTION_RESULT, (client, handler, buf, response) -> {
            var payload = WandNetworking.MoveSelectionResultPayload.read(buf);
            client.execute(() -> ClientWandController.onMoveSelectionResult(payload.accepted()));
        });
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.MOVE_EXECUTION_RESULT, (client, handler, buf, response) -> {
            var payload = WandNetworking.MoveExecutionResultPayload.read(buf);
            client.execute(() -> ClientWandController.onMoveExecutionResult(payload.completed()));
        });
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.MOVE_MEMORY, (client, handler, buf, response) -> {
            var payload = WandNetworking.MoveMemoryPayload.read(buf);
            client.execute(() -> ClientWandController.onMoveMemory(payload.hasMemory(), payload.first(), payload.second()));
        });
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.OPEN_SETTINGS,
                (client, handler, buf, response) -> { var payload = WandNetworking.OpenSettingsPayload.read(buf); client.execute(() -> client.setScreen(
                        new WandSettingsScreen(payload.planeLimit(), payload.cubeLimit(), payload.moveLimit(), payload.editable())));
                });
        ClientPlayNetworking.registerGlobalReceiver(WandNetworking.SETTINGS_SAVED,
                (client, handler, buf, response) -> { var payload = WandNetworking.SettingsSavedPayload.read(buf); client.execute(() ->
                        WandSettingsScreen.applySavedSettings(payload.accepted(), payload.planeLimit(),
                                payload.cubeLimit(), payload.moveLimit()));
                });
    }
}
