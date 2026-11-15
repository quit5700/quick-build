package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableLightModeTransitionTest {
    @Test
    void pureVanillaOperationsNeverEnterTheDaylightSyncPath() {
        assertFalse(VariableLightModeTransition.needsDaylightSync(
                VariableLightMode.VANILLA_INVISIBLE,
                VariableLightMode.VANILLA_INVISIBLE));
    }

    @Test
    void enteringOrLeavingADaylightModeRequiresSync() {
        assertTrue(VariableLightModeTransition.needsDaylightSync(
                VariableLightMode.VANILLA_INVISIBLE, VariableLightMode.LOCAL_DAYLIGHT));
        assertTrue(VariableLightModeTransition.needsDaylightSync(
                VariableLightMode.HYBRID, VariableLightMode.VANILLA_INVISIBLE));
    }
}
