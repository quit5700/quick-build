package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VariableLightPresentationTest {
    @Test void originalAddsAreaModeAndCopyKeepsOnlyItsOriginalThree() {
        int original = 0, copy = 0;
        for (var mode : VariableLightMode.values()) {
            if (VariableLightPresentation.showsMode(false, mode)) original++;
            if (VariableLightPresentation.showsMode(true, mode)) copy++;
        }
        assertEquals(4, original); assertEquals(3, copy);
        assertTrue(VariableLightPresentation.showsMode(false,VariableLightMode.AREA_FULL_BRIGHT));
        assertFalse(VariableLightPresentation.showsMode(true,VariableLightMode.AREA_FULL_BRIGHT));
        assertFalse(VariableLightPresentation.showsMode(true,VariableLightMode.AREA_REAL_LIGHT));
        assertTrue(VariableLightPresentation.showsMode(false,VariableLightMode.AREA_REAL_LIGHT));
    }
    @Test void savedHybridRemainsBoostedVanillaWithoutChangingItsOrdinal() {
        assertEquals(VariableLightMode.VANILLA_INVISIBLE, VariableLightPresentation.primaryMode(false, VariableLightMode.fromOrdinal(2)));
        assertEquals(VariableLightMode.HYBRID, VariableLightPresentation.primaryMode(true, VariableLightMode.fromOrdinal(2)));
        assertEquals(VariableLightMode.HYBRID, VariableLightPresentation.toggleBoost(VariableLightMode.VANILLA_INVISIBLE));
        assertEquals(VariableLightMode.VANILLA_INVISIBLE, VariableLightPresentation.toggleBoost(VariableLightMode.HYBRID));
    }
}
