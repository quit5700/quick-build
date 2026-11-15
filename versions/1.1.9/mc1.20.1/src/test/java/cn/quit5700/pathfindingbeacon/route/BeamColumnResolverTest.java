package cn.quit5700.pathfindingbeacon.route;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeamColumnResolverTest {
    private static final UUID OWNER = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void newestCrossingWinsAndOldColorReturnsAfterRemoval() {
        RouteEdge oldEdge = edge(1, pos(-2, 0, 0), pos(2, 0, 0), 1);
        RouteEdge newEdge = edge(2, pos(0, 0, -2), pos(0, 0, 2), 2);

        BeamColumnResolver resolver = BeamColumnResolver.resolve(List.of(), List.of(oldEdge, newEdge));
        assertEquals(2, resolver.colorAt(new Column(0, 0), 0));

        BeamColumnResolver afterRemoval = BeamColumnResolver.resolve(List.of(), List.of(oldEdge));
        assertEquals(1, afterRemoval.colorAt(new Column(0, 0), 0));
    }

    @Test
    void stackedEndpointsCycleByNumberOncePerSecond() {
        RoutePosition sharedLow = pos(0, 20, 0);
        RoutePosition sharedHigh = pos(0, 80, 0);
        List<RouteNode> nodes = List.of(
                new RouteNode(5, OWNER, sharedHigh, true),
                new RouteNode(2, OWNER, sharedLow, true)
        );
        List<RouteEdge> edges = List.of(
                edge(5, sharedHigh, pos(3, 80, 0), 1),
                edge(2, sharedLow, pos(-3, 20, 0), 2)
        );

        BeamColumnResolver resolver = BeamColumnResolver.resolve(nodes, edges);

        assertEquals(2, resolver.colorAt(new Column(0, 0), 0));
        assertEquals(5, resolver.colorAt(new Column(0, 0), 1));
        assertEquals(2, resolver.colorAt(new Column(0, 0), 2));
    }

    private static RouteEdge edge(int number, RoutePosition first, RoutePosition second, long created) {
        return new RouteEdge(number, first, second, created);
    }

    private static RoutePosition pos(int x, int y, int z) {
        return new RoutePosition(x, y, z);
    }
}
