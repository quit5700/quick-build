package cn.quit5700.buildingwand.logic;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class DeleteWandFlowTest {
    private static final DeleteSelection.Point A=new DeleteSelection.Point(-17,-2,-17);
    private static final DeleteSelection.Point B=new DeleteSelection.Point(18,2,18);

    @Test void firstAndSecondNeverDeleteWithoutThirdConfirmation() {
        DeleteSelection s=new DeleteSelection();
        assertNull(s.confirm(0));assertTrue(s.pick(A,0));assertEquals(1,s.stage());
        assertNull(s.confirm(1));assertTrue(s.pick(B,1));assertEquals(2,s.stage());
        assertFalse(s.pick(A,2));assertNotNull(s.confirm(2));assertEquals(0,s.stage());
        assertNull(s.confirm(2));
    }
    @Test void cancelFirstRestoresIdleAndRequiresNewFirstPoint() {
        DeleteSelection s=new DeleteSelection();s.pick(A,0);
        assertEquals(1,s.cancel());assertEquals(0,s.stage());assertNull(s.first());
        assertTrue(s.pick(B,s.revision()));assertEquals(B,s.first());assertEquals(1,s.stage());
    }
    @Test void cancelConfirmedRangeDeletesNothingAndRejectsDelayedConfirmation() {
        DeleteSelection s=new DeleteSelection();s.pick(A,0);s.pick(B,1);
        long old=s.revision();assertEquals(2,s.cancel());assertNull(s.confirm(old));
        assertNull(s.first());assertNull(s.second());
    }
    @Test void oldConfirmCannotDeleteANewSelection() {
        DeleteSelection s=new DeleteSelection();s.pick(A,0);s.pick(B,1);long old=s.revision();s.cancel();
        s.pick(B,s.revision());s.pick(A,s.revision());assertNull(s.confirm(old));
        assertEquals(2,s.stage());assertNotNull(s.confirm(s.revision()));
    }
    @Test void stalePointDoesNotChangeSelection() {
        DeleteSelection s=new DeleteSelection();assertFalse(s.pick(A,99));assertEquals(0,s.stage());
        s.pick(A,0);assertFalse(s.pick(B,0));assertEquals(1,s.stage());
    }
    @Test void confirmationCannotCarryAcrossSessionNonces() {
        DeleteSelection old=new DeleteSelection(100);old.pick(A,100);old.pick(B,101);
        DeleteSelection fresh=new DeleteSelection(200);fresh.pick(A,200);fresh.pick(B,201);
        assertNull(fresh.confirm(old.revision()));assertEquals(2,fresh.stage());
        assertNotNull(fresh.confirm(fresh.revision()));
    }
    @Test void endpointsInclusiveAndEitherOrderingAccepted() {
        DeleteBox box=DeleteBox.between(18,2,18,-17,-2,-17);
        assertEquals(new DeleteBox(-17,-2,-17,18,2,18),box);
        assertEquals(BigInteger.valueOf(36L*5*36),box.volume());
        assertTrue(box.contains(-17,-2,-17));assertTrue(box.contains(18,2,18));assertFalse(box.contains(19,2,18));
    }
    @Test void volumeBeyondLongAndLargeRangesAreNotCapped() {
        DeleteBox box=new DeleteBox(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);
        assertEquals(BigInteger.ONE.shiftLeft(96),box.volume());
        DeleteCursor cursor=new DeleteCursor(box);for(int n=0;n<100;n++)cursor.advance();
        assertEquals(Integer.MIN_VALUE+100,cursor.y());assertFalse(cursor.done());
    }
    @Test void singleBlockTraversedExactlyOnce() {
        DeleteCursor c=new DeleteCursor(new DeleteBox(7,8,9,7,8,9));assertFalse(c.done());c.advance();assertTrue(c.done());c.advance();assertTrue(c.done());
    }
    @Test void chunkMajorTraversalCoversNegativeCoordinatesAndPartialChunksExactlyOnce() {
        DeleteBox box=new DeleteBox(-17,-2,-17,18,2,18);DeleteCursor c=new DeleteCursor(box);
        HashSet<String> seen=new HashSet<>(),finishedChunks=new HashSet<>();String previous=null;
        while(!c.done()) {
            assertTrue(box.contains(c.x(),c.y(),c.z()));assertTrue(seen.add(c.x()+","+c.y()+","+c.z()));
            String chunk=c.chunkX()+","+c.chunkZ();
            if(!chunk.equals(previous)){assertFalse(finishedChunks.contains(chunk));if(previous!=null)finishedChunks.add(previous);previous=chunk;}
            c.advance();
        }
        assertEquals(box.volume().intValueExact(),seen.size());
    }
    @Test void persistedCursorResumesWithoutRepeatingOrSkippingPositions() {
        DeleteBox box=new DeleteBox(-20,-3,-20,20,3,20);DeleteCursor uninterrupted=new DeleteCursor(box);
        List<String> expected=new ArrayList<>();
        while(!uninterrupted.done()){expected.add(uninterrupted.x()+","+uninterrupted.y()+","+uninterrupted.z());uninterrupted.advance();}
        DeleteCursor cursor=new DeleteCursor(box);List<String> actual=new ArrayList<>();int count=0;
        while(!cursor.done()){
            actual.add(cursor.x()+","+cursor.y()+","+cursor.z());cursor.advance();
            if(++count%83==0&&!cursor.done())cursor=new DeleteCursor(box,cursor.x(),cursor.y(),cursor.z(),false);
        }
        assertEquals(expected,actual);
    }
    @Test void invalidPersistedCursorRejected() {
        DeleteBox box=new DeleteBox(0,0,0,1,1,1);
        assertThrows(IllegalArgumentException.class,()->new DeleteCursor(box,2,0,0,false));
        assertThrows(IllegalArgumentException.class,()->new DeleteBox(1,0,0,0,1,1));
    }
}
