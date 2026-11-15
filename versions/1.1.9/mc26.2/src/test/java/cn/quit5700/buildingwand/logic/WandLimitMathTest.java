package cn.quit5700.buildingwand.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WandLimitMathTest {
    @Test
    void increasingHasNoModDefinedMaximumAndNeverOverflows() {
        assertEquals(8192, WandLimitMath.change(4096, true, 64));
        assertEquals(Integer.MAX_VALUE, WandLimitMath.change(Integer.MAX_VALUE - 1, true, 64));
    }

    @Test
    void decreasingStillKeepsTheConfiguredMinimum() {
        assertEquals(64, WandLimitMath.change(64, false, 64));
    }
}
