package cn.quit5700.pathfindingbeacon.route;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RouteService {
    private final RouteData data;

    public RouteService(RouteData data) {
        this.data = data;
    }

    public PlacementResult place(int number, UUID player, RoutePosition position) {
        validateNumber(number);
        boolean duplicate = data.nodes().stream().anyMatch(node ->
                node.number() == number
                        && node.position().x() == position.x()
                        && node.position().z() == position.z());
        if (duplicate) {
            return new PlacementResult(PlacementStatus.DUPLICATE_XZ, null);
        }

        RouteNode node = new RouteNode(number, player, position, true);
        data.mutableNodes().put(position, node);

        ArrayList<RoutePosition> order = data.mutableOrder(number);
        if (!order.isEmpty()) {
            addEdge(number, order.get(order.size() - 1), position);
        }
        order.add(position);
        return new PlacementResult(PlacementStatus.ACTIVE, node);
    }

    public RouteNode remove(RoutePosition position) {
        RouteNode removed = data.mutableNodes().remove(position);
        if (removed == null) {
            return null;
        }
        int number = removed.number();
        data.mutableOrder(number).remove(position);
        data.mutableEdges(number).entrySet().removeIf(entry -> entry.getValue().touches(position));
        return removed;
    }

    public boolean canRemove(RoutePosition position, UUID player, boolean creative) {
        return data.node(position) != null;
    }

    public void removeEdge(int number, RoutePosition first, RoutePosition second) {
        data.mutableEdges(number).remove(new RouteData.EdgeKey(first, second));
    }

    public ReorderStatus reorder(
            int number,
            UUID player,
            RoutePosition first,
            RoutePosition second,
            boolean creative
    ) {
        RouteNode firstNode = data.node(first);
        RouteNode secondNode = data.node(second);
        if (first.equals(second)
                || firstNode == null
                || secondNode == null
                || !firstNode.active()
                || !secondNode.active()
                || firstNode.number() != number
                || secondNode.number() != number) {
            return ReorderStatus.INVALID;
        }
        Set<RoutePosition> component = connectedComponent(number, first);
        if (!component.contains(second)) {
            addEdge(number, first, second);
            return ReorderStatus.RECONNECTED;
        }

        ArrayList<RoutePosition> order = data.mutableOrder(number);
        int firstIndex = order.indexOf(first);
        int secondIndex = order.indexOf(second);
        RoutePosition older = firstIndex < secondIndex ? first : second;
        RoutePosition newer = firstIndex < secondIndex ? second : first;
        order.remove(newer);
        order.add(order.indexOf(older), newer);

        data.mutableEdges(number).entrySet().removeIf(entry ->
                component.contains(entry.getValue().first()) && component.contains(entry.getValue().second()));
        List<RoutePosition> componentOrder = order.stream().filter(component::contains).toList();
        for (int i = 1; i < componentOrder.size(); i++) {
            addEdge(number, componentOrder.get(i - 1), componentOrder.get(i));
        }
        return ReorderStatus.REORDERED;
    }

    public List<RouteNode> clearColor(int number) {
        validateNumber(number);
        List<RouteNode> removed = data.nodes().stream()
                .filter(node -> node.number() == number)
                .toList();
        removed.forEach(node -> data.mutableNodes().remove(node.position()));
        data.mutableOwners().remove(number);
        data.mutableOrders().remove(number);
        data.mutableAllEdges().remove(number);
        return removed;
    }

    public boolean removeOwnershipRestrictions() {
        boolean changed = !data.mutableOwners().isEmpty();
        data.mutableOwners().clear();
        for (RouteNode node : List.copyOf(data.nodes())) {
            RouteNode activeNode = node;
            if (!node.active()) {
                activeNode = new RouteNode(node.number(), node.placedBy(), node.position(), true);
                data.mutableNodes().put(node.position(), activeNode);
                changed = true;
            }
            ArrayList<RoutePosition> order = data.mutableOrder(activeNode.number());
            if (!order.contains(activeNode.position())) {
                if (!order.isEmpty()) {
                    addEdge(activeNode.number(), order.get(order.size() - 1), activeNode.position());
                }
                order.add(activeNode.position());
                changed = true;
            }
        }
        return changed;
    }

    public RouteMoveStatus moveNodes(
            Map<RoutePosition, RoutePosition> movements,
            Set<RoutePosition> affectedPositions
    ) {
        if (movements.isEmpty() && affectedPositions.isEmpty()) {
            return RouteMoveStatus.SUCCESS;
        }
        for (Map.Entry<RoutePosition, RoutePosition> movement : movements.entrySet()) {
            if (data.node(movement.getKey()) == null || movement.getValue() == null) {
                return RouteMoveStatus.INVALID;
            }
        }

        LinkedHashMap<RoutePosition, RouteNode> movedNodes = new LinkedHashMap<>();
        for (RouteNode node : data.nodes()) {
            RoutePosition movedPosition = transformedPosition(
                    node.position(), movements, affectedPositions);
            if (movedPosition == null) {
                continue;
            }
            RouteNode movedNode = movedPosition.equals(node.position())
                    ? node
                    : new RouteNode(node.number(), node.placedBy(), movedPosition, node.active());
            if (movedNodes.put(movedPosition, movedNode) != null) {
                return RouteMoveStatus.INVALID;
            }
        }

        Set<RouteColumn> occupiedColumns = new HashSet<>();
        for (RouteNode node : movedNodes.values()) {
            RouteColumn column = new RouteColumn(
                    node.number(), node.position().x(), node.position().z());
            if (!occupiedColumns.add(column)) {
                return RouteMoveStatus.DUPLICATE_XZ;
            }
        }

        Map<Integer, ArrayList<RoutePosition>> movedOrders = new HashMap<>();
        data.mutableOrders().forEach((number, order) -> {
            ArrayList<RoutePosition> movedOrder = new ArrayList<>();
            for (RoutePosition position : order) {
                RoutePosition movedPosition = transformedPosition(
                        position, movements, affectedPositions);
                if (movedPosition != null && !movedOrder.contains(movedPosition)) {
                    movedOrder.add(movedPosition);
                }
            }
            movedOrders.put(number, movedOrder);
        });

        Map<Integer, LinkedHashMap<RouteData.EdgeKey, RouteEdge>> movedEdges = new HashMap<>();
        data.mutableAllEdges().forEach((number, edges) -> {
            LinkedHashMap<RouteData.EdgeKey, RouteEdge> result = new LinkedHashMap<>();
            for (RouteEdge edge : edges.values()) {
                RoutePosition first = transformedPosition(
                        edge.first(), movements, affectedPositions);
                RoutePosition second = transformedPosition(
                        edge.second(), movements, affectedPositions);
                if (first == null || second == null || first.equals(second)) {
                    continue;
                }
                RouteEdge movedEdge = new RouteEdge(number, first, second, edge.createdOrder());
                result.put(new RouteData.EdgeKey(first, second), movedEdge);
            }
            movedEdges.put(number, result);
        });

        data.mutableNodes().clear();
        data.mutableNodes().putAll(movedNodes);
        data.mutableOrders().clear();
        data.mutableOrders().putAll(movedOrders);
        data.mutableAllEdges().clear();
        data.mutableAllEdges().putAll(movedEdges);
        return RouteMoveStatus.SUCCESS;
    }

    private static RoutePosition transformedPosition(
            RoutePosition position,
            Map<RoutePosition, RoutePosition> movements,
            Set<RoutePosition> affectedPositions
    ) {
        RoutePosition moved = movements.get(position);
        if (moved != null) {
            return moved;
        }
        return affectedPositions.contains(position) ? null : position;
    }

    private void addEdge(int number, RoutePosition first, RoutePosition second) {
        RouteData.EdgeKey key = new RouteData.EdgeKey(first, second);
        data.mutableEdges(number).computeIfAbsent(key, ignored ->
                new RouteEdge(number, first, second, data.nextEdgeOrder()));
    }

    private Set<RoutePosition> connectedComponent(int number, RoutePosition start) {
        Set<RoutePosition> visited = new HashSet<>();
        ArrayDeque<RoutePosition> pending = new ArrayDeque<>();
        pending.add(start);
        while (!pending.isEmpty()) {
            RoutePosition current = pending.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (RouteEdge edge : data.edges(number)) {
                if (edge.first().equals(current)) pending.add(edge.second());
                if (edge.second().equals(current)) pending.add(edge.first());
            }
        }
        return visited;
    }

    private static void validateNumber(int number) {
        if (number < 1 || number > 30) {
            throw new IllegalArgumentException("Route number must be between 1 and 30");
        }
    }

    private record RouteColumn(int number, int x, int z) {
    }
}

