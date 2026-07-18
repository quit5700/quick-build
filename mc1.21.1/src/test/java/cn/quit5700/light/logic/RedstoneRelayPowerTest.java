package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedstoneRelayPowerTest {
    @Test
    void entityDetectionAlwaysOutputsFullPower() {
        assertEquals(15, RedstoneRelayPower.calculate(true, 0, 0, 0));
    }

    @Test
    void directSourcesKeepTheirStrength() {
        assertEquals(15, RedstoneRelayPower.calculate(false, 15, 0, 0));
    }

    @Test
    void wireInputDecaysLikeRedstoneDust() {
        assertEquals(14, RedstoneRelayPower.calculate(false, 0, 15, 0));
        assertEquals(0, RedstoneRelayPower.calculate(false, 0, 0, 0));
    }

    @Test
    void ignoresWirePowerThatCanOnlyComeFromTheSensorItself() {
        assertEquals(0, RedstoneRelayPower.calculate(false, 0, 14, 14));
        assertEquals(9, RedstoneRelayPower.calculate(false, 0, 10, 9));
    }

    @Test
    void clampsInvalidSignalValues() {
        assertEquals(15, RedstoneRelayPower.calculate(false, 30, -1, 0));
    }
}
