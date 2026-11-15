package cn.quit5700.pathfindingbeacon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmokeTest {
    @Test
    void exposesThirtyRouteColors() {
        assertEquals(30, RouteColors.size());
    }
}
