package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ConnectorInteractionPolicyTest {
    @Test
    void onlyHandlesUseWhileShiftIsHeld() {
        assertFalse(ConnectorInteractionPolicy.shouldHandle(false));
        assertTrue(ConnectorInteractionPolicy.shouldHandle(true));
    }
}
