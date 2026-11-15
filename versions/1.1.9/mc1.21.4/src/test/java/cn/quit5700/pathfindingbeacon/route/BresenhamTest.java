package cn.quit5700.pathfindingbeacon.route;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BresenhamTest {
    @Test
    void createsContinuousOneBlockWideLine() {
        List<Column> points = Bresenham.line(0, 0, 5, 3);

        assertEquals(new Column(0, 0), points.get(0));
        assertEquals(new Column(5, 3), points.get(points.size() - 1));
        for (int i = 1; i < points.size(); i++) {
            int dx = Math.abs(points.get(i).x() - points.get(i - 1).x());
            int dz = Math.abs(points.get(i).z() - points.get(i - 1).z());
            assertTrue(dx <= 1 && dz <= 1 && dx + dz > 0);
        }
    }

    @Test
    void includesSingleColumnForCoincidentEndpoints() {
        assertEquals(List.of(new Column(4, -2)), Bresenham.line(4, -2, 4, -2));
    }
}
