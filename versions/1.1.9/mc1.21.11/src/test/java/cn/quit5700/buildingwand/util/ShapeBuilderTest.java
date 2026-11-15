package cn.quit5700.buildingwand.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ShapeBuilderTest {
    @Test
    void hollowLineFallsBackToOrdinaryLine() {
        assertEquals(4, ShapeCoordinates.between(
                0, 0, 0,
                3, 0, 0,
                ShapeMode.HOLLOW
        ).size());
    }

    @Test
    void hollowPlaneKeepsOnlyItsBorder() {
        assertEquals(8, ShapeCoordinates.between(
                0, 0, 0,
                2, 2, 0,
                ShapeMode.HOLLOW
        ).size());
    }

    @Test
    void hollowCubeKeepsOnlyItsShell() {
        assertEquals(26, ShapeCoordinates.between(
                0, 0, 0,
                2, 2, 2,
                ShapeMode.HOLLOW
        ).size());
    }

    @Test
    void solidCubeKeepsEveryPosition() {
        assertEquals(27, ShapeCoordinates.between(
                0, 0, 0,
                2, 2, 2,
                ShapeMode.SOLID
        ).size());
    }

    @Test
    void planeModeCollapsesTheShortestAxis() {
        assertEquals(20, ShapeCoordinates.between(
                0, 0, 0,
                2, 3, 4,
                ShapeMode.PLANE
        ).size());
    }
}
