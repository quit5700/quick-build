package cn.quit5700.buildingwand.aux;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class AuxConnectionFaceTest {
 @BeforeAll static void version(){net.minecraft.SharedConstants.tryDetectVersion();}
 @Test void connectingToShortAxisFaceKeepsThatFaceInsteadOfPresetFront(){
  var row=new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(1,6,10)));
  assertEquals(new BlockPos(1,0,0),AuxFacing.connectionNormal(List.of(row),new BlockPos(1,3,5),new Vec3(8,3.5,5.5),new Vec3(-8,3.5,5.5)));
 }
 @Test void horizontalHalfTurnDoesNotRollOrFlipTopAndBottom(){
  var row=new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(1,6,10)));
  AuxRotationGesture.apply(row,AuxFacing.steps(new BlockPos(0,0,-1),new BlockPos(0,0,1)),BlockPos.ZERO);
  assertEquals(new BlockPos(0,1,0),row.world(0,1,0).subtract(row.world(0,0,0)));
 }
 @Test void wholeGroupKeepsEveryCellAndMetadataAcrossSixDirections(){
  var a=new AuxRow(AuxRow.create(new BlockPos(-4,-2,3),new BlockPos(-3,4,13)));a.remove(a.world(0,3,5));
  var b=new AuxRow(AuxRow.create(new BlockPos(7,3,-6),new BlockPos(9,5,-2)));
  var originals=List.of(a,b);var pivot=AuxFacing.centerBlock(originals);
  for(var desired:List.of(new BlockPos(1,0,0),new BlockPos(-1,0,0),new BlockPos(0,1,0),new BlockPos(0,-1,0),new BlockPos(0,0,1),new BlockPos(0,0,-1))){
   var steps=AuxFacing.steps(new BlockPos(1,0,0),desired);
   var copies=originals.stream().map(AuxRow::copy).toList();
   for(int n=0;n<copies.size();n++){var copy=copies.get(n);var src=originals.get(n);AuxRotationGesture.apply(copy,steps,pivot);
    assertEquals(desired,copy.world(1,0,0).subtract(copy.world(0,0,0)));
    for(int x=0;x<src.w;x++)for(int y=0;y<src.h;y++)for(int z=0;z<src.d;z++)assertEquals(src.contains(x,y,z),copy.contains(x,y,z));
    var old=src.data.copy();var changed=copy.data.copy();for(String key:List.of("X","Y","Z","Matrix")){old.remove(key);changed.remove(key);}assertEquals(old,changed);
    var loaded=new AuxRow(copy.data.copy());assertEquals(copy.world(1,2,1),loaded.world(1,2,1));
   }
   assertEquals(a.world(0,0,0).distSqr(b.world(0,0,0)),copies.get(0).world(0,0,0).distSqr(copies.get(1).world(0,0,0)));
  }
 }
}
