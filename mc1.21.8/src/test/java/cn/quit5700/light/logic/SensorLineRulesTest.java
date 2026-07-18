package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static cn.quit5700.light.logic.SensorLineRules.Result.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class SensorLineRulesTest {
    @Test
    void acceptsOneToTenAirBlocksOnOneAxis() {
        assertEquals(VALID, SensorLineRules.validateOffset(2, 0, 0));
        assertEquals(VALID, SensorLineRules.validateOffset(0, -11, 0));
    }

    @Test
    void rejectsDiagonalAndOverlongSensorPairs() {
        assertEquals(NOT_ALIGNED, SensorLineRules.validateOffset(2, 0, 2));
        assertEquals(TOO_FAR, SensorLineRules.validateOffset(0, 0, 12));
    }

    @Test
    void treatsInvisibleVanillaLightAsClearButSolidBlocksAsObstructions() {
        assertEquals(VALID, SensorLineRules.validatePath(
                5, 0, 0, step -> SensorLineRules.isClear(false, true)));
        assertEquals(BLOCKED, SensorLineRules.validatePath(
                5, 0, 0, step -> step != 3));
    }
}
