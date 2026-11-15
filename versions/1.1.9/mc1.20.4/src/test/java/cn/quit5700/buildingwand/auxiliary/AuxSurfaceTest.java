package cn.quit5700.buildingwand.aux;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AuxSurfaceTest {
 @BeforeAll static void init(){SharedConstants.tryDetectVersion();}
 @Test void whiteOutlinesRemainVisibleAgainstTheSky(){assertEquals(0x455C65,AuxSurface.outlineColor(0xFFFFFF));assertEquals(0x2255FF,AuxSurface.outlineColor(0x2255FF));}
 @Test void silhouetteWidthRemainsVisibleWithoutCoveringAVoxel(){assertEquals(.054,AuxSurface.outlineWidth(30),.00001);assertEquals(.018,AuxSurface.outlineWidth(0));assertEquals(.12,AuxSurface.outlineWidth(1000));}
 @Test void faceInteriorIsNotConfusedWithTheOutline(){
  var row=new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(6,6,6)));
  assertEquals(0,AuxSurface.edgeMask(row,new BlockPos(3,6,3),Direction.UP));
  assertEquals(15,AuxSurface.edgeMask(new AuxRow(AuxRow.create(BlockPos.ZERO,BlockPos.ZERO)),BlockPos.ZERO,Direction.UP));
  assertEquals(1|8,AuxSurface.edgeMask(row,new BlockPos(0,6,0),Direction.UP));
 }
 @Test void visibleFacesFollowCameraInsteadOfShowingEveryRearGrid(){
  assertTrue(AuxSurface.frontFacing(BlockPos.ZERO,Direction.SOUTH,.5,.5,10));
  assertFalse(AuxSurface.frontFacing(BlockPos.ZERO,Direction.NORTH,.5,.5,10));
  assertFalse(AuxSurface.frontFacing(BlockPos.ZERO,Direction.SOUTH,.5,.5,-10));
  for(var d:Direction.values())assertEquals(4,AuxSurface.edges(d).length);
 }
 @Test void reportedStarIsAStarPrismWithAnIdenticalCrossSectionThroughoutItsDepth(){
  var tag=AuxRow.create(new BlockPos(-43,-59,1),new BlockPos(-23,-48,37));tag.putInt("Shape",8);var star=new AuxRow(tag);
  int count=0;for(int x=0;x<21;x++)for(int y=0;y<12;y++)for(int z=0;z<37;z++){
   assertEquals(star.contains(x,y,0),star.contains(x,y,z));if(star.contains(x,y,z))count++;
  }
  assertTrue(count>0);assertFalse(star.contains(0,0,0));assertTrue(star.contains(10,6,18));
 }
 @Test void starOutlineAndShapeRotateTogether(){
  var tag=AuxRow.create(BlockPos.ZERO,new BlockPos(20,20,4));tag.putInt("Shape",8);var before=new AuxRow(tag.copy());var after=new AuxRow(tag);after.rotate(1,BlockPos.ZERO,1);
  for(long i=0;i<before.volume();i++){int[] cell=before.coordinates(i);assertEquals(before.contains(cell[0],cell[1],cell[2]),after.contains(cell[0],cell[1],cell[2]));}
 }
}
