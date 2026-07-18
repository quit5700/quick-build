package cn.quit5700.pathfindingbeacon;

import cn.quit5700.pathfindingbeacon.client.BeamRenderer;
import cn.quit5700.pathfindingbeacon.client.ClientRouteState;
import net.fabricmc.api.ClientModInitializer;

public final class PathfindingBeaconClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientRouteState.registerReceiver();
        BeamRenderer.register();
    }
}

