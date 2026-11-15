package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BuildWearTest {
 @Test void hundredAcrossJobs(){var a=BuildWear.placed(0,99);assertEquals(0,a.damage());var b=BuildWear.placed(a.remainder(),1);assertEquals(1,b.damage());assertEquals(0,b.remainder());}
 @Test void atomicPairCrossesLastPoint(){var p=BuildWear.placed(99,2);assertEquals(1,p.damage());assertEquals(1,p.remainder());}
 @Test void zeroDoesNotCharge(){var p=BuildWear.placed(99,0);assertEquals(99,p.remainder());assertEquals(0,p.damage());}
 @Test void bulkAndLegacyNormalization(){var p=BuildWear.placed(99,201);assertEquals(3,p.damage());assertEquals(0,p.remainder());assertEquals(99,BuildWear.remainder(-1));assertThrows(IllegalArgumentException.class,()->BuildWear.placed(0,-1));}
}
