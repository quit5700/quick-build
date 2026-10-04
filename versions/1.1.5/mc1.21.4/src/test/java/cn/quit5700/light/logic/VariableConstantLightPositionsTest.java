package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableConstantLightPositionsTest {
    @Test
    void offsetsStayInsideTheRequestedCubeAndIncludeEveryBoundary() {
        List<VariableConstantLightPositions.Offset> offsets =
                VariableConstantLightPositions.relativeOffsets(14, 4);

        assertFalse(offsets.isEmpty());
        assertTrue(offsets.stream().allMatch(offset ->
                Math.abs(offset.dx()) <= 14
                        && Math.abs(offset.dy()) <= 14
                        && Math.abs(offset.dz()) <= 14));
        assertTrue(offsets.stream().anyMatch(offset -> offset.dx() == -14));
        assertTrue(offsets.stream().anyMatch(offset -> offset.dx() == 14));
        assertTrue(offsets.stream().anyMatch(offset -> offset.dy() == -14));
        assertTrue(offsets.stream().anyMatch(offset -> offset.dy() == 14));
        assertTrue(offsets.stream().anyMatch(offset -> offset.dz() == -14));
        assertTrue(offsets.stream().anyMatch(offset -> offset.dz() == 14));
    }

    @Test
    void maximumRadiusUsesSparsePositionsInsteadOfFillingTheCube() {
        List<VariableConstantLightPositions.Offset> offsets =
                VariableConstantLightPositions.relativeOffsets(64, 4);

        assertTrue(offsets.size() < 50_000);
        assertTrue(offsets.size() > 30_000);
        assertFalse(offsets.contains(new VariableConstantLightPositions.Offset(0, 0, 0)));
    }

    @Test
    void noAxisGapExceedsFourBlocks() {
        List<Integer> xCoordinates = VariableConstantLightPositions.relativeOffsets(64, 4).stream()
                .map(VariableConstantLightPositions.Offset::dx)
                .distinct()
                .sorted()
                .toList();

        for (int index = 1; index < xCoordinates.size(); index++) {
            assertTrue(xCoordinates.get(index) - xCoordinates.get(index - 1) <= 4);
        }
    }

    @Test
    void radiusIsClampedBeforeGeneratingOffsets() {
        assertEquals(
                VariableConstantLightPositions.relativeOffsets(1, 1),
                VariableConstantLightPositions.relativeOffsets(0, 0));
        assertEquals(
                VariableConstantLightPositions.relativeOffsets(64, 7),
                VariableConstantLightPositions.relativeOffsets(100, 100));
    }

    @Test
    void spacingOneFillsEveryPositionInTheCubeExceptTheLamp() {
        List<VariableConstantLightPositions.Offset> offsets =
                VariableConstantLightPositions.relativeOffsets(2, 1);

        assertEquals(124, offsets.size());
        assertTrue(offsets.contains(new VariableConstantLightPositions.Offset(1, 1, 1)));
        assertFalse(offsets.contains(new VariableConstantLightPositions.Offset(0, 0, 0)));
    }

    @Test
    void selectedSpacingControlsTheMaximumAxisGap() {
        for (int spacing = 1; spacing <= 7; spacing++) {
            List<Integer> coordinates = VariableConstantLightPositions.relativeOffsets(20, spacing).stream()
                    .map(VariableConstantLightPositions.Offset::dx)
                    .distinct()
                    .sorted()
                    .toList();
            for (int index = 1; index < coordinates.size(); index++) {
                assertTrue(coordinates.get(index) - coordinates.get(index - 1) <= spacing);
            }
        }
    }

    @Test
    void coverageCheckMatchesGeneratedOffsets() {
        for (int spacing = 1; spacing <= 7; spacing++) {
            Set<VariableConstantLightPositions.Offset> offsets = new HashSet<>(
                    VariableConstantLightPositions.relativeOffsets(8, spacing));
            for (int dx = -8; dx <= 8; dx++) {
                for (int dy = -8; dy <= 8; dy++) {
                    for (int dz = -8; dz <= 8; dz++) {
                        assertEquals(
                                offsets.contains(new VariableConstantLightPositions.Offset(dx, dy, dz)),
                                VariableConstantLightPositions.containsOffset(8, spacing, dx, dy, dz));
                    }
                }
            }
        }
    }
}
