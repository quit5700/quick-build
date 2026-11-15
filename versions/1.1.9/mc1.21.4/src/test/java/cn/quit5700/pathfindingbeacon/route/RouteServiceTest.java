package cn.quit5700.pathfindingbeacon.route;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteServiceTest {
    private static final UUID ALICE = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID BOB = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private RouteData data;
    private RouteService service;

    @BeforeEach
    void setUp() {
        data = new RouteData();
        service = new RouteService(data);
    }

    @Test
    void everyPlayerCreatesAnActiveNodeWithoutOwningTheColor() {
        assertEquals(PlacementStatus.ACTIVE, service.place(1, ALICE, pos(0)).status());
        PlacementResult second = service.place(1, BOB, pos(1));

        assertEquals(PlacementStatus.ACTIVE, second.status());
        assertNull(data.owner(1));
        assertTrue(second.node().active());
        assertEquals(List.of(pos(0), pos(1)), data.order(1));
    }

    @Test
    void rejectsSameNumberAtSameHorizontalCoordinates() {
        service.place(1, ALICE, new RoutePosition(2, 10, 3));

        assertEquals(
                PlacementStatus.DUPLICATE_XZ,
                service.place(1, ALICE, new RoutePosition(2, 80, 3)).status()
        );
    }

    @Test
    void removingMiddleNodeDeletesOnlyIncidentEdges() {
        RoutePosition a = pos(0);
        RoutePosition b = pos(1);
        RoutePosition c = pos(2);
        RoutePosition d = pos(3);
        service.place(1, ALICE, a);
        service.place(1, ALICE, b);
        service.place(1, ALICE, c);
        service.place(1, ALICE, d);

        service.remove(b);

        assertEquals(List.of(a, c, d), data.order(1));
        assertEquals(1, data.edges(1).size());
        assertTrue(data.hasEdge(1, c, d));
        assertFalse(data.hasEdge(1, a, c));
    }

    @Test
    void anyPlayerCanRemoveARecordedNode() {
        RoutePosition a = pos(0);
        service.place(4, ALICE, a);

        assertTrue(service.canRemove(a, BOB, false));
    }

    @Test
    void reconnectingComponentsAddsOnlySelectedEdge() {
        RoutePosition a = pos(0);
        RoutePosition b = pos(1);
        RoutePosition c = pos(2);
        RoutePosition d = pos(3);
        service.place(2, ALICE, a);
        service.place(2, ALICE, b);
        service.place(2, ALICE, c);
        service.place(2, ALICE, d);
        service.removeEdge(2, b, c);

        assertEquals(ReorderStatus.RECONNECTED, service.reorder(2, ALICE, b, c, false));
        assertTrue(data.hasEdge(2, a, b));
        assertTrue(data.hasEdge(2, b, c));
        assertTrue(data.hasEdge(2, c, d));
    }

    @Test
    void reorderingConnectedNodesMovesNewerBeforeOlder() {
        RoutePosition a = pos(0);
        RoutePosition b = pos(1);
        RoutePosition c = pos(2);
        RoutePosition d = pos(3);
        service.place(3, ALICE, a);
        service.place(3, ALICE, b);
        service.place(3, ALICE, c);
        service.place(3, ALICE, d);

        assertEquals(ReorderStatus.REORDERED, service.reorder(3, ALICE, d, b, false));
        assertEquals(List.of(a, d, b, c), data.order(3));
        assertTrue(data.hasEdge(3, a, d));
        assertTrue(data.hasEdge(3, d, b));
        assertTrue(data.hasEdge(3, b, c));
        assertEquals(3, data.edges(3).size());
    }

    @Test
    void survivalPlayerCanEditAnotherPlayersRoute() {
        RoutePosition a = pos(0);
        RoutePosition b = pos(1);
        service.place(5, ALICE, a);
        service.place(5, ALICE, b);

        assertEquals(ReorderStatus.REORDERED, service.reorder(5, BOB, a, b, false));
    }

    @Test
    void migratesLegacyInactiveNodesAndOwners() {
        RoutePosition legacyPosition = pos(7);
        RouteData legacy = RouteData.fromSnapshot(new RouteSnapshot(
                java.util.Map.of(6, ALICE),
                List.of(new RouteNode(6, BOB, legacyPosition, false)),
                java.util.Map.of(),
                List.of(),
                1L
        ));
        RouteService legacyService = new RouteService(legacy);

        assertTrue(legacyService.removeOwnershipRestrictions());
        assertNull(legacy.owner(6));
        assertTrue(legacy.node(legacyPosition).active());
        assertEquals(List.of(legacyPosition), legacy.order(6));
    }

    @Test
    void movingNodesPreservesRouteOrderAndEdges() {
        RoutePosition first = new RoutePosition(0, 64, 0);
        RoutePosition second = new RoutePosition(2, 64, 0);
        RoutePosition third = new RoutePosition(4, 64, 0);
        RoutePosition movedFirst = new RoutePosition(10, 70, 3);
        RoutePosition movedSecond = new RoutePosition(12, 70, 3);
        service.place(7, ALICE, first);
        service.place(7, BOB, second);
        service.place(7, ALICE, third);

        RouteMoveStatus status = service.moveNodes(
                Map.of(first, movedFirst, second, movedSecond),
                Set.of(first, second)
        );

        assertEquals(RouteMoveStatus.SUCCESS, status);
        assertNull(data.node(first));
        assertNull(data.node(second));
        assertEquals(ALICE, data.node(movedFirst).placedBy());
        assertEquals(BOB, data.node(movedSecond).placedBy());
        assertEquals(List.of(movedFirst, movedSecond, third), data.order(7));
        assertTrue(data.hasEdge(7, movedFirst, movedSecond));
        assertTrue(data.hasEdge(7, movedSecond, third));
    }

    @Test
    void rejectedColumnConflictLeavesAllRouteDataUntouched() {
        RoutePosition moving = new RoutePosition(0, 64, 0);
        RoutePosition stationary = new RoutePosition(5, 80, 0);
        RoutePosition conflictingTarget = new RoutePosition(5, 64, 0);
        service.place(8, ALICE, moving);
        service.place(8, BOB, stationary);
        RouteSnapshot before = data.snapshot();

        RouteMoveStatus status = service.moveNodes(
                Map.of(moving, conflictingTarget),
                Set.of(moving)
        );

        assertEquals(RouteMoveStatus.DUPLICATE_XZ, status);
        assertEquals(before, data.snapshot());
    }

    @Test
    void movingNodeCanReplaceRouteNodeAtAnOverwrittenTarget() {
        RoutePosition moving = new RoutePosition(0, 64, 0);
        RoutePosition overwritten = new RoutePosition(6, 64, 0);
        service.place(9, ALICE, moving);
        service.place(9, BOB, overwritten);

        RouteMoveStatus status = service.moveNodes(
                Map.of(moving, overwritten),
                Set.of(moving, overwritten)
        );

        assertEquals(RouteMoveStatus.SUCCESS, status);
        assertEquals(ALICE, data.node(overwritten).placedBy());
        assertEquals(List.of(overwritten), data.order(9));
        assertTrue(data.edges(9).isEmpty());
    }

    @Test
    void overwrittenRouteNodeIsRemovedWhenTargetReceivesANonRouteBlock() {
        RoutePosition overwritten = new RoutePosition(6, 64, 0);
        service.place(10, ALICE, overwritten);

        RouteMoveStatus status = service.moveNodes(Map.of(), Set.of(overwritten));

        assertEquals(RouteMoveStatus.SUCCESS, status);
        assertNull(data.node(overwritten));
        assertTrue(data.order(10).isEmpty());
        assertTrue(data.edges(10).isEmpty());
    }

    private static RoutePosition pos(int x) {
        return new RoutePosition(x, 64, 0);
    }
}
