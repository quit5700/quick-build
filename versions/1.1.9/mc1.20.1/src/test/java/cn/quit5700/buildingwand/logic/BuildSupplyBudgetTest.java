package cn.quit5700.buildingwand.logic;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BuildSupplyBudgetTest {
 @Test void largeRegionsDoNotTruncateToInt(){assertEquals(2147483653L,BuildSupplyBudget.remainingItems(new BigInteger("2147483653"),BigInteger.ZERO,false));}
 @Test void pairsCostOneItemAndOddLegacyProgressRoundsUp(){assertEquals(2,BuildSupplyBudget.remainingItems(BigInteger.valueOf(4),BigInteger.ZERO,true));assertEquals(2,BuildSupplyBudget.remainingItems(BigInteger.valueOf(4),BigInteger.ONE,true));}
 @Test void completedLegacyTaskNeverRequestsNegativeMaterial(){assertEquals(0,BuildSupplyBudget.remainingItems(BigInteger.valueOf(5),BigInteger.valueOf(7),false));}
 @Test void overflowAndNegativeProgressAreRejected(){assertThrows(ArithmeticException.class,()->BuildSupplyBudget.remainingItems(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE),BigInteger.ZERO,false));assertThrows(IllegalArgumentException.class,()->BuildSupplyBudget.remainingItems(BigInteger.ONE,BigInteger.valueOf(-1),false));}
}
