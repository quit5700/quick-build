package cn.quit5700.pathfindingbeacon.client;

import cn.quit5700.pathfindingbeacon.network.RouteNetworking;
import cn.quit5700.pathfindingbeacon.route.BeamColumnResolver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.List;

public final class ClientRouteState {
    private static volatile BeamColumnResolver resolver = BeamColumnResolver.resolve(List.of(), List.of());

    private ClientRouteState() {
    }

    public static BeamColumnResolver resolver() {
        return resolver;
    }

    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(RouteNetworking.RouteSnapshotPayload.ID,
                (payload, context) -> context.client().execute(() ->
                        resolver = BeamColumnResolver.resolve(payload.nodes(), payload.edges())));
    }
}

