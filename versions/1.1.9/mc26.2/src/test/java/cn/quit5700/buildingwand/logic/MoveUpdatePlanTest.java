package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MoveUpdatePlanTest {
 @Test void interiorOfSeparatedSourceAndTargetDoesNotReceiveArtificialUpdates(){var a=new DeleteBox(0,0,0,4,4,4);var b=new DeleteBox(100,0,0,104,4,4);assertFalse(MoveUpdatePlan.boundary(a,b,2,2,2));assertFalse(MoveUpdatePlan.boundary(a,b,102,2,2));assertTrue(MoveUpdatePlan.boundary(a,b,100,2,2));assertTrue(MoveUpdatePlan.boundary(a,b,0,2,2));}
 @Test void overlappingBoundaryAndThinStructuresAreNotMissed(){var a=new DeleteBox(0,0,0,4,4,4);var b=new DeleteBox(2,0,0,6,4,4);assertTrue(MoveUpdatePlan.boundary(a,b,2,2,2));assertFalse(MoveUpdatePlan.boundary(a,b,3,2,2));var thin=new DeleteBox(0,0,0,100,0,100);assertTrue(MoveUpdatePlan.boundary(thin,thin,50,0,50));}
 @Test void warningIsAdvisoryAndUsesLongSafeVolume(){assertFalse(MoveSizeNotice.warn(new DeleteBox(0,0,0,31,31,31)));assertTrue(MoveSizeNotice.warn(new DeleteBox(0,0,0,32,31,31)));assertTrue(MoveSizeNotice.warn(new DeleteBox(-30000000,-64,-30000000,30000000,319,30000000)));}
}
