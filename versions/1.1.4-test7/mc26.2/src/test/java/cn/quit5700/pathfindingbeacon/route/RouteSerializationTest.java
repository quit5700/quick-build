package cn.quit5700.pathfindingbeacon.route;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteSerializationTest {
    @Test
    void roundTripsOwnersNodesOrderAndEdges() {
        UUID alice = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID bob = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        RouteData original = new RouteData();
        RouteService service = new RouteService(original);
        service.place(7, alice, new RoutePosition(1, 64, 2));
        service.place(7, alice, new RoutePosition(6, 70, 8));
        service.place(7, bob, new RoutePosition(9, 80, 10));

        RouteData restored = RouteData.fromSnapshot(original.snapshot());

        assertEquals(original.owner(7), restored.owner(7));
        assertEquals(original.nodes(), restored.nodes());
        assertEquals(original.order(7), restored.order(7));
        assertEquals(original.edges(7), restored.edges(7));
    }
}
