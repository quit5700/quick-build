package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RealAreaPolicyTest {
    @Test void volumeIsLimitedIndependentlyOfSideLength() {
        assertTrue(RealAreaPolicy.accepts(LightBox.of(0,0,0,63,15,63)));
        assertTrue(RealAreaPolicy.accepts(LightBox.of(0,0,0,128,128,128)));
        assertTrue(RealAreaPolicy.accepts(LightBox.of(0,0,0,255,63,127)));
        assertFalse(RealAreaPolicy.accepts(LightBox.of(0,0,0,128,128,129)));
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
    @Test void sparsePassIsUniqueAndStartsAcrossTheVolumeBeforeDensePass() {
        var box=LightBox.of(0,0,0,128,128,128);
        int sparse=RealAreaPolicy.sparseCount(box);
        assertEquals(4913,sparse);
        var seen=new java.util.HashSet<AreaSelectionRules.Point>();
        int minX=129,minY=129,minZ=129,maxX=-1,maxY=-1,maxZ=-1;
        for(int i=0;i<sparse;i++) {
            var p=RealAreaPolicy.scanPosition(box,i,1);assertTrue(seen.add(p));
            assertEquals(0,p.x()%8);assertEquals(0,p.y()%8);assertEquals(0,p.z()%8);
            if(i<512){minX=Math.min(minX,p.x());minY=Math.min(minY,p.y());minZ=Math.min(minZ,p.z());
                maxX=Math.max(maxX,p.x());maxY=Math.max(maxY,p.y());maxZ=Math.max(maxZ,p.z());}
        }
        assertTrue(maxX-minX>=112);assertTrue(maxY-minY>=112);assertTrue(maxZ-minZ>=112);
        assertEquals(RealAreaPolicy.position(box,0),RealAreaPolicy.scanPosition(box,sparse,1));
    }
    @Test void densePassFillsIrregularAndNegativeBoxesAndLegacyCursorsStayLinear() {
        for(var box:java.util.List.of(LightBox.of(-17,-2,-31,8,4,-3),LightBox.of(1,2,3,1,2,3))) {
            var seen=new java.util.HashSet<AreaSelectionRules.Point>();
            int count=RealAreaPolicy.operationCount(box,1);
            for(int i=0;i<count;i++) {
                var p=RealAreaPolicy.scanPosition(box,i,1);assertTrue(box.contains(p.x(),p.y(),p.z()));seen.add(p);
            }
            assertEquals(box.volume(),seen.size());
            for(int i=0;i<box.volume();i++)assertEquals(RealAreaPolicy.position(box,i),RealAreaPolicy.scanPosition(box,i,0));
        }
    }
}
