package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BuildPlanTest {
 @Test void defaultAndCorruptModeUseSingle(){assertEquals(0,BuildPlan.mode(0));assertEquals(0,BuildPlan.mode(-1));assertEquals(0,BuildPlan.mode(100));assertEquals(2,BuildPlan.mode(2));}
 @Test void hollowPreservesInterior(){var b=new DeleteBox(0,0,0,4,4,4);assertEquals(BigInteger.valueOf(98),BuildPlan.total(b,2));assertFalse(BuildPlan.includes(b,2,2,2,2));assertTrue(BuildPlan.includes(b,0,2,2,2));assertTrue(BuildPlan.includes(b,2,2,2,1));}
 @Test void oneAndTwoThickShellsAreFull(){for(int h=1;h<=2;h++){var b=new DeleteBox(-3,0,-4,4,h-1,5);assertEquals(b.volume(),BuildPlan.total(b,2));}}
 @Test void hugePlanHasNoIntOverflowOrArtificialLimit(){var b=new DeleteBox(-30000000,-64,-30000000,30000000,319,30000000);assertTrue(BuildPlan.total(b,1).compareTo(BigInteger.valueOf(Integer.MAX_VALUE))>0);assertTrue(BuildPlan.total(b,2).signum()>0);}
 @Test void totalsMatchDirectedTraversalForAllCorners(){Random random=new Random(510);for(int n=0;n<30;n++){var b=new DeleteBox(-17,-2,-17,-17+random.nextInt(6),-2+random.nextInt(6),-17+random.nextInt(6));for(int dx:new int[]{-1,1})for(int dy:new int[]{-1,1})for(int dz:new int[]{-1,1}){var c=new DirectedDeleteCursor(b,dx,dy,dz);long solid=0,hollow=0;while(!c.done()){solid++;if(BuildPlan.includes(b,c.x(),c.y(),c.z(),2))hollow++;c.advance();}assertEquals(BigInteger.valueOf(solid),BuildPlan.total(b,1));assertEquals(BigInteger.valueOf(hollow),BuildPlan.total(b,2));}}}
 @Test void pausedCursorResumesExactlyWithoutRepeatingPlacements(){var b=new DeleteBox(0,0,0,2,4,2);var c=new DirectedDeleteCursor(b,1,1,1);for(int i=0;i<7;i++)c.advance();int x=c.x(),y=c.y(),z=c.z();var saved=new DeleteTaskData(UUID.randomUUID().toString(),List.of(0,0,0,2,4,2),List.of(x,y,z),"7","7",List.of(1,1,1),UUID.randomUUID().toString());var restored=saved.restoreDirectedCursor();assertEquals(x,restored.x());assertEquals(y,restored.y());assertEquals(z,restored.z());long remaining=0;while(!restored.done()){remaining++;restored.advance();}assertEquals(45-7,remaining);}
 @Test void rightCancelCannotBecomeAnotherModePick(){var s=new DeleteSelection(1);assertTrue(s.pick(new DeleteSelection.Point(0,0,0),1));assertEquals(1,s.cancel());assertEquals(0,s.stage());}
}
