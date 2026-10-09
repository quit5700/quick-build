package cn.quit5700.pathfindingbeacon.route;

import cn.quit5700.pathfindingbeacon.network.RouteNetworking;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WorldRouteManager {
    private WorldRouteManager() {
    }

    public static RoutePersistentState state(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(RoutePersistentState.TYPE);
    }

    public static PlacementResult place(ServerLevel world, int number, UUID player, BlockPos pos) {
        RoutePersistentState state = state(world);
        PlacementResult result = state.service().place(number, player, toRoutePosition(pos));
        state.setDirty();
        RouteNetworking.syncWorld(world);
        return result;
    }

    public static boolean hasNumberAtColumn(ServerLevel world, int number, int x, int z) {
        return state(world).data().nodes().stream().anyMatch(node ->
                node.number() == number && node.position().x() == x && node.position().z() == z);
    }

    public static boolean isActive(ServerLevel world, RoutePosition position) {
        RouteNode node = state(world).data().node(position);
        return node != null && node.active();
    }

    public static boolean canRemove(ServerLevel world, UUID player, BlockPos pos, boolean creative) {
        return state(world).service().canRemove(toRoutePosition(pos), player, creative);
    }

    public static void remove(ServerLevel world, BlockPos pos) {
        RoutePersistentState state = state(world);
        if (state.service().remove(toRoutePosition(pos)) != null) {
            state.setDirty();
            RouteNetworking.syncWorld(world);
        }
    }

    public static ReorderStatus reorder(
            ServerLevel world,
            int number,
            UUID player,
            RoutePosition first,
            RoutePosition second,
            boolean creative
    ) {
        RoutePersistentState state = state(world);
        ReorderStatus result = state.service().reorder(number, player, first, second, creative);
        if (result == ReorderStatus.RECONNECTED || result == ReorderStatus.REORDERED) {
            state.setDirty();
            RouteNetworking.syncWorld(world);
        }
        return result;
    }

    public static int clearColor(ServerLevel world, int number) {
        RoutePersistentState state = state(world);
        List<RouteNode> removed = state.service().clearColor(number);
        state.setDirty();
        for (RouteNode node : removed) {
            BlockPos pos = toBlockPos(node.position());
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        RouteNetworking.syncWorld(world);
        return removed.size();
    }

    public static RouteSnapshot snapshot(ServerLevel world) {
        return state(world).data().snapshot();
    }

    public static RouteMoveStatus validateMove(
            RouteSnapshot snapshot,
            Map<BlockPos, BlockPos> movements,
            Set<BlockPos> affectedPositions
    ) {
        RouteData copy = RouteData.fromSnapshot(snapshot);
        return new RouteService(copy).moveNodes(
                toRouteMovements(movements),
                toRoutePositions(affectedPositions)
        );
    }

    public static void applyMove(
            ServerLevel world,
            RouteSnapshot snapshot,
            Map<BlockPos, BlockPos> movements,
            Set<BlockPos> affectedPositions
    ) {
        RoutePersistentState state = state(world);
        state.data().restore(snapshot);
        RouteMoveStatus status = state.service().moveNodes(
                toRouteMovements(movements),
                toRoutePositions(affectedPositions)
        );
        if (status != RouteMoveStatus.SUCCESS) {
            state.data().restore(snapshot);
            throw new IllegalStateException("Route move became invalid: " + status);
        }
        state.setDirty();
        RouteNetworking.syncWorld(world);
    }

    public static void restoreSnapshot(ServerLevel world, RouteSnapshot snapshot) {
        RoutePersistentState state = state(world);
        state.data().restore(snapshot);
        state.setDirty();
        RouteNetworking.syncWorld(world);
    }

    private static Map<RoutePosition, RoutePosition> toRouteMovements(
            Map<BlockPos, BlockPos> movements
    ) {
        Map<RoutePosition, RoutePosition> result = new LinkedHashMap<>();
        movements.forEach((source, target) ->
                result.put(toRoutePosition(source), toRoutePosition(target)));
        return result;
    }

    private static Set<RoutePosition> toRoutePositions(Set<BlockPos> positions) {
        Set<RoutePosition> result = new LinkedHashSet<>();
        positions.forEach(position -> result.add(toRoutePosition(position)));
        return result;
    }

    public static RoutePosition toRoutePosition(BlockPos pos) {
        return new RoutePosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos toBlockPos(RoutePosition pos) {
        return new BlockPos(pos.x(), pos.y(), pos.z());
    }
}

