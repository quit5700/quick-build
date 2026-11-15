package cn.quit5700.pathfindingbeacon.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RouteData {
    private final Map<Integer, UUID> owners = new HashMap<>();
    private final Map<RoutePosition, RouteNode> nodes = new LinkedHashMap<>();
    private final Map<Integer, ArrayList<RoutePosition>> orders = new HashMap<>();
    private final Map<Integer, LinkedHashMap<EdgeKey, RouteEdge>> edges = new HashMap<>();
    private long nextEdgeOrder = 1L;

    public UUID owner(int number) {
        return owners.get(number);
    }

    public RouteNode node(RoutePosition position) {
        return nodes.get(position);
    }

    public Collection<RouteNode> nodes() {
        return List.copyOf(nodes.values());
    }

    public List<RoutePosition> order(int number) {
        return List.copyOf(orders.computeIfAbsent(number, ignored -> new ArrayList<>()));
    }

    public List<RouteEdge> edges(int number) {
        return List.copyOf(edges.computeIfAbsent(number, ignored -> new LinkedHashMap<>()).values());
    }

    public Collection<RouteEdge> allEdges() {
        return edges.values().stream().flatMap(map -> map.values().stream()).toList();
    }

    public RouteSnapshot snapshot() {
        Map<Integer, List<RoutePosition>> orderCopy = new HashMap<>();
        orders.forEach((number, order) -> orderCopy.put(number, List.copyOf(order)));
        return new RouteSnapshot(
                Map.copyOf(owners),
                List.copyOf(nodes.values()),
                Map.copyOf(orderCopy),
                List.copyOf(allEdges()),
                nextEdgeOrder
        );
    }

    public static RouteData fromSnapshot(RouteSnapshot snapshot) {
        RouteData data = new RouteData();
        data.owners.putAll(snapshot.owners());
        snapshot.nodes().forEach(node -> data.nodes.put(node.position(), node));
        snapshot.orders().forEach((number, order) ->
                data.orders.put(number, new ArrayList<>(order)));
        snapshot.edges().forEach(edge -> data.mutableEdges(edge.number()).put(
                new EdgeKey(edge.first(), edge.second()), edge));
        data.setNextEdgeOrder(snapshot.nextEdgeOrder());
        return data;
    }

    void restore(RouteSnapshot snapshot) {
        RouteData restored = fromSnapshot(snapshot);
        owners.clear();
        owners.putAll(restored.owners);
        nodes.clear();
        nodes.putAll(restored.nodes);
        orders.clear();
        restored.orders.forEach((number, order) ->
                orders.put(number, new ArrayList<>(order)));
        edges.clear();
        restored.edges.forEach((number, routeEdges) ->
                edges.put(number, new LinkedHashMap<>(routeEdges)));
        nextEdgeOrder = restored.nextEdgeOrder;
    }

    public boolean hasEdge(int number, RoutePosition first, RoutePosition second) {
        return edges.computeIfAbsent(number, ignored -> new LinkedHashMap<>())
                .containsKey(new EdgeKey(first, second));
    }

    long nextEdgeOrder() {
        return nextEdgeOrder++;
    }

    void setNextEdgeOrder(long value) {
        nextEdgeOrder = Math.max(1L, value);
    }

    long currentNextEdgeOrder() {
        return nextEdgeOrder;
    }

    Map<Integer, UUID> mutableOwners() {
        return owners;
    }

    Map<RoutePosition, RouteNode> mutableNodes() {
        return nodes;
    }

    ArrayList<RoutePosition> mutableOrder(int number) {
        return orders.computeIfAbsent(number, ignored -> new ArrayList<>());
    }

    Map<Integer, ArrayList<RoutePosition>> mutableOrders() {
        return orders;
    }

    LinkedHashMap<EdgeKey, RouteEdge> mutableEdges(int number) {
        return edges.computeIfAbsent(number, ignored -> new LinkedHashMap<>());
    }

    Map<Integer, LinkedHashMap<EdgeKey, RouteEdge>> mutableAllEdges() {
        return edges;
    }

    record EdgeKey(RoutePosition low, RoutePosition high) {
        private static int compare(RoutePosition first, RoutePosition second) {
            int x = Integer.compare(first.x(), second.x());
            if (x != 0) return x;
            int y = Integer.compare(first.y(), second.y());
            return y != 0 ? y : Integer.compare(first.z(), second.z());
        }

        EdgeKey {
            Objects.requireNonNull(low);
            Objects.requireNonNull(high);
            if (compare(low, high) > 0) {
                RoutePosition swap = low;
                low = high;
                high = swap;
            }
        }
    }
}

