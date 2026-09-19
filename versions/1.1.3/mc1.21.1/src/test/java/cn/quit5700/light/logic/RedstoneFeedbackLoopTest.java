package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RedstoneFeedbackLoopTest {
    @Test
    void sensorAndDelayTurnOffAfterTheExternalWireSourceIsRemoved() {
        DelayedSignalState delay = new DelayedSignalState(2, false, false);
        int sensorOutput = RedstoneRelayPower.calculate(false, 0, 15, 0);
        delay.capture(0, sensorOutput > 0);
        assertTrue(delay.advance(2));
        assertTrue(delay.outputPowered());

        int wireAfterSourceRemoval = sensorOutput;
        sensorOutput = RedstoneRelayPower.calculate(
                false, 0, wireAfterSourceRemoval, sensorOutput);
        delay.capture(3, sensorOutput > 0);
        assertTrue(delay.advance(5));

        assertFalse(delay.outputPowered());
    }
}
