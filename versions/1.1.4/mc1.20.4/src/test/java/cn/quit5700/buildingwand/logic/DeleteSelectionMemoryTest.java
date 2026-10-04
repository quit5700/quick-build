package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeleteSelectionMemoryTest {
    private DeleteSelectionMemory selected(int stage){
        var memory=new DeleteSelectionMemory();memory.activate("overworld");long sequence=memory.request();
        memory.apply(stage,new DeleteSelection.Point(10,20,30),new DeleteSelection.Point(-10,-20,-30),77,sequence,false,false);return memory;
    }
    @Test void switchingItemsRetainsFirstPointAndFlashesWhileHidden(){
        var memory=selected(1);memory.pause();assertFalse(memory.visible());assertEquals(1,memory.stage);
        assertEquals(10,memory.first.x());assertTrue(memory.flashing(0));assertFalse(memory.flashing(20));
        assertTrue(memory.activate("overworld"));assertEquals(1,memory.stage);assertTrue(memory.visible());
    }
    @Test void switchingItemsRetainsConfirmedRange(){
        var memory=selected(2);memory.pause();memory.activate("overworld");assertEquals(2,memory.stage);assertEquals(-10,memory.second.x());
    }
    @Test void dimensionChangeAndDisconnectClearSelectionAndFlashing(){
        var memory=selected(2);memory.dimension("nether");assertEquals(0,memory.stage);assertFalse(memory.flashing(0));
        memory=selected(1);memory.disconnect();assertEquals(0,memory.stage);assertEquals(0,memory.sequence);
    }
    @Test void cancellationStopsFlashingAndOldRepliesCannotRestoreIt(){
        var memory=selected(2);long old=memory.sequence;memory.request();memory.cancelLocally();
        memory.apply(2,new DeleteSelection.Point(1,2,3),new DeleteSelection.Point(3,2,1),88,old,false,false);
        assertEquals(0,memory.stage);assertFalse(memory.flashing(0));
    }
    @Test void pendingCountAndInsufficientWarningSurviveItemSwitch(){
        var memory=selected(2);memory.apply(2,memory.first,memory.second,77,memory.sequence,true,false);memory.pause();
        assertTrue(memory.checking);memory.apply(2,memory.first,memory.second,77,memory.sequence,false,true);
        assertFalse(memory.checking);assertTrue(memory.insufficient);
    }
}
