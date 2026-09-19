package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WandAreaGestureTest {
    @Test void twoClicksPrepareAndNextPressConfirms() {
        var g=new WandAreaGesture<Integer>();
        assertEquals(WandAreaGesture.Action.FIRST,g.press(1));g.release(1);
        assertNull(g.pending());
        assertEquals(WandAreaGesture.Action.NONE,g.press(8));g.release(8);
        assertEquals(8,g.pending());
        assertEquals(WandAreaGesture.Action.CONFIRM,g.press(9));assertEquals(8,g.pending());
    }
    @Test void dragAndCancelMatchWandGestureLifecycle() {
        var g=new WandAreaGesture<Integer>();g.press(1);g.release(5);
        assertEquals(5,g.pending());g.reject();assertNull(g.pending());assertEquals(1,g.first());
        g.clear();assertNull(g.first());assertNull(g.pending());
    }
}
