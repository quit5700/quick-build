package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LightBoxTest {
    @Test void reversedCornersAndNegativeCoordinatesAreNormalized() {
        var box = LightBox.of(4,5,6,-1,-2,-3);
        assertEquals(LightBox.of(-1,-2,-3,4,5,6),box);
        assertTrue(box.contains(-1,-2,-3)); assertTrue(box.contains(4,5,6));
        assertFalse(box.contains(5,0,0)); assertFalse(box.contains(0,-3,0));
    }
    @Test void singleBlockAndInclusiveBoundary() {
        var box=LightBox.of(2,3,4,2,3,4);
        assertTrue(box.contains(2,3,4)); assertFalse(box.contains(2,3,5));
        assertEquals(1,box.volume());
    }
    @Test void hugeOrOverflowingSelectionsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->LightBox.of(0,0,0,512,0,0));
        assertThrows(IllegalArgumentException.class,()->LightBox.of(Integer.MIN_VALUE,0,0,Integer.MAX_VALUE,0,0));
        assertEquals(512L*512*512,LightBox.of(0,0,0,511,511,511).volume());
    }
}
