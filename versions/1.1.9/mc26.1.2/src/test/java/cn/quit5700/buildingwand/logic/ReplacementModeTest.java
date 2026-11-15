package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;
class ReplacementModeTest {
 @Test void fourthModeDoesNotChangeDefaultOrOtherModes(){for(int m=0;m<=3;m++)assertEquals(m,BuildPlan.mode(m));assertEquals(0,BuildPlan.mode(4));assertEquals(0,BuildPlan.mode(-1));}
 @Test void replacementVisitsWholeAreaIncludingInterior(){var b=new DeleteBox(-2,-3,-4,2,3,4);assertTrue(BuildPlan.includes(b,0,0,0,3));assertFalse(BuildPlan.includes(b,3,0,0,3));assertEquals(b.volume(),BuildPlan.total(b,3));}
 @Test void replacementRetainsUnlimitedLongSafeDirectedTraversal(){var b=new DeleteBox(Integer.MIN_VALUE,-64,Integer.MIN_VALUE,Integer.MAX_VALUE,319,Integer.MAX_VALUE);assertTrue(BuildPlan.total(b,3).compareTo(BigInteger.valueOf(Long.MAX_VALUE))>0);}
}
