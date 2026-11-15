package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.logic.DeleteBox;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class BuildBoxPlacementTest {
 @BeforeAll static void version(){net.minecraft.SharedConstants.tryDetectVersion();}
 @Test void playerInsideWideShallowSelectionGetsNearbyBoxAboveOrBelow(){
  var bounds=new DeleteBox(-30,80,-30,30,81,30);var p=new BlockPos(0,80,0);
  var at=BuildBoxPlacement.find(bounds,p,q->q.getY()>=80&&!q.equals(p)&&!q.equals(p.above()));
  assertNotNull(at);assertTrue(at.distSqr(p)<=25,"Box must remain reachable instead of appearing at a distant corner: "+at);
  assertFalse(bounds.contains(at.getX(),at.getY(),at.getZ()));
 }
 @Test void blockedPlayerHeightDoesNotHideFreeNearbyUpperPosition(){
  var p=new BlockPos(-14,-53,-25);var bounds=new DeleteBox(50,-60,50,52,-55,52);
  var at=BuildBoxPlacement.find(bounds,p,q->q.equals(p.offset(1,1,0)));
  assertEquals(p.offset(1,1,0),at);
 }
 @Test void noSpaceReturnsNullWithoutOverwritingAnything(){assertNull(BuildBoxPlacement.find(new DeleteBox(0,0,0,1,1,1),BlockPos.ZERO,p->false));}
 @Test void distantPlayerGetsBoxAtNearestSelectionBoundary(){var b=new DeleteBox(50,70,50,60,80,60);var p=new BlockPos(0,75,55);assertEquals(new BlockPos(49,75,55),BuildBoxPlacement.find(b,p,q->true));}
 @Test void blockedNearestBoundaryUsesAdjacentBoundaryInsteadOfRemotePlayer(){var b=new DeleteBox(50,70,50,60,80,60);var p=new BlockPos(0,75,55);var blocked=new BlockPos(49,75,55);var at=BuildBoxPlacement.find(b,p,q->!q.equals(blocked));assertEquals(49,at.getX());assertTrue(Math.abs(at.getY()-75)+Math.abs(at.getZ()-55)==1);}
 @Test void highPlayerGetsBoxAtTopBoundary(){assertEquals(new BlockPos(55,81,55),BuildBoxPlacement.find(new DeleteBox(50,70,50,60,80,60),new BlockPos(55,120,55),q->true));}
}
