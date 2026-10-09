package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class DaylightVisibilityTest {
    @Test void emptyRangeIsVisibleInEveryDirection() {
        for (int x = -8; x <= 8; x++)
            for (int y = -8; y <= 8; y++)
                for (int z = -8; z <= 8; z++)
                    assertTrue(DaylightVisibility.visible(0, 0, 0, x, y, z, (a,b,c) -> false));
    }

    @Test void wallBlocksButItsNearSurfaceRemainsLit() {
        assertFalse(DaylightVisibility.visible(0, 0, 0, 10, 0, 0, (x,y,z) -> x == 5));
        assertTrue(DaylightVisibility.visible(0, 0, 0, 5, 0, 0, (x,y,z) -> x == 5));
        assertFalse(DaylightVisibility.visible(0, 0, 0, -10, 0, 0, (x,y,z) -> x == -5));
    }

    @Test void diagonalCornerDoesNotLeakThroughSolidCells() {
        assertFalse(DaylightVisibility.visible(0, 0, 0, 4, 4, 0,
                (x,y,z) -> x == 1 && y == 0));
        assertFalse(DaylightVisibility.visible(0, 0, 0, 4, 4, 4,
                (x,y,z) -> x == 0 && y == 1 && z == 0));
    }

    @Test void openingWallRestoresVisibilityAndSourceDoesNotBlockItself() {
        boolean[] closed = {true};
        DaylightVisibility.Obstacle wall = (x,y,z) -> closed[0] && x == 2;
        assertFalse(DaylightVisibility.visible(0, 0, 0, 4, 0, 0, wall));
        closed[0] = false;
        assertTrue(DaylightVisibility.visible(0, 0, 0, 4, 0, 0, wall));
        assertTrue(DaylightVisibility.visible(0, 0, 0, 4, 0, 0, (x,y,z) -> x == 0));
    }

    @Test void blockedRayStopsEarlyAndNegativeCoordinatesWork() {
        AtomicInteger checks = new AtomicInteger();
        assertFalse(DaylightVisibility.visible(-100, -20, -100, -36, -20, -100,
                (x,y,z) -> { checks.incrementAndGet(); return x == -99; }));
        assertEquals(1, checks.get());
    }
}
