package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConnectedLightVolumeTest {
    private ConnectedLightVolume build(int r, ConnectedLightVolume.Passage passage) {
        var volume = new ConnectedLightVolume(r);
        while (!volume.complete()) volume.advance(17, passage);
        return volume;
    }
    @Test void openVolumeFillsAllCellsButNotOutsideRadius() {
        var volume = build(5, (x,y,z,a,b,c) -> true);
        for (int x=-5;x<=5;x++) for(int y=-5;y<=5;y++) for(int z=-5;z<=5;z++)
            assertTrue(volume.contains(x,y,z));
        assertFalse(volume.contains(6,0,0));
    }
    @Test void fullWallStopsLightAndIsNotItselfMarkedAsAir() {
        var volume = build(5,(x,y,z,a,b,c)->a!=2);
        assertTrue(volume.contains(1,0,0));
        assertFalse(volume.contains(2,0,0));
        assertFalse(volume.contains(3,0,0));
    }
    @Test void pillarDoesNotCastAnInfiniteBlackShadow() {
        var volume = build(5,(x,y,z,a,b,c)->!(a==2 && c==0));
        assertTrue(volume.contains(3,0,0));
        assertFalse(volume.contains(2,0,0));
    }
    @Test void doorwayConnectsRoomsClosingItRestoresBarrier() {
        var open = build(5,(x,y,z,a,b,c)->a!=2 || (b==0 && c==0));
        var closed = build(5,(x,y,z,a,b,c)->a!=2);
        assertTrue(open.contains(4,0,0));
        assertFalse(closed.contains(4,0,0));
    }
    @Test void cannotCrossOnlyDiagonalGap() {
        var volume = build(2,(x,y,z,a,b,c)->a==b && b==c);
        assertFalse(volume.contains(1,1,1));
    }
    @Test void processingIsBudgetedAndCompletedQueueCanBeQueriedAgain() {
        var volume = new ConnectedLightVolume(3);
        assertEquals(1, volume.advance(1,(x,y,z,a,b,c)->true));
        assertFalse(volume.complete());
        while(!volume.complete()) assertTrue(volume.advance(7,(x,y,z,a,b,c)->true)<=7);
        assertEquals(0,volume.advance(10,(x,y,z,a,b,c)->true));
    }
}
