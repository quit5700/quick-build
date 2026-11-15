package cn.quit5700.pathfindingbeacon.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BeamColumnResolver {
    private final Map<Column, ResolvedColumn> columns;

    private BeamColumnResolver(Map<Column, ResolvedColumn> columns) {
        this.columns = Map.copyOf(columns);
    }

    public static BeamColumnResolver resolve(Collection<RouteNode> nodes, Collection<RouteEdge> edges) {
        Map<Column, Candidate> newestAtColumn = new HashMap<>();
        Set<RoutePosition> connectedPositions = new HashSet<>();
        for (RouteEdge edge : edges) {
            connectedPositions.add(edge.first());
            connectedPositions.add(edge.second());
            for (Column column : Bresenham.line(
                    edge.first().x(), edge.first().z(), edge.second().x(), edge.second().z())) {
                Candidate current = newestAtColumn.get(column);
                if (current == null || edge.createdOrder() >= current.createdOrder()) {
                    newestAtColumn.put(column, new Candidate(edge.number(), edge.createdOrder()));
                }
            }
        }

        Map<Column, List<Integer>> stackedEndpoints = new HashMap<>();
        for (RouteNode node : nodes) {
            if (!node.active() || !connectedPositions.contains(node.position())) {
                continue;
            }
            Column column = new Column(node.position().x(), node.position().z());
            stackedEndpoints.computeIfAbsent(column, ignored -> new ArrayList<>()).add(node.number());
        }

        Map<Column, ResolvedColumn> result = new HashMap<>();
        newestAtColumn.forEach((column, candidate) -> {
            List<Integer> cycling = stackedEndpoints.getOrDefault(column, List.of()).stream()
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .toList();
            result.put(column, new ResolvedColumn(candidate.number(), cycling.size() > 1 ? cycling : List.of()));
        });
        return new BeamColumnResolver(result);
    }

    public int colorAt(Column column, long epochSecond) {
        ResolvedColumn resolved = columns.get(column);
        if (resolved == null) {
            return 0;
        }
        if (resolved.cyclingNumbers().isEmpty()) {
            return resolved.fixedNumber();
        }
        int index = Math.floorMod(epochSecond, resolved.cyclingNumbers().size());
        return resolved.cyclingNumbers().get(index);
    }

    public Set<Column> columns() {
        return columns.keySet();
    }

    private record Candidate(int number, long createdOrder) {
    }

    private record ResolvedColumn(int fixedNumber, List<Integer> cyclingNumbers) {
    }
}

