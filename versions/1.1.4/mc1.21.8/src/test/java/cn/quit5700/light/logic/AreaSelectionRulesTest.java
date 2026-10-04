package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AreaSelectionRulesTest {
    @Test void airPointMatchesWandDistanceAndNegativeCoordinateFlooring() {
        assertEquals(new AreaSelectionRules.Point(4,2,0),AreaSelectionRules.airPoint(.2,2.8,.7,1,0,0));
        assertEquals(new AreaSelectionRules.Point(-4,-2,-1),AreaSelectionRules.airPoint(.2,-1.2,-.1,-1,0,0));
        assertEquals(new AreaSelectionRules.Point(0,6,0),AreaSelectionRules.airPoint(0,2,0,0,1,0));
    }
    @Test void finishingOrCancelingHidesPreviewButExplicitShowPinsIt() {
        assertTrue(AreaSelectionRules.showPreview(true,false));
        boolean completed=AreaSelectionRules.pinPreview(false,false,true);
        assertFalse(AreaSelectionRules.showPreview(false,completed));
        boolean requested=AreaSelectionRules.pinPreview(false,true,true);
        assertTrue(AreaSelectionRules.showPreview(false,requested));
        assertFalse(AreaSelectionRules.pinPreview(false,true,false));
        assertFalse(AreaSelectionRules.pinPreview(true,true,true));
        assertFalse(AreaSelectionRules.showPreview(false,false));
    }
}
