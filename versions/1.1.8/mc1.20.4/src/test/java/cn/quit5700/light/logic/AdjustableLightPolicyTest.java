package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AdjustableLightPolicyTest {
    @Test void normalLampExposesOnlyTwoAreaModesInContiguousOrder() {
        assertEquals(List.of(VariableLightMode.AREA_FULL_BRIGHT, VariableLightMode.AREA_REAL_LIGHT),
                AdjustableLightPolicy.modes(false));
    }
    @Test void legacyLampKeepsOriginalModesAndSavedBehavior() {
        assertEquals(List.of(VariableLightMode.VANILLA_INVISIBLE, VariableLightMode.LOCAL_DAYLIGHT,
                VariableLightMode.HYBRID), AdjustableLightPolicy.modes(true));
        for (var mode : AdjustableLightPolicy.modes(true))
            assertEquals(mode, AdjustableLightPolicy.savedMode(true, mode));
    }
    @Test void removedModesMigrateToAreaWithoutChangingExistingAreaModes() {
        for (var mode : VariableLightMode.values()) {
            assertEquals(mode.isAreaMode() ? mode : VariableLightMode.AREA_FULL_BRIGHT,
                    AdjustableLightPolicy.savedMode(false, mode));
        }
    }
}
