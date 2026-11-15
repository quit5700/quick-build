package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuxRotationGestureTest {
 @org.junit.jupiter.api.BeforeAll static void detect(){net.minecraft.SharedConstants.tryDetectVersion();}
 @Test void composedAxesPreserveTheDownwardNormalAndSurviveTranslation(){
  var row=new AuxRow(AuxRow.create(net.minecraft.core.BlockPos.ZERO,new net.minecraft.core.BlockPos(6,6,2)));var steps=new net.minecraft.nbt.ListTag();var x=new net.minecraft.nbt.CompoundTag();x.putInt("Axis",0);x.putInt("Turns",1);steps.add(x);var y=new net.minecraft.nbt.CompoundTag();y.putInt("Axis",1);y.putInt("Turns",1);steps.add(y);AuxRotationGesture.apply(row,steps,net.minecraft.core.BlockPos.ZERO);
  assertEquals(net.minecraft.core.BlockPos.ZERO.below(),row.world(0,0,1));row.translate(8,9,10);assertEquals(new net.minecraft.core.BlockPos(8,8,10),row.world(0,0,1));var restored=new AuxRow(row.data.copy());assertEquals(row.world(0,0,1),restored.world(0,0,1));
 }

 @Test void lookingDownRotatesVerticalSectionOntoTheGround(){assertEquals(1,AuxRotationGesture.turns(0,0,90,0));assertEquals(1,AuxRotationGesture.turns(2,0,90,0));}
 @Test void lookingUpRotatesOppositeDirection(){assertEquals(-1,AuxRotationGesture.turns(0,0,-90,0));}
 @Test void yawIsReservedForTheVerticalAxis(){assertEquals(1,AuxRotationGesture.turns(1,90,0,0));assertEquals(0,AuxRotationGesture.turns(0,90,0,0));}
 @Test void fullPitchRangeCanInvertAHorizontalPlane(){assertEquals(2,AuxRotationGesture.turns(0,0,180,0));}
 @Test void orbitingTheAnchorRemainsAvailable(){assertEquals(2,AuxRotationGesture.turns(2,0,0,Math.PI));}
}
