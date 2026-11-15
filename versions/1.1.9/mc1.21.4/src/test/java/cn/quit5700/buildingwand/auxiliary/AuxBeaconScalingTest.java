package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.*;
import net.minecraft.core.BlockPos;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AuxBeaconScalingTest {
 @BeforeAll static void detect(){net.minecraft.SharedConstants.tryDetectVersion();}
 private static AuxRow row(int w,int h,int d){var n=AuxRow.create(BlockPos.ZERO,new BlockPos(w-1,h-1,d-1));n.putInt("Shape",AuxBeacon.SHAPE);n.putString("Material","minecraft:iron_block");return new AuxRow(n);}
 @Test void fifthAndTenthLayersAreNotCappedAtFour(){assertEquals(5,AuxBeacon.levels(11,6,11));assertEquals(10,AuxBeacon.levels(21,11,21));assertEquals(49,AuxBeacon.levels(100,100,100));}
 @Test void allDimensionsConstrainTheLargestCompleteTower(){assertEquals(3,AuxBeacon.levels(101,4,101));assertEquals(6,AuxBeacon.levels(13,100,101));assertEquals(4,AuxBeacon.levels(101,100,9));}
 @Test void tenthLayerHas1770BaseBlocksAndExactlyOneBeacon(){var row=row(21,11,21);var counts=new HashMap<String,Integer>();for(long i=0;i<row.volume();i++){var p=row.coordinates(i);if(row.contains(p[0],p[1],p[2]))counts.merge(cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(row.material(p[0],p[1],p[2]),"id",""),1,Integer::sum);}assertEquals(Map.of("minecraft:iron_block",1770,"minecraft:beacon",1),counts);}
 @Test void oversizedSelectionUsesFootprintAndHeightWithoutExceedingBounds(){var row=row(30,9,40);assertEquals(8,AuxBeacon.levels(row.w,row.h,row.d));assertEquals(17L*9*17,row.volume());assertTrue(row.contains(6,0,11));assertTrue(row.contains(14,8,19));assertFalse(row.contains(5,0,11));assertFalse(row.contains(23,0,11));}
 @Test void tallTowerStillKeepsThreeByThreeTopPlatform(){var row=row(21,11,21);int count=0;for(int x=0;x<21;x++)for(int z=0;z<21;z++)if(row.contains(x,9,z))count++;assertEquals(9,count);assertEquals("minecraft:beacon",cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(row.material(10,10,10),"id",""));}
 @Test void tallTowerSurvivesRotationTranslationAndRecordReload(){var row=row(21,11,21);var pivot=new BlockPos(10,5,10);var top=row.world(10,10,10);row.rotate(1,pivot,1);row.translate(-100,30,80);var loaded=new AuxRow(row.data.copy());assertEquals(10,AuxBeacon.levels(loaded.w,loaded.h,loaded.d));assertEquals(row.world(10,10,10),loaded.world(10,10,10));assertEquals(top.offset(-100,30,80),loaded.world(10,10,10));}
 @Test void largeBoundsAreCalculatedWithoutAllocatingTheWholeShape(){assertEquals(499999,AuxBeacon.levels(1000000,1000000,1000000));assertEquals(999999,AuxBeacon.side(499999));}
}
