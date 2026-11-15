package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SectionRefreshQueueTest {
    @Test void waitsForBlockUpdatesAndCoalescesNegativeHeightSections(){
        var queue=new SectionRefreshQueue();queue.offer(-2,3,-4,-2,0);queue.offer(-2,3,-3,1,2);
        assertEquals(1,queue.size());assertTrue(queue.drain(5,2).isEmpty());
        var refresh=queue.drain(6,2).get(0);assertEquals(-4,refresh.minY());assertEquals(1,refresh.maxY());assertEquals(0,queue.size());
    }
    @Test void onlyModifiedChunksAreQueuedAndDrainBudgetIsRespected(){
        var queue=new SectionRefreshQueue();for(int n=0;n<10;n++)queue.offer(n,n,0,2,0);
        assertEquals(2,queue.drain(4,2).size());assertEquals(8,queue.size());assertEquals(8,queue.drain(5,20).size());
    }
    @Test void changingDimensionsClearsPendingRefreshes(){
        var queue=new SectionRefreshQueue();queue.offer(1,2,3,4,0);queue.clear();assertTrue(queue.drain(10,10).isEmpty());
    }
    @Test void newerBlocksPostponeFinalRefresh(){
        var queue=new SectionRefreshQueue();queue.offer(1,2,0,0,0);queue.offer(1,2,0,0,3);
        assertTrue(queue.drain(4,1).isEmpty());assertEquals(1,queue.drain(7,1).size());
    }
}
