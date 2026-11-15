package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RedstoneDelayFacePolicyTest {
    private static final TestDirection ARROW_TAIL = TestDirection.NORTH;
    private static final TestDirection ARROW_HEAD = TestDirection.SOUTH;

    @Test
    void onlyTheArrowTailIsAnInputInBothModes() {
        for (TestDirection side : TestDirection.values()) {
            assertEquals(side == ARROW_TAIL,
                    RedstoneDelayFacePolicy.acceptsInputFrom(side, ARROW_TAIL), side.name());
        }
    }

    @Test
    void directionalModeOutputsOnlyFromTheArrowHead() {
        for (TestDirection side : TestDirection.values()) {
            assertEquals(side == ARROW_HEAD,
                    RedstoneDelayFacePolicy.emitsToPhysicalSide(
                            false, side, ARROW_TAIL, TestDirection::opposite), side.name());
        }
    }

    @Test
    void fiveFaceModeOutputsFromEverySideExceptTheArrowTail() {
        for (TestDirection side : TestDirection.values()) {
            assertEquals(side != ARROW_TAIL,
                    RedstoneDelayFacePolicy.emitsToPhysicalSide(
                            true, side, ARROW_TAIL, TestDirection::opposite), side.name());
        }
    }

    @Test
    void minecraftQueryDirectionsMapBackToTheCorrectPhysicalSides() {
        assertTrue(RedstoneDelayFacePolicy.emitsForQueryDirection(
                false, TestDirection.NORTH, ARROW_TAIL, TestDirection::opposite));
        assertFalse(RedstoneDelayFacePolicy.emitsForQueryDirection(
                false, TestDirection.SOUTH, ARROW_TAIL, TestDirection::opposite));

        EnumSet<TestDirection> fiveFaceQueries = EnumSet.noneOf(TestDirection.class);
        for (TestDirection query : TestDirection.values()) {
            if (RedstoneDelayFacePolicy.emitsForQueryDirection(
                    true, query, ARROW_TAIL, TestDirection::opposite)) {
                fiveFaceQueries.add(query);
            }
        }
        assertEquals(EnumSet.complementOf(EnumSet.of(TestDirection.SOUTH)), fiveFaceQueries);
    }

    private enum TestDirection {
        EAST,
        WEST,
        NORTH,
        SOUTH,
        UP,
        DOWN;

        TestDirection opposite() {
            return switch (this) {
                case EAST -> WEST;
                case WEST -> EAST;
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case UP -> DOWN;
                case DOWN -> UP;
            };
        }
    }
}
