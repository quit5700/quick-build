package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RealAreaPolicyTest {
    @Test void volumeIsLimitedIndependentlyOfSideLength() {
        assertTrue(RealAreaPolicy.accepts(LightBox.of(0,0,0,63,15,63)));
        assertFalse(RealAreaPolicy.accepts(LightBox.of(0,0,0,63,16,63)));
        assertFalse(RealAreaPolicy.accepts(null));
    }
    @Test void lazyScanCoversEveryCellOnceIncludingNegativeBounds() {
        var box=LightBox.of(-2,-3,-4,1,0,0);
        var positions=new java.util.HashSet<AreaSelectionRules.Point>();
        for(int i=0;i<box.volume();i++) {
            var p=RealAreaPolicy.position(box,i);
            assertTrue(box.contains(p.x(),p.y(),p.z()));assertTrue(positions.add(p));
        }
        assertEquals(box.volume(),positions.size());
        assertThrows(IndexOutOfBoundsException.class,()->RealAreaPolicy.position(box,(int)box.volume()));
    }
    @Test void cleanupPreservesUnownedAndSharedLightsAndSolidBlocks() {
        assertTrue(RealAreaPolicy.canRemove(true,false,false,true));
        assertFalse(RealAreaPolicy.canRemove(false,false,false,true));
        assertFalse(RealAreaPolicy.canRemove(true,true,false,true));
        assertFalse(RealAreaPolicy.canRemove(true,false,true,true));
        assertFalse(RealAreaPolicy.canRemove(true,false,false,false));
    }
}
