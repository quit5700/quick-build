package cn.quit5700.pathfindingbeacon.route;

public record RouteEdge(
        int number,
        RoutePosition first,
        RoutePosition second,
        long createdOrder
) {
    public boolean touches(RoutePosition position) {
        return first.equals(position) || second.equals(position);
    }
}

