package cn.quit5700.pathfindingbeacon.network;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import cn.quit5700.pathfindingbeacon.route.RouteEdge;
import cn.quit5700.pathfindingbeacon.route.RouteNode;
import cn.quit5700.pathfindingbeacon.route.RoutePersistentState;
import cn.quit5700.pathfindingbeacon.route.RoutePosition;
import cn.quit5700.pathfindingbeacon.route.WorldRouteManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RouteNetworking {
    public static final ResourceLocation ROUTE_SNAPSHOT = PathfindingBeaconMod.id("route_snapshot");
    private static final UUID SERVER_OWNER = new UUID(0L, 0L);

    private RouteNetworking() {}
    public static void registerServer() {}

    public static void syncWorld(ServerLevel world) {
        for (ServerPlayer player : PlayerLookup.world(world)) syncPlayer(player);
    }

    public static void syncPlayer(ServerPlayer player) {
        RoutePersistentState state = WorldRouteManager.state((ServerLevel) player.level());
        FriendlyByteBuf buf = PacketByteBufs.create();
        new RouteSnapshotPayload(List.copyOf(state.data().nodes()), List.copyOf(state.data().allEdges())).write(buf);
        ServerPlayNetworking.send(player, ROUTE_SNAPSHOT, buf);
    }

    public record RouteSnapshotPayload(List<RouteNode> nodes, List<RouteEdge> edges) {
        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(nodes.size());
            for (RouteNode node : nodes) {
                buf.writeVarInt(node.number());
                buf.writeBlockPos(WorldRouteManager.toBlockPos(node.position()));
                buf.writeBoolean(node.active());
            }
            buf.writeVarInt(edges.size());
            for (RouteEdge edge : edges) {
                buf.writeVarInt(edge.number());
                buf.writeBlockPos(WorldRouteManager.toBlockPos(edge.first()));
                buf.writeBlockPos(WorldRouteManager.toBlockPos(edge.second()));
                buf.writeLong(edge.createdOrder());
            }
        }

        public static RouteSnapshotPayload read(FriendlyByteBuf buf) {
            int nodeCount = buf.readVarInt();
            List<RouteNode> nodes = new ArrayList<>(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                int number = buf.readVarInt();
                BlockPos pos = buf.readBlockPos();
                nodes.add(new RouteNode(number, SERVER_OWNER, toRoutePosition(pos), buf.readBoolean()));
            }
            int edgeCount = buf.readVarInt();
            List<RouteEdge> edges = new ArrayList<>(edgeCount);
            for (int i = 0; i < edgeCount; i++) {
                int number = buf.readVarInt();
                RoutePosition first = toRoutePosition(buf.readBlockPos());
                RoutePosition second = toRoutePosition(buf.readBlockPos());
                edges.add(new RouteEdge(number, first, second, buf.readLong()));
            }
            return new RouteSnapshotPayload(nodes, edges);
        }

        private static RoutePosition toRoutePosition(BlockPos pos) {
            return new RoutePosition(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
