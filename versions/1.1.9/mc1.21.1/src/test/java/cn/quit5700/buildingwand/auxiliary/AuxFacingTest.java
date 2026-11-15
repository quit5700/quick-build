package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class AuxFacingTest {
 @BeforeAll static void detect(){net.minecraft.SharedConstants.tryDetectVersion();}
 @Test void verticalFacingWorksWithoutIllegalPitch(){assertEquals(new BlockPos(0,1,0),AuxFacing.direction(new Vec3(.1,8,.2)));assertEquals(new BlockPos(0,-1,0),AuxFacing.direction(new Vec3(.1,-8,.2)));}
 @Test void everySourceNormalCanFaceAllSixPlayerDirections(){
  var normals=List.of(new BlockPos(1,0,0),new BlockPos(-1,0,0),new BlockPos(0,1,0),new BlockPos(0,-1,0),new BlockPos(0,0,1),new BlockPos(0,0,-1));
  for(var source:normals)for(var desired:normals){var row=new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(4,4,4)));AuxRotationGesture.apply(row,AuxFacing.steps(new BlockPos(0,0,-1),source),new BlockPos(2,2,2));var pivot=new BlockPos(2,2,2);AuxRotationGesture.apply(row,AuxFacing.steps(AuxFacing.normal(row),desired),pivot);assertEquals(desired,AuxFacing.normal(row));assertEquals(pivot,row.world(2,2,2));}
 }
 @Test void centralPivotIsIndependentOfTheClickedEdge(){var row=new AuxRow(AuxRow.create(new BlockPos(-8,2,10),new BlockPos(-2,8,16)));assertEquals(new BlockPos(-5,5,13),AuxFacing.centerBlock(List.of(row)));}
 @Test void evenDimensionsChooseStableCentralBlockInsteadOfHalfGrid(){var row=new AuxRow(AuxRow.create(new BlockPos(-8,2,10),new BlockPos(-3,8,11)));var pivot=AuxFacing.centerBlock(List.of(row));assertEquals(new BlockPos(-6,5,10),pivot);var local=row.local(pivot);AuxRotationGesture.apply(row,AuxFacing.steps(AuxFacing.normal(row),new BlockPos(0,-1,0)),pivot);assertEquals(pivot,row.world(local[0],local[1],local[2]));}
 @Test void endpointOrderSurvivesRotationAndReload(){var first=new BlockPos(6,8,10);var second=new BlockPos(2,4,6);var row=new AuxRow(AuxRow.create(first,second));assertArrayEquals(new BlockPos[]{first,second},AuxFacing.endpoints(row));var pivot=AuxFacing.centerBlock(List.of(row));row.rotate(0,pivot,1);var loaded=new AuxRow(row.data.copy());assertArrayEquals(AuxFacing.endpoints(row),AuxFacing.endpoints(loaded));assertEquals(48,AuxFacing.endpoints(row)[0].distSqr(AuxFacing.endpoints(row)[1]));}
 @Test void selectedGroupKeepsRelativeGeometryAndCancelSource(){var a=new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(2,2,2)));var b=new AuxRow(AuxRow.create(new BlockPos(4,0,0),new BlockPos(6,2,2)));var pivot=AuxFacing.centerBlock(List.of(a,b));var steps=AuxFacing.steps(AuxFacing.normal(a),new BlockPos(0,1,0));var ca=a.copy();var cb=b.copy();AuxRotationGesture.apply(ca,steps,pivot);AuxRotationGesture.apply(cb,steps,pivot);assertEquals(a.world(0,0,0).distSqr(b.world(0,0,0)),ca.world(0,0,0).distSqr(cb.world(0,0,0)));assertEquals(BlockPos.ZERO,a.world(0,0,0));}
}
