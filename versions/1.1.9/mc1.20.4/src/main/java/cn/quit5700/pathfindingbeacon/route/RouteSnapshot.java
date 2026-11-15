package cn.quit5700.pathfindingbeacon.route;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RouteSnapshot(
        Map<Integer, UUID> owners,
        List<RouteNode> nodes,
        Map<Integer, List<RoutePosition>> orders,
        List<RouteEdge> edges,
        long nextEdgeOrder
) {
}

