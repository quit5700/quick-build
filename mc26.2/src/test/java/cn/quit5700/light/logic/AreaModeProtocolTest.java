package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class AreaModeProtocolTest {
    @Test void allModeButtonsAreUniqueAndDoNotCollideWithApplyOrSelection() {
        var ids=new HashSet<Integer>();
        for(var mode:VariableLightMode.values()) {
            int id=VariableConstantLightSettings.modeButtonId(mode);
            assertTrue(ids.add(id));assertTrue(id>=0 && id<=127);
            assertTrue(VariableConstantLightSettings.isModeButtonId(id));
            assertEquals(mode,VariableConstantLightSettings.modeFromButtonId(id));
        }
        assertFalse(ids.contains(VariableConstantLightSettings.APPLY_BUTTON_ID));
        assertFalse(ids.contains(VariableConstantLightSettings.SELECT_AREA_BUTTON_ID));
        assertFalse(VariableConstantLightSettings.isModeButtonId(VariableConstantLightSettings.APPLY_BUTTON_ID));
        assertEquals(74,VariableConstantLightSettings.APPLY_BUTTON_ID);
    }
    @Test void previewCommandsCannotChangeLightingConfiguration() {
        var ids=new HashSet<Integer>();
        ids.add(VariableConstantLightSettings.APPLY_BUTTON_ID);
        ids.add(VariableConstantLightSettings.SELECT_AREA_BUTTON_ID);
        for(int id:new int[]{VariableConstantLightSettings.SHOW_AREA_BUTTON_ID,VariableConstantLightSettings.HIDE_AREA_BUTTON_ID}) {
            assertTrue(ids.add(id));
            assertTrue(id>=0 && id<=127);
            assertFalse(VariableConstantLightSettings.isRadiusButtonId(id));
            assertFalse(VariableConstantLightSettings.isSpacingButtonId(id));
            assertFalse(VariableConstantLightSettings.isModeButtonId(id));
        }
    }
}
