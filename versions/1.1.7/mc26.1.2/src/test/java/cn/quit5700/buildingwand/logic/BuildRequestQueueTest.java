package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BuildRequestQueueTest {
 @Test void clickOnFirstHeldTickSurvivesInitialQuery(){var q=new BuildRequestQueue<String>();q.activate();q.enqueue("left");var query=q.next(0);assertTrue(query.query());assertNull(q.next(1));assertTrue(q.acknowledge(query.sequence()));assertEquals("left",q.next(2).action());}
 @Test void twoFastMaterialClicksStayOrdered(){var q=new BuildRequestQueue<String>();q.activate();q.enqueue("material");q.enqueue("filter");var query=q.next(0);q.acknowledge(query.sequence());var first=q.next(1);assertEquals("material",first.action());assertNull(q.next(2));q.acknowledge(first.sequence());assertEquals("filter",q.next(3).action());}
 @Test void timeoutReconcilesWithoutRepeatingConfirmation(){var q=new BuildRequestQueue<String>();q.activate();q.acknowledge(q.next(0).sequence());q.enqueue("confirm");assertEquals("confirm",q.next(1).action());var query=q.next(21);assertTrue(query.query());q.acknowledge(query.sequence());assertNull(q.next(22));}
 @Test void delayedReplyCannotAcknowledgeNewQuery(){var q=new BuildRequestQueue<String>();q.activate();var first=q.next(0);var retry=q.next(20);assertFalse(q.acknowledge(first.sequence()));assertTrue(q.acknowledge(retry.sequence()));}
 @Test void changingToolDiscardsDeferredClicks(){var q=new BuildRequestQueue<String>();q.activate();q.enqueue("confirm");var old=q.next(0);q.deactivate();assertNull(q.next(30));q.activate();var fresh=q.next(31);assertTrue(fresh.query());assertFalse(q.acknowledge(old.sequence()));q.acknowledge(fresh.sequence());assertNull(q.next(32));}
 @Test void repliesWithWrongSequenceAreIgnored(){var q=new BuildRequestQueue<String>();q.activate();var p=q.next(0);assertFalse(q.acknowledge(p.sequence()+1));assertFalse(q.acknowledge(0));assertTrue(q.acknowledge(p.sequence()));}
 @Test void hotbarChangesKeepSequenceMonotonic(){var q=new BuildRequestQueue<String>();q.activate();long first=q.next(0).sequence();q.activate();assertTrue(q.next(1).sequence()>first);}
 @Test void disconnectResetsSequenceOnlyAfterSessionEnds(){var q=new BuildRequestQueue<String>();q.activate();q.next(0);q.reset();assertNull(q.next(1));q.activate();assertEquals(1,q.next(2).sequence());}
 @Test void timeoutBeforeDueDoesNotSendExtraAction(){var q=new BuildRequestQueue<String>();q.activate();q.next(0);q.enqueue("pick");assertNull(q.next(19));assertTrue(q.next(20).query());}
}
